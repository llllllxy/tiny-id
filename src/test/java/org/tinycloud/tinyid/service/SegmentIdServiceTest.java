package org.tinycloud.tinyid.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.tinycloud.tinyid.bean.entity.TIdTable;
import org.tinycloud.tinyid.dao.IdTableDao;
import org.tinycloud.tinyid.enums.CoreErrorCode;
import org.tinycloud.tinyid.exception.CoreException;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * SegmentIdService 号段取号单元测试（纯 Mockito，不依赖数据库）
 *
 * <p>重点覆盖修复后的三条关键路径：</p>
 * <ul>
 *     <li>异步预加载失败后不挂死，能通过同步兜底自恢复且不产生重号；</li>
 *     <li>id_step 配置非法（<=0 或 null）时快速失败，而不是无限循环打库；</li>
 *     <li>正常路径取号连续、无重号。</li>
 * </ul>
 */
class SegmentIdServiceTest {

    private static final String ID_CODE = "ORDER";

    private SegmentIdService segmentIdService;

    private IdTableDao idTableDao;

    @BeforeEach
    void setUp() {
        idTableDao = Mockito.mock(IdTableDao.class);
        // 使用真实的线程池，覆盖异步预加载的真实调度路径
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(10);
        executor.initialize();
        segmentIdService = new SegmentIdService();
        ReflectionTestUtils.setField(segmentIdService, "idTableDao", idTableDao);
        ReflectionTestUtils.setField(segmentIdService, "asyncServiceExecutor", executor);
    }

    /**
     * 构造一条流水号规则配置（无前后缀，编码总长 5 位，纯数字）
     */
    private TIdTable buildIdTable(long idValue, int idStep) {
        TIdTable idTable = new TIdTable();
        idTable.setIdCode(ID_CODE);
        idTable.setIdValue(idValue);
        idTable.setIdStep(idStep);
        idTable.setIdLength(5);
        idTable.setHasPrefix(0);
        idTable.setHasSuffix(0);
        return idTable;
    }

    /**
     * 场景：第一次同步加载成功取到 [1,5]，触发的异步预加载（第二次刷新）抛异常，
     * 队列耗尽后应通过同步兜底（第三次刷新）取到 [6,10]。
     * 修复前该场景会导致 preloaded 永久卡在 true，取号线程无限空转。
     */
    @Test
    void preloadFailureShouldNotHangAndShouldRecover() {
        Mockito.when(idTableDao.get(ID_CODE)).thenReturn(buildIdTable(0L, 5));
        AtomicInteger refreshCount = new AtomicInteger();
        Mockito.when(idTableDao.refreshByIdCode(ID_CODE)).thenAnswer(invocation -> {
            int n = refreshCount.incrementAndGet();
            if (n == 2) {
                // 模拟异步预加载时 DB 抖动
                throw new CoreException(CoreErrorCode.SERIOUSLY_ERROR);
            }
            // 第一次同步加载返回 [1,5]，第三次兜底加载返回 [6,10]
            return buildIdTable(n == 1 ? 5L : 10L, 5);
        });

        Set<String> ids = new HashSet<>();
        // 用超时断言保证「不挂死」：修复前这里会无限空转导致用例超时失败
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            for (int i = 0; i < 10; i++) {
                ids.add(segmentIdService.nextId(ID_CODE));
            }
        });
        assertEquals(10, ids.size(), "10 次取号应全部成功且无重号");
    }

    /**
     * 场景：id_step 配置为 0。refreshByIdCode 正常返回但生成空号段，
     * 应快速抛出业务异常，而不是在取号循环里无限打库。
     */
    @Test
    void invalidStepShouldFailFast() {
        Mockito.when(idTableDao.get(ID_CODE)).thenReturn(buildIdTable(0L, 0));
        Mockito.when(idTableDao.refreshByIdCode(ID_CODE)).thenReturn(buildIdTable(100L, 0));

        assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
                assertThrows(CoreException.class, () -> segmentIdService.nextId(ID_CODE)));
    }

    /**
     * 场景：正常回归。步长 10，取 25 个号，需要跨 3 个号段，
     * 断言结果连续、无重号且含异步预加载路径（剩余低于阈值时触发）。
     */
    @Test
    void normalPathShouldReturnUniqueIds() {
        Mockito.when(idTableDao.get(ID_CODE)).thenReturn(buildIdTable(0L, 10));
        AtomicInteger idValue = new AtomicInteger(0);
        Mockito.when(idTableDao.refreshByIdCode(ID_CODE)).thenAnswer(invocation ->
                buildIdTable(idValue.addAndGet(10), 10));

        Set<String> ids = new HashSet<>();
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            for (int i = 0; i < 25; i++) {
                ids.add(segmentIdService.nextId(ID_CODE));
            }
        });
        assertEquals(25, ids.size(), "取号不应出现重号");
        for (int i = 1; i <= 25; i++) {
            String expected = String.format("%05d", i);
            assertEquals(true, ids.contains(expected), "应包含连续流水号 " + expected);
        }
    }

    /**
     * 场景：批量大小入参防御。null、0、负数、超过上限都应快速抛业务异常，
     * 尤其超大批量不能进入取号循环反复打库。
     */
    @Test
    void batchSizeValidationShouldRejectInvalidValues() {
        Mockito.when(idTableDao.get(ID_CODE)).thenReturn(buildIdTable(0L, 10));
        Mockito.when(idTableDao.refreshByIdCode(ID_CODE)).thenReturn(buildIdTable(10L, 10));

        assertThrows(CoreException.class, () -> segmentIdService.nextBatchId(ID_CODE, null), "null 应拒绝");
        assertThrows(CoreException.class, () -> segmentIdService.nextBatchId(ID_CODE, 0), "0 应拒绝");
        assertThrows(CoreException.class, () -> segmentIdService.nextBatchId(ID_CODE, -1), "负数应拒绝");
        assertThrows(CoreException.class, () -> segmentIdService.nextBatchId(ID_CODE, 1001), "超过上限应拒绝");
        Mockito.verify(idTableDao, Mockito.never()).refreshByIdCode(ID_CODE);

        // 上限边界值应正常放行
        assertEquals(1000, segmentIdService.nextBatchId(ID_CODE, 1000).size());
    }
}

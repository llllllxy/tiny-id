package org.tinycloud.tinyid.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SegmentIdService 号段发号单元测试（纯 Mockito，不依赖数据库）
 *
 * <p>覆盖游标方案的关键行为：正常连续取号、批量参数校验、
 * 配置错误时的快速失败与自愈（坏缓冲丢弃后可恢复，无需重启）。</p>
 */
class SegmentIdServiceTest {

    private static final String ID_CODE = "ORDER";

    private SegmentIdService segmentIdService;

    private IdTableDao idTableDao;

    @BeforeEach
    void setUp() {
        idTableDao = Mockito.mock(IdTableDao.class);
        segmentIdService = new SegmentIdService();
        ReflectionTestUtils.setField(segmentIdService, "idTableDao", idTableDao);
    }

    /**
     * 构造一条流水号规则配置（无前后缀，纯数字）
     */
    private TIdTable buildIdTable(long idValue, int idStep, int idLength) {
        TIdTable idTable = new TIdTable();
        idTable.setIdCode(ID_CODE);
        idTable.setIdValue(idValue);
        idTable.setIdStep(idStep);
        idTable.setIdLength(idLength);
        idTable.setHasPrefix(0);
        idTable.setHasSuffix(0);
        return idTable;
    }

    /**
     * 场景：正常回归。步长10取25个号跨3个号段，结果连续、无重号。
     */
    @Test
    void normalPathShouldReturnUniqueIds() {
        AtomicInteger idValue = new AtomicInteger(0);
        when(idTableDao.refreshByIdCode(ID_CODE)).thenAnswer(invocation ->
                buildIdTable(idValue.addAndGet(10), 10, 5));

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
     * 场景：批量大小入参防御。null、0、负数、超过上限都应快速抛业务异常。
     */
    @Test
    void batchSizeValidationShouldRejectInvalidValues() {
        when(idTableDao.refreshByIdCode(ID_CODE)).thenReturn(buildIdTable(1000L, 1000, 5));

        assertThrows(CoreException.class, () -> segmentIdService.nextBatchId(ID_CODE, null), "null 应拒绝");
        assertThrows(CoreException.class, () -> segmentIdService.nextBatchId(ID_CODE, 0), "0 应拒绝");
        assertThrows(CoreException.class, () -> segmentIdService.nextBatchId(ID_CODE, -1), "负数应拒绝");
        assertThrows(CoreException.class, () -> segmentIdService.nextBatchId(ID_CODE, 1001), "超过上限应拒绝");
        verify(idTableDao, Mockito.never()).refreshByIdCode(ID_CODE);

        // 上限边界值应正常放行
        assertEquals(1000, segmentIdService.nextBatchId(ID_CODE, 1000).size());
    }

    /**
     * 场景：id_step 配置非法（0）。应快速抛业务异常；
     * 坏缓冲被丢弃，配置修复后下一次调用即可自愈，无需重启。
     */
    @Test
    void invalidStepShouldFailFastThenSelfHeal() {
        when(idTableDao.refreshByIdCode(ID_CODE))
                .thenReturn(buildIdTable(100L, 0, 5))
                .thenReturn(buildIdTable(105L, 5, 5));

        CoreException exception = assertThrows(CoreException.class,
                () -> segmentIdService.nextId(ID_CODE));
        assertEquals(CoreErrorCode.THE_ID_STEP_CONFIG_INVALID.getCode(), exception.getCode());

        // 配置修复后自愈
        assertEquals("00101", segmentIdService.nextId(ID_CODE));
    }

    /**
     * 场景：格式化失败（编码长度放不下数字）不应毒化缓冲。
     * 修复配置后下一次调用即可自愈，这是游标方案必须保证的关键行为。
     */
    @Test
    void formatFailureShouldNotPoisonBuffer() {
        when(idTableDao.refreshByIdCode(ID_CODE))
                .thenReturn(buildIdTable(5L, 5, 0))
                .thenReturn(buildIdTable(10L, 5, 5));

        CoreException exception = assertThrows(CoreException.class,
                () -> segmentIdService.nextId(ID_CODE));
        assertEquals(CoreErrorCode.THE_ID_LENGTH_IS_NOT_ENOUGH.getCode(), exception.getCode());

        // 配置修复后自愈：拿到 [6,10] 段的第一个号
        assertEquals("00006", segmentIdService.nextId(ID_CODE));
    }

    /**
     * 场景：idCode 已被删除。refreshByIdCode 命中0行抛异常，
     * 每次调用都应重新尝试（不缓存失败结果），异常向上传播由调用方感知。
     */
    @Test
    void deletedIdCodeShouldPropagateErrorAndRetry() {
        when(idTableDao.refreshByIdCode(ID_CODE))
                .thenThrow(new IllegalStateException("刷新流水号失败"))
                .thenThrow(new IllegalStateException("刷新流水号失败"));

        assertThrows(IllegalStateException.class, () -> segmentIdService.nextId(ID_CODE));
        assertThrows(IllegalStateException.class, () -> segmentIdService.nextId(ID_CODE));
        verify(idTableDao, times(2)).refreshByIdCode(ID_CODE);
    }
}

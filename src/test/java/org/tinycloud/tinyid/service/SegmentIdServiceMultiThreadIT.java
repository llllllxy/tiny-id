package org.tinycloud.tinyid.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 号段模式多线程取号回归测试（需要真实MySQL，写入t_idtable，默认不随mvn test执行）。
 *
 * <p>运行方式（需本地MySQL可用）：</p>
 * <pre>SEG_IT=true mvn test -Dtest=SegmentIdServiceMultiThreadIT</pre>
 *
 * <p>验证目标：20线程并发取2000个号，结果无重号；用例结束自动清理测试数据。</p>
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "SEG_IT", matches = "true")
class SegmentIdServiceMultiThreadIT {

    private static final String TEST_ID_CODE = "SEG_IT_TEST";
    private static final int THREADS = 20;
    private static final int IDS_PER_THREAD = 100;

    @Autowired
    private SegmentIdService segmentIdService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void prepare(@Autowired JdbcTemplate jdbcTemplate) {
        // 准备测试配置：步长1000，初始值0
        jdbcTemplate.update("DELETE FROM t_idtable WHERE id_code = ?", TEST_ID_CODE);
        jdbcTemplate.update("INSERT INTO t_idtable (id_code, id_name, id_step, id_length, has_prefix, has_suffix) VALUES (?, ?, 1000, 10, 0, 0)",
                TEST_ID_CODE, "号段并发回归测试");
    }

    @AfterAll
    static void cleanup(@Autowired JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("DELETE FROM t_idtable WHERE id_code = ?", TEST_ID_CODE);
    }

    @Test
    void multiThreadShouldNotDuplicate() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        Set<String> ids = ConcurrentHashMap.newKeySet();
        AtomicInteger failures = new AtomicInteger(0);

        for (int t = 0; t < THREADS; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < IDS_PER_THREAD; i++) {
                        ids.add(segmentIdService.nextId(TEST_ID_CODE));
                    }
                } catch (Exception e) {
                    failures.incrementAndGet();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS), "取号线程应在超时前完成");

        assertEquals(0, failures.get(), "不应有取号线程抛异常");
        assertEquals(THREADS * IDS_PER_THREAD, ids.size(), "2000个号应全部成功且无重号");
    }
}

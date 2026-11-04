package org.tinycloud.tinyid.dao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.tinycloud.tinyid.utils.snowflake.SnowflakeSingleton;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * WorkNodeDao 心跳单元测试（纯 Mockito，不依赖数据库）
 *
 * <p>重点覆盖心跳行被清理后的自愈逻辑：UPDATE 命中 0 行时自动重新注册，
 * 防止活节点的 workerID 被新实例复用导致雪花 ID 重复。</p>
 */
class WorkNodeDaoTest {

    private WorkNodeDao workNodeDao;

    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void initSnowflake() {
        // sign() 依赖全局单例已初始化（init 内部有防重入保护，多次调用安全）
        SnowflakeSingleton.init(3L, 1L);
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        workNodeDao = new WorkNodeDao();
        ReflectionTestUtils.setField(workNodeDao, "jdbcTemplate", jdbcTemplate);
        ReflectionTestUtils.setField(workNodeDao, "serverPort", 9999);
    }

    /**
     * 场景：心跳正常。UPDATE 命中 1 行，不触发重新注册。
     */
    @Test
    void signShouldSucceedWhenRowExists() {
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        assertTrue(workNodeDao.sign());
        // 只有 UPDATE 一次调用，没有 INSERT
        verify(jdbcTemplate, times(1)).update(anyString(), any(Object[].class));
    }

    /**
     * 场景：心跳行已被清理（UPDATE 命中 0 行）。应自动重新注册（INSERT），心跳返回成功。
     */
    @Test
    void signShouldReRegisterWhenRowMissing() {
        // 第一次调用是 UPDATE（0 行），第二次是重新注册的 INSERT（成功）
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(0, 1);

        assertTrue(workNodeDao.sign());
        verify(jdbcTemplate, times(2)).update(anyString(), any(Object[].class));
    }

    /**
     * 场景：重新注册时身份组合已被其它实例抢注（唯一索引冲突）。
     * 心跳应返回失败并保持告警，绝不静默。
     */
    @Test
    void signShouldFailLoudlyWhenIdentityTaken() {
        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenReturn(0)
                .thenThrow(new DuplicateKeyException("duplicate"));

        assertFalse(workNodeDao.sign());
        verify(jdbcTemplate, times(2)).update(anyString(), any(Object[].class));
    }
}

package org.tinycloud.tinyid.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>
 *     映射项目配置为配置类
 * </p>
 *
 * @author liuxingyu01
 * @since 2023-12-05 15:29
 */
@Component
@ConfigurationProperties(prefix = "tinyid")
public class ApplicationConfig {

    /**
     * 系统名称
     */
    private String name;

    /**
     * 系统版本
     */
    private String version;

    /**
     * 系统数据库类型
     */
    private String dbType = "mysql";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }
}

package com.github.platform.core.gateway.infra.configuration.mybatis;

import com.github.platform.core.common.constant.SpringBeanOrderConstant;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;

/**
 * druid数据源配置
 *
 * @author: yxkong
 * @date: 2021/6/2 4:46 下午
 * @version: 1.0
 */
@Configuration
@Order(SpringBeanOrderConstant.DB_INIT)
@Slf4j
public class DataSourceConfiguration {
    // 添加连接池监控
    @Bean
    public HikariPoolMXBean hikariPoolMXBean() {
        return ((HikariDataSource) sysDataSource()).getHikariPoolMXBean();
    }
    /**
     * arbitration库（可能多个）
     * ConfigurationProperties会去自动读取YAML中datasource.loan开头的配置
     *
     * @return DataSource
     */
    @Bean(name = "sysDataSource")
    @ConfigurationProperties(prefix = "datasource.sys")
    @Primary
    public DataSource sysDataSource() {
        log.info("-------------------- Initializing HikariCP DataSource ---------------------");
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }
}

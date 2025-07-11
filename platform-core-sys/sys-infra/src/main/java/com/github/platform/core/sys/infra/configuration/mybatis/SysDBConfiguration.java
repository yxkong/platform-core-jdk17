package com.github.platform.core.sys.infra.configuration.mybatis;

import com.github.platform.core.common.plugin.mybatis.ShardingInterceptor;
import com.github.platform.core.sys.infra.configuration.mybatis.plugin.DataScopeInterceptor;
import com.github.platform.core.sys.infra.configuration.mybatis.plugin.UpdateParamInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import jakarta.annotation.Resource;
import javax.sql.DataSource;

/**
 * loan库动态数据源
 *
 * @author: yxkong
 * @date: 2021/6/3 4:26 下午
 * @version: 1.0
 */
@Configuration
@MapperScan(basePackages = {
        "com.github.platform.core.persistence.mapper"}, sqlSessionFactoryRef = "sqlSessionFactorySys")
public class SysDBConfiguration {

    @Resource(name = "sysDataSource")
    private DataSource dataSource;

    /**
     * 设置只走sys库的mapper
     * @return
     * @throws Exception
     */
    @Bean(name = "sqlSessionFactorySys")
    public SqlSessionFactory sqlSessionFactorySys() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        //添加分表插件
        ShardingInterceptor shardingInterceptor = new ShardingInterceptor();
        UpdateParamInterceptor updateParamInterceptor = new UpdateParamInterceptor();
        DataScopeInterceptor dataScopeInterceptor = new DataScopeInterceptor();
        factoryBean.setPlugins(shardingInterceptor, updateParamInterceptor,dataScopeInterceptor);
        factoryBean.setDataSource(dataSource);
        // 设置 Mapper 文件路径
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mybatis/**/*Mapper.xml"));
        // 设置类型别名包
        factoryBean.setTypeAliasesPackage("com.github..*.infra.domain.common.entity");
        // 添加性能优化配置
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        // 优化查询性能,默认查询2000条
        configuration.setDefaultFetchSize(2000);
        // 自动驼峰转换
        configuration.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(configuration);
        return factoryBean.getObject();
    }

    @Bean(name = "sqlSessionTemplateSys")
    public SqlSessionTemplate sqlSessionTemplateSys() throws Exception {
        // 使用上面配置的Factory
        return new SqlSessionTemplate(sqlSessionFactorySys());
    }

}

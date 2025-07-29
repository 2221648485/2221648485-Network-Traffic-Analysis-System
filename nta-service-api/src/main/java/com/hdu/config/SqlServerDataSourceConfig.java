package com.hdu.config;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
// 扫描SQL Server对应的Mapper接口包
@MapperScan(
        basePackages = "com.hdu.mapper",
        sqlSessionFactoryRef = "sqlserverSqlSessionFactory"
)
public class SqlServerDataSourceConfig {

    /**
     * 创建SQL Server数据源
     */
    @Bean(name = "sqlserverDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.sqlserver")  // 关联配置文件中的sqlserver前缀配置
    public DataSource sqlserverDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 创建SQL Server的SqlSessionFactory
     */
    @Bean(name = "sqlserverSqlSessionFactory")
    public SqlSessionFactoryBean sqlserverSqlSessionFactory(@Qualifier("sqlserverDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        // 设置SQL Server的Mapper.xml路径
        sessionFactory.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mapper/sqlserver/*.xml")
        );
        return sessionFactory;
    }

    /**
     * 创建SQL Server的事务管理器
     */
    @Bean(name = "sqlserverTransactionManager")
    public DataSourceTransactionManager sqlserverTransactionManager(@Qualifier("sqlserverDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 创建SQL Server的SqlSessionTemplate
     */
    @Bean(name = "sqlserverSqlSessionTemplate")
    public SqlSessionTemplate sqlserverSqlSessionTemplate(@Qualifier("sqlserverSqlSessionFactory") SqlSessionFactoryBean sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory.getObject());
    }
}

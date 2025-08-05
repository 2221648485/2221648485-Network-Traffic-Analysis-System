package com.hdu.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * 绑定spring.datasource.sqlserver配置属性的类
 */
@Data
@ConfigurationProperties(prefix = "spring.datasource.sqlserver")
class SqlServerDataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
}

@EnableConfigurationProperties(SqlServerDataSourceProperties.class)
@Configuration
@MapperScan(
        basePackages = "com.hdu.mapper",
        sqlSessionFactoryRef = "sqlserverSqlSessionFactory"
)
public class SqlServerDataSourceConfig {

    private final SqlServerDataSourceProperties properties;

    public SqlServerDataSourceConfig(SqlServerDataSourceProperties properties) {
        this.properties = properties;
    }

    /**
     * 创建SQL Server数据源，手动设置HikariDataSource参数
     */
    @Bean(name = "sqlserverDataSource")
    public DataSource sqlserverDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(properties.getUrl());
        ds.setUsername(properties.getUsername());
        ds.setPassword(properties.getPassword());
        ds.setDriverClassName(properties.getDriverClassName());
        return ds;
    }

    /**
     * 创建SQL Server的SqlSessionFactory
     */
    @Bean(name = "sqlserverSqlSessionFactory")
    public SqlSessionFactory sqlserverSqlSessionFactory(@Qualifier("sqlserverDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mapper/sqlserver/*.xml")
        );
        return sessionFactory.getObject();
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
    public SqlSessionTemplate sqlserverSqlSessionTemplate(@Qualifier("sqlserverSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}

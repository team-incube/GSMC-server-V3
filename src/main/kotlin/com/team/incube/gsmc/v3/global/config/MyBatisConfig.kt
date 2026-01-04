package com.team.incube.gsmc.v3.global.config

import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionFactoryBean
import org.mybatis.spring.annotation.MapperScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import javax.sql.DataSource

@Configuration
@MapperScan("com.team.incube.gsmc.v3.domain.archive.mapper")
class MyBatisConfig {
    @Bean
    fun sqlSessionFactory(dataSource: DataSource): SqlSessionFactory {
        val sessionFactory = SqlSessionFactoryBean()
        sessionFactory.setDataSource(dataSource)
        sessionFactory.setMapperLocations(
            *PathMatchingResourcePatternResolver().getResources("classpath:mapper/**/*.xml"),
        )
        sessionFactory.setTypeAliasesPackage("com.team.incube.gsmc.v3.domain.archive.dto")

        val configuration = org.apache.ibatis.session.Configuration()
        configuration.isMapUnderscoreToCamelCase = true
        configuration.defaultFetchSize = 100
        configuration.defaultStatementTimeout = 30
        sessionFactory.setConfiguration(configuration)

        return sessionFactory.`object`!!
    }
}


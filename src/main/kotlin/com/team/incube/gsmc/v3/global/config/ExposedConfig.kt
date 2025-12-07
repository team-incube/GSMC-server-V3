package com.team.incube.gsmc.v3.global.config

import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
class ExposedConfig {
    @Bean
    fun database(dataSource: DataSource): Database {
        val database =
            Database.connect(
                datasource = dataSource,
                databaseConfig =
                    DatabaseConfig {
                        useNestedTransactions = true
                    },
            )
        TransactionManager.defaultDatabase = database
        return database
    }

    @Bean
    fun transactionManager(dataSource: DataSource): PlatformTransactionManager = DataSourceTransactionManager(dataSource)
}

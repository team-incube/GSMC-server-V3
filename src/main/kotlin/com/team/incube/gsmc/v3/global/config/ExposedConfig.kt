package com.team.incube.gsmc.v3.global.config

import com.team.incube.gsmc.v3.domain.alert.entity.AlertExposedEntity
import com.team.incube.gsmc.v3.domain.evidence.entity.EvidenceExposedEntity
import com.team.incube.gsmc.v3.domain.file.entity.EvidenceFileExposedEntity
import com.team.incube.gsmc.v3.domain.file.entity.FileExposedEntity
import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import com.team.incube.gsmc.v3.domain.project.entity.ProjectExposedEntity
import com.team.incube.gsmc.v3.domain.project.entity.ProjectFileExposedEntity
import com.team.incube.gsmc.v3.domain.project.entity.ProjectParticipantExposedEntity
import com.team.incube.gsmc.v3.domain.project.entity.ProjectScoreExposedEntity
import com.team.incube.gsmc.v3.domain.score.entity.ScoreExposedEntity
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
class ExposedConfig(
    @param:Value("\${spring.exposed.generate-ddl:true}")
    private val generateDdl: Boolean,
    // IDEA 워닝 방지를 위한 설정
    @param:Value("\${spring.exposed.show-sql:true}")
    private val showSql: Boolean,
) {
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
        transaction {
            if (generateDdl) {
                SchemaUtils.createMissingTablesAndColumns(*allTables())
            }
            logger().info(SchemaUtils.listTables().joinToString(", ") { it } + " tables are created or already exist.")
        }
        return database
    }

    private fun allTables() =
        arrayOf(
            ScoreExposedEntity,
            EvidenceExposedEntity,
            FileExposedEntity,
            AlertExposedEntity,
            EvidenceFileExposedEntity,
            MemberExposedEntity,
            ProjectFileExposedEntity,
            ProjectParticipantExposedEntity,
            ProjectScoreExposedEntity,
            ProjectExposedEntity,
        )

    @Bean
    fun transactionManager(dataSource: DataSource): PlatformTransactionManager = DataSourceTransactionManager(dataSource)
}

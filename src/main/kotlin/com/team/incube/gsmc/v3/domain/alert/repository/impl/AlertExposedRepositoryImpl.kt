package com.team.incube.gsmc.v3.domain.alert.repository.impl

import com.team.incube.gsmc.v3.domain.alert.dto.Alert
import com.team.incube.gsmc.v3.domain.alert.dto.constant.AlertType
import com.team.incube.gsmc.v3.domain.alert.entity.AlertExposedEntity
import com.team.incube.gsmc.v3.domain.alert.repository.AlertExposedRepository
import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.member.dto.Member
import com.team.incube.gsmc.v3.domain.member.entity.MemberExposedEntity
import com.team.incube.gsmc.v3.domain.score.dto.Score
import com.team.incube.gsmc.v3.domain.score.entity.ScoreExposedEntity
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneId

@Repository
class AlertExposedRepositoryImpl : AlertExposedRepository {
    override fun findById(alertId: Long): Alert? {
        val senderAlias = MemberExposedEntity.alias("sender")
        val receiverAlias = MemberExposedEntity.alias("receiver")

        return AlertExposedEntity
            .join(ScoreExposedEntity, JoinType.INNER) {
                AlertExposedEntity.scoreId eq ScoreExposedEntity.id
            }.join(senderAlias, JoinType.INNER) {
                AlertExposedEntity.senderId eq senderAlias[MemberExposedEntity.id]
            }.join(receiverAlias, JoinType.INNER) {
                AlertExposedEntity.receiverId eq receiverAlias[MemberExposedEntity.id]
            }.selectAll()
            .where { AlertExposedEntity.id eq alertId }
            .limit(1)
            .map { row ->
                val sender =
                    Member(
                        id = row[senderAlias[MemberExposedEntity.id]],
                        name = row[senderAlias[MemberExposedEntity.name]],
                        email = row[senderAlias[MemberExposedEntity.email]],
                        grade = row[senderAlias[MemberExposedEntity.grade]],
                        classNumber = row[senderAlias[MemberExposedEntity.classNumber]],
                        number = row[senderAlias[MemberExposedEntity.number]],
                        role = row[senderAlias[MemberExposedEntity.role]],
                    )

                val receiver =
                    Member(
                        id = row[receiverAlias[MemberExposedEntity.id]],
                        name = row[receiverAlias[MemberExposedEntity.name]],
                        email = row[receiverAlias[MemberExposedEntity.email]],
                        grade = row[receiverAlias[MemberExposedEntity.grade]],
                        classNumber = row[receiverAlias[MemberExposedEntity.classNumber]],
                        number = row[receiverAlias[MemberExposedEntity.number]],
                        role = row[receiverAlias[MemberExposedEntity.role]],
                    )

                val score =
                    Score(
                        id = row[ScoreExposedEntity.id],
                        member = receiver,
                        categoryType = CategoryType.fromEnglishName(row[ScoreExposedEntity.categoryEnglishName]),
                        status = row[ScoreExposedEntity.status],
                        sourceId = row[ScoreExposedEntity.sourceId],
                        activityName = row[ScoreExposedEntity.activityName],
                        scoreValue = row[ScoreExposedEntity.scoreValue],
                        rejectionReason = row[ScoreExposedEntity.rejectionReason],
                    )

                val createdAtInstant = row[AlertExposedEntity.createdAt]
                val createdAt = LocalDateTime.ofInstant(createdAtInstant, ZoneId.systemDefault())

                Alert(
                    id = row[AlertExposedEntity.id],
                    sender = sender,
                    receiver = receiver,
                    score = score,
                    alertType = row[AlertExposedEntity.alertType],
                    isRead = row[AlertExposedEntity.isRead],
                    content = row[AlertExposedEntity.content],
                    createdAt = createdAt,
                )
            }.firstOrNull()
    }

    override fun deleteById(alertId: Long): Int =
        AlertExposedEntity.deleteWhere {
            AlertExposedEntity.id eq alertId
        }

    override fun findAllByReceiverId(receiverId: Long): List<Alert> {
        val senderAlias = MemberExposedEntity.alias("sender")
        val receiverAlias = MemberExposedEntity.alias("receiver")

        return AlertExposedEntity
            .join(ScoreExposedEntity, JoinType.INNER) {
                AlertExposedEntity.scoreId eq ScoreExposedEntity.id
            }.join(senderAlias, JoinType.INNER) {
                AlertExposedEntity.senderId eq senderAlias[MemberExposedEntity.id]
            }.join(receiverAlias, JoinType.INNER) {
                AlertExposedEntity.receiverId eq receiverAlias[MemberExposedEntity.id]
            }.selectAll()
            .where { AlertExposedEntity.receiverId eq receiverId }
            .orderBy(
                AlertExposedEntity.createdAt to SortOrder.DESC,
                AlertExposedEntity.id to SortOrder.DESC,
            ).map { row ->
                val sender =
                    Member(
                        id = row[senderAlias[MemberExposedEntity.id]],
                        name = row[senderAlias[MemberExposedEntity.name]],
                        email = row[senderAlias[MemberExposedEntity.email]],
                        grade = row[senderAlias[MemberExposedEntity.grade]],
                        classNumber = row[senderAlias[MemberExposedEntity.classNumber]],
                        number = row[senderAlias[MemberExposedEntity.number]],
                        role = row[senderAlias[MemberExposedEntity.role]],
                    )

                val receiver =
                    Member(
                        id = row[receiverAlias[MemberExposedEntity.id]],
                        name = row[receiverAlias[MemberExposedEntity.name]],
                        email = row[receiverAlias[MemberExposedEntity.email]],
                        grade = row[receiverAlias[MemberExposedEntity.grade]],
                        classNumber = row[receiverAlias[MemberExposedEntity.classNumber]],
                        number = row[receiverAlias[MemberExposedEntity.number]],
                        role = row[receiverAlias[MemberExposedEntity.role]],
                    )

                val score =
                    Score(
                        id = row[ScoreExposedEntity.id],
                        member = receiver,
                        categoryType = CategoryType.fromEnglishName(row[ScoreExposedEntity.categoryEnglishName]),
                        status = row[ScoreExposedEntity.status],
                        sourceId = row[ScoreExposedEntity.sourceId],
                        activityName = row[ScoreExposedEntity.activityName],
                        scoreValue = row[ScoreExposedEntity.scoreValue],
                        rejectionReason = row[ScoreExposedEntity.rejectionReason],
                    )

                val createdAtInstant = row[AlertExposedEntity.createdAt]
                val createdAt = LocalDateTime.ofInstant(createdAtInstant, ZoneId.systemDefault())

                Alert(
                    id = row[AlertExposedEntity.id],
                    sender = sender,
                    receiver = receiver,
                    score = score,
                    alertType = row[AlertExposedEntity.alertType],
                    isRead = row[AlertExposedEntity.isRead],
                    content = row[AlertExposedEntity.content],
                    createdAt = createdAt,
                )
            }
    }

    override fun save(
        sender: Member,
        receiver: Member,
        score: Score,
        alertType: AlertType,
        content: String,
    ): Alert {
        val now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()

        val insertedId =
            AlertExposedEntity.insert {
                it[this.senderId] = sender.id
                it[this.receiverId] = receiver.id
                it[this.scoreId] = score.id!!
                it[this.alertType] = alertType
                it[this.isRead] = false
                it[this.content] = content
                it[this.createdAt] = now
            } get AlertExposedEntity.id

        return Alert(
            id = insertedId,
            sender = sender,
            receiver = receiver,
            score = score,
            alertType = alertType,
            isRead = false,
            content = content,
            createdAt = LocalDateTime.ofInstant(now, ZoneId.systemDefault()),
        )
    }

    override fun updateIsReadTrueByReceiverIdAndLastAlertId(
        receiverId: Long,
        lastAlertId: Long,
    ): Int =
        AlertExposedEntity.update({
            (AlertExposedEntity.receiverId eq receiverId) and
                (AlertExposedEntity.id lessEq lastAlertId)
        }) {
            it[isRead] = true
        }
}

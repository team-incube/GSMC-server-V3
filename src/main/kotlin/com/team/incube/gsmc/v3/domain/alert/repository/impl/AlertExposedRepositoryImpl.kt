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
import org.jetbrains.exposed.v1.core.Alias
import org.jetbrains.exposed.v1.core.ResultRow
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
            .map { row -> row.toAlert(senderAlias, receiverAlias) }
            .firstOrNull()
    }

    override fun deleteById(alertId: Long): Int {
        return AlertExposedEntity.deleteWhere {
            AlertExposedEntity.id eq alertId
        }
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
            )
            .map { row -> row.toAlert(senderAlias, receiverAlias) }
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

    private fun ResultRow.toAlert(
        senderAlias: Alias<MemberExposedEntity>,
        receiverAlias: Alias<MemberExposedEntity>,
    ): Alert {
        val sender =
            Member(
                id = this[senderAlias[MemberExposedEntity.id]],
                name = this[senderAlias[MemberExposedEntity.name]],
                email = this[senderAlias[MemberExposedEntity.email]],
                grade = this[senderAlias[MemberExposedEntity.grade]],
                classNumber = this[senderAlias[MemberExposedEntity.classNumber]],
                number = this[senderAlias[MemberExposedEntity.number]],
                role = this[senderAlias[MemberExposedEntity.role]],
            )

        val receiver =
            Member(
                id = this[receiverAlias[MemberExposedEntity.id]],
                name = this[receiverAlias[MemberExposedEntity.name]],
                email = this[receiverAlias[MemberExposedEntity.email]],
                grade = this[receiverAlias[MemberExposedEntity.grade]],
                classNumber = this[receiverAlias[MemberExposedEntity.classNumber]],
                number = this[receiverAlias[MemberExposedEntity.number]],
                role = this[receiverAlias[MemberExposedEntity.role]],
            )

        val score =
            Score(
                id = this[ScoreExposedEntity.id],
                member = receiver,
                categoryType = CategoryType.fromEnglishName(this[ScoreExposedEntity.categoryEnglishName]),
                status = this[ScoreExposedEntity.status],
                sourceId = this[ScoreExposedEntity.sourceId],
                activityName = this[ScoreExposedEntity.activityName],
                scoreValue = this[ScoreExposedEntity.scoreValue],
                rejectionReason = this[ScoreExposedEntity.rejectionReason],
            )

        val createdAtInstant = this[AlertExposedEntity.createdAt]
        val createdAt = LocalDateTime.ofInstant(createdAtInstant, ZoneId.systemDefault())

        return Alert(
            id = this[AlertExposedEntity.id],
            sender = sender,
            receiver = receiver,
            score = score,
            alertType = this[AlertExposedEntity.alertType],
            isRead = this[AlertExposedEntity.isRead],
            content = this[AlertExposedEntity.content],
            createdAt = createdAt,
        )
    }
}

package com.team.incube.gsmc.v3.domain.category.entity

import com.team.incube.gsmc.v3.domain.category.dto.constant.EvidenceType
import org.jetbrains.exposed.sql.Table

object CategoryExposedEntity : Table(name = "tb_category") {
    val id = long(name = "category_id").autoIncrement()
    val englishName = varchar(name = "category_english_name", length = 255).nullable()
    val koreanName = varchar(name = "category_korean_name", length = 255).nullable()
    val weight = integer(name = "weight").nullable()
    val maximumValue = integer(name = "category_maximum_value").nullable()
    val isAccumulated = bool(name = "is_accumulated").nullable()
    val evidenceType = enumeration<EvidenceType>(name = "evidence_type").default(EvidenceType.UNREQUIRED)

    override val primaryKey = PrimaryKey(id)
}

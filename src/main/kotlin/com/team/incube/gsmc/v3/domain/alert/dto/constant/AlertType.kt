package com.team.incube.gsmc.v3.domain.alert.dto.constant

enum class AlertType(
    val title: String,
) {
    ADD_SCORE(
        title = "점수 등록 알림",
    ),
    REJECTED(
        title = "거절 알림",
    ),
    APPROVED(
        title = "합격 알림",
    ),
}

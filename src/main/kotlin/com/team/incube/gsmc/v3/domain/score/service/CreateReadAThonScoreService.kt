package com.team.incube.gsmc.v3.domain.score.service

import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse

interface CreateReadAThonScoreService {
    /**
     * 독서마라톤 점수를 추가하거나 갱신합니다.
     *
     * @param grade 독서마라톤 단계 (1=거북이, 2=악어, 3=토끼, 4=타조, 5=사자, 6=호랑이, 7=월계관). Score.scoreValue에 저장됩니다.
     * @param fileId 증빙 파일 ID
     * @return 생성 또는 갱신된 점수 정보
     */
    fun execute(
        grade: Int,
        fileId: Long,
    ): CreateScoreResponse
}

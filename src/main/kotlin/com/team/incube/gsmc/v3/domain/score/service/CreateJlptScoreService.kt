package com.team.incube.gsmc.v3.domain.score.service

import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse

interface CreateJlptScoreService {
    /**
     * JLPT 점수를 추가하거나 갱신합니다.
     *
     * @param grade JLPT 등급 (N1, N2, N3, N4, N5). Score.activityName에 저장됩니다.
     * @param fileId 증빙 파일 ID
     * @return 생성 또는 갱신된 점수 정보
     */
    fun execute(
        grade: String,
        fileId: Long,
    ): CreateScoreResponse
}

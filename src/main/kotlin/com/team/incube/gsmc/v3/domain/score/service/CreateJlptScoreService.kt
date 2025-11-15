package com.team.incube.gsmc.v3.domain.score.service

import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse

interface CreateJlptScoreService {
    /**
     * JLPT 점수를 추가하거나 갱신합니다.
     *
     * @param grade JLPT 등급 (1=N1, 2=N2, 3=N3, 4=N4, 5=N5). Score.scoreValue에 저장됩니다.
     * @param fileId 증빙 파일 ID
     * @return 생성 또는 갱신된 점수 정보
     */
    fun execute(
        grade: Int,
        fileId: Long,
    ): CreateScoreResponse
}

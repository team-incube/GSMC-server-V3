package com.team.incube.gsmc.v3.domain.evidence.service.impl

import com.team.incube.gsmc.v3.domain.evidence.dto.Evidence
import com.team.incube.gsmc.v3.domain.evidence.repository.EvidenceExposedRepository
import com.team.incube.gsmc.v3.domain.evidence.service.CreateEvidenceService
import com.team.incube.gsmc.v3.domain.file.repository.FileExposedRepository
import com.team.incube.gsmc.v3.domain.score.repository.ScoreExposedRepository
import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CreateEvidenceServiceImpl(
    private val evidenceExposedRepository: EvidenceExposedRepository,
    private val scoreExposedRepository: ScoreExposedRepository,
    private val fileExposedRepository: FileExposedRepository,
) : CreateEvidenceService {
    override fun execute(
        scoreIds: List<Long>,
        title: String,
        content: String,
        fileIds: List<Long>,
    ): Evidence {
        // 점수 ID들 존재 여부 확인
        if (!scoreExposedRepository.existsByIdIn(scoreIds)) {
            throw GsmcException(ErrorCode.SCORE_NOT_FOUND)
        }

        // 이미 증빙을 가진 점수가 있는지 확인
        if (scoreExposedRepository.existsAnyWithEvidence(scoreIds)) {
            throw GsmcException(ErrorCode.SCORE_ALREADY_HAS_EVIDENCE)
        }

        // 파일 존재 여부 확인 (파일이 있는 경우만)
        if (fileIds.isNotEmpty() && !fileExposedRepository.existsByIdIn(fileIds)) {
            throw GsmcException(ErrorCode.FILE_NOT_FOUND)
        }

        // 증빙자료 생성
        val evidence = evidenceExposedRepository.save(
            title = title,
            content = content,
            fileIds = fileIds,
        )

        // 점수들에 증빙자료 ID 연결
        scoreExposedRepository.updateEvidenceId(scoreIds, evidence.id)

        return evidence
    }
}

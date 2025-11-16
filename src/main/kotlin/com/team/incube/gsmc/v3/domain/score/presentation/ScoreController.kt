package com.team.incube.gsmc.v3.domain.score.presentation

import com.team.incube.gsmc.v3.domain.score.presentation.data.request.CreateAwardScoreRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.request.CreateCertificateScoreRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.request.CreateJlptScoreRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.request.CreateNcsScoreRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.request.CreateReadAThonScoreRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.request.CreateToeicScoreRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.request.CreateTopcitScoreRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.request.CreateVolunteerScoreRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.request.UpdateScoreStatusRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetTotalScoreResponse
import com.team.incube.gsmc.v3.domain.score.service.CalculateTotalScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateAwardScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateCertificateScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateJlptScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateNcsScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateReadAThonScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateToeicScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateTopcitScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateVolunteerScoreService
import com.team.incube.gsmc.v3.domain.score.service.DeleteScoreService
import com.team.incube.gsmc.v3.domain.score.service.UpdateScoreStatusService
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
import com.team.incube.gsmc.v3.global.security.jwt.util.CurrentMemberProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Score API", description = "인증제 점수 관련 API")
@RestController
@RequestMapping("/api/v3/scores")
class ScoreController(
    private val updateScoreStatusService: UpdateScoreStatusService,
    private val deleteScoreService: DeleteScoreService,
    private val createCertificateScoreService: CreateCertificateScoreService,
    private val createAwardScoreService: CreateAwardScoreService,
    private val createTopcitScoreService: CreateTopcitScoreService,
    private val createToeicScoreService: CreateToeicScoreService,
    private val createJlptScoreService: CreateJlptScoreService,
    private val createReadAThonScoreService: CreateReadAThonScoreService,
    private val createVolunteerScoreService: CreateVolunteerScoreService,
    private val createNcsScoreService: CreateNcsScoreService,
    private val calculateTotalScoreService: CalculateTotalScoreService,
    private val currentMemberProvider: CurrentMemberProvider,
) {
    @Operation(summary = "인증제 점수 상태 업데이트", description = "인증제 점수의 승인/거절 상태를 업데이트합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 인증제 점수를 매핑함",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{scoreId}/status")
    fun updateEvidenceStatus(
        @PathVariable scoreId: Long,
        @Valid @RequestBody request: UpdateScoreStatusRequest,
    ): CommonApiResponse<Nothing> {
        updateScoreStatusService.execute(
            scoreId = scoreId,
            scoreStatus = request.scoreStatus,
        )
        return CommonApiResponse.success("OK")
    }

    @Operation(summary = "인증제 점수 삭제", description = "인증제 점수 및 연관된 증빙자료와 파일을 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 점수를 매핑함",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{scoreId}")
    fun deleteScore(
        @PathVariable scoreId: Long,
    ): CommonApiResponse<Nothing> {
        deleteScoreService.execute(scoreId = scoreId)
        return CommonApiResponse.success("OK")
    }

    @Operation(summary = "자격증 영역 인증제 점수 추가", description = "현재 인증된 사용자의 자격증 영역에 대한 인증제 점수를 추가합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 파일을 매핑함",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "409",
                description = "이미 해당 영역에 대한 인증제 점수를 전부 취득함",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/certificates")
    fun addCertificateScore(
        @RequestBody @Valid request: CreateCertificateScoreRequest,
    ): CreateScoreResponse =
        createCertificateScoreService.execute(
            certificateName = request.certificateName,
            fileId = request.fileId,
        )

    @Operation(summary = "수상경력 영역 인증제 점수 추가", description = "현재 인증된 사용자의 수상경력 영역에 대한 인증제 점수를 추가합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 파일을 매핑함",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "409",
                description = "이미 해당 영역에 대한 인증제 점수를 전부 취득함",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/awards")
    fun addAwardScore(
        @RequestBody @Valid request: CreateAwardScoreRequest,
    ): CreateScoreResponse =
        createAwardScoreService.execute(
            awardName = request.awardName,
            fileId = request.fileId,
        )

    @Operation(summary = "TOPCIT 영역 인증제 점수 추가 또는 갱신", description = "현재 인증된 사용자의 TOPCIT 영역에 대한 인증제 점수를 추가하거나 갱신합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 파일을 매핑함",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/topcit")
    fun addTopcitScore(
        @Valid @RequestBody request: CreateTopcitScoreRequest,
    ): CreateScoreResponse =
        createTopcitScoreService.execute(
            value = request.value,
            fileId = request.fileId,
        )

    @Operation(summary = "TOEIC 영역 인증제 점수 추가 또는 갱신", description = "현재 인증된 사용자의 TOEIC 영역에 대한 인증제 점수를 추가하거나 갱신합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 파일을 매핑함",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/toeic")
    fun addToeicScore(
        @Valid @RequestBody request: CreateToeicScoreRequest,
    ): CreateScoreResponse =
        createToeicScoreService.execute(
            value = request.value,
            fileId = request.fileId,
        )

    @Operation(summary = "JLPT 영역 인증제 점수 추가 또는 갱신", description = "현재 인증된 사용자의 JLPT 영역에 대한 인증제 점수를 추가하거나 갱신합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 파일을 매핑함",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/jlpt")
    fun addJlptScore(
        @Valid @RequestBody request: CreateJlptScoreRequest,
    ): CreateScoreResponse =
        createJlptScoreService.execute(
            grade = request.grade,
            fileId = request.fileId,
        )

    @Operation(summary = "빛고을독서마라톤 영역 인증제 점수 추가 또는 갱신", description = "현재 인증된 사용자의 빛고을독서마라톤 영역에 대한 인증제 점수를 추가하거나 갱신합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 파일을 매핑함",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/readathon")
    fun addReadAThonScore(
        @Valid @RequestBody request: CreateReadAThonScoreRequest,
    ): CreateScoreResponse =
        createReadAThonScoreService.execute(
            grade = request.grade,
            fileId = request.fileId,
        )

    @Operation(summary = "봉사활동 영역 인증제 점수 추가 또는 갱신", description = "현재 인증된 사용자의 봉사활동 영역에 대한 인증제 점수를 추가하거나 갱신합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/volunteer")
    fun addVolunteerScore(
        @Valid @RequestBody request: CreateVolunteerScoreRequest,
    ): CreateScoreResponse =
        createVolunteerScoreService.execute(
            hours = request.hours,
        )

    @Operation(summary = "직업기초능력평가 영역 인증제 점수 추가 또는 갱신", description = "현재 인증된 사용자의 직업기초능력평가 영역에 대한 인증제 점수를 추가하거나 갱신합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 파일을 매핑함",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/ncs")
    fun addNcsScore(
        @Valid @RequestBody request: CreateNcsScoreRequest,
    ): CreateScoreResponse =
        createNcsScoreService.execute(
            averageScore = request.averageScore,
            fileId = request.fileId,
        )

    @Operation(summary = "현재 사용자의 총점 조회", description = "현재 인증된 사용자의 인증제 총점을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/total")
    fun getTotalScore(
        @RequestParam(
            name = "includeApprovedOnly",
            defaultValue = "true",
            required = false,
        ) includeApprovedOnly: Boolean,
    ): GetTotalScoreResponse {
        val member = currentMemberProvider.getCurrentUser()
        val totalScore =
            calculateTotalScoreService.execute(
                memberId = member.id,
                includeApprovedOnly = includeApprovedOnly,
            )
        return GetTotalScoreResponse(totalScore = totalScore)
    }
}

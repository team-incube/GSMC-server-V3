package com.team.incube.gsmc.v3.domain.score.presentation

import com.team.incube.gsmc.v3.domain.category.constant.CategoryType
import com.team.incube.gsmc.v3.domain.score.dto.constant.ScoreStatus
import com.team.incube.gsmc.v3.domain.score.presentation.data.request.CreateProjectParticipationScoreRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.request.CreateScoreWithValueAndFileRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.request.CreateScoreWithValueRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.request.RejectScoreRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.request.UpdateScoreStatusRequest
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.CreateScoreResponse
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetMyScoresResponse
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetScoreResponse
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetScoresByCategoryResponse
import com.team.incube.gsmc.v3.domain.score.presentation.data.response.GetTotalScoreResponse
import com.team.incube.gsmc.v3.domain.score.service.ApproveScoreService
import com.team.incube.gsmc.v3.domain.score.service.CalculateTotalScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateAcademicGradeScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateAwardScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateCertificateScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateExternalActivityScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateJlptScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateNcsScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateNewrrowSchoolScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateProjectParticipationService
import com.team.incube.gsmc.v3.domain.score.service.CreateReadAThonScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateToeicScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateTopcitScoreService
import com.team.incube.gsmc.v3.domain.score.service.CreateVolunteerScoreService
import com.team.incube.gsmc.v3.domain.score.service.DeleteScoreService
import com.team.incube.gsmc.v3.domain.score.service.FindMyScoresService
import com.team.incube.gsmc.v3.domain.score.service.FindScoreByScoreIdService
import com.team.incube.gsmc.v3.domain.score.service.FindScoresByCategoryService
import com.team.incube.gsmc.v3.domain.score.service.RejectScoreService
import com.team.incube.gsmc.v3.domain.score.service.UpdateScoreStatusService
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
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
    private val approveScoreService: ApproveScoreService,
    private val rejectScoreService: RejectScoreService,
    private val deleteScoreService: DeleteScoreService,
    private val createCertificateScoreService: CreateCertificateScoreService,
    private val createAwardScoreService: CreateAwardScoreService,
    private val createTopcitScoreService: CreateTopcitScoreService,
    private val createToeicScoreService: CreateToeicScoreService,
    private val createJlptScoreService: CreateJlptScoreService,
    private val createReadAThonScoreService: CreateReadAThonScoreService,
    private val createVolunteerScoreService: CreateVolunteerScoreService,
    private val createNcsScoreService: CreateNcsScoreService,
    private val createNewrrowSchoolScoreService: CreateNewrrowSchoolScoreService,
    private val createAcademicGradeScoreService: CreateAcademicGradeScoreService,
    private val createExternalActivityScoreService: CreateExternalActivityScoreService,
    private val createProjectParticipationService: CreateProjectParticipationService,
    private val calculateTotalScoreService: CalculateTotalScoreService,
    private val findMyScoresService: FindMyScoresService,
    private val findScoreByScoreIdService: FindScoreByScoreIdService,
    private val findScoresByCategoryService: FindScoresByCategoryService,
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

    @Operation(summary = "인증제 점수 승인", description = "인증제 점수를 승인합니다")
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
    @PatchMapping("/{scoreId}/approve")
    fun approveScore(
        @PathVariable scoreId: Long,
    ): CommonApiResponse<Nothing> {
        approveScoreService.execute(scoreId = scoreId)
        return CommonApiResponse.success("OK")
    }

    @Operation(summary = "인증제 점수 거절", description = "인증제 점수를 거절하고 거절 사유를 저장합니다")
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
    @PatchMapping("/{scoreId}/reject")
    fun rejectScore(
        @PathVariable scoreId: Long,
        @Valid @RequestBody request: RejectScoreRequest,
    ): CommonApiResponse<Nothing> {
        rejectScoreService.execute(
            scoreId = scoreId,
            rejectionReason = request.rejectionReason,
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
        @RequestBody @Valid request: CreateScoreWithValueAndFileRequest,
    ): CreateScoreResponse =
        createCertificateScoreService.execute(
            value = request.value,
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
        @RequestBody @Valid request: CreateScoreWithValueAndFileRequest,
    ): CreateScoreResponse =
        createAwardScoreService.execute(
            value = request.value,
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
                responseCode = "400",
                description = "유효하지 않은 점수 값이거나 점수 값이 허용 범위(1-1000)를 벗어남",
                content = [Content()],
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
        @Valid @RequestBody request: CreateScoreWithValueAndFileRequest,
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
                responseCode = "400",
                description = "유효하지 않은 점수 값이거나 점수 값이 허용 범위(10-990)를 벗어남",
                content = [Content()],
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
        @Valid @RequestBody request: CreateScoreWithValueAndFileRequest,
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
                responseCode = "400",
                description = "유효하지 않은 점수 값이거나 점수 값이 허용 범위(1-5)를 벗어남",
                content = [Content()],
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
        @Valid @RequestBody request: CreateScoreWithValueAndFileRequest,
    ): CreateScoreResponse =
        createJlptScoreService.execute(
            value = request.value,
            fileId = request.fileId,
        )

    @Operation(summary = "독서마라톤 영역 인증제 점수 추가 또는 갱신", description = "현재 인증된 사용자의 독서마라톤 영역에 대한 인증제 점수를 추가하거나 갱신합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 점수 값이거나 점수 값이 허용 범위(1-7)를 벗어남",
                content = [Content()],
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
        @Valid @RequestBody request: CreateScoreWithValueAndFileRequest,
    ): CreateScoreResponse =
        createReadAThonScoreService.execute(
            value = request.value,
            fileId = request.fileId,
        )

    @Operation(summary = "봉사활동 영역 인증제 점수 추가 또는 갱신", description = "현재 인증된 사용자의 봉사활동 영역에 대한 인증제 점수를 추가하거나 갱신합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 점수 값이거나 점수 값이 허용 범위(최소 1)를 벗어남",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/volunteer")
    fun addVolunteerScore(
        @Valid @RequestBody request: CreateScoreWithValueRequest,
    ): CreateScoreResponse =
        createVolunteerScoreService.execute(
            value = request.value,
        )

    @Operation(summary = "직업기초능력평가 영역 인증제 점수 추가 또는 갱신", description = "현재 인증된 사용자의 직업기초능력평가 영역에 대한 인증제 점수를 추가하거나 갱신합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 점수 값이거나 점수 값이 허용 범위(1.0-5.0)를 벗어남",
                content = [Content()],
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
        @Valid @RequestBody request: CreateScoreWithValueAndFileRequest,
    ): CreateScoreResponse =
        createNcsScoreService.execute(
            value = request.value,
            fileId = request.fileId,
        )

    @Operation(summary = "뉴로우스쿨 참여 영역 인증제 점수 추가 또는 갱신", description = "현재 인증된 사용자의 뉴로우스쿨 참여 영역에 대한 인증제 점수를 추가하거나 갱신합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 점수 값이거나 점수 값이 허용 범위(0-100)를 벗어남",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 파일을 매핑함",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/newrrow-school")
    fun addNewrrowSchoolScore(
        @Valid @RequestBody request: CreateScoreWithValueAndFileRequest,
    ): CreateScoreResponse =
        createNewrrowSchoolScoreService.execute(
            value = request.value,
            fileId = request.fileId,
        )

    @Operation(summary = "교과성적 영역 인증제 점수 추가 또는 갱신", description = "현재 인증된 사용자의 교과성적 영역에 대한 인증제 점수를 추가하거나 갱신합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "400",
                description = "유효하지 않은 점수 값이거나 점수 값이 허용 범위(1.0-9.0)를 벗어남",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/academic-grade")
    fun addAcademicGradeScore(
        @Valid @RequestBody request: CreateScoreWithValueRequest,
    ): CreateScoreResponse =
        createAcademicGradeScoreService.execute(
            value = request.value,
        )

    @Operation(summary = "외부활동 영역 인증제 점수 추가", description = "현재 인증된 사용자의 외부활동 영역에 대한 인증제 점수를 추가합니다")
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
    @PostMapping("/external-activities")
    fun addExternalActivityScore(
        @RequestBody @Valid request: CreateScoreWithValueAndFileRequest,
    ): CreateScoreResponse =
        createExternalActivityScoreService.execute(
            value = request.value,
            fileId = request.fileId,
        )

    @Operation(summary = "프로젝트 참여 영역 인증제 점수 추가", description = "현재 인증된 사용자의 프로젝트 참여 영역에 대한 인증제 점수를 추가합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "403",
                description = "해당 프로젝트의 프로젝트 참가자가 아님",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 프로젝트를 매핑함",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "409",
                description = "이미 해당 프로젝트에 대한 점수가 존재하거나 해당 영역에 대한 인증제 점수를 전부 취득함",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/project-participation")
    fun addProjectParticipationScore(
        @RequestBody @Valid request: CreateProjectParticipationScoreRequest,
    ): CreateScoreResponse =
        createProjectParticipationService.execute(
            projectId = request.projectId,
        )

    @Operation(summary = "점수 단건 조회", description = "점수 ID로 점수 상세 정보를 조회합니다 (증거자료 및 파일 포함)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 점수를 매핑함",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{scoreId}")
    fun getScoreByScoreId(
        @PathVariable scoreId: Long,
    ): GetScoreResponse = findScoreByScoreIdService.execute(scoreId = scoreId)

    @Operation(summary = "현재 사용자의 점수 목록 조회", description = "현재 인증된 사용자의 인증제 점수 목록을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    fun getMyScores(
        @RequestParam(required = false) categoryType: CategoryType?,
        @RequestParam(required = false) status: ScoreStatus?,
    ): GetMyScoresResponse = findMyScoresService.execute(categoryType = categoryType, status = status)

    @Operation(summary = "현재 사용자의 카테고리별 점수 조회", description = "현재 인증된 사용자의 인증제 점수를 카테고리별로 그룹핑하여 환산 점수와 함께 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/by-category")
    fun getScoresByCategory(
        @RequestParam(required = false) status: ScoreStatus?,
    ): GetScoresByCategoryResponse = findScoresByCategoryService.execute(status = status)

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
    ): GetTotalScoreResponse = calculateTotalScoreService.execute(includeApprovedOnly = includeApprovedOnly)
}

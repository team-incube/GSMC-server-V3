package com.team.incube.gsmc.v3.domain.project.presentation.controller

import com.team.incube.gsmc.v3.domain.project.presentation.data.request.CreateProjectDraftRequest
import com.team.incube.gsmc.v3.domain.project.presentation.data.request.CreateProjectRequest
import com.team.incube.gsmc.v3.domain.project.presentation.data.request.PatchProjectRequest
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetMyProjectScoreAndEvidenceResponse
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetProjectDraftResponse
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.GetProjectResponse
import com.team.incube.gsmc.v3.domain.project.presentation.data.response.SearchProjectResponse
import com.team.incube.gsmc.v3.domain.project.service.CreateProjectDraftService
import com.team.incube.gsmc.v3.domain.project.service.CreateProjectService
import com.team.incube.gsmc.v3.domain.project.service.DeleteProjectDraftService
import com.team.incube.gsmc.v3.domain.project.service.DeleteProjectService
import com.team.incube.gsmc.v3.domain.project.service.FindMyProjectScoreAndEvidenceService
import com.team.incube.gsmc.v3.domain.project.service.FindMyProjectsService
import com.team.incube.gsmc.v3.domain.project.service.FindProjectByIdService
import com.team.incube.gsmc.v3.domain.project.service.FindMyProjectDraftService
import com.team.incube.gsmc.v3.domain.project.service.SearchProjectService
import com.team.incube.gsmc.v3.domain.project.service.UpdateProjectService
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Project API", description = "프로젝트 관련 API")
@RestController
@RequestMapping("/api/v3/projects")
class ProjectController(
    private val createProjectService: CreateProjectService,
    private val updateProjectService: UpdateProjectService,
    private val deleteProjectService: DeleteProjectService,
    private val searchProjectService: SearchProjectService,
    private val findMyProjectsService: FindMyProjectsService,
    private val findProjectByIdService: FindProjectByIdService,
    private val createProjectDraftService: CreateProjectDraftService,
    private val findMyProjectDraftService: FindMyProjectDraftService,
    private val deleteProjectDraftService: DeleteProjectDraftService,
    private val findMyProjectScoreAndEvidenceService: FindMyProjectScoreAndEvidenceService,
) {
    @Operation(summary = "프로젝트 생성", description = "현재 인증된 사용자를 대표자로 하는 프로젝트를 생성합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    fun createProject(
        @Valid @RequestBody request: CreateProjectRequest,
    ): GetProjectResponse =
        createProjectService.execute(
            title = request.title,
            description = request.description,
            fileIds = request.fileIds,
            participantIds = request.participantIds,
        )

    @Operation(summary = "프로젝트 수정", description = "현재 인증된 사용자가 소유한 프로젝트를 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "403",
                description = "접근 권한이 없음",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 프로젝트",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{projectId}")
    fun updateProject(
        @PathVariable projectId: Long,
        @Valid @RequestBody request: PatchProjectRequest,
    ): GetProjectResponse =
        updateProjectService.execute(
            projectId = projectId,
            title = request.title,
            description = request.description,
            fileIds = request.fileIds,
            participantIds = request.participantIds,
        )

    @Operation(summary = "프로젝트 삭제", description = "현재 인증된 사용자가 소유한 프로젝트를 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "403",
                description = "접근 권한이 없음",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 프로젝트",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{projectId}")
    fun deleteProject(
        @PathVariable projectId: Long,
    ): CommonApiResponse<Nothing> {
        deleteProjectService.execute(projectId)
        return CommonApiResponse.success("OK")
    }

    @Operation(summary = "프로젝트 검색", description = "프로젝트를 검색합니다. 파라미터가 없으면 전체 프로젝트를 페이징 처리하여 반환합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/search")
    fun searchProjects(
        @RequestParam(required = false) title: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): SearchProjectResponse =
        searchProjectService.execute(
            title = title,
            pageable = PageRequest.of(page, size),
        )

    @Operation(summary = "내 프로젝트 목록 조회", description = "현재 인증된 사용자가 참여 중인 프로젝트 목록을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/current")
    fun getCurrentProjects(): List<GetProjectResponse> = findMyProjectsService.execute()

    @Operation(summary = "프로젝트 단건 조회", description = "프로젝트 ID로 프로젝트를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 프로젝트",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{projectId}")
    fun getProject(
        @PathVariable projectId: Long,
    ): GetProjectResponse = findProjectByIdService.execute(projectId)

    @Operation(summary = "프로젝트 임시저장 생성", description = "프로젝트를 임시저장합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "임시저장 성공",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/draft")
    fun createProjectDraft(
        @Valid @RequestBody request: CreateProjectDraftRequest,
    ): GetProjectDraftResponse = createProjectDraftService.execute(request = request)

    @Operation(summary = "프로젝트 임시저장 조회", description = "임시저장된 프로젝트를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "임시저장 조회 성공",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/draft")
    fun getProjectDraft(): GetProjectDraftResponse? = findMyProjectDraftService.execute()

    @Operation(summary = "프로젝트 임시저장 삭제", description = "임시저장된 프로젝트를 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "임시저장 삭제 성공",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/draft")
    fun deleteProjectDraft(): CommonApiResponse<Nothing> {
        deleteProjectDraftService.execute()
        return CommonApiResponse.success("OK")
    }

    @Operation(
        summary = "내 프로젝트 참여 점수 및 증빙자료 조회",
        description = "현재 인증된 사용자가 특정 프로젝트에 대해 작성한 점수와 증빙자료를 조회합니다. 작성하지 않았으면 해당 필드는 null입니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
            ApiResponse(
                responseCode = "403",
                description = "프로젝트 참가자가 아님",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 프로젝트",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{projectId}/my-score-and-evidence")
    fun getMyProjectScoreAndEvidence(
        @PathVariable projectId: Long,
    ): GetMyProjectScoreAndEvidenceResponse = findMyProjectScoreAndEvidenceService.execute(projectId)
}

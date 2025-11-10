package com.team.incube.gsmc.v3.domain.developer.presentation

import com.team.incube.gsmc.v3.domain.developer.presentation.data.request.DeleteMemberRequest
import com.team.incube.gsmc.v3.domain.developer.presentation.data.request.PatchMemberRoleRequest
import com.team.incube.gsmc.v3.domain.developer.service.DeleteMemberService
import com.team.incube.gsmc.v3.domain.developer.service.UpdateMemberRoleService
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody

@Tag(name = "Developer API", description = "개발자 전용 사용자 관리 API")
@RestController
@RequestMapping("/api/v3/developer")
@Validated
class DeveloperController(
    private val patchMemberRoleService: UpdateMemberRoleService,
    private val withdrawMemberService: DeleteMemberService,
) {
    @Operation(
        summary = "사용자 권한 변경",
        description = "요청 바디의 email, role 로 사용자의 권한을 변경합니다.",
    )
    @SwaggerRequestBody(
        required = true,
        content = [
            Content(
                schema = Schema(implementation = PatchMemberRoleRequest::class),
            ),
        ],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청이 성공함"),
            ApiResponse(responseCode = "404", description = "존재하지 않는 사용자", content = [Content()]),
        ],
    )
    @PatchMapping("/member/role")
    fun changeMemberRole(
        @RequestBody @Valid request: PatchMemberRoleRequest,
    ): CommonApiResponse<Nothing> {
        patchMemberRoleService.execute(request.email, request.role)
        return CommonApiResponse.success("OK")
    }

    @Operation(
        summary = "회원탈퇴",
        description = "요청 바디의 email 로 해당 사용자를 삭제합니다.",
    )
    @SwaggerRequestBody(
        required = true,
        content = [
            Content(
                schema = Schema(implementation = DeleteMemberRequest::class),
            ),
        ],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청이 성공함"),
            ApiResponse(responseCode = "404", description = "존재하지 않는 사용자", content = [Content()]),
        ],
    )
    @DeleteMapping("/member")
    fun delete(
        @RequestBody @Valid request: DeleteMemberRequest,
    ): CommonApiResponse<Nothing> {
        withdrawMemberService.execute(request.email)
        return CommonApiResponse.success("OK")
    }
}

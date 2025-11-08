package com.team.incube.gsmc.v3.domain.developer.presentation

import com.team.incube.gsmc.v3.domain.developer.service.ChangeMemberRoleService
import com.team.incube.gsmc.v3.domain.developer.service.WithdrawMemberService
import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Developer API", description = "개발자 전용 사용자 관리 API")
@RestController
@RequestMapping("/api/v3/developer")
class DeveloperController(
    private val changeMemberRoleService: ChangeMemberRoleService,
    private val withdrawMemberService: WithdrawMemberService,
) {

    @Operation(
        summary = "사용자 권한 변경",
        description = "query parameter email, role 로 사용자의 권한을 변경합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청이 성공함"),
            ApiResponse(responseCode = "404", description = "존재하지 않는 사용자", content = [Content()]),
            ApiResponse(responseCode = "500", description = "서버 내부 오류", content = [Content()]),
        ],
    )
    @PatchMapping("/member/role")
    fun changeMemberRole(
        @RequestParam email: String,
        @RequestParam role: MemberRole,
    ): CommonApiResponse<Nothing> {
        changeMemberRoleService.execute(email, role)
        return CommonApiResponse.success("OK")
    }

    @Operation(
        summary = "회원탈퇴",
        description = "query parameter email 로 해당 사용자를 삭제합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청이 성공함"),
            ApiResponse(responseCode = "404", description = "존재하지 않는 사용자", content = [Content()]),
            ApiResponse(responseCode = "500", description = "서버 내부 오류", content = [Content()]),
        ],
    )
    @DeleteMapping("/withdrawal")
    fun withdraw(
        @RequestParam email: String,
    ): CommonApiResponse<Nothing> {
        withdrawMemberService.execute(email)
        return CommonApiResponse.success("OK")
    }
}

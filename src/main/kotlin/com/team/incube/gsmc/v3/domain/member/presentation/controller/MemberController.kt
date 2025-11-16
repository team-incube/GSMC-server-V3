package com.team.incube.gsmc.v3.domain.member.presentation.controller

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import com.team.incube.gsmc.v3.domain.member.presentation.data.response.GetMemberResponse
import com.team.incube.gsmc.v3.domain.member.presentation.data.response.SearchMemberResponse
import com.team.incube.gsmc.v3.domain.member.service.GetCurrentMemberService
import com.team.incube.gsmc.v3.domain.member.service.SearchMemberService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Member API", description = "사용자 조회 API")
@RestController
@RequestMapping("/api/v3/members")
class MemberController(
    private val searchMemberService: SearchMemberService,
    private val getCurrentMemberService: GetCurrentMemberService,
) {
    @Operation(
        summary = "사용자 정보 검색",
        description = "이메일, 이름, 학년, 반, 번호로 회원을 검색합니다",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "사용자 정보 검색 성공",
                content = [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = SearchMemberResponse::class)),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/search")
    fun searchMember(
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) role: MemberRole?,
        @RequestParam(required = false) grade: Int?,
        @RequestParam(required = false) classNumber: Int?,
        @RequestParam(required = false) number: Int?,
        @RequestParam(required = false) limit: Int = 100,
        @RequestParam(required = false) page: Int = 0,
    ): SearchMemberResponse =
        searchMemberService.execute(
            email = email,
            name = name,
            role = role,
            grade = grade,
            classNumber = classNumber,
            number = number,
            pageable = PageRequest.of(page, limit),
        )

    @Operation(
        summary = "현재 로그인된 사용자 정보 조회",
        description = " JWT 토큰을 통해 현재 로그인된 사용자의 정보를 조회합니다",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "현재 로그인된 사용자 정보 검색 성공",
                content = [
                    Content(
                        schema = Schema(implementation = SearchMemberResponse::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/current")
    fun getCurrentMember(): GetMemberResponse = getCurrentMemberService.execute()
}

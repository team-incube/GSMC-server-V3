package com.team.incube.gsmc.v3.domain.member.presentation.data.response

import com.team.incube.gsmc.v3.domain.member.dto.constant.MemberRole
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "현재 사용자 정보 응답")
data class GetMemberResponse(
    @param:Schema(description = "사용자 ID", example = "1")
    val id: Long,
    @param:Schema(description = "이름", example = "홍길동")
    val name: String,
    @param:Schema(description = "이메일", example = "s25069@gsm.hs.kr")
    val email: String,
    @param:Schema(description = "학년", example = "2")
    val grade: Int?,
    @param:Schema(description = "반", example = "3")
    val classNumber: Int?,
    @param:Schema(description = "번호", example = "15")
    val number: Int?,
    @param:Schema(description = "권한", example = "STUDENT")
    val role: MemberRole,
)

package com.team.incube.gsmc.v3.domain.member.dto.constant

import org.springframework.security.core.GrantedAuthority

enum class MemberRole : GrantedAuthority {
    UNAUTHORIZED {
        override fun getAuthority(): String = "ROLE_UNAUTHORIZED"
    },
    STUDENT {
        override fun getAuthority(): String = "ROLE_STUDENT"
    },
    TEACHER {
        override fun getAuthority(): String = "ROLE_TEACHER"
    },
    ROOT {
        override fun getAuthority(): String = "ROLE_ROOT"
    }
}

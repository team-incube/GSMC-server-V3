package com.team.incube.gsmc.v3.domain.category.presentation

import com.team.incube.gsmc.v3.domain.category.presentation.data.response.GetCategoriesResponse
import com.team.incube.gsmc.v3.domain.category.service.FindAllCategoryService
import com.team.incube.gsmc.v3.domain.category.service.SearchCategoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Category API", description = "인증제 항목 조회 API")
@RestController
@RequestMapping("/api/v3/categories")
class CategoryController(
    private val findAllCategoriesService: FindAllCategoryService,
    private val searchCategoriesService: SearchCategoryService,
) {
    @Operation(summary = "전체 인증제 항목 조회", description = "모든 인증제 항목 목록을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "인증제 항목 목록 조회 성공",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    fun getCategories(): GetCategoriesResponse = findAllCategoriesService.execute()

    @Operation(summary = "인증제 항목 검색", description = "키워드로 인증제 항목을 검색합니다 (국문명 또는 영문명)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "인증제 항목 검색 성공",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/search")
    fun searchCategories(
        @Parameter(description = "검색 키워드", example = "자격증", required = false)
        @RequestParam(required = false)
        keyword: String?,
    ): GetCategoriesResponse = searchCategoriesService.execute(keyword)
}

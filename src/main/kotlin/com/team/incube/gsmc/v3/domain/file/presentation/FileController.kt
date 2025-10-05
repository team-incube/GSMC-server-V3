package com.team.incube.gsmc.v3.domain.file.presentation

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.CreateFileResponse
import com.team.incube.gsmc.v3.domain.file.service.CreateFileService
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "File API", description = "파일 업로드 관리 API")
@RestController
@RequestMapping("/api/v3/files")
class FileController(
    private val createFileService: CreateFileService,
) {

    @Operation(summary = "증빙자료 파일 업로드", description = "증빙자료용 파일을 업로드합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "파일 업로드 성공",
                content = [Content(schema = Schema(implementation = CreateFileResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 파일 형식 또는 파일 크기 초과",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.OK)
    fun uploadFile(
        @RequestParam("file") file: MultipartFile,
    ): CommonApiResponse<CreateFileResponse> {
        val uploadedFile = createFileService.execute(file)
        val response = CreateFileResponse.from(uploadedFile)
        return CommonApiResponse.success("OK", response)
    }
}

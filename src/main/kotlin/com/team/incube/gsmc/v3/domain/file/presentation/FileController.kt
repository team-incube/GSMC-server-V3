package com.team.incube.gsmc.v3.domain.file.presentation

import com.team.incube.gsmc.v3.domain.file.presentation.data.response.CreateFileResponse
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetMyFilesResponse
import com.team.incube.gsmc.v3.domain.file.service.CreateFileService
import com.team.incube.gsmc.v3.domain.file.service.DeleteFileService
import com.team.incube.gsmc.v3.domain.file.service.FindMyFilesService
import com.team.incube.gsmc.v3.domain.file.service.FindMyUnusedFilesService
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "File API", description = "파일 관련 API")
@RestController
@RequestMapping("/api/v3/files")
class FileController(
    private val createFileService: CreateFileService,
    private val deleteFileService: DeleteFileService,
    private val findMyFilesService: FindMyFilesService,
    private val findMyUnusedFilesService: FindMyUnusedFilesService,
) {
    @Operation(summary = "파일 업로드", description = "파일을 업로드합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "파일 업로드 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 파일 형식 또는 파일 크기 초과",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    fun uploadFile(
        @RequestParam("file") file: MultipartFile,
    ): CreateFileResponse = createFileService.execute(file = file)

    @Operation(summary = "파일 삭제", description = "업로드된 파일을 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "파일 삭제 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "해당하는 파일이 없음",
                content = [Content()],
            ),
        ],
    )
    @Operation(summary = "내 파일 목록 조회", description = "현재 인증된 사용자가 소유한 모든 파일 목록을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my")
    fun getMyFiles(): GetMyFilesResponse = findMyFilesService.execute()

    @Operation(summary = "미사용 파일 목록 조회", description = "현재 인증된 사용자가 소유하고 있지만 어디에도 사용되지 않는 파일 목록을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "요청이 성공함",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my/unused")
    fun getMyUnusedFiles(): GetMyFilesResponse = findMyUnusedFilesService.execute()

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{fileId}")
    fun deleteFile(
        @PathVariable fileId: Long,
    ): CommonApiResponse<Nothing> {
        deleteFileService.execute(fileId = fileId)
        return CommonApiResponse.success("OK")
    }
}

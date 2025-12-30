package com.team.incube.gsmc.v3.domain.file.presentation

import com.team.incube.gsmc.v3.domain.file.presentation.data.request.ConfirmFileUploadRequest
import com.team.incube.gsmc.v3.domain.file.presentation.data.request.CreatePresignedUploadUrlRequest
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.CreateFileResponse
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.CreatePresignedUploadUrlResponse
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetFileResponse
import com.team.incube.gsmc.v3.domain.file.presentation.data.response.GetMyFilesResponse
import com.team.incube.gsmc.v3.domain.file.service.ConfirmFileUploadService
import com.team.incube.gsmc.v3.domain.file.service.CreateFileService
import com.team.incube.gsmc.v3.domain.file.service.CreatePresignedUploadUrlService
import com.team.incube.gsmc.v3.domain.file.service.DeleteFileService
import com.team.incube.gsmc.v3.domain.file.service.FindFileByIdService
import com.team.incube.gsmc.v3.domain.file.service.FindMyFilesService
import com.team.incube.gsmc.v3.domain.file.service.FindMyUnusedFilesService
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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
    private val findFileByIdService: FindFileByIdService,
    private val findMyFilesService: FindMyFilesService,
    private val findMyUnusedFilesService: FindMyUnusedFilesService,
    private val createPresignedUploadUrlService: CreatePresignedUploadUrlService,
    private val confirmFileUploadService: ConfirmFileUploadService,
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

    @Operation(summary = "파일 단건 조회", description = "파일 ID로 파일 정보를 조회합니다. 파일 소유자만 조회할 수 있습니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "파일 조회 성공",
            ),
            ApiResponse(
                responseCode = "403",
                description = "파일에 대한 접근 권한이 없음",
                content = [Content()],
            ),
            ApiResponse(
                responseCode = "404",
                description = "해당하는 파일이 없음",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{fileId}")
    fun getFileById(
        @PathVariable fileId: Long,
    ): GetFileResponse = findFileByIdService.execute(fileId = fileId)

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
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{fileId}")
    fun deleteFile(
        @PathVariable fileId: Long,
    ): CommonApiResponse<Nothing> {
        deleteFileService.execute(fileId = fileId)
        return CommonApiResponse.success("OK")
    }

    @Operation(
        summary = "Pre-signed URL 생성",
        description = "S3 파일 업로드를 위한 Pre-signed URL을 생성합니다. 클라이언트는 이 URL을 사용하여 S3에 직접 파일을 업로드할 수 있습니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Pre-signed URL 생성 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 파일 형식 또는 파일 크기 초과",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/presigned-url")
    fun createPresignedUploadUrl(
        @Valid @RequestBody request: CreatePresignedUploadUrlRequest,
    ): CreatePresignedUploadUrlResponse = createPresignedUploadUrlService.execute(request)

    @Operation(
        summary = "파일 업로드 확인",
        description = "Pre-signed URL을 통해 S3에 업로드된 파일을 확인하고 데이터베이스에 등록합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "파일 업로드 확인 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "S3에서 파일을 찾을 수 없음",
                content = [Content()],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/confirm")
    fun confirmFileUpload(
        @Valid @RequestBody request: ConfirmFileUploadRequest,
    ): CreateFileResponse = confirmFileUploadService.execute(request)
}

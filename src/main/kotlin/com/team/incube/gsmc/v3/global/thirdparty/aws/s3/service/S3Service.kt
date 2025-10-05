package com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service

import org.springframework.web.multipart.MultipartFile

interface S3Service {
    fun uploadFile(file: MultipartFile): String
    fun deleteFile(fileUri: String)
}

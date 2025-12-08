package com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service

import org.springframework.web.multipart.MultipartFile

interface S3UploadService {
    fun execute(file: MultipartFile): String
}

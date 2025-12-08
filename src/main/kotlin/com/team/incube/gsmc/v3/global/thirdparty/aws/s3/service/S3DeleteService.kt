package com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service

interface S3DeleteService {
    fun execute(fileUri: String)

    fun execute(fileUris: List<String>)
}

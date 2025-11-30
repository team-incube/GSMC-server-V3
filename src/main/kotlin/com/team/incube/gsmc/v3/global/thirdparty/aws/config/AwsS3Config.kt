package com.team.incube.gsmc.v3.global.thirdparty.aws.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Configuration
class AwsS3Config(
    private val awsEnvironment: AwsEnvironment,
) {
    @Bean
    fun s3Client(): S3Client {
        val credentials =
            AwsBasicCredentials.create(
                awsEnvironment.credentials.accessKey,
                awsEnvironment.credentials.secretKey,
            )
        return S3Client
            .builder()
            .region(Region.of(awsEnvironment.region.static))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()
    }
}

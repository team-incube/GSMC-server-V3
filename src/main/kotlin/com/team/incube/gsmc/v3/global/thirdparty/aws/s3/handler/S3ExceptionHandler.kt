package com.team.incube.gsmc.v3.global.thirdparty.aws.s3.handler

import com.team.incube.gsmc.v3.global.common.error.ErrorCode
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import io.awspring.cloud.s3.S3Exception
import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.core.exception.SdkClientException
import java.io.IOException

object S3ExceptionHandler {
    inline fun <T> handleS3Operation(
        uploadOperation: Boolean = true,
        operation: () -> T,
    ): T =
        try {
            operation()
        } catch (_: IOException) {
            throw GsmcException(ErrorCode.S3_IO_ERROR)
        } catch (_: S3Exception) {
            val errorCode =
                if (uploadOperation) {
                    ErrorCode.S3_FILE_UPLOAD_FAILED
                } else {
                    ErrorCode.S3_FILE_DELETE_FAILED
                }
            throw GsmcException(errorCode)
        } catch (_: AwsServiceException) {
            throw GsmcException(ErrorCode.S3_SERVICE_ERROR)
        } catch (_: SdkClientException) {
            throw GsmcException(ErrorCode.S3_CLIENT_ERROR)
        }

    fun handleUploadOperation(operation: () -> String): String =
        handleS3Operation(uploadOperation = true, operation = operation)

    fun handleDeleteOperation(operation: () -> Unit) {
        handleS3Operation(uploadOperation = false, operation = operation)
    }
}

package com.team.incube.gsmc.v3.global.common.error.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.incube.gsmc.v3.global.common.error.exception.FeignClientException
import com.team.incube.gsmc.v3.global.common.error.exception.GsmcException
import com.team.incube.gsmc.v3.global.common.response.data.CommonApiResponse
import com.team.incube.gsmc.v3.global.config.logger
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@EnableWebMvc
@RestControllerAdvice
class GlobalExceptionHandler {
    companion object {
        private val objectMapper = ObjectMapper()
    }

    @ExceptionHandler(GsmcException::class)
    fun handleGsmcException(ex: GsmcException): CommonApiResponse<Nothing> {
        logger().warn("GsmcException : {}", ex.message)
        logger().trace("GsmcException Details : ", ex)
        return CommonApiResponse.error(ex.message ?: "알 수 없는 오류", ex.statusCode)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class, HttpMessageNotReadableException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): CommonApiResponse<Nothing> {
        logger().warn("Validation Failed : {}", ex.message)
        logger().trace("Validation Failed Details : ", ex)
        return CommonApiResponse.error(
            message = createValidationErrorMessage(ex),
            status = HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): CommonApiResponse<Nothing> {
        logger().warn("Field validation failed : {}", ex.message)
        logger().trace("Field validation failed : ", ex)
        return CommonApiResponse.error(
            message = "필드 유효성 검사 실패 : ${ex.message}",
            status = HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(FeignClientException::class)
    fun handleFeignClientException(ex: FeignClientException): CommonApiResponse<Nothing> {
        logger().warn("FeignClientException : {}", ex.message)
        logger().trace("FeignClientException Details : ", ex)
        return CommonApiResponse.error(ex.message ?: "Feign 클라이언트 오류", ex.statusCode)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleUnexpectedException(ex: RuntimeException): CommonApiResponse<Nothing> {
        logger().error("UnexpectedException Occur : ", ex)
        return CommonApiResponse.error(
            message = "서버 내부 오류가 발생했습니다.",
            status = HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(ex: NoHandlerFoundException): CommonApiResponse<Nothing> {
        logger().warn("Not Found Endpoint : {}", ex.message)
        logger().trace("Not Found Endpoint Details : ", ex)
        return CommonApiResponse.error(ex.message ?: "올바르지 않은 엔드포인트입니다", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceededException(ex: MaxUploadSizeExceededException): CommonApiResponse<Nothing> {
        logger().warn("The file is too big : {}", ex.message)
        logger().trace("The file is too big Details : ", ex)
        return CommonApiResponse.error(
            message = "파일이 너무 큽니다, 최대 파일 용량 : ${ex.maxUploadSize}",
            status = HttpStatus.BAD_REQUEST,
        )
    }

    private fun createValidationErrorMessage(ex: MethodArgumentNotValidException): String {
        val bindingResult = ex.bindingResult
        val objectName = bindingResult.objectName

        val errorMap =
            buildMap<String, Any> {
                // Global errors
                bindingResult.globalErrors.forEach { error ->
                    put(objectName, error.defaultMessage ?: "유효성 검사 오류")
                }

                // Field errors
                val fieldErrors =
                    bindingResult.fieldErrors.associate { error ->
                        error.field to (error.defaultMessage ?: "유효성 검사 오류")
                    }

                if (fieldErrors.isNotEmpty()) {
                    put(objectName, fieldErrors)
                }
            }

        return try {
            objectMapper.writeValueAsString(errorMap).replace("\"", "'")
        } catch (e: Exception) {
            logger().error("Error serializing validation errors", e)
            "Validation failed"
        }
    }
}

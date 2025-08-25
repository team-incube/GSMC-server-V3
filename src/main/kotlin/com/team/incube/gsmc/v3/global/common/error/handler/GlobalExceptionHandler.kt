package com.team.incube.gsmc.v3.global.common.error.handler

import com.fasterxml.jackson.databind.ObjectMapper
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
        return CommonApiResponse.error(ex.message ?: "Unknown error", ex.statusCode)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class, HttpMessageNotReadableException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): CommonApiResponse<Nothing> {
        logger().warn("Validation Failed : {}", ex.message)
        logger().trace("Validation Failed Details : ", ex)
        return CommonApiResponse.error(
            message = createValidationErrorMessage(ex),
            status = HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): CommonApiResponse<Nothing> {
        logger().warn("Field validation failed : {}", ex.message)
        logger().trace("Field validation failed : ", ex)
        return CommonApiResponse.error(
            message = "field validation failed : ${ex.message}",
            status = HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleUnexpectedException(ex: RuntimeException): CommonApiResponse<Nothing> {
        logger().error("UnexpectedException Occur : ", ex)
        return CommonApiResponse.error(
            message = "internal server error has occurred",
            status = HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(ex: NoHandlerFoundException): CommonApiResponse<Nothing> {
        logger().warn("Not Found Endpoint : {}", ex.message)
        logger().trace("Not Found Endpoint Details : ", ex)
        return CommonApiResponse.error(ex.message ?: "Endpoint not found", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceededException(ex: MaxUploadSizeExceededException): CommonApiResponse<Nothing> {
        logger().warn("The file is too big : {}", ex.message)
        logger().trace("The file is too big Details : ", ex)
        return CommonApiResponse.error(
            message = "The file is too big, limited file size : ${ex.maxUploadSize}",
            status = HttpStatus.BAD_REQUEST
        )
    }

    private fun createValidationErrorMessage(ex: MethodArgumentNotValidException): String {
        val bindingResult = ex.bindingResult
        val objectName = bindingResult.objectName

        val errorMap = buildMap<String, Any> {
            // Global errors
            bindingResult.globalErrors.forEach { error ->
                put(objectName, error.defaultMessage ?: "Validation error")
            }

            // Field errors
            val fieldErrors = bindingResult.fieldErrors.associate { error ->
                error.field to (error.defaultMessage ?: "Invalid field")
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
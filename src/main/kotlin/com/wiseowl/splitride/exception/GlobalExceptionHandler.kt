package com.wiseowl.splitride.exception

import com.wiseowl.splitride.feature.response.SplitRideResponse
import com.wiseowl.splitride.feature.response.SplitRideResponse.Companion.createErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice


@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleNotFound(ex: IllegalArgumentException): SplitRideResponse<Any>? {
        return createErrorResponse(
            errorMessage = ex.message ?: "Unknown error",
            status = HttpStatus.BAD_REQUEST.value(),
        )
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun badCredentialsException(
        ex: Exception?
    ): ResponseEntity<String> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ex?.message)
    }
}
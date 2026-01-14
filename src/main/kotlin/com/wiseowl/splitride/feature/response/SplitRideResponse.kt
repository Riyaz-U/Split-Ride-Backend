package com.wiseowl.splitride.feature.response

data class SplitRideResponse<T>(
    val success: Boolean,
    val status: Int,
    val data: T?,
    val errorMessage: String?
){
    companion object{
        fun <T> createSuccessResponse(
            data: T?,
            status: Int,
            success: Boolean = true,
        ): SplitRideResponse<T> {
            return SplitRideResponse(
                success = success,
                status = status,
                data = data,
                errorMessage = null
            )
        }

        fun createErrorResponse(
            errorMessage: String,
            status: Int,
            success: Boolean = false,
        ): SplitRideResponse<Any> {
            return SplitRideResponse(
                success = success,
                status = status,
                data = null,
                errorMessage = errorMessage
            )
        }
    }
}
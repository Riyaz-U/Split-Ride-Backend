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
            status: Int
        ): SplitRideResponse<T> {
            return SplitRideResponse(
                success = true,
                status = status,
                data = data,
                errorMessage = null
            )
        }

        fun <T> createErrorResponse(
            errorMessage: String,
            status: Int,
        ): SplitRideResponse<T> {
            return SplitRideResponse<T>(
                success = false,
                status = status,
                data = null,
                errorMessage = errorMessage
            )
        }
    }
}
package com.example.notification.common

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(true, "Success", data)
        fun <T> error(message: String): ApiResponse<T> = ApiResponse(false, message, null)
    }
}

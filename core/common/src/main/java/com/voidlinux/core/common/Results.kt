package com.voidlinux.core.common

sealed class VoidResult<out T> {

    data class Success<T>(val data: T) : VoidResult<T>()

    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val code: Int = -1
    ) : VoidResult<Nothing>()

    object Loading : VoidResult<Nothing>()

    object Empty : VoidResult<Nothing>()

    inline fun onSuccess(action: (T) -> Unit): VoidResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (Error) -> Unit): VoidResult<T> {
        if (this is Error) action(this)
        return this
    }

    fun getOrNull(): T? = (this as? Success)?.data
}
package com.bium.youngssoo.core.domain

sealed interface Result<out D, out E: Error> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E: com.bium.youngssoo.core.domain.Error>(val error: E): Result<Nothing, E>
    // ⬇️ 프로그레스 추가: 값은 0f..1f(알 수 없으면 null)
    data class Progress(val value: Float? = null): Result<Nothing, Nothing>
}

inline fun <T, E: Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
        is Result.Progress -> this // 그대로 전달(variance로 타입 OK)
    }
}

fun <T, E: Error> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
    return when (this) {
        is Result.Success  -> Result.Success(Unit)
        is Result.Error    -> Result.Error(error)
        is Result.Progress -> this // 그대로 전달
    }
}

inline fun <T, E: Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error    -> this
        is Result.Success  -> { action(data); this }
        is Result.Progress -> this
    }
}

inline fun <T, E: Error> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error    -> { action(error); this }
        is Result.Success  -> this
        is Result.Progress -> this
    }
}

// (선택) 프로그레스 콜백
inline fun <T, E: Error> Result<T, E>.onProgress(action: (Float?) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Progress -> { action(value); this as Result<T, E> }
        else -> this
    }
}

typealias EmptyResult<E> = Result<Unit, E>

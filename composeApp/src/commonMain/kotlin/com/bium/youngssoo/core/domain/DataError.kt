package com.bium.youngssoo.core.domain

sealed interface DataError : Error {
    enum class Remote : DataError {
        REQUEST_TIMEOUT, TOO_MANY_REQUESTS, NO_INTERNET, SERVER, SERIALIZATION, UNKNOWN, LOGIN_FAILED, FAILED_BOARD, FAILED_COMMENT, REQUEST_ERROR, REFRESH_TOKEN_FAILED
    }

    enum class Local : DataError {
        DISK_FULL, UNKNOWN, EMPTY_TEMP_WRITING
    }
}
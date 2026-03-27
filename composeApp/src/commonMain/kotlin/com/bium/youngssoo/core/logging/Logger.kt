package com.bium.youngssoo.core.logging

interface Logger {
    /**
     * priority: 로깅 우선순위를 나타내는 정수 값 (예: DEBUG = 3, ERROR = 6 등)
     */
    fun log(priority: Int, tag: String?, message: String, throwable: Throwable? = null)
}
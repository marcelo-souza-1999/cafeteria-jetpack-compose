package com.targaryen.cafeteria.core_network.util

interface AppLogger {
    fun d(
        tag: String,
        message: String,
    )

    fun i(
        tag: String,
        message: String,
    )

    fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )
}

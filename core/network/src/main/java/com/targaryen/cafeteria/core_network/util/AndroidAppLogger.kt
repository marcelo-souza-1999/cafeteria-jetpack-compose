package com.targaryen.cafeteria.core_network.util

import android.util.Log
import org.koin.core.annotation.Single

@Single
class AndroidAppLogger : AppLogger {
    override fun d(
        tag: String,
        message: String,
    ) {
        Log.d(tag, message)
    }

    override fun i(
        tag: String,
        message: String,
    ) {
        Log.i(tag, message)
    }

    override fun e(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        Log.e(tag, message, throwable)
    }
}

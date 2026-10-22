package com.example.util

import android.util.Log

/**
 * Standardized logging utility for StreamDeck TV with tag consistency and level filtering.
 */
object Logger {
    private const val TAG_PREFIX = "StreamDeckTV"

    fun d(tag: String, message: String) {
        Log.d("$TAG_PREFIX:$tag", message)
    }

    fun i(tag: String, message: String) {
        Log.i("$TAG_PREFIX:$tag", message)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w("$TAG_PREFIX:$tag", message, throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e("$TAG_PREFIX:$tag", message, throwable)
    }
}

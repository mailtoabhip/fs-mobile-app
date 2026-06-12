package com.dfd.delfin.utils

import android.content.Context

object JsonUtils {

    fun readJsonFromRaw(context: Context, rawResId: Int): String {
        return context.resources.openRawResource(rawResId)
            .bufferedReader()
            .use { it.readText() }
    }
}
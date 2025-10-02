package com.coheser.app.activitesfragments.livestreaming.utils

import android.content.Context
import android.content.SharedPreferences
import com.coheser.app.activitesfragments.livestreaming.StreamingConstants

object PrefManager {
    fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(StreamingConstants.PREF_NAME, Context.MODE_PRIVATE)
    }
}

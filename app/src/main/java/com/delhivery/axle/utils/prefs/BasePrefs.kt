package com.delhivery.axle.utils.prefs

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

@SuppressLint("CommitPrefEdits")
abstract class BasePrefs(
  private val context: Context
) {

  /**
   * Get Prefs Name or file name
   */
  abstract fun prefsName(): PrefNames

  /**
   * [SharedPreferences] instance
   */
  protected val prefs: SharedPreferences by lazy {
    context.getSharedPreferences(prefsName().prefName, Context.MODE_PRIVATE)
  }

  /**
   * [SharedPreferences.Editor] instance
   */
  protected val editor: SharedPreferences.Editor by lazy {
    prefs.edit()
  }

  /**
   * Clear all preferences
   */
  fun clearPrefs() = editor.clear().apply()
}

enum class PrefNames(val prefName: String) {
  GlobalPrefs("global_prefs"),
  UserPrefs("user_prefs")
}
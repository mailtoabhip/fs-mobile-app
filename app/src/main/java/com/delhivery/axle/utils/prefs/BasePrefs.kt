package com.delhivery.axle.utils.prefs

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

@SuppressLint("CommitPrefEdits")
abstract class BasePrefs(
  private val context: Context
) {

  /**
   * Get Prefs Name or file name
   */
  abstract fun prefsName(): PrefNames

  /**
   * Whether this prefs file should use EncryptedSharedPreferences.
   * Subclasses opt in by overriding to true.
   */
  protected open val isEncrypted: Boolean = false

  /**
   * [SharedPreferences] instance — plain or encrypted based on [isEncrypted]
   */
  protected val prefs: SharedPreferences by lazy {
    if (isEncrypted) createEncryptedPreferences() else createPlainPreferences()
  }

  /**
   * [SharedPreferences.Editor] instance
   */
  protected val editor: SharedPreferences.Editor by lazy {
    prefs.edit()
  }

  private fun createPlainPreferences(): SharedPreferences {
    return context.getSharedPreferences(prefsName().prefName, Context.MODE_PRIVATE)
  }

  private fun createEncryptedPreferences(): SharedPreferences {
    return try {
      val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
      EncryptedSharedPreferences.create(
        context,
        prefsName().prefName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
      )
    } catch (e: Exception) {
      Log.w("BasePrefs", "Keystore failure for ${prefsName().prefName}, falling back to plain", e)
      createPlainPreferences()
    }
  }

}

enum class PrefNames(val prefName: String) {
  GlobalPrefs("global_prefs"),
  UserPrefs("user_prefs")
}
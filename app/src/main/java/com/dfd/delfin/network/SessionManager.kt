package com.dfd.delfin.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton that manages session state across the app.
 * When the token refresh fails, it emits a session-expired event
 * that BaseActivity observes to force-navigate to the login screen.
 */
@Singleton
class SessionManager @Inject constructor() {

    private val _sessionExpired = MutableLiveData<Boolean>()
    val sessionExpired: LiveData<Boolean> = _sessionExpired

    /**
     * Call when the refresh token fails and local session is cleared.
     * This triggers all active activities to navigate to login.
     */
    fun onSessionExpired() {
        Log.w(TAG, "Session expired — forcing logout across all screens")
        _sessionExpired.postValue(true)
    }

    /**
     * Reset after handling the event to avoid re-triggering on config changes.
     */
    fun resetSessionExpired() {
        _sessionExpired.value = false
    }

    companion object {
        private const val TAG = "SessionManager"
    }
}

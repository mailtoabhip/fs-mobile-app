package com.dfd.delfin.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Production implementation of ErrorLogger that logs exceptions to Firebase Crashlytics.
 * This enables centralized error monitoring and crash reporting for the application.
 */
class CrashlyticsErrorLogger : ErrorLogger {
    /**
     * Logs the exception to Firebase Crashlytics for monitoring and debugging.
     *
     * @param exception The exception to log
     */
    override fun log(exception: Exception) {
        FirebaseCrashlytics.getInstance().recordException(exception)
    }
}

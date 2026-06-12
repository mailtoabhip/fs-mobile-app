package com.dfd.delfin.utils

/**
 * Interface for logging errors throughout the application.
 * This abstraction allows for different implementations in production and testing environments.
 *
 * Production implementations typically log to crash reporting services like Firebase Crashlytics,
 * while test implementations can verify logging behavior or suppress logs.
 */
interface ErrorLogger {
    /**
     * Log an exception for monitoring and debugging purposes.
     *
     * @param exception The exception to log
     */
    fun log(exception: Exception)
}

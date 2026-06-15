package com.dfd.delfin.injection.qualifier

import javax.inject.Qualifier

/**
 * Dagger qualifier annotation for injecting the IO CoroutineDispatcher.
 * Use this qualifier when injecting dispatchers for IO-bound operations like network calls and database access.
 *
 * Example usage:
 * ```
 * @Inject
 * @IoDispatcher
 * lateinit var ioDispatcher: CoroutineDispatcher
 * ```
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

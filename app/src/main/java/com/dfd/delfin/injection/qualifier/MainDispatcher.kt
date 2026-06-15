package com.dfd.delfin.injection.qualifier

import javax.inject.Qualifier

/**
 * Dagger qualifier annotation for injecting the Main CoroutineDispatcher.
 * Use this qualifier when injecting dispatchers for UI-bound operations that need to run on the main thread.
 *
 * Example usage:
 * ```
 * @Inject
 * @MainDispatcher
 * lateinit var mainDispatcher: CoroutineDispatcher
 * ```
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

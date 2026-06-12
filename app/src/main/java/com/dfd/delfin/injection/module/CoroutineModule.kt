package com.dfd.delfin.injection.module

import com.dfd.delfin.injection.qualifier.IoDispatcher
import com.dfd.delfin.injection.qualifier.MainDispatcher
import com.dfd.delfin.utils.CrashlyticsErrorLogger
import com.dfd.delfin.utils.ErrorLogger
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * Dagger module providing coroutine dispatchers and error logging dependencies.
 * This module enables testable coroutine-based code by allowing dispatcher injection.
 */
@Module
class CoroutineModule {

    /**
     * Provides the IO CoroutineDispatcher for IO-bound operations.
     * Use this dispatcher for network calls, database access, and file operations.
     *
     * @return Dispatchers.IO
     */
    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Provides the Main CoroutineDispatcher for UI-bound operations.
     * Use this dispatcher for operations that need to update the UI.
     *
     * @return Dispatchers.Main
     */
    @Provides
    @Singleton
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    /**
     * Provides the ErrorLogger implementation for logging exceptions.
     * Production implementation logs to Firebase Crashlytics.
     *
     * @return CrashlyticsErrorLogger instance
     */
    @Provides
    @Singleton
    fun provideErrorLogger(): ErrorLogger = CrashlyticsErrorLogger()
}

package com.delhivery.axle.testutils

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers

/**
 * Reusable test setup for ViewModel tests.
 * Handles LiveData and RxJava scheduler configuration for synchronous testing.
 * 
 * Usage in Kotest FunSpec:
 * ```
 * beforeSpec { ViewModelTestSetup.setup() }
 * afterSpec { ViewModelTestSetup.teardown() }
 * ```
 */
object ViewModelTestSetup {
    
    /**
     * Sets up synchronous execution for LiveData and RxJava.
     * Call this in beforeSpec or beforeTest.
     */
    fun setup() {
        setupLiveData()
        setupRxJava()
    }

    /**
     * Resets all test configurations.
     * Call this in afterSpec or afterTest.
     */
    fun teardown() {
        teardownLiveData()
        teardownRxJava()
    }

    /**
     * Configure ArchTaskExecutor for synchronous LiveData execution.
     */
    private fun setupLiveData() {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })
    }

    /**
     * Reset ArchTaskExecutor to default behavior.
     */
    private fun teardownLiveData() {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }

    /**
     * Configure RxJava schedulers for synchronous execution.
     * Uses trampoline scheduler which executes work on the current thread.
     */
    private fun setupRxJava() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    /**
     * Reset RxJava schedulers to default behavior.
     */
    private fun teardownRxJava() {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }
}

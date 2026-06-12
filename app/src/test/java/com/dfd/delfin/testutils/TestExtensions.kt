package com.dfd.delfin.testutils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.mockk.mockk

/**
 * Test utility extensions for cleaner test code.
 */

// ==================== MockK Helpers ====================

/**
 * Creates a relaxed mock that returns default values for all methods.
 * Useful when you only care about specific method behaviors.
 * 
 * Usage:
 * ```
 * val repo: InvoiceRepository = relaxedMock()
 * ```
 */
inline fun <reified T : Any> relaxedMock(): T = mockk(relaxed = true)

/**
 * Creates a strict mock (default MockK behavior).
 * All method calls must be stubbed or will throw.
 * 
 * Usage:
 * ```
 * val repo: InvoiceRepository = strictMock()
 * ```
 */
inline fun <reified T : Any> strictMock(): T = mockk()

// ==================== LiveData Extensions ====================

/**
 * Captures all values emitted by a LiveData during a test.
 * Returns a mutable list that updates as new values are emitted.
 * Note: Does NOT capture the current value, only new emissions.
 * 
 * Usage:
 * ```
 * val loadingStates = viewModel.isLoadingLiveData.captureValues()
 * viewModel.fetchData()
 * loadingStates shouldContainExactly listOf(true, false)
 * ```
 */
fun <T> LiveData<T>.captureValues(): MutableList<T> {
    val values = mutableListOf<T>()
    val currentValue = this.value
    this.observeForever { value ->
        // Skip if this is the initial value we already had
        if (values.isEmpty() && value == currentValue) return@observeForever
        value?.let { values.add(it) }
    }
    return values
}

/**
 * Captures all values including nulls emitted by a LiveData.
 * 
 * Usage:
 * ```
 * val errors = viewModel.errorLiveData.captureAllValues()
 * ```
 */
fun <T> LiveData<T>.captureAllValues(): MutableList<T?> {
    val values = mutableListOf<T?>()
    this.observeForever { values.add(it) }
    return values
}

/**
 * Gets the current value or throws if null.
 * Useful for assertions where you expect a value to exist.
 * 
 * Usage:
 * ```
 * viewModel.dataLiveData.requireValue() shouldBe expectedData
 * ```
 */
fun <T> LiveData<T>.requireValue(): T {
    return value ?: throw AssertionError("LiveData value is null")
}

/**
 * Waits for a specific number of emissions and returns them.
 * Clears any previous values before starting capture.
 * 
 * Usage:
 * ```
 * val states = viewModel.stateLiveData.awaitValues(count = 3) {
 *     viewModel.performAction()
 * }
 * ```
 */
fun <T> LiveData<T>.awaitValues(count: Int, action: () -> Unit): List<T> {
    val values = mutableListOf<T>()
    val observer = Observer<T> { value ->
        value?.let { values.add(it) }
    }
    this.observeForever(observer)
    action()
    this.removeObserver(observer)
    return values.take(count)
}

// ==================== MutableLiveData Extensions ====================

/**
 * Clears the current value by posting null.
 * Useful for resetting state between tests.
 * 
 * Usage:
 * ```
 * viewModel.errorLiveData.clear()
 * ```
 */
fun <T> MutableLiveData<T>.clear() {
    this.postValue(null)
}

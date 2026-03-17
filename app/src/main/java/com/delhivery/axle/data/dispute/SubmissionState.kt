package com.delhivery.axle.data.dispute

/**
 * Sealed class representing form submission states
 */
sealed class SubmissionState {
    object Idle : SubmissionState()
    object Loading : SubmissionState()
    data class Success(val message: String, val srId: String? = null) : SubmissionState()
    data class Error(val message: String) : SubmissionState()
}

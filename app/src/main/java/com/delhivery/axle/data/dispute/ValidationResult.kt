package com.delhivery.axle.data.dispute

/**
 * Result of field validation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

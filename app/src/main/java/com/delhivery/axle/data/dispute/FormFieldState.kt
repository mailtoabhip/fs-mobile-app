package com.delhivery.axle.data.dispute

import android.net.Uri
import com.delhivery.axle.api.response.FormField

/**
 * UI state model for a form field
 */
data class FormFieldState(
    val field: FormField,
    val value: Any? = null,
    val validationResult: ValidationResult = ValidationResult(true),
    val fileUri: Uri? = null,
    val fileUrl: String? = null
)

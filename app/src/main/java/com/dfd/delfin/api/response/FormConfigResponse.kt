package com.dfd.delfin.api.response

import com.google.gson.annotations.SerializedName

/**
 * Response model for dispute form configuration
 * API returns: { "success": true, "data": [ ...FormField list... ] }
 * After convertResponse(), we receive List<FormField> directly.
 * FormConfigResponse wraps it for use in the ViewModel.
 */
data class FormConfigResponse(
    @SerializedName("data")
    val fields: List<FormField>
)

/**
 * Individual form field configuration
 */
data class FormField(
    @SerializedName("displayOrder")
    val displayOrder: Int?,
    
    @SerializedName("fieldType")
    val fieldType: String?,
    
    @SerializedName("displayLabel")
    val displayLabel: String?,
    
    @SerializedName("placeholder")
    val placeholder: String?,
    
    @SerializedName("mandatory")
    val mandatory: Boolean,
    
    @SerializedName("minLength")
    val minLength: Int?,
    
    @SerializedName("maxLength")
    val maxLength: Int?,
    
    @SerializedName("validationRule")
    val validationRule: String?,
    
    @SerializedName("validationErrorMessage")
    val validationErrorMessage: String?,
    
    @SerializedName("helpText")
    val helpText: String? = null,
    
    @SerializedName("allowedFileTypes")
    val allowedFileTypes: List<String>?,
    
    @SerializedName("maxFileSizeMB")
    val maxFileSizeMB: Int?
) {
    /**
     * Generate unique field ID from display order
     */
    val fieldId: String
        get() = "field_$displayOrder"
    
    /**
     * Parse field type enum
     */
    val fieldTypeEnum: FieldType
        get() = try {
            FieldType.valueOf(fieldType?:"TEXTAREA")
        } catch (e: IllegalArgumentException) {
            FieldType.TEXT
        }
}

/**
 * Enum for field types
 */
enum class FieldType {
    NUMBER, TEXT, TEXTAREA, FILE
}

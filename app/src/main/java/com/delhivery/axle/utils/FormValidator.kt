package com.delhivery.axle.utils

import android.content.Context
import android.net.Uri
import com.delhivery.axle.api.response.FormField
import com.delhivery.axle.data.dispute.ValidationResult

/**
 * Utility object for validating form fields
 */
object FormValidator {

    /**
     * Validate a text field (TEXT or TEXTAREA)
     */
    fun validateTextField(
        value: String,
        field: FormField
    ): ValidationResult {
        // Check mandatory
        if (field.mandatory && value.trim().isEmpty()) {
            return ValidationResult(
                isValid = false,
                errorMessage = field.validationErrorMessage ?: "This field is required"
            )
        }

        // Skip validation if field is optional and empty
        if (!field.mandatory && value.trim().isEmpty()) {
            return ValidationResult(isValid = true)
        }

        // Check min length
        field.minLength?.let { minLen ->
            if (value.length < minLen) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = field.validationErrorMessage 
                        ?: "Minimum $minLen characters required"
                )
            }
        }

        // Check max length
        field.maxLength?.let { maxLen ->
            if (value.length > maxLen) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = field.validationErrorMessage 
                        ?: "Maximum $maxLen characters allowed"
                )
            }
        }

        // Check validation rule
        field.validationRule?.let { rule ->
            if (!evaluateTextValidationRule(value, rule)) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = field.validationErrorMessage 
                        ?: "Invalid input format"
                )
            }
        }

        return ValidationResult(isValid = true)
    }

    /**
     * Validate a number field
     */
    fun validateNumberField(
        value: String,
        field: FormField
    ): ValidationResult {
        // Check mandatory
        if (field.mandatory && value.trim().isEmpty()) {
            return ValidationResult(
                isValid = false,
                errorMessage = field.validationErrorMessage ?: "This field is required"
            )
        }

        // Skip validation if field is optional and empty
        if (!field.mandatory && value.trim().isEmpty()) {
            return ValidationResult(isValid = true)
        }

        // Check if it's a valid number
        val numericValue = value.toDoubleOrNull()
        if (numericValue == null) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Please enter a valid number"
            )
        }

        // Check min length (for string representation)
        field.minLength?.let { minLen ->
            if (value.length < minLen) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = field.validationErrorMessage 
                        ?: "Minimum $minLen digits required"
                )
            }
        }

        // Check max length (for string representation)
        field.maxLength?.let { maxLen ->
            if (value.length > maxLen) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = field.validationErrorMessage 
                        ?: "Maximum $maxLen digits allowed"
                )
            }
        }

        // Check validation rule (regex and complex rules)
        field.validationRule?.let { rule ->
            if (!evaluateComplexValidation(value, rule)) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = field.validationErrorMessage 
                        ?: "Invalid number format"
                )
            }
        }

        return ValidationResult(isValid = true)
    }

    /**
     * Validate a file field
     */
    fun validateFileField(
        uri: Uri?,
        field: FormField,
        context: Context
    ): ValidationResult {
        // Check mandatory
        if (field.mandatory && uri == null) {
            return ValidationResult(
                isValid = false,
                errorMessage = field.validationErrorMessage ?: "Please select a file"
            )
        }

        // Skip validation if field is optional and no file selected
        if (!field.mandatory && uri == null) {
            return ValidationResult(isValid = true)
        }

        uri?.let { fileUri ->
            // Parse validationRule for FILE_TYPE and MAX_SIZE if present
            val ruleAllowedTypes = field.validationRule?.let { parseFileTypes(it) }
            val ruleMaxSizeMB = field.validationRule?.let { parseMaxSizeMB(it) }

            // Use validationRule values, fall back to dedicated fields
            val allowedTypes = ruleAllowedTypes ?: field.allowedFileTypes
            val maxSizeMB = ruleMaxSizeMB ?: field.maxFileSizeMB

            // Validate file type
            allowedTypes?.let { types ->
                val extension = getFileExtension(fileUri, context)
                if (!types.any { it.equals(extension, ignoreCase = true) }) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = field.validationErrorMessage
                            ?: "Only ${types.joinToString(", ")} files are allowed"
                    )
                }
            }

            // Validate file size
            maxSizeMB?.let { maxSize ->
                val fileSizeBytes = getFileSize(fileUri, context)
                val fileSizeMB = fileSizeBytes / (1024.0 * 1024.0)
                if (fileSizeMB > maxSize) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = field.validationErrorMessage
                            ?: "File size exceeds ${maxSize}MB limit"
                    )
                }
            }
        }

        return ValidationResult(isValid = true)
    }

    /**
     * Evaluate validation rule for TEXT/TEXTAREA fields.
     * Supports: "MIN_LENGTH:10, MAX_LENGTH:500" style and regex/complex rules.
     */
    private fun evaluateTextValidationRule(value: String, rule: String): Boolean {
        // Check for MIN_LENGTH / MAX_LENGTH directives
        if (rule.contains("MIN_LENGTH", ignoreCase = true) || rule.contains("MAX_LENGTH", ignoreCase = true)) {
            val parts = rule.split(",").map { it.trim() }
            for (part in parts) {
                val keyValue = part.split(":").map { it.trim() }
                if (keyValue.size == 2) {
                    val key = keyValue[0].uppercase()
                    val num = keyValue[1].toIntOrNull()
                    if (key == "MIN_LENGTH" && num != null && value.length < num) return false
                    if (key == "MAX_LENGTH" && num != null && value.length > num) return false
                }
            }
            return true
        }
        // Fallback to complex validation (regex + AND expressions)
        return evaluateComplexValidation(value, rule)
    }

    /**
     * Parse FILE_TYPE from validationRule string.
     * e.g. "FILE_TYPE: [JPG, JPEG, PNG], MAX_SIZE: 2MB" -> ["JPG", "JPEG", "PNG"]
     */
    private fun parseFileTypes(rule: String): List<String>? {
        val match = Regex("FILE_TYPE\\s*:\\s*\\[([^]]+)]", RegexOption.IGNORE_CASE).find(rule)
        return match?.groupValues?.get(1)?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
    }

    /**
     * Parse MAX_SIZE from validationRule string.
     * e.g. "FILE_TYPE: [JPG, JPEG, PNG], MAX_SIZE: 2MB" -> 2
     */
    private fun parseMaxSizeMB(rule: String): Int? {
        val match = Regex("MAX_SIZE\\s*:\\s*(\\d+)\\s*MB", RegexOption.IGNORE_CASE).find(rule)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    /**
     * Check if a value matches a regex pattern
     */
    private fun matchesRegex(value: String, pattern: String): Boolean {
        return try {
            // Use DOTALL flag to make . match newlines as well
            val regex = Regex(pattern, RegexOption.DOT_MATCHES_ALL)
            value.matches(regex)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Evaluate complex validation rules
     * Supports patterns like: "^\\d+(\\.\\d{1,2})?$ AND value > 0"
     */
    private fun evaluateComplexValidation(value: String, rule: String): Boolean {
        // Split by AND operator
        val parts = rule.split(" AND ", ignoreCase = true)
        
        return parts.all { part ->
            val trimmedPart = part.trim()
            when {
                // Regex pattern (starts with ^)
                trimmedPart.startsWith("^") -> matchesRegex(value, trimmedPart)
                
                // Comparison operators
                trimmedPart.contains(">") || 
                trimmedPart.contains("<") || 
                trimmedPart.contains("=") -> evaluateComparison(value, trimmedPart)
                
                // Default: treat as regex
                else -> matchesRegex(value, trimmedPart)
            }
        }
    }

    /**
     * Evaluate comparison expressions like "value > 0"
     */
    private fun evaluateComparison(value: String, expression: String): Boolean {
        return try {
            val numericValue = value.toDoubleOrNull() ?: return false
            
            when {
                expression.contains(">=") -> {
                    val threshold = expression.substringAfter(">=").trim().toDoubleOrNull() ?: return false
                    numericValue >= threshold
                }
                expression.contains("<=") -> {
                    val threshold = expression.substringAfter("<=").trim().toDoubleOrNull() ?: return false
                    numericValue <= threshold
                }
                expression.contains(">") -> {
                    val threshold = expression.substringAfter(">").trim().toDoubleOrNull() ?: return false
                    numericValue > threshold
                }
                expression.contains("<") -> {
                    val threshold = expression.substringAfter("<").trim().toDoubleOrNull() ?: return false
                    numericValue < threshold
                }
                expression.contains("==") || expression.contains("=") -> {
                    val operator = if (expression.contains("==")) "==" else "="
                    val threshold = expression.substringAfter(operator).trim().toDoubleOrNull() ?: return false
                    numericValue == threshold
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get file extension from URI
     */
    private fun getFileExtension(uri: Uri, context: Context): String {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)
            
            // Try to get extension from MIME type
            mimeType?.let {
                when {
                    it.contains("jpeg") || it.contains("jpg") -> "JPG"
                    it.contains("png") -> "PNG"
                    it.contains("pdf") -> "PDF"
                    else -> {
                        // Fallback: get from file name
                        val fileName = uri.lastPathSegment ?: ""
                        fileName.substringAfterLast(".", "").uppercase()
                    }
                }
            } ?: run {
                // Fallback: get from file name
                val fileName = uri.lastPathSegment ?: ""
                fileName.substringAfterLast(".", "").uppercase()
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Get file size in bytes from URI
     */
    private fun getFileSize(uri: Uri, context: Context): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.available().toLong()
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

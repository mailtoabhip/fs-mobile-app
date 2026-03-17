package com.delhivery.axle.ui.fastag

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.response.FieldType
import com.delhivery.axle.api.response.FormConfigResponse
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.data.dispute.SubmissionState
import com.delhivery.axle.data.dispute.ValidationResult
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.FileUploadManager
import com.delhivery.axle.utils.FormValidator
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

class FastagDynamicDisputeFormViewModel @Inject constructor(
    private val loadboardRepository: LoadboardRepository,
    private val fileUploadManager: FileUploadManager
) : BaseViewModel() {

    // LiveData
    val formConfigData = MutableLiveData<FormConfigResponse>()
    val validationStateData = MutableLiveData<Map<String, ValidationResult>>()
    val submissionStateData = MutableLiveData<SubmissionState>()
    val progressData = MutableLiveData<Boolean>()
    val errorData = MutableLiveData<String>()
    val submitEnabledData = MutableLiveData<Boolean>()

    // State
    private val fieldValues = mutableMapOf<String, Any?>()
    private val fileUris = mutableMapOf<String, Uri>()
    private var formConfig: FormConfigResponse? = null

    /**
     * Load form configuration from API
     */
    fun loadFormConfig(disputeTypeCode: String) {
        progressData.value = true

        compositeDisposable += loadboardRepository.getDisputeFormConfig(disputeTypeCode)
            .onBackground()
            .progress()
            .subscribe { response, error ->
                progressData.value = false

                if (!error && response != null) {
                    formConfig = response
                    formConfigData.value = response
                } else {
                    error.handle()
                    errorData.value = error.message ?: "Failed to load form configuration"
                }
            }
    }

    /**
     * Validate a single field
     */
    fun validateField(fieldId: String, value: Any?, context: Context): ValidationResult {
        val field = formConfig?.fields?.find { it.fieldId == fieldId } ?: return ValidationResult(false, "Field not found")

        return when (field.fieldTypeEnum) {
            FieldType.TEXT, FieldType.TEXTAREA -> {
                FormValidator.validateTextField(value as? String ?: "", field)
            }
            FieldType.NUMBER -> {
                FormValidator.validateNumberField(value as? String ?: "", field)
            }
            FieldType.FILE -> {
                val uri = fileUris[fieldId]
                FormValidator.validateFileField(uri, field, context)
            }
        }
    }

    /**
     * Validate all fields
     */
    fun validateAllFields(context: Context): Boolean {
        val validationResults = mutableMapOf<String, ValidationResult>()
        var allValid = true

        formConfig?.fields?.forEach { field ->
            val value = fieldValues[field.fieldId]
            val result = validateField(field.fieldId, value, context)
            validationResults[field.fieldId] = result

            if (!result.isValid) {
                allValid = false
            }
        }

        validationStateData.value = validationResults
        return allValid
    }

    /**
     * Update field value
     */
    fun updateFieldValue(fieldId: String, value: Any?) {
        fieldValues[fieldId] = value
        checkMandatoryFieldsComplete()
    }

    /**
     * Check if all mandatory fields have values and update submit button state
     */
    fun checkMandatoryFieldsComplete() {
        val mandatoryFields = formConfig?.fields?.filter { it.mandatory } ?: emptyList()
        
        var allComplete = true
        for (field in mandatoryFields) {
            if (field.fieldTypeEnum == FieldType.FILE) {
                if (fileUris[field.fieldId] == null) {
                    allComplete = false
                    break
                }
            } else {
                val str = fieldValues[field.fieldId]?.toString() ?: ""
                if (str.isBlank()) {
                    allComplete = false
                    break
                }
            }
        }
        
        submitEnabledData.value = allComplete
    }

    /**
     * Get field value
     */
    fun getFieldValue(fieldId: String): Any? {
        return fieldValues[fieldId]
    }

    /**
     * Set file URI for a field
     */
    fun setFileUri(fieldId: String, uri: Uri, context: Context) {
        fileUris[fieldId] = uri
        
        // Validate file immediately
        val field = formConfig?.fields?.find { it.fieldId == fieldId }
        if (field != null) {
            val validationResult = fileUploadManager.validateFile(
                uri,
                field.allowedFileTypes ?: emptyList(),
                field.maxFileSizeMB ?: 10,
                context
            )

            val currentValidation = validationStateData.value?.toMutableMap() ?: mutableMapOf()
            currentValidation[fieldId] = validationResult
            validationStateData.value = currentValidation
        }
        
        checkMandatoryFieldsComplete()
    }

    /**
     * Remove uploaded file
     */
    fun removeFile(fieldId: String) {
        fileUris.remove(fieldId)
        fieldValues.remove(fieldId)
        checkMandatoryFieldsComplete()
    }

    /**
     * Submit form via POST /api/v1/disputes (multipart/form-data)
     *
     * Maps dynamic form fields to the API contract:
     *   txn_id, toll_plaza_id, refundRequestedAmount, comment, raisedAgainst,
     *   upload_doc1, upload_doc2, upload_doc3
     */
    fun submitForm(
        disputeTypeCode: String,
        transactionId: String?,
        additionalTxnId:String?=null,
        fastagId: String,
        tollPlazaId: String,
        context: Context
    ) {
        // Validate all fields first
        if (!validateAllFields(context)) {
            submissionStateData.value = SubmissionState.Error("Please fix all validation errors before submitting")
            return
        }

        val txnId = transactionId ?: ""

        // Extract refundRequestedAmount from the first NUMBER field
        var refundRequestedAmount = 0.0
        formConfig?.fields
            ?.filter { it.fieldTypeEnum == FieldType.NUMBER }
            ?.firstOrNull()
            ?.let { field ->
                refundRequestedAmount = (fieldValues[field.fieldId] as? String)?.toDoubleOrNull() ?: 0.0
            }

        // Extract comment from TEXTAREA / TEXT fields
        val commentParts = mutableListOf<String>()
        formConfig?.fields
            ?.filter { it.fieldTypeEnum == FieldType.TEXTAREA || it.fieldTypeEnum == FieldType.TEXT }
            ?.forEach { field ->
                val text = fieldValues[field.fieldId] as? String
                if (!text.isNullOrBlank()) {
                    commentParts.add(text)
                }
            }
        val comment = commentParts.joinToString("\n")

        // Collect file parts — required docs first, then additional docs
        val fileParts = mutableListOf<MultipartBody.Part?>()
        var fileIndex = 1

        // Required file fields from form config
        formConfig?.fields
            ?.filter { it.fieldTypeEnum == FieldType.FILE }
            ?.forEach { field ->
                val uri = fileUris[field.fieldId]
                if (uri != null) {
                    val part = createMultipartFromUri(uri, "uploadDoc$fileIndex", context)
                    fileParts.add(part)
                    fileIndex++
                } else if (field.mandatory) {
                    submissionStateData.value = SubmissionState.Error("Please upload all required files")
                    return
                }
            }

        // Additional documents (user-added)
        fileUris.filter { it.key.startsWith("additional_doc_") }
            .forEach { (_, uri) ->
                if (fileIndex <= 3) {
                    val part = createMultipartFromUri(uri, "uploadDoc$fileIndex", context)
                    fileParts.add(part)
                    fileIndex++
                }
            }

        // Pad to exactly 3 slots (API accepts upload_doc1, upload_doc2, upload_doc3)
        while (fileParts.size < 3) {
            fileParts.add(null)
        }

        // Fire the request
        submissionStateData.value = SubmissionState.Loading
        progressData.value = true

        compositeDisposable += loadboardRepository.submitDispute(
            txnId = MultipartBody.Part.createFormData("txn_id", transactionId?:""),
            additionalTxnId = MultipartBody.Part.createFormData("additional_txn_id", additionalTxnId?:""),
            tollPlazaId = MultipartBody.Part.createFormData("toll_plaza_id", tollPlazaId),
            refundAmount = MultipartBody.Part.createFormData("refundRequestedAmount", Gson().toJson(refundRequestedAmount)),
            comment = MultipartBody.Part.createFormData("comment", comment),
            raisedAgainst = MultipartBody.Part.createFormData("raisedAgainst", disputeTypeCode),
            doc1 = fileParts.getOrNull(0),
            doc2 = fileParts.getOrNull(1),
            doc3 = fileParts.getOrNull(2)
        )
            .onBackground()
            .subscribe { response, error ->
                progressData.value = false

                if (!error && response != null) {
                    submissionStateData.value = SubmissionState.Success(
                        "Dispute submitted successfully. SR ID: ${response.srId ?: "N/A"}",
                        srId = response.srId
                    )
                } else {
                    error.handle()
                    submissionStateData.value = SubmissionState.Error(
                        error.message ?: "Submission failed. Please try again."
                    )
                }
            }
    }

    /**
     * Create MultipartBody.Part from URI
     */
    private fun createMultipartFromUri(uri: Uri, partName: String, context: Context): MultipartBody.Part? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpeg")
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val requestBody = RequestBody.create(MediaType.parse(mimeType), file)
            MultipartBody.Part.createFormData(partName, file.name, requestBody)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get file URI for a field
     */
    fun getFileUri(fieldId: String): Uri? {
        return fileUris[fieldId]
    }
}

package com.delhivery.axle.ui.fastag

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.delhivery.axle.R
import com.delhivery.axle.api.response.FieldType
import com.delhivery.axle.api.response.FormField
import com.delhivery.axle.data.dispute.SubmissionState
import com.delhivery.axle.databinding.ActivityFastagDynamicDisputeFormBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.customviews.DynamicFileUploadView
import com.delhivery.axle.ui.customviews.DynamicTextInputView
import com.delhivery.axle.utils.WindowInsetsUtils

class FastagDynamicDisputeFormActivity : BaseActivity<ActivityFastagDynamicDisputeFormBinding, FastagDynamicDisputeFormViewModel>() {

    override fun getViewModelClass() = FastagDynamicDisputeFormViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_dynamic_dispute_form
    override fun requireConnection() = true

    private var disputeTypeCode: String = ""
    private var transactionId: String? = null
    private var fastagId: String = ""
    private var tollPlazaId: String = ""
    private var transactionTollName: String? = null
    private var transactionTimestamp: String? = null
    private var transactionAmount: Double = 0.0
    private var currentFileFieldId: String? = null
    private var isAdditionalFilePicker = false
    private val fieldViews = mutableMapOf<String, View>()
    private val validationHandler = Handler(Looper.getMainLooper())
    private val validationRunnables = mutableMapOf<String, Runnable>()
    private var additionalDocCount = 0

    companion object {
        const val EXTRA_DISPUTE_TYPE_CODE = "dispute_type_code"
        const val EXTRA_SELECTED_TRANSACTION_ID = "selected_transaction_id"
        const val EXTRA_FASTAG_ID = "fastag_id"
        const val EXTRA_TOLL_PLAZA_ID = "toll_plaza_id"
        const val EXTRA_TRANSACTION_TOLL_NAME = "transaction_toll_name"
        const val EXTRA_TRANSACTION_TIMESTAMP = "transaction_timestamp"
        const val EXTRA_TRANSACTION_AMOUNT = "transaction_amount"
    }

    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                if (isAdditionalFilePicker) {
                    handleAdditionalFileSelected(uri)
                } else {
                    currentFileFieldId?.let { fieldId ->
                        handleFileSelected(fieldId, uri)
                    }
                }
            }
        }
        isAdditionalFilePicker = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        observeData()
        loadFormConfig()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        /* Handle window insets for edge-to-edge display (API 35+) */
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
        }
    }

    private fun setupUI() {
        // Get intent extras
        disputeTypeCode = intent.getStringExtra(EXTRA_DISPUTE_TYPE_CODE) ?: ""
        transactionId = intent.getStringExtra(EXTRA_SELECTED_TRANSACTION_ID)
        fastagId = intent.getStringExtra(EXTRA_FASTAG_ID) ?: ""
        tollPlazaId = intent.getStringExtra(EXTRA_TOLL_PLAZA_ID) ?: ""
        transactionTollName = intent.getStringExtra(EXTRA_TRANSACTION_TOLL_NAME)
        transactionTimestamp = intent.getStringExtra(EXTRA_TRANSACTION_TIMESTAMP)
        transactionAmount = intent.getDoubleExtra(EXTRA_TRANSACTION_AMOUNT, 0.0)

        // Setup header
        binding.tvTitle.text = "Dispute submission  form"
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        // Setup selected transaction card
        setupSelectedTransaction()

        // Setup terms and conditions
        setupTermsAndConditions()

        // Setup submit button - initially disabled until mandatory fields are filled
        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.alpha = 0.5f
        binding.btnSubmit.setOnClickListener {
            onSubmitClicked()
        }

        // Setup add document button
        binding.btnAddDocument.setOnClickListener {
            openAdditionalFilePicker()
        }
    }

    private fun setupSelectedTransaction() {
        if (transactionId != null && transactionTollName != null) {
            binding.llSelectedTransaction.visibility = View.VISIBLE
            binding.tvTransactionTollName.text = transactionTollName
            binding.tvTransactionDateTime.text = transactionTimestamp ?: ""

            val amountAbs = kotlin.math.abs(transactionAmount)
            binding.tvTransactionAmount.text = String.format("₹%.2f", amountAbs)

            binding.tvChangeTransaction.setOnClickListener {
                // Go back to transaction selection
                finish()
            }
        }
    }

    private fun setupTermsAndConditions() {
        val fullText = "By submitting you agree to the Terms & Conditions for raising a dispute and confirm the details provided are correct."
        val spannable = SpannableString(fullText)

        val termsStart = fullText.indexOf("Terms & Conditions")
        val termsEnd = termsStart + "Terms & Conditions".length

        if (termsStart >= 0) {
            spannable.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // Open terms and conditions
                    Toast.makeText(this@FastagDynamicDisputeFormActivity, "Terms & Conditions", Toast.LENGTH_SHORT).show()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = android.graphics.Color.parseColor("#1A56DB")
                    ds.isUnderlineText = false
                    ds.isFakeBoldText = true
                }
            }, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.tvTermsAndConditions.text = spannable
        binding.tvTermsAndConditions.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun loadFormConfig() {
        viewModel.loadFormConfig(disputeTypeCode)
    }

    private fun observeData() {
        // Observe progress
        viewModel.progressData.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe form configuration
        viewModel.formConfigData.observe(this) { formConfig ->
            // Update issue category title from API response
            binding.tvIssueTitle.text = formConfig.issueCategory

            // Render form fields
            renderFormFields(formConfig.fields)
        }

        // Observe validation state
        viewModel.validationStateData.observe(this) { validationMap ->
            updateFieldErrors(validationMap)
        }

        // Observe submission state
        viewModel.submissionStateData.observe(this) { state ->
            handleSubmissionState(state)
        }

        // Observe errors
        viewModel.errorData.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }

        // Observe submit button enabled state
        viewModel.submitEnabledData.observe(this) { isEnabled ->
            binding.btnSubmit.isEnabled = isEnabled
            binding.btnSubmit.alpha = if (isEnabled) 1.0f else 0.5f
        }
    }

    private fun renderFormFields(fields: List<FormField>) {
        // Clear existing views
        binding.llFormFields.removeAllViews()
        binding.llFileFields.removeAllViews()
        fieldViews.clear()

        // Sort fields by display order
        val sortedFields = fields.sortedBy { it.displayOrder }

        // Separate text/number fields from file fields
        val textFields = sortedFields.filter { it.fieldTypeEnum != FieldType.FILE }
        val fileFields = sortedFields.filter { it.fieldTypeEnum == FieldType.FILE }

        // Render text/number fields in the main form container
        textFields.forEach { field ->
            val fieldView = createTextInputView(field)
            binding.llFormFields.addView(fieldView)
            fieldViews[field.fieldId] = fieldView
        }

        // Render file fields in the dedicated file section
        if (fileFields.isNotEmpty()) {
            binding.llUploadDocumentsHeader.visibility = View.VISIBLE
            binding.llFileSizeHint.visibility = View.VISIBLE
            binding.llAdditionalDocuments.visibility = View.VISIBLE

            // Build file size hint from field configs
            val maxSize = fileFields.mapNotNull { it.maxFileSizeMB }.maxOrNull() ?: 2
            val allowedTypes = fileFields.flatMap { it.allowedFileTypes ?: emptyList() }.distinct().joinToString(", ")
            binding.tvFileSizeHint.text = "Maximum file size: 100Kb to ${maxSize}.0MB per document. ${allowedTypes.ifEmpty { "JPG, PNG" }} only"

            fileFields.forEach { field ->
                val fieldView = createFileUploadView(field)
                binding.llFileFields.addView(fieldView)
                fieldViews[field.fieldId] = fieldView
            }
        }
    }

    private fun createTextInputView(field: FormField): DynamicTextInputView {
        val view = DynamicTextInputView(this)
        view.setFieldConfig(field)

        // Set initial value if exists
        val existingValue = viewModel.getFieldValue(field.fieldId) as? String
        if (existingValue != null) {
            view.setValue(existingValue)
        }

        // Listen for value changes - just update value, don't validate while typing
        view.setOnValueChangedListener { value ->
            viewModel.updateFieldValue(field.fieldId, value)
            // Clear error when user starts typing again
            view.clearError()
        }

        return view
    }

    private fun createFileUploadView(field: FormField): DynamicFileUploadView {
        val view = DynamicFileUploadView(this)
        view.setFieldConfig(field)

        // Set initial file if exists
        val existingUri = viewModel.getFileUri(field.fieldId)
        if (existingUri != null) {
            view.setFileUri(existingUri)
        }

        // Listen for file selection
        view.setOnFileSelectedListener {
            isAdditionalFilePicker = false
            currentFileFieldId = field.fieldId
            openFilePicker(field)
        }

        // Listen for file removal
        view.setOnFileRemovedListener {
            viewModel.removeFile(field.fieldId)
        }

        return view
    }

    private fun openFilePicker(field: FormField) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)

            // Set MIME types based on allowed file types
            field.allowedFileTypes?.let { allowedTypes ->
                val mimeTypes = allowedTypes.mapNotNull { type ->
                    when (type.uppercase()) {
                        "JPG", "JPEG" -> "image/jpeg"
                        "PNG" -> "image/png"
                        "PDF" -> "application/pdf"
                        "DOC" -> "application/msword"
                        "DOCX" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        else -> null
                    }
                }
                if (mimeTypes.isNotEmpty()) {
                    putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes.toTypedArray())
                    if (mimeTypes.first().startsWith("image/")) {
                        type = "image/*"
                    }
                }
            }
        }

        try {
            filePickerLauncher.launch(Intent.createChooser(intent, "Select ${field.displayLabel}"))
        } catch (e: Exception) {
            Toast.makeText(this, "No file picker app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAdditionalFilePicker() {
        isAdditionalFilePicker = true
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }

        try {
            filePickerLauncher.launch(Intent.createChooser(intent, "Select additional document"))
        } catch (e: Exception) {
            Toast.makeText(this, "No file picker app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFileSelected(fieldId: String, uri: Uri) {
        val view = fieldViews[fieldId] as? DynamicFileUploadView
        view?.setFileUri(uri)
        view?.clearError()
        viewModel.setFileUri(fieldId, uri, this)
    }

    private fun handleAdditionalFileSelected(uri: Uri) {
        additionalDocCount++
        val fieldId = "additional_doc_$additionalDocCount"

        // Create a temporary FormField for the additional document
        val additionalField = FormField(
            displayOrder = 100 + additionalDocCount,
            fieldType = "FILE",
            displayLabel = "Additional Document $additionalDocCount",
            placeholder = null,
            mandatory = false,
            minLength = null,
            maxLength = null,
            validationRule = null,
            validationErrorMessage = null,
            helpText = null,
            allowedFileTypes = listOf("JPG", "JPEG", "PNG"),
            maxFileSizeMB = 2
        )

        val view = DynamicFileUploadView(this)
        view.setFieldConfig(additionalField)
        view.removeHorizontalPadding()
        view.setAdditionalDocMode()
        view.setFileUri(uri)

        view.setOnFileRemovedListener {
            binding.llAdditionalFileFields.removeView(view)
            viewModel.removeFile(fieldId)
            fieldViews.remove(fieldId)
        }

        binding.llAdditionalFileFields.addView(view)
        fieldViews[fieldId] = view
        viewModel.setFileUri(fieldId, uri, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up validation handlers
        validationRunnables.values.forEach { validationHandler.removeCallbacks(it) }
        validationRunnables.clear()
    }

    private fun updateFieldErrors(validationMap: Map<String, com.delhivery.axle.data.dispute.ValidationResult>) {
        validationMap.forEach { (fieldId, result) ->
            val view = fieldViews[fieldId]
            when (view) {
                is DynamicTextInputView -> {
                    if (result.isValid) view.clearError() else view.setError(result.errorMessage)
                }
                is DynamicFileUploadView -> {
                    if (result.isValid) view.clearError() else view.setError(result.errorMessage)
                }
            }
        }

        // Scroll to first error
        if (validationMap.any { !it.value.isValid }) {
            val firstErrorField = validationMap.entries.find { !it.value.isValid }
            firstErrorField?.let { entry ->
                val view = fieldViews[entry.key]
                view?.let {
                    binding.scrollView.post {
                        binding.scrollView.smoothScrollTo(0, it.top)
                    }
                }
            }
        }
    }

    private fun onSubmitClicked() {
        viewModel.submitForm(disputeTypeCode, transactionId, fastagId, tollPlazaId, this)
    }

    private fun handleSubmissionState(state: SubmissionState) {
        when (state) {
            is SubmissionState.Loading -> {
                binding.btnSubmit.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
            }
            is SubmissionState.Success -> {
                binding.btnSubmit.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
            is SubmissionState.Error -> {
                binding.btnSubmit.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
            is SubmissionState.Idle -> {
                binding.btnSubmit.isEnabled = true
            }
        }
    }
}

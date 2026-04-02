package com.delhivery.axle.ui.fastag

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.delhivery.axle.R
import com.delhivery.axle.api.response.FieldType
import com.delhivery.axle.api.response.FormField
import com.delhivery.axle.data.dispute.SubmissionState
import com.delhivery.axle.databinding.ActivityFastagDynamicDisputeFormBinding
import com.delhivery.axle.databinding.DialogDisputeSuccessBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.customviews.DynamicFileUploadView
import com.delhivery.axle.ui.customviews.DynamicTextInputView
import com.delhivery.axle.utils.DocumentUtils
import com.delhivery.axle.utils.FileCompressor
import com.delhivery.axle.utils.WindowInsetsUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class FastagDynamicDisputeFormActivity : BaseActivity<ActivityFastagDynamicDisputeFormBinding, FastagDynamicDisputeFormViewModel>() {

    override fun getViewModelClass() = FastagDynamicDisputeFormViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_dynamic_dispute_form
    override fun requireConnection() = true


    @Inject lateinit var documentUtils: DocumentUtils
    @Inject lateinit var fileCompressor: FileCompressor

    private var disputeTypeCode: String = ""
    private var disputeTitle: String = ""
    private var transactionId: String? = null
    private var additionalTransactionId: String? = null
    private var fastagId: String = ""
    private var tollPlazaId: String = ""
    private var transactionTollName: String? = null
    private var transactionTimestamp: String? = null
    private var transactionAmount: Double = 0.0
    private var currentFileFieldId: String? = null
    private var isAdditionalFilePicker = false
    private val fieldViews = mutableMapOf<String, View>()
    private var additionalDocCount = 0

    companion object {
        private const val MAX_TOTAL_UPLOADS = 3
        const val DISPUTE_TITLE = "dispute_title"
        const val EXTRA_DISPUTE_TYPE_CODE = "dispute_type_code"
        const val EXTRA_SELECTED_TRANSACTION_ID = "selected_transaction_id"
        const val EXTRA_FASTAG_ID = "fastag_id"
        const val EXTRA_TOLL_PLAZA_ID = "toll_plaza_id"
        const val EXTRA_TXN_ID = "txn_id"
        const val EXTRA_TRANSACTION_TOLL_NAME = "transaction_toll_name"
        const val EXTRA_TRANSACTION_TIMESTAMP = "transaction_timestamp"
        const val EXTRA_TRANSACTION_AMOUNT = "transaction_amount"
        const val EXTRA_SHOW_TRANSACTION = "show_transaction"
        const val EXTRA_SUBTITLE = "subtitle"
    }

    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->

                val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
                val extension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(mimeType) ?: "bin"

                val timestamp = System.currentTimeMillis()
                if (extension.lowercase() == "png") {
                    lifecycleScope.launch {
                        val destPath = File(cacheDir, "converted_${timestamp}.jpg").absolutePath
                        documentUtils.convertPngToJpg(
                            sourcePath = documentUtils.getPathFromUri(
                                context = this@FastagDynamicDisputeFormActivity,
                                uri = uri
                            ),
                            destPath = destPath
                        )
                        val compressedUri = compressAndGetUri(File(destPath), timestamp)
                        dispatchFileResult(compressedUri)
                    }
                } else {
                    val filePath = documentUtils.getPathFromUri(
                        context = this@FastagDynamicDisputeFormActivity,
                        uri = uri
                    )
                    val compressedUri = compressAndGetUri(File(filePath), timestamp)
                    dispatchFileResult(compressedUri)
                }
            }
        }
        isAdditionalFilePicker = false
    }

    private fun compressAndGetUri(file: File, timestamp: Long): Uri {
        val compressedFile = fileCompressor.compressToFile(file, "compressed_$timestamp.jpg")
        return Uri.fromFile(compressedFile)
    }

    private fun dispatchFileResult(uri: Uri) {
        if (isAdditionalFilePicker) {
            handleAdditionalFileSelected(uri)
        } else {
            currentFileFieldId?.let { fieldId ->
                handleFileSelected(fieldId, uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        hasInlineProgress = true 
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
        disputeTitle = intent.getStringExtra(DISPUTE_TITLE) ?: ""
        transactionId = intent.getStringExtra(EXTRA_TXN_ID)
        additionalTransactionId = intent.getStringExtra(EXTRA_SELECTED_TRANSACTION_ID)
        fastagId = intent.getStringExtra(EXTRA_FASTAG_ID) ?: ""
        tollPlazaId = intent.getStringExtra(EXTRA_TOLL_PLAZA_ID) ?: ""
        transactionTollName = intent.getStringExtra(EXTRA_TRANSACTION_TOLL_NAME)
        transactionTimestamp = intent.getStringExtra(EXTRA_TRANSACTION_TIMESTAMP)
        transactionAmount = intent.getDoubleExtra(EXTRA_TRANSACTION_AMOUNT, 0.0)

        // Setup header
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        // Setup selected transaction card
        setupSelectedTransaction()

        // Setup terms and conditions
//        setupTermsAndConditions()

        // Setup submit button - initially disabled until mandatory fields are filled
        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.alpha = 0.3f
        binding.btnSubmit.setOnClickListener {
            onSubmitClicked()
        }

        // Setup add document button
        binding.btnAddDocument.setOnClickListener {
            openAdditionalFilePicker()
        }
    }

    private fun setupSelectedTransaction() {
        // Check if we should show the transaction card
        val showTransaction = intent.getBooleanExtra(EXTRA_SHOW_TRANSACTION, true)
        
        if (showTransaction && transactionId != null && transactionTollName != null) {
            binding.llSelectedTransaction.visibility = View.VISIBLE
            binding.tvTransactionTollName.text = transactionTollName
            binding.tvTransactionDateTime.text = formatTransactionDateTime(transactionTimestamp ?: "")

            val amountAbs = kotlin.math.abs(transactionAmount)
            binding.tvTransactionAmount.text = String.format("₹%.2f", amountAbs)

            binding.tvChangeTransaction.setOnClickListener {
                // Go back to transaction selection
                finish()
            }
        } else {
            binding.llSelectedTransaction.visibility = View.GONE
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
        viewModel.progressData.observe(this) { isLoading ->
            if (isLoading) {
                uiUtils.showProgress()
            } else {
                uiUtils.hideProgress()
                binding.scrollView.visibility = View.VISIBLE
                binding.llFooter.visibility = View.VISIBLE
            }
        }

        // Observe form configuration
        viewModel.formConfigData.observe(this) { formConfig ->
            binding.tvIssueTitle.text = disputeTitle

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

        val fileFields = sortedFields.filter { it.fieldTypeEnum == FieldType.FILE }
        var fileHeaderInserted = false

        if (fileFields.isNotEmpty()) {
            val maxSize = fileFields.mapNotNull { it.maxFileSizeMB }.maxOrNull() ?: 2
            val allowedTypes = fileFields.flatMap { it.allowedFileTypes ?: emptyList() }.distinct().joinToString(", ")
            binding.tvFileSizeHint.text = "Maximum file size: ${maxSize} MB per document. ${allowedTypes.ifEmpty { "JPEG," }} only"
        }

        binding.llUploadDocumentsHeader.visibility = View.GONE
        binding.llFileSizeHint.visibility = View.GONE

        val lastFileIndex = sortedFields.indexOfLast { it.fieldTypeEnum == FieldType.FILE }

        // Render all fields in displayOrder into the single form container
        sortedFields.forEachIndexed { index, field ->
            // Insert upload header before the first FILE field
            if (field.fieldTypeEnum == FieldType.FILE && !fileHeaderInserted) {
                fileHeaderInserted = true

                // Reparent header into llFormFields
                (binding.llUploadDocumentsHeader.parent as? android.view.ViewGroup)?.removeView(binding.llUploadDocumentsHeader)
                binding.llUploadDocumentsHeader.visibility = View.VISIBLE
                binding.llFormFields.addView(binding.llUploadDocumentsHeader)
            }

            val fieldView = if (field.fieldTypeEnum == FieldType.FILE) {
                createFileUploadView(field)
            } else {
                createTextInputView(field)
            }
            binding.llFormFields.addView(fieldView)
            fieldViews[field.fieldId] = fieldView

            // Insert file size hint right after the last FILE field
            if (index == lastFileIndex && fileFields.isNotEmpty()) {
                (binding.llFileSizeHint.parent as? android.view.ViewGroup)?.removeView(binding.llFileSizeHint)
                binding.llFileSizeHint.visibility = View.VISIBLE
                binding.llFormFields.addView(binding.llFileSizeHint)
            }
        }

        // Show additional documents section if there are file fields and limit not reached
        if (fileFields.isNotEmpty()) {
            // Reparent additional documents section into llFormFields
            (binding.llAdditionalDocuments.parent as? android.view.ViewGroup)?.removeView(binding.llAdditionalDocuments)
            binding.llFormFields.addView(binding.llAdditionalDocuments)
            updateAdditionalDocVisibility()
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

    private fun openFilePicker(field: FormField) {//here
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)

            val mimetypes = arrayOf("image/*")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
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
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
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
            updateAdditionalDocVisibility()
        }

        binding.llAdditionalFileFields.addView(view)
        fieldViews[fieldId] = view
        viewModel.setFileUri(fieldId, uri, this)
        updateAdditionalDocVisibility()
    }

    /**
     * Count total file uploads (API file fields + additional docs) and
     * hide the additional documents section when the limit is reached.
     */
    private fun getTotalFileUploadCount(): Int {
        val apiFileCount = fieldViews.count { (id, _) -> !id.startsWith("additional_doc_") && fieldViews[id] is DynamicFileUploadView }
        val additionalFileCount = fieldViews.count { (id, _) -> id.startsWith("additional_doc_") }
        return apiFileCount + additionalFileCount
    }

    private fun updateAdditionalDocVisibility() {
        binding.llAdditionalDocuments.visibility = View.VISIBLE
        if (getTotalFileUploadCount() >= MAX_TOTAL_UPLOADS) {
            binding.btnAddDocument.visibility = View.GONE
        } else {
            binding.btnAddDocument.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
        viewModel.submitForm(disputeTypeCode = disputeTypeCode,
            transactionId = transactionId,
            additionalTxnId = additionalTransactionId,
            fastagId = fastagId,
            tollPlazaId = tollPlazaId,
            this)
    }

    private fun handleSubmissionState(state: SubmissionState) {
        when (state) {
            is SubmissionState.Loading -> {
                binding.btnSubmit.isEnabled = false
                uiUtils.showProgress()
            }
            is SubmissionState.Success -> {
                uiUtils.hideProgress()
                binding.btnSubmit.isEnabled = true
                showSuccessBottomSheet(state.srId)
            }
            is SubmissionState.Error -> {
                uiUtils.hideProgress()
                binding.btnSubmit.isEnabled = true
            }
            is SubmissionState.Idle -> {
                binding.btnSubmit.isEnabled = true
            }
        }
    }

    private fun formatTransactionDateTime(dateTime: String): String {
        if (dateTime.isEmpty()) return ""
        return com.delhivery.axle.utils.DateUtils.formatFastagTransactionDate(dateTime)
    }

    private fun showSuccessBottomSheet(srId: String?) {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setCanceledOnTouchOutside(false)

        val dialogBinding = DialogDisputeSuccessBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(dialogBinding.root)

        dialogBinding.tvTicketId.text = "#${srId ?: "N/A"}"

        dialogBinding.ivClose.setOnClickListener {
            bottomSheetDialog.dismiss()
            setResult(RESULT_OK)
            finish()
        }

        dialogBinding.btnGoToLoads.setOnClickListener {
            bottomSheetDialog.dismiss()
            val intent = Intent(this, com.delhivery.axle.ui.home.activity.home.HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("fragment_type", "load")
            }
            startActivity(intent)
            finish()
        }

        bottomSheetDialog.show()
    }
}

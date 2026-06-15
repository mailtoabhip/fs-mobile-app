package com.dfd.delfin.ui.kyc.documentverification

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dfd.delfin.R
import com.dfd.delfin.api.response.ServiceRequirementsResponse
import com.dfd.delfin.databinding.ActivityDocumentVerificationBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.common.UiState
import com.dfd.delfin.ui.kyc.dashboard.KycActivityResultHelper
import com.dfd.delfin.ui.kyc.dashboard.KycDocType
import kotlinx.coroutines.launch

class DocumentVerificationActivity :
    BaseActivity<ActivityDocumentVerificationBinding, DocumentVerificationViewModel>() {

    companion object {
        private const val EXTRA_SERVICE_ID = "extra_service_id"

        fun documentVerificationIntent(context: Context, serviceId: String = ""): Intent {
            return Intent(context, DocumentVerificationActivity::class.java).apply {
                putExtra(EXTRA_SERVICE_ID, serviceId)
            }
        }
    }

    private val serviceId: String by lazy {
        intent.getStringExtra(EXTRA_SERVICE_ID) ?: ""
    }

    private lateinit var resultHelper: KycActivityResultHelper

    override fun layoutId(): Int = R.layout.activity_document_verification

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resultHelper = KycActivityResultHelper(this) { docType, isSuccess ->
            onDocumentResult(docType, isSuccess)
        }
        resultHelper.registerLaunchers()

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivHelp.setOnClickListener {
            // TODO: open help screen
        }

        binding.rvDocuments.layoutManager = LinearLayoutManager(this)

        collectState()

        // Fetch requirements from API
        viewModel.fetchRequirements(serviceId)
    }

    private fun collectState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // Shimmer loading state
                    binding.documentShimmer.root.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE

                    // Hide progress section during loading
                    if (state.isLoading) {
                        binding.progressBar.visibility = View.GONE
                        binding.tvDocumentsCount.visibility = View.GONE
                        binding.rvDocuments.visibility = View.GONE
                    }

                    when (val uiState = state.uiState) {
                        is UiState.Success -> {
                            binding.rvDocuments.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.VISIBLE
                            binding.tvDocumentsCount.visibility = View.VISIBLE
                            renderRequirements(uiState.data)
                        }
                        is UiState.Error -> {
                            binding.rvDocuments.visibility = View.GONE
                        }
                        is UiState.Empty -> {
                            binding.rvDocuments.visibility = View.GONE
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    /**
     * Converts the server-driven response into the adapter's list format
     * and updates the UI (progress bar, document count, RecyclerView).
     */
    private fun renderRequirements(response: ServiceRequirementsResponse) {
        val items = mutableListOf<Any>()

        for (section in response.sections) {
            // Add section header
            items.add(section.section)

            // Add visible documents sorted by sequence
            section.documents
                .filter { it.isVisible }
                .sortedBy { it.sequence }
                .forEach { doc ->
                    items.add(
                        DocumentItem(
                            name = doc.label,
                            isRequired = doc.isRequired,
                            status = mapStatus(doc.status),
                            documentType = doc.documentType,
                            isEnabled = doc.isEnabled,
                            isCompleted = doc.isCompleted
                        )
                    )
                }
        }

        val adapter = DocumentVerificationAdapter(items) { documentItem ->
            onDocumentClick(documentItem)
        }
        binding.rvDocuments.adapter = adapter

        // Update progress from server response
        updateProgress(response)
    }

    // ────────────────────────── Click Handling ─────────────────────────

    /**
     * Handles document card click.
     * Maps the API document_type to KycDocType and launches the appropriate screen.
     */
    private fun onDocumentClick(item: DocumentItem) {
        // Don't allow click on disabled documents
        if (!item.isEnabled || item.isCompleted) {
//            Toast.makeText(this, "Complete previous steps first", Toast.LENGTH_SHORT).show()
            return
        }

        val docType = mapDocumentTypeToKycDocType(item.documentType)
        if (docType != null && docType.isImplemented) {
            resultHelper.launchForDocument(docType)
        } else {
            Toast.makeText(this, "${item.name} - Coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Handles the result from a document verification screen.
     */
    private fun onDocumentResult(docType: KycDocType, isSuccess: Boolean) {
        if (isSuccess) {
            Toast.makeText(this, "${docType.displayName} verified successfully", Toast.LENGTH_SHORT).show()
            // Refresh requirements to get updated status from server
            viewModel.fetchRequirements(serviceId)
        } else {
            Toast.makeText(this, "${docType.displayName} - Verification cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // ────────────────────────── Mapping Helpers ────────────────────────

    /**
     * Maps API document_type string to KycDocType enum.
     * This is the single mapping point — no changes needed when API goes live.
     */
    private fun mapDocumentTypeToKycDocType(documentType: String?): KycDocType? {
        return when (documentType?.uppercase()) {
            "PAN" -> KycDocType.PAN_CARD
            "AADHAAR" -> KycDocType.AADHAAR_CARD
            "GST" -> KycDocType.GST_DETAILS
            "MSME" -> KycDocType.MSME_CERTIFICATE
            "BUSINESS_PROOF" -> KycDocType.BUSINESS_PROOF
            "CLIENT_LIST" -> KycDocType.CLIENT_LIST
            "TRUCK_BUSINESS_PROOF" -> KycDocType.TRUCK_BUSINESS_PROOF
            "BANK_ACCOUNT" -> KycDocType.PAYMENT_DETAILS
            "DECLARATION_194C" -> KycDocType.DECLARATION_194C
            "VENDOR_POLICY" -> KycDocType.VENDOR_POLICY
            "ITR" -> KycDocType.ITR
            else -> null
        }
    }

    /**
     * Maps API status string to local DocumentStatus enum.
     */
    private fun mapStatus(apiStatus: String): DocumentStatus {
        return when (apiStatus.uppercase()) {
            "APPROVED", "VERIFIED" -> DocumentStatus.VERIFIED
            "UNDER_REVIEW" -> DocumentStatus.UNDER_REVIEW
            "REJECTED" -> DocumentStatus.REJECTED
            else -> DocumentStatus.NONE // PENDING, or any unknown status
        }
    }

    /**
     * Updates the progress bar and count text from the server-provided progress data.
     * Color logic: VERIFIED → green, UNDER_REVIEW → yellow, else → red
     */
    private fun updateProgress(response: ServiceRequirementsResponse) {
        val progress = response.progress

        binding.progressBar.max = progress.requiredDocuments
        binding.progressBar.progress = progress.completedDocuments
        binding.tvDocumentsCount.text = "${progress.completedDocuments}/${progress.requiredDocuments}"

        val colorRes = when (response.onboardingStatus.uppercase()) {
            "VERIFIED" -> R.color.progress_green
            "UNDER_REVIEW" -> R.color.progress_yellow
            else -> R.color.progress_red
        }
        val color = ContextCompat.getColor(this, colorRes)
        binding.progressBar.progressTintList = ColorStateList.valueOf(color)
    }

    override fun getViewModelClass(): Class<DocumentVerificationViewModel> =
        DocumentVerificationViewModel::class.java

    override fun requireConnection(): Boolean = true
}

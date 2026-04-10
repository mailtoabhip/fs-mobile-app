package com.delhivery.axle.ui.kyc.documentverification

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityDocumentVerificationBinding
import com.delhivery.axle.ui.base.BaseActivity

class DocumentVerificationActivity :
    BaseActivity<ActivityDocumentVerificationBinding, DocumentVerificationViewModel>() {

    companion object {
        fun documentVerificationIntent(context: Context): Intent {
            return Intent(context, DocumentVerificationActivity::class.java)
        }
    }

    override fun layoutId(): Int = R.layout.activity_document_verification

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Back button
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Help button
        binding.ivHelp.setOnClickListener {
            // TODO: open help screen
        }

        setupDocumentList()
    }

    private fun setupDocumentList() {
        val items = buildHardcodedDocumentList()

        val adapter = DocumentVerificationAdapter(items) { documentItem ->
            // TODO: Handle document item click - navigate to upload/detail screen
        }

        binding.rvDocuments.layoutManager = LinearLayoutManager(this)
        binding.rvDocuments.adapter = adapter

        updateProgress(items)
    }

    private fun updateProgress(items: List<Any>) {
        val documents = items.filterIsInstance<DocumentItem>()
        val total = documents.size
        val uploaded = documents.count { it.status != DocumentStatus.NONE }

        // Update progress bar
        binding.progressBar.max = total
        binding.progressBar.progress = uploaded

        // Update count text
        binding.tvDocumentsCount.text = "$uploaded/$total"

        // Color based on completion
        val colorRes = when {
            uploaded == total && documents.all { it.status == DocumentStatus.VERIFIED } ->
                R.color.bid_placed_green
            uploaded == total ->
                R.color.pending_font
            else ->
                R.color.black_title
        }
        val color = ContextCompat.getColor(this, colorRes)
        binding.progressBar.progressTintList = ColorStateList.valueOf(color)
        binding.tvDocumentsCount.setTextColor(color)
    }

    /**
     * Builds the hardcoded list of section headers and document items
     * matching the provided design screenshot.
     */
    private fun buildHardcodedDocumentList(): List<Any> {
        return listOf(
            // Identity Section
            "Identity",
            DocumentItem(
                name = "PAN Card",
                isRequired = true,
                status = DocumentStatus.VERIFIED
            ),
            DocumentItem(
                name = "Aadhaar Card",
                isRequired = true,
                status = DocumentStatus.NONE
            ),

            // Business Details Section
            "Business Details",
            DocumentItem(
                name = "GST Details",
                isRequired = true,
                status = DocumentStatus.UNDER_REVIEW
            ),
            DocumentItem(
                name = "MSME Certificate",
                isRequired = true,
                status = DocumentStatus.NONE
            ),
            DocumentItem(
                name = "Business Proof",
                isRequired = true,
                status = DocumentStatus.REJECTED
            ),
            DocumentItem(
                name = "Client List",
                isRequired = true,
                status = DocumentStatus.NONE
            ),

            // Vehicle Details Section
            "Vehicle Details",
            DocumentItem(
                name = "Truck Business Proof",
                subtitle = "RC/Bilty/LR",
                isRequired = true,
                status = DocumentStatus.NONE
            ),

            // Banking & Payments Section
            "Banking & Payments",
            DocumentItem(
                name = "Payment Details",
                subtitle = "Cancelled Cheque/Bank Passbook",
                isRequired = true,
                status = DocumentStatus.NONE
            ),
            DocumentItem(
                name = "194C Declaration",
                isRequired = false,
                status = DocumentStatus.NONE
            ),

            // Policy & Agreements Section
            "Policy & Agreements",
            DocumentItem(
                name = "Vendor Policy",
                isRequired = true,
                status = DocumentStatus.NONE
            ),

            // Tax Information Section
            "Tax Information",
            DocumentItem(
                name = "ITR",
                isRequired = true,
                status = DocumentStatus.NONE
            )
        )
    }

    override fun getViewModelClass(): Class<DocumentVerificationViewModel> =
        DocumentVerificationViewModel::class.java


    override fun requireConnection(): Boolean = true
}

package com.dfd.delfin.ui.kyc.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dfd.delfin.R
import com.google.android.material.button.MaterialButton

class KycDashboardActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, KycDashboardActivity::class.java))
        }
    }

    private lateinit var resultHelper: KycActivityResultHelper
    private lateinit var adapter: SimpleKycAdapter
    private val documentList = mutableListOf<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Must register launchers before setContentView
        resultHelper = KycActivityResultHelper(this) { docType, isSuccess ->
            onDocumentResult(docType, isSuccess)
        }
        resultHelper.registerLaunchers()
        
        setContentView(R.layout.activity_kyc_dashboard_simple)

        setupToolbar()
        setupRecyclerView()
        setupSubmitButton()
    }
    
    private fun onDocumentResult(docType: KycDocType, isSuccess: Boolean) {
        if (isSuccess) {
            // Document verified successfully
            Toast.makeText(this, "${docType.displayName} verified successfully", Toast.LENGTH_SHORT).show()
            // TODO: Update document status to VERIFIED and refresh list
        } else {
            // Screen closed without verification (back press / arrow back)
            Toast.makeText(this, "${docType.displayName} - Verification cancelled", Toast.LENGTH_SHORT).show()
            // TODO: Keep document status as is (NOT_STARTED or previous state)
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        findViewById<TextView>(R.id.toolbar_title).text = "FTL Loads (GST)"
        findViewById<TextView>(R.id.toolbar_subtitle).text = "Document Verification"

        toolbar.setNavigationOnClickListener { finish() }

        // Set progress
        findViewById<ProgressBar>(R.id.progress_bar).progress = 20
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_documents)
        recyclerView.layoutManager = LinearLayoutManager(this)
        documentList.addAll(getDummyData())
        adapter = SimpleKycAdapter(documentList) { item ->
            onDocumentClick(item)
        }
        recyclerView.adapter = adapter
    }

    private fun onDocumentClick(item: DocItem) {
        if (item.docType.isImplemented) {
            resultHelper.launchForDocument(item.docType)
        } else {
            Toast.makeText(this, "${item.docType.displayName} - Coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSubmitButton() {
        val btnSubmit = findViewById<MaterialButton>(R.id.btn_submit)
        btnSubmit.isEnabled = false
        btnSubmit.alpha = 0.5f
        btnSubmit.setOnClickListener {
            Toast.makeText(this, "Submit clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDummyData(): List<Any> {
        return listOf(
            // Identity
            SectionHeader("Identity"),
            DocItem(KycDocType.PAN_CARD, null, true, DocStatus.VERIFIED),
            DocItem(KycDocType.AADHAAR_CARD, null, true, DocStatus.NOT_STARTED),

            // Business Details
            SectionHeader("Business Details"),
            DocItem(KycDocType.GST_DETAILS, null, true, DocStatus.UNDER_REVIEW),
            DocItem(KycDocType.MSME_CERTIFICATE, null, true, DocStatus.NOT_STARTED),
            DocItem(KycDocType.BUSINESS_PROOF, null, true, DocStatus.REJECTED),
            DocItem(KycDocType.CLIENT_LIST, null, true, DocStatus.NOT_STARTED),

            // Vehicle Details
            SectionHeader("Vehicle Details"),
            DocItem(KycDocType.TRUCK_BUSINESS_PROOF, "RC/Bilty/LR", true, DocStatus.NOT_STARTED),

            // Banking & Payments
            SectionHeader("Banking & Payments"),
            DocItem(KycDocType.PAYMENT_DETAILS, "Cancelled Cheque/Bank Passbook", true, DocStatus.NOT_STARTED),
            DocItem(KycDocType.DECLARATION_194C, null, false, DocStatus.NOT_STARTED),

            // Policy & Agreements
            SectionHeader("Policy & Agreements"),
            DocItem(KycDocType.VENDOR_POLICY, null, true, DocStatus.NOT_STARTED),

            // Tax Information
            SectionHeader("Tax Information"),
            DocItem(KycDocType.ITR, null, true, DocStatus.NOT_STARTED),
            
            // ========== TEST SCREENS ==========
            // Identity Verification (based on PAN type)
            SectionHeader("🧪 Identity Flows (Test)"),
            DocItem(KycDocType.IDENTITY_PERSONAL_PAN_NO_GST, "Personal PAN + No GST → Aadhaar", false, DocStatus.NOT_STARTED),
            DocItem(KycDocType.IDENTITY_BUSINESS_PAN_NO_GST, "Business PAN + No GST → CIN/Udyog/Shop", false, DocStatus.NOT_STARTED),
            DocItem(KycDocType.IDENTITY_HAS_GST, "Has GST → GST Verification", false, DocStatus.NOT_STARTED),
            
            // Address Verification (based on user type)
            SectionHeader("🧪 Address Flows (Test)"),
            DocItem(KycDocType.ADDRESS_GST_USER, "GST User → Auto-filled from GST", false, DocStatus.NOT_STARTED),
            DocItem(KycDocType.ADDRESS_NON_GST_USER, "Non-GST User → Manual Entry", false, DocStatus.NOT_STARTED)
        )
    }
}

// Simple data classes
data class SectionHeader(val title: String)
data class DocItem(
    val docType: KycDocType,
    val subtitle: String?,
    val isRequired: Boolean,
    val status: DocStatus
)

enum class DocStatus(val displayName: String, val colorRes: Int) {
    NOT_STARTED("", R.color.faded_black),
    UNDER_REVIEW("Under review", R.color.orange),
    VERIFIED("Verified", R.color.green),
    REJECTED("Rejected", R.color.error_red)
}

// Simple adapter
class SimpleKycAdapter(
    private val items: List<Any>,
    private val onClick: (DocItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is SectionHeader) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderVH(inflater.inflate(R.layout.item_kyc_section_header_simple, parent, false))
        } else {
            ItemVH(inflater.inflate(R.layout.item_kyc_document_simple, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderVH -> holder.bind(items[position] as SectionHeader)
            is ItemVH -> holder.bind(items[position] as DocItem, onClick)
        }
    }

    override fun getItemCount() = items.size

    class HeaderVH(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.text_section_title)
        fun bind(header: SectionHeader) {
            title.text = header.title
        }
    }

    class ItemVH(view: View) : RecyclerView.ViewHolder(view) {
        private val card: CardView = view.findViewById(R.id.card_document)
        private val name: TextView = view.findViewById(R.id.text_document_name)
        private val subtitle: TextView = view.findViewById(R.id.text_document_subtitle)
        private val status: TextView = view.findViewById(R.id.text_document_status)

        fun bind(item: DocItem, onClick: (DocItem) -> Unit) {
            // Name with asterisk for required
            name.text = if (item.isRequired) "${item.docType.displayName} *" else item.docType.displayName

            // Subtitle
            if (item.subtitle != null) {
                subtitle.text = item.subtitle
                subtitle.visibility = View.VISIBLE
            } else {
                subtitle.visibility = View.GONE
            }

            // Status
            if (item.status != DocStatus.NOT_STARTED) {
                status.text = item.status.displayName
                status.setTextColor(ContextCompat.getColor(itemView.context, item.status.colorRes))
                status.visibility = View.VISIBLE
            } else {
                status.visibility = View.GONE
            }

            // Card background
            val bgRes = when (item.status) {
                DocStatus.VERIFIED -> R.drawable.bg_kyc_document_verified
                DocStatus.REJECTED -> R.drawable.bg_kyc_document_rejected
                DocStatus.UNDER_REVIEW -> R.drawable.bg_kyc_document_review
                else -> R.drawable.bg_kyc_document_default
            }
            card.setBackgroundResource(bgRes)

            itemView.setOnClickListener { onClick(item) }
        }
    }
}

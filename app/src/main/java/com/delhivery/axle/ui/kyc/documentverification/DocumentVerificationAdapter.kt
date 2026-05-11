package com.delhivery.axle.ui.kyc.documentverification

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ItemDocumentVerificationBinding
import androidx.core.graphics.toColorInt

/**
 * Data class representing a document item or a section header.
 */
data class DocumentItem(
    val name: String,
    val subtitle: String? = null,
    val isRequired: Boolean = true,
    val status: DocumentStatus = DocumentStatus.NONE,
    val isSectionHeader: Boolean = false,
    val documentType: String? = null,
    val isEnabled: Boolean = true,
    val isCompleted: Boolean = true,
)

enum class DocumentStatus {
    NONE,
    VERIFIED,
    UNDER_REVIEW,
    REJECTED
}

/**
 * Adapter for the Document Verification screen.
 * Handles both section headers and document card items.
 */
class DocumentVerificationAdapter(
    private val items: List<Any>, // Can be String (section header) or DocumentItem
    private val onItemClick: (DocumentItem) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_DOCUMENT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) VIEW_TYPE_HEADER else VIEW_TYPE_DOCUMENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_doc_verification_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val binding = ItemDocumentVerificationBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            DocumentViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(items[position] as String)
            is DocumentViewHolder -> {
                val item = items[position] as DocumentItem
                holder.bind(item)
                holder.itemView.setOnClickListener { onItemClick(item) }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * ViewHolder for section headers (e.g., "Identity", "Business Details")
     */
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: androidx.appcompat.widget.AppCompatTextView =
            itemView.findViewById(R.id.tvSectionHeader)

        fun bind(title: String) {
            tvHeader.text = title
        }
    }

    /**
     * ViewHolder for document card items
     */
    class DocumentViewHolder(
        private val binding: ItemDocumentVerificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DocumentItem) {
            val context = binding.root.context

            // Set document name with mandatory asterisk
            if (item.isRequired) {
                val spannable = SpannableString("${item.name} *")
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorDelhiveryRed)),
                    item.name.length,
                    spannable.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    item.name.length,
                    spannable.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.tvDocName.text = spannable
            } else {
                binding.tvDocName.text = item.name
            }


            // Status badge vs. arrow
            when (item.status) {
                DocumentStatus.VERIFIED -> {
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.ivArrow.visibility = View.GONE
                    binding.tvStatus.text = "Verified"
                    binding.tvStatus.setTextColor("#1BA86E".toColorInt())
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_status_verified)
                    binding.cardContainer.setBackgroundResource(R.drawable.bg_doc_card_highlighted_verified)
                }
                DocumentStatus.UNDER_REVIEW -> {
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.ivArrow.visibility = View.GONE
                    binding.tvStatus.text = "Under review"
                    binding.tvStatus.setTextColor("#7B6414".toColorInt())
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_status_under_review)
                    binding.cardContainer.setBackgroundResource(R.drawable.bg_doc_card_highlighted_pending)
                }
                DocumentStatus.REJECTED -> {
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.ivArrow.visibility = View.VISIBLE
                    binding.tvStatus.text = "Rejected"
                    binding.tvStatus.setTextColor("#DC143C".toColorInt())
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_status_rejected)
                    binding.cardContainer.setBackgroundResource(R.drawable.bg_doc_card_highlighted_rejected)
                }
                DocumentStatus.NONE -> {
                    binding.tvStatus.visibility = View.GONE
                    binding.ivArrow.visibility = View.VISIBLE
                    binding.cardContainer.setBackgroundResource(R.drawable.bg_doc_card_default)
                }
            }

            // Disabled state — grey tint overlay
            if (!item.isEnabled) {
                binding.cardContainer.alpha = 0.5f
                binding.root.isClickable = false
            } else {
                binding.cardContainer.alpha = 1.0f
                binding.root.isClickable = true
            }
        }
    }
}

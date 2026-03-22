package com.delhivery.axle.ui.invoicereview

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.delhivery.axle.R
import com.delhivery.axle.databinding.DialogInvoiceConfirmationBinding

/**
 * Confirmation dialog for invoice accept/reject actions
 */
class InvoiceConfirmationDialog(
    context: Context,
    private val type: ConfirmationType,
    private val onConfirm: () -> Unit
) : AlertDialog(context) {

    enum class ConfirmationType(val value: String) {
        ACCEPT("accept"),
        REJECT("reject")
    }

    private lateinit var binding: DialogInvoiceConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogInvoiceConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Make dialog background transparent to show rounded corners
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        setupDialog()
        setupClickListeners()
    }

    private fun setupDialog() {
        when (type) {
            ConfirmationType.ACCEPT -> {
                binding.ivIcon.setImageResource(R.drawable.ic_check_circle_green)
                binding.tvTitle.text = "Accept Invoice?"
                binding.tvMessage.text = "Are you sure you want to accept this invoice for payment processing?"
                binding.btnConfirm.text = "Confirm Accept"
                binding.btnConfirm.backgroundTintList = ContextCompat.getColorStateList(context, R.color.black)
                binding.btnConfirm.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            ConfirmationType.REJECT -> {
                binding.ivIcon.setImageResource(R.drawable.ic_close_circle_red)
                binding.tvTitle.text = "Reject Invoice?"
                binding.tvMessage.text = "Please reach out to the centre at +91 800-123-4567 for re-raising of invoice acceptance request."
                binding.btnConfirm.text = "Confirm Reject"
                binding.btnConfirm.backgroundTintList = ContextCompat.getColorStateList(context, R.color.switch_red)
                binding.btnConfirm.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            onConfirm()
            dismiss()
        }
    }
}

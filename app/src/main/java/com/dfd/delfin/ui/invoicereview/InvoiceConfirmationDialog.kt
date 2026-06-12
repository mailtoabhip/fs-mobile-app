package com.dfd.delfin.ui.invoicereview

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.dfd.delfin.R
import com.dfd.delfin.databinding.DialogInvoiceConfirmationBinding
import androidx.core.graphics.drawable.toDrawable

/**
 * Confirmation dialog for invoice accept/reject actions
 */
class InvoiceConfirmationDialog(
    context: Context,
    private val type: ConfirmationType,
    private val centerContactNumber : String?,
    private val onConfirm: () -> Unit
) : AlertDialog(context) {

    private lateinit var binding: DialogInvoiceConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogInvoiceConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val margin = context.resources.getDimensionPixelSize(R.dimen.size_40dp)
        window?.decorView?.setPadding(margin, 0, margin, 0)

        setupDialog()
        setupClickListeners()
    }

    private fun setupDialog() {
        when (type) {
            ConfirmationType.ACCEPT -> {
                binding.ivIcon.setImageResource(R.drawable.circle_check)
                binding.ivIcon.backgroundTintList = ContextCompat.getColorStateList(context, R.color.light_green)
                binding.tvTitle.text = context.getString(R.string.dialog_accept_invoice_title)
                binding.tvMessage.text = context.getString(R.string.dialog_accept_invoice_message)
                binding.btnConfirm.text = context.getString(R.string.dialog_accept_invoice_confirm)
                binding.btnConfirm.backgroundTintList = ContextCompat.getColorStateList(context, R.color.black)
                binding.btnConfirm.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            ConfirmationType.REJECT -> {
                binding.ivIcon.setImageResource(R.drawable.ic_close)
                binding.ivIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.colorRed)
                binding.ivIcon.backgroundTintList = ContextCompat.getColorStateList(context, R.color.light_red)
                binding.tvTitle.text = context.getString(R.string.dialog_reject_invoice_title)
                binding.tvMessage.text = context.getString(R.string.dialog_reject_invoice_message, centerContactNumber?:"")
                binding.btnConfirm.text = context.getString(R.string.dialog_reject_invoice_confirm)
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

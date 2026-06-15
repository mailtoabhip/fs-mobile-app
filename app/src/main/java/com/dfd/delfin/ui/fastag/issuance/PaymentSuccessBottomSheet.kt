package com.dfd.delfin.ui.fastag.issuance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.dfd.delfin.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PaymentSuccessBottomSheet : BottomSheetDialogFragment() {

    private var onAction: (() -> Unit)? = null
    private var isSuccess: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_payment_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        val ivIcon = view.findViewById<ImageView>(R.id.ivSuccessIcon)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val btnAction = view.findViewById<AppCompatButton>(R.id.btnCollectFastags)

        if (isSuccess) {
            Glide.with(this)
                .asGif()
                .load(R.raw.successful_check)
                .into(ivIcon)
            tvTitle.text = "Payment done successfully!"
            tvDescription.text = "Payment has been successfully done."
            btnAction.text = "Collect FASTags"
            btnAction.visibility = View.VISIBLE
        } else {
            Glide.with(this)
                .asGif()
                .load(R.raw.ic_payment_failed)
                .into(ivIcon)
            tvTitle.text = "Payment Failed"
            tvDescription.text = "We're unable to proceed with your payment\n" +
                    "due to a technical issue. Please try again later."
            btnAction.visibility = View.GONE
            isCancelable = true
        }

        btnAction.setOnClickListener {
            dismiss()
            onAction?.invoke()
        }
    }

    companion object {
        const val TAG = "PaymentSuccessBottomSheet"

        fun newInstance(onCollectFastags: () -> Unit): PaymentSuccessBottomSheet {
            return PaymentSuccessBottomSheet().apply {
                this.isSuccess = true
                this.onAction = onCollectFastags
            }
        }

        fun newFailedInstance(onRetry: () -> Unit): PaymentSuccessBottomSheet {
            return PaymentSuccessBottomSheet().apply {
                this.isSuccess = false
                this.onAction = onRetry
            }
        }
    }
}

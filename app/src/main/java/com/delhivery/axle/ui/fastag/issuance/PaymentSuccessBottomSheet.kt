package com.delhivery.axle.ui.fastag.issuance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delhivery.axle.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PaymentSuccessBottomSheet : BottomSheetDialogFragment() {

    private var onCollectFastags: (() -> Unit)? = null

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

        view.findViewById<View>(R.id.btnCollectFastags).setOnClickListener {
            dismiss()
            onCollectFastags?.invoke()
        }
    }

    companion object {
        const val TAG = "PaymentSuccessBottomSheet"

        fun newInstance(onCollectFastags: () -> Unit): PaymentSuccessBottomSheet {
            return PaymentSuccessBottomSheet().apply {
                this.onCollectFastags = onCollectFastags
            }
        }
    }
}

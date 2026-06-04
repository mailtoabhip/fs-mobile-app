package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delhivery.axle.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DocumentVerificationBottomSheet : BottomSheetDialogFragment() {

    private var salesCode: String = ""
    private var orderId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_document_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        salesCode = arguments?.getString(ARG_SALES_CODE) ?: ""
        orderId = arguments?.getString(ARG_ORDER_ID) ?: ""

        view.findViewById<View>(R.id.btnContinueToPay).setOnClickListener {
            dismiss()
            val intent = Intent(requireContext(), PaymentBreakupActivity::class.java).apply {
                putExtra(PaymentBreakupActivity.EXTRA_SALES_CODE, salesCode)
                putExtra(PaymentBreakupActivity.EXTRA_ORDER_ID, orderId)
            }
            startActivity(intent)
        }

        view.findViewById<View>(R.id.btnPayLater).setOnClickListener {
            dismiss()
            val intent = Intent(requireContext(), com.delhivery.axle.ui.home.activity.home.HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }

    companion object {
        const val TAG = "DocumentVerificationBottomSheet"
        private const val ARG_SALES_CODE = "arg_sales_code"
        private const val ARG_ORDER_ID = "arg_order_id"

        fun newInstance(salesCode: String = "", orderId: String = ""): DocumentVerificationBottomSheet {
            return DocumentVerificationBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_SALES_CODE, salesCode)
                    putString(ARG_ORDER_ID, orderId)
                }
            }
        }
    }
}

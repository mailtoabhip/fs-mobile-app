package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delhivery.axle.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DocumentVerificationBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_document_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.ivClose).setOnClickListener {
            dismiss()
        }

        view.findViewById<View>(R.id.btnContinueToPay).setOnClickListener {
            dismiss()
            val intent = Intent(requireContext(), PaymentBreakupActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        const val TAG = "DocumentVerificationBottomSheet"

        fun newInstance(): DocumentVerificationBottomSheet {
            return DocumentVerificationBottomSheet()
        }
    }
}

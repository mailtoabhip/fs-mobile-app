package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.delhivery.axle.R
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.delhivery.axle.utils.extensions.viewModelFactoryExtension
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RequestReceivedBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_request_received, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        view.findViewById<View>(R.id.btnBackToHome).setOnClickListener {
            dismiss()
            val intent = Intent(requireContext(), HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
        val imageView = view.findViewById<ImageView>(R.id.ivCheckIcon)
        Glide.with(this)
            .asGif()
            .load(R.raw.successful_check)
            .into(imageView)
    }

    companion object {
        const val TAG = "RequestReceivedBottomSheet"

        fun newInstance(): RequestReceivedBottomSheet {
            return RequestReceivedBottomSheet()
        }
    }
}

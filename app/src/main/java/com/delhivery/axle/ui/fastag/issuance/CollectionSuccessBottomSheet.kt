package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.delhivery.axle.R
import com.delhivery.axle.ui.home.activity.home.HomeActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CollectionSuccessBottomSheet : BottomSheetDialogFragment() {

    private var onPrimaryAction: (() -> Unit)? = null
    private var isSuccess: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_collection_success, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        val ivIcon = view.findViewById<ImageView>(R.id.ivSuccessIcon)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvSubtitle = view.findViewById<TextView>(R.id.tvSubtitle)
        val btnPrimary = view.findViewById<View>(R.id.btnAssignFastags)
        val btnMaybeLater = view.findViewById<View>(R.id.btnMaybeLater)
        val tvPrimary = btnPrimary as TextView
        val tvMaybeLater = btnMaybeLater as TextView

        if (isSuccess) {
            ivIcon.setImageResource(R.drawable.ic_check_circle_green)
            tvTitle.text = "Collected FASTags successfully"
            tvSubtitle.text = "You can now continue to assign FASTags\nto your vehicles"
            tvPrimary.text = "Assign FASTags"
            tvMaybeLater.text = "Maybe Later"
        } else {
            ivIcon.setImageResource(R.drawable.ic_error_circle_red)
            tvTitle.text = "FASTags collection failed"
            tvSubtitle.text = "Unable to proceed to FASTag collection\nWe're having trouble loading the next step.\nPlease try again."
            tvPrimary.text = "Try again"
            tvMaybeLater.text = "Maybe later"
        }

        btnPrimary.setOnClickListener {
            dismiss()
            onPrimaryAction?.invoke()
        }

        btnMaybeLater.setOnClickListener {
            dismiss()
            val intent = Intent(requireContext(), HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }

    companion object {
        const val TAG = "CollectionSuccessBottomSheet"

        fun newInstance(onAssignFastags: () -> Unit): CollectionSuccessBottomSheet {
            return CollectionSuccessBottomSheet().apply {
                this.isSuccess = true
                this.onPrimaryAction = onAssignFastags
            }
        }

        fun newFailedInstance(onRetry: () -> Unit): CollectionSuccessBottomSheet {
            return CollectionSuccessBottomSheet().apply {
                this.isSuccess = false
                this.onPrimaryAction = onRetry
            }
        }
    }
}

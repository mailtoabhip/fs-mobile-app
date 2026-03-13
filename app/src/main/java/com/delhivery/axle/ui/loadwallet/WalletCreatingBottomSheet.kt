package com.delhivery.axle.ui.loadwallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delhivery.axle.R
import com.delhivery.axle.databinding.DialogWalletCreatingBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WalletCreatingBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: DialogWalletCreatingBinding

    companion object {
        fun newInstance() = WalletCreatingBottomSheet()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogWalletCreatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialog

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val bottomSheetDialog =
                dialog as com.google.android.material.bottomsheet.BottomSheetDialog
            val bottomSheetInternal = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheetInternal?.let { bottomSheet ->
                bottomSheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                bottomSheet.requestLayout()
            }
            val behavior = bottomSheetDialog.behavior
            behavior.isFitToContents = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isDraggable = true
        }
    }
}

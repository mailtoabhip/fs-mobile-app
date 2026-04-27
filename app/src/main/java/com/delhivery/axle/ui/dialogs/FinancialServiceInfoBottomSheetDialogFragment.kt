package com.delhivery.axle.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delhivery.axle.R
import com.delhivery.axle.databinding.DialogFinancialServiceInfoBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FinancialServiceInfoBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: DialogFinancialServiceInfoBottomSheetBinding

    companion object {
        fun newInstance(): FinancialServiceInfoBottomSheetDialogFragment {
            return FinancialServiceInfoBottomSheetDialogFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFinancialServiceInfoBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDialog()
        setupClickListeners()
    }

    override fun getTheme(): Int {
        return R.style.TransparentBottomSheetDialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val bottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheetInternal = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheetInternal?.let { bottomSheet ->
                val layoutParams = bottomSheet.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                bottomSheet.layoutParams = layoutParams
            }
            val behavior = bottomSheetDialog.behavior
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = true
            behavior.peekHeight = 0
            behavior.skipCollapsed = true
        }
    }

    private fun setupDialog() {
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setGravity(Gravity.BOTTOM)
    }

    private fun setupClickListeners() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }
}

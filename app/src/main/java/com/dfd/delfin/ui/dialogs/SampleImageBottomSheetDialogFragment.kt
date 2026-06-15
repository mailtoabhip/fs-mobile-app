package com.dfd.delfin.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.dfd.delfin.R
import com.dfd.delfin.databinding.DialogBottomSampleImageBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SampleImageBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: DialogBottomSampleImageBinding

    private var title: String = ""
    @DrawableRes
    private var sampleImageRes: Int = 0
    private var guidelines: String = ""

    companion object {
        fun newInstance(
            title: String,
            @DrawableRes sampleImageRes: Int,
            guidelines: String
        ): SampleImageBottomSheetDialogFragment {
            val fragment = SampleImageBottomSheetDialogFragment()
            fragment.title = title
            fragment.sampleImageRes = sampleImageRes
            fragment.guidelines = guidelines
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBottomSampleImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDialog()
        setupViews()
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

    private fun setupViews() {
        binding.tvTitle.text = title
        if (sampleImageRes != 0) {
            binding.ivSampleImage.setImageResource(sampleImageRes)
        }
        binding.tvGuidelines.text = guidelines
    }

    private fun setupClickListeners() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }
}

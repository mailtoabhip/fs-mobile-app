package com.dfd.delfin.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import com.dfd.delfin.R
import com.dfd.delfin.databinding.DialogBottomRcUploadFailedBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RcUploadFailedBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: DialogBottomRcUploadFailedBinding

    private var onTryAgain: (() -> Unit)? = null
    private var onMaybeLater: (() -> Unit)? = null
    private var title: String = ""
    private var subtitle: String = ""
    @DrawableRes
    private var iconRes: Int = R.drawable.ic_error_circle_red
    private var showButtons: Boolean = true

    companion object {
        fun newInstance(
            title: String,
            subtitle: String,
            @DrawableRes iconRes: Int = R.drawable.ic_error_circle_red,
            showButtons: Boolean = true,
            onTryAgain: () -> Unit = {},
            onMaybeLater: () -> Unit = {}
        ): RcUploadFailedBottomSheetDialogFragment {
            val fragment = RcUploadFailedBottomSheetDialogFragment()
            fragment.title = title
            fragment.subtitle = subtitle
            fragment.iconRes = iconRes
            fragment.showButtons = showButtons
            fragment.onTryAgain = onTryAgain
            fragment.onMaybeLater = onMaybeLater
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBottomRcUploadFailedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitle.text = title
        binding.tvSubtitle.text = subtitle
        binding.ivErrorIcon.setImageResource(iconRes)

        if (showButtons) {
            binding.btnTryAgain.visibility = View.VISIBLE
            binding.tvMaybeLater.visibility = View.VISIBLE
        } else {
            binding.btnTryAgain.visibility = View.GONE
            binding.tvMaybeLater.visibility = View.GONE
        }

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

    private fun setupClickListeners() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        binding.btnTryAgain.setOnClickListener {
            dismiss()
            onTryAgain?.invoke()
        }

        binding.tvMaybeLater.setOnClickListener {
            dismiss()
            onMaybeLater?.invoke()
        }
    }
}

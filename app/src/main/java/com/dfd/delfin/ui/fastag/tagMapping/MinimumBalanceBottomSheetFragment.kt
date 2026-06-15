package com.dfd.delfin.ui.fastag.tagMapping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dfd.delfin.R
import com.dfd.delfin.databinding.DialogBottomMinimumBalanceBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MinimumBalanceBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: DialogBottomMinimumBalanceBinding? = null
    private val binding get() = _binding!!

    private var onContinueClick: (() -> Unit)? = null
    private var isLoading = false

    companion object {
        fun newInstance(onContinue: () -> Unit): MinimumBalanceBottomSheetFragment {
            val fragment = MinimumBalanceBottomSheetFragment()
            fragment.onContinueClick = onContinue
            return fragment
        }
    }

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBottomMinimumBalanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivClose.visibility = View.GONE

        binding.cbAcknowledge.setOnCheckedChangeListener { _, isChecked ->
            binding.btnContinue.isEnabled = isChecked && !isLoading
            if (isChecked && !isLoading) {
                binding.btnContinue.setBackgroundResource(R.drawable.bg_tag_mapping_continue_enabled)
                binding.btnContinue.setTextColor(resources.getColor(android.R.color.white, null))
            } else {
                binding.btnContinue.setBackgroundResource(R.drawable.bg_tag_mapping_continue_disabled)
                binding.btnContinue.setTextColor(resources.getColor(R.color.color_hint, null))
            }
        }

        binding.btnContinue.setOnClickListener {
            if (!isLoading) {
                onContinueClick?.invoke()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val behavior = (dialog as? BottomSheetDialog)?.behavior ?: return
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isDraggable = false
        isCancelable = false
    }

    fun showLoading() {
        isLoading = true
        _binding?.btnContinue?.isEnabled = false
        _binding?.btnContinue?.text = ""
        _binding?.progressBar?.visibility = View.VISIBLE
    }

    fun hideLoading() {
        isLoading = false
        _binding?.progressBar?.visibility = View.GONE
        _binding?.btnContinue?.text = "Continue"
        _binding?.btnContinue?.isEnabled = _binding?.cbAcknowledge?.isChecked == true
    }

    fun dismissSheet() {
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

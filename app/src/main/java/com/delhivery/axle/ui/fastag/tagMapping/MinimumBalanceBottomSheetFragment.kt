package com.delhivery.axle.ui.fastag.tagMapping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delhivery.axle.R
import com.delhivery.axle.databinding.DialogBottomMinimumBalanceBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MinimumBalanceBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: DialogBottomMinimumBalanceBinding? = null
    private val binding get() = _binding!!

    private var onContinueClick: (() -> Unit)? = null

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

        binding.ivClose.setOnClickListener { dismiss() }

        binding.cbAcknowledge.setOnCheckedChangeListener { _, isChecked ->
            binding.btnContinue.isEnabled = isChecked
            if (isChecked) {
                binding.btnContinue.setBackgroundResource(R.drawable.bg_tag_mapping_continue_enabled)
                binding.btnContinue.setTextColor(resources.getColor(android.R.color.white, null))
            } else {
                binding.btnContinue.setBackgroundResource(R.drawable.bg_tag_mapping_continue_disabled)
                binding.btnContinue.setTextColor(resources.getColor(R.color.color_hint, null))
            }
        }

        binding.btnContinue.setOnClickListener {
            onContinueClick?.invoke()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val behavior = (dialog as? BottomSheetDialog)?.behavior ?: return
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

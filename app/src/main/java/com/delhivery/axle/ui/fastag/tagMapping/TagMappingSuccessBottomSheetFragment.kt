package com.delhivery.axle.ui.fastag.tagMapping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delhivery.axle.R
import com.delhivery.axle.databinding.DialogBottomTagMappingSuccessBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TagMappingSuccessBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: DialogBottomTagMappingSuccessBinding? = null
    private val binding get() = _binding!!

    private var onContinueToKyv: (() -> Unit)? = null

    companion object {
        fun newInstance(onContinue: () -> Unit): TagMappingSuccessBottomSheetFragment {
            val fragment = TagMappingSuccessBottomSheetFragment()
            fragment.onContinueToKyv = onContinue
            return fragment
        }
    }

    override fun getTheme(): Int = R.style.TransparentBottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBottomTagMappingSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        binding.btnContinueToKyv.setOnClickListener {
            dismiss()
                onContinueToKyv?.invoke()
        }
    }

    override fun onStart() {
        super.onStart()
        val behavior = (dialog as? BottomSheetDialog)?.behavior ?: return
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isDraggable = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

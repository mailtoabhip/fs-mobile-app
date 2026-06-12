package com.dfd.delfin.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dfd.delfin.R
import com.dfd.delfin.databinding.DialogLoadsServiceInfoBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LoadsServiceInfoBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: DialogLoadsServiceInfoBottomSheetBinding

    private var title: String = ""
    private var loadType: String = ""
    private var movement: String = ""
    private var paymentMode: String = ""
    private var bestFor: String = ""

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_LOAD_TYPE = "arg_load_type"
        private const val ARG_MOVEMENT = "arg_movement"
        private const val ARG_PAYMENT_MODE = "arg_payment_mode"
        private const val ARG_BEST_FOR = "arg_best_for"

        fun newInstance(
            title: String = "FTL Loads",
            loadType: String = "Full Truck Load (FT)",
            movement: String = "PAN India",
            paymentMode: String = "Advance/ Credit (lease)",
            bestFor: String = "PAN India Trips for client loads"
        ): LoadsServiceInfoBottomSheetDialogFragment {
            return LoadsServiceInfoBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_LOAD_TYPE, loadType)
                    putString(ARG_MOVEMENT, movement)
                    putString(ARG_PAYMENT_MODE, paymentMode)
                    putString(ARG_BEST_FOR, bestFor)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE, "FTL Loads")
            loadType = it.getString(ARG_LOAD_TYPE, "Full Truck Load (FT)")
            movement = it.getString(ARG_MOVEMENT, "PAN India")
            paymentMode = it.getString(ARG_PAYMENT_MODE, "Advance/ Credit (lease)")
            bestFor = it.getString(ARG_BEST_FOR, "PAN India Trips for client loads")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogLoadsServiceInfoBottomSheetBinding.inflate(inflater, container, false)
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

    private fun setupViews() {
        binding.tvTitle.text = title
        binding.tvLoadTypeValue.text = loadType
        binding.tvMovementValue.text = movement
        binding.tvPaymentModeValue.text = paymentMode
        binding.tvBestForValue.text = bestFor
    }

    private fun setupClickListeners() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }
}

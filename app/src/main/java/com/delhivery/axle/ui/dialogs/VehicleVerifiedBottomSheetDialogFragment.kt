package com.delhivery.axle.ui.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.delhivery.axle.R
import com.delhivery.axle.databinding.DialogBottomVehicleVerifiedBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class VehicleVerifiedBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: DialogBottomVehicleVerifiedBinding

    private var vehicleNumber: String = ""
    private var onGoToHomepage: (() -> Unit)? = null

    companion object {
        fun newInstance(
            vehicleNumber: String,
            onGoToHomepage: () -> Unit
        ): VehicleVerifiedBottomSheetDialogFragment {
            val fragment = VehicleVerifiedBottomSheetDialogFragment()
            fragment.vehicleNumber = vehicleNumber
            fragment.onGoToHomepage = onGoToHomepage
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBottomVehicleVerifiedBinding.inflate(inflater, container, false)
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
            behavior.isDraggable = false
            behavior.skipCollapsed = true
        }
    }

    private fun setupDialog() {
        isCancelable = false
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setGravity(Gravity.BOTTOM)
    }

    private fun setupViews() {
        binding.tvSubtitle.text = getString(R.string.vehicle_verified_subtitle, vehicleNumber)

        Glide.with(this)
            .asGif()
            .load(R.raw.success)
            .into(binding.ivSuccessAnimation)
    }

    private fun setupClickListeners() {
        binding.btnGoToHomepage.setOnClickListener {
            dismiss()
            onGoToHomepage?.invoke()
        }
    }
}

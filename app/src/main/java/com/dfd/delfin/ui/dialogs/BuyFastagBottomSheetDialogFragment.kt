package com.dfd.delfin.ui.dialogs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dfd.delfin.R
import com.dfd.delfin.data.CityModel
import com.dfd.delfin.databinding.DialogBottomBuyFastagBinding
import com.dfd.delfin.ui.searchCity.searchCityIntent
import com.dfd.delfin.utils.extensions.getSerializable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BuyFastagBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: DialogBottomBuyFastagBinding
    private var truckCount: Int = 1
    private var maxTruckCount: Int = 255
    private var selectedCity: CityModel? = null
    private var onSubmitCallback: ((CityModel, Int) -> Unit)? = null

    private val cityReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val cityType = intent.getStringExtra("city_type")
            val data = intent.getSerializable("City", CityModel::class.java)
            if (cityType == "origin" && data != null) {
                selectedCity = data
                binding.tvOperatingLocation.text = data.cityName().trim()
                updateSubmitButtonState()
            }
        }
    }

    companion object {
        fun newInstance(
            maxTrucks: Int = 255,
            onSubmit: (CityModel, Int) -> Unit
        ): BuyFastagBottomSheetDialogFragment {
            val fragment = BuyFastagBottomSheetDialogFragment()
            fragment.maxTruckCount = maxTrucks
            fragment.onSubmitCallback = onSubmit
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogBottomBuyFastagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDialog()
        setupClickListeners()
        updateTruckCount()
        updateSubmitButtonState()

        // Register broadcast receiver for city selection
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(cityReceiver, IntentFilter("get_selected_city"))
    }

    override fun getTheme(): Int {
        return R.style.TransparentBottomSheetDialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val bottomSheetDialog = dialog as com.google.android.material.bottomsheet.BottomSheetDialog
            val bottomSheetInternal = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheetInternal?.let { bottomSheet ->
                val displayMetrics = resources.displayMetrics
                val screenHeight = displayMetrics.heightPixels
                val desiredHeight = (screenHeight * 0.75).toInt()
                
                val layoutParams = bottomSheet.layoutParams
                layoutParams.height = desiredHeight
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
        // Close button
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        // Operating location selector
        binding.llOperatingLocation.setOnClickListener {
            requireContext().startActivity(
                searchCityIntent(requireContext(), "origin", true)
            )
        }

        // Decrease truck count
        binding.btnDecrease.setOnClickListener {
            if (truckCount > 1) {
                truckCount--
                updateTruckCount()
            }
        }

        // Increase truck count
        binding.btnIncrease.setOnClickListener {
            if (truckCount < maxTruckCount) {
                truckCount++
                updateTruckCount()
            }
        }

        // Submit button
        binding.btnSubmit.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun updateTruckCount() {
        binding.tvTruckCount.text = truckCount.toString()

        // Update decrease button state
        binding.btnDecrease.isEnabled = truckCount > 1


        binding.btnIncrease.isEnabled = truckCount < maxTruckCount
    }

    private fun updateSubmitButtonState() {
        val isLocationSelected = selectedCity != null
        binding.btnSubmit.isEnabled = isLocationSelected
        
        if (isLocationSelected) {
            binding.btnSubmit.setBackgroundResource(R.drawable.bg_all_round_corner_solid_black)
            binding.btnSubmit.setTextColor(resources.getColor(android.R.color.white, null))
            binding.btnSubmit.alpha = 1.0f
        } else {
            binding.btnSubmit.setBackgroundResource(R.drawable.bg_button_disabled)
            binding.btnSubmit.setTextColor(resources.getColor(R.color.heading_black, null))
            binding.btnSubmit.alpha = 0.5f
        }
    }

    private fun validateAndSubmit() {
        if (selectedCity != null) {
            onSubmitCallback?.invoke(selectedCity!!, truckCount)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(cityReceiver)
        } catch (e: Exception) {
            // Receiver might already be unregistered
        }
    }
}

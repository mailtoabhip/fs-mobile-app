package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.delhivery.axle.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Unified bottom sheet for vehicle details.
 * Handles both hotlisted and eligible (confirm) states.
 */
class VehicleDetailsBottomSheet : BottomSheetDialogFragment() {

    private var onAction: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_vehicle_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments ?: return

        val isHotlisted = args.getBoolean(ARG_IS_HOTLISTED, false)
        val vehicleNumber = args.getString(ARG_VEHICLE_NUMBER, "")
        val col1Label = args.getString(ARG_COL1_LABEL, "")
        val col1Value = args.getString(ARG_COL1_VALUE, "")
        val col2Label = args.getString(ARG_COL2_LABEL, "")
        val col2Value = args.getString(ARG_COL2_VALUE, "")
        val col3Label = args.getString(ARG_COL3_LABEL, "")
        val col3Value = args.getString(ARG_COL3_VALUE, "")
        val buttonText = args.getString(ARG_BUTTON_TEXT, "")
        val colorCode = args.getString(ARG_COLOR_CODE, "GREEN")
        val issuerPhone = args.getString(ARG_ISSUER_PHONE, "")

        // Title
        view.findViewById<TextView>(R.id.tvTitle).text =
            if (isHotlisted) "FASTag Hotlisted" else "Confirm Vehicle Class & Details"

        // Vehicle number
        view.findViewById<TextView>(R.id.tvVehicleNumber).text = vehicleNumber

        // Status badge
        val badge = view.findViewById<TextView>(R.id.tvStatusBadge)
        badge.visibility = if (isHotlisted) View.VISIBLE else View.GONE

        // Color indicator
        view.findViewById<View>(R.id.colorIndicator).setBackgroundColor(mapColor(colorCode))

        // Columns
        view.findViewById<TextView>(R.id.tvCol1Label).text = col1Label
        view.findViewById<TextView>(R.id.tvCol1Value).text = col1Value
        view.findViewById<TextView>(R.id.tvCol2Label).text = col2Label
        view.findViewById<TextView>(R.id.tvCol2Value).text = col2Value
        view.findViewById<TextView>(R.id.tvCol3Label).text = col3Label
        view.findViewById<TextView>(R.id.tvCol3Value).text = col3Value

        // Button
        val btnAction = view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnAction)
        btnAction.text = buttonText

        view.findViewById<View>(R.id.ivClose).setOnClickListener { dismiss() }

        btnAction.setOnClickListener {
            dismiss()
            if (isHotlisted && issuerPhone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$issuerPhone"))
                startActivity(intent)
            } else {
                onAction?.invoke()
            }
        }
    }

    private fun mapColor(colorCode: String): Int {
        return when (colorCode.uppercase()) {
            "RED" -> requireContext().getColor(R.color.class_red)
            "YELLOW" -> requireContext().getColor(R.color.class_yellow)
            "GREEN" -> requireContext().getColor(R.color.class_green)
            "PINK" -> requireContext().getColor(R.color.class_pink)
            "BLUE" -> requireContext().getColor(R.color.class_blue)
            else -> requireContext().getColor(R.color.class_green)
        }
    }

    companion object {
        const val TAG = "VehicleDetailsBottomSheet"
        private const val ARG_IS_HOTLISTED = "arg_is_hotlisted"
        private const val ARG_VEHICLE_NUMBER = "arg_vehicle_number"
        private const val ARG_COL1_LABEL = "arg_col1_label"
        private const val ARG_COL1_VALUE = "arg_col1_value"
        private const val ARG_COL2_LABEL = "arg_col2_label"
        private const val ARG_COL2_VALUE = "arg_col2_value"
        private const val ARG_COL3_LABEL = "arg_col3_label"
        private const val ARG_COL3_VALUE = "arg_col3_value"
        private const val ARG_BUTTON_TEXT = "arg_button_text"
        private const val ARG_COLOR_CODE = "arg_color_code"
        private const val ARG_ISSUER_PHONE = "arg_issuer_phone"

        /**
         * Show for hotlisted vehicle
         */
        fun newHotlisted(
            vehicleNumber: String,
            balance: String,
            provider: String,
            vehicleClass: String,
            colorCode: String,
            issuerPhone: String
        ): VehicleDetailsBottomSheet {
            return VehicleDetailsBottomSheet().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_HOTLISTED, true)
                    putString(ARG_VEHICLE_NUMBER, vehicleNumber)
                    putString(ARG_COL1_LABEL, "Balance")
                    putString(ARG_COL1_VALUE, "₹$balance")
                    putString(ARG_COL2_LABEL, "Provider")
                    putString(ARG_COL2_VALUE, provider)
                    putString(ARG_COL3_LABEL, "Vehicle Class")
                    putString(ARG_COL3_VALUE, vehicleClass)
                    putString(ARG_BUTTON_TEXT, "Call Issuer")
                    putString(ARG_COLOR_CODE, colorCode)
                    putString(ARG_ISSUER_PHONE, issuerPhone)
                }
            }
        }

        /**
         * Show for eligible vehicle (confirm class)
         */
        fun newConfirm(
            vehicleNumber: String,
            vehicleClass: String,
            tagColor: String,
            vehicleType: String,
            colorCode: String,
            onConfirm: () -> Unit
        ): VehicleDetailsBottomSheet {
            return VehicleDetailsBottomSheet().apply {
                this.onAction = onConfirm
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_HOTLISTED, false)
                    putString(ARG_VEHICLE_NUMBER, vehicleNumber)
                    putString(ARG_COL1_LABEL, "Vehicle Class")
                    putString(ARG_COL1_VALUE, vehicleClass)
                    putString(ARG_COL2_LABEL, "Tag Color")
                    putString(ARG_COL2_VALUE, tagColor)
                    putString(ARG_COL3_LABEL, "Vehicle Type")
                    putString(ARG_COL3_VALUE, vehicleType)
                    putString(ARG_BUTTON_TEXT, "Confirm")
                    putString(ARG_COLOR_CODE, colorCode)
                }
            }
        }
    }
}

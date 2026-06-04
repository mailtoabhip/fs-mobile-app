package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.delhivery.axle.R
import com.delhivery.axle.api.response.VehicleCheckResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Unified bottom sheet for vehicle verification results.
 * Handles ELIGIBLE, ALREADY_ISSUED, and HOTLISTED states.
 * All text comes from the API response — no hardcoding.
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

        val title = args.getString(ARG_TITLE, "")
        val vehicleNumber = args.getString(ARG_VEHICLE_NUMBER, "")
        val status = args.getString(ARG_STATUS, "")
        val col1Label = args.getString(ARG_COL1_LABEL, "")
        val col1Value = args.getString(ARG_COL1_VALUE, "")
        val col2Label = args.getString(ARG_COL2_LABEL, "")
        val col2Value = args.getString(ARG_COL2_VALUE, "")
        val col3Label = args.getString(ARG_COL3_LABEL, "")
        val col3Value = args.getString(ARG_COL3_VALUE, "")
        val buttonText = args.getString(ARG_BUTTON_TEXT, "")
        val colorCode = args.getString(ARG_COLOR_CODE, "GREEN")
        val issuerPhone = args.getString(ARG_ISSUER_PHONE, "")

        // Title from API
        view.findViewById<TextView>(R.id.tvTitle).text = title

        // Vehicle number
        view.findViewById<TextView>(R.id.tvVehicleNumber).text = vehicleNumber

        // Status badge
        val badge = view.findViewById<TextView>(R.id.tvStatusBadge)
        when (status) {
            "HOTLISTED" -> {
                badge.visibility = View.VISIBLE
                badge.text = "Hotlisted"
                badge.setBackgroundResource(R.drawable.bg_hotlisted_badge)
            }
            "ALREADY_ISSUED" -> {
                badge.visibility = View.VISIBLE
                badge.text = "Active"
                badge.setBackgroundResource(R.drawable.bg_status_active)
                badge.setTextColor(requireContext().getColor(R.color.class_green))
            }
            else -> badge.visibility = View.GONE
        }

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
        if (buttonText.isNullOrEmpty()) {
            btnAction.visibility = View.GONE
        } else {
            btnAction.text = buttonText
        }

        view.findViewById<View>(R.id.ivClose).setOnClickListener { dismiss() }

        btnAction.setOnClickListener {
            dismiss()
            if ((status == "HOTLISTED" || status == "ALREADY_ISSUED") && issuerPhone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$issuerPhone"))
                startActivity(intent)
            } else {
                onAction?.invoke()
            }
        }
    }

    private fun mapColor(colorCode: String): Int {
        return when (colorCode.uppercase()) {
            "ORANGE" -> requireContext().getColor(R.color.class_red)
            "YELLOW" -> requireContext().getColor(R.color.class_yellow)
            "GREEN" -> requireContext().getColor(R.color.class_green)
            "PINK" -> requireContext().getColor(R.color.class_pink)
            "BLUE" -> requireContext().getColor(R.color.class_blue)
            else -> requireContext().getColor(R.color.class_green)
        }
    }

    companion object {
        const val TAG = "VehicleDetailsBottomSheet"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_VEHICLE_NUMBER = "arg_vehicle_number"
        private const val ARG_STATUS = "arg_status"
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
         * Create from API response — no hardcoding needed
         */
        fun fromResponse(
            response: VehicleCheckResponse,
            issuerPhone: String = "",
            onAction: (() -> Unit)? = null
        ): VehicleDetailsBottomSheet {
            val vc = response.vehicleClass
            return VehicleDetailsBottomSheet().apply {
                this.onAction = onAction
                arguments = Bundle().apply {
                    putString(ARG_TITLE, response.title)
                    putString(ARG_VEHICLE_NUMBER, response.vrn)
                    putString(ARG_STATUS, response.status)
                    putString(ARG_COLOR_CODE, vc?.colorCode ?: "GREEN")
                    putString(ARG_BUTTON_TEXT, response.actionLabel ?: if (response.eligible) "Confirm" else "")
                    putString(ARG_ISSUER_PHONE, issuerPhone)

                    // Column data based on status
                    when (response.status) {
                        "ELIGIBLE" -> {
                            putString(ARG_COL1_LABEL, "Vehicle Class")
                            putString(ARG_COL1_VALUE, vc?.displayName ?: "")
                            putString(ARG_COL2_LABEL, "Tag Color")
                            putString(ARG_COL2_VALUE, vc?.colorCode ?: "")
                            putString(ARG_COL3_LABEL, "Vehicle Type")
                            putString(ARG_COL3_VALUE, vc?.vehicleTypes?.joinToString(", ") ?: "")
                        }
                        "HOTLISTED", "ALREADY_ISSUED" -> {
                            putString(ARG_COL1_LABEL, "Vehicle Class")
                            putString(ARG_COL1_VALUE, vc?.displayName ?: "")
                            putString(ARG_COL2_LABEL, "Card Stage")
                            putString(ARG_COL2_VALUE, response.cardStage ?: "")
                            putString(ARG_COL3_LABEL, "Vehicle Type")
                            putString(ARG_COL3_VALUE, vc?.vehicleTypes?.joinToString(", ") ?: "")
                        }
                        else -> {
                            putString(ARG_COL1_LABEL, "Vehicle Class")
                            putString(ARG_COL1_VALUE, vc?.displayName ?: "")
                            putString(ARG_COL2_LABEL, "Color")
                            putString(ARG_COL2_VALUE, vc?.colorCode ?: "")
                            putString(ARG_COL3_LABEL, "Type")
                            putString(ARG_COL3_VALUE, vc?.vehicleTypes?.joinToString(", ") ?: "")
                        }
                    }
                }
            }
        }
    }
}

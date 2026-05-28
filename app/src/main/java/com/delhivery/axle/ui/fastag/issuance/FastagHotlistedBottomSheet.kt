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

class FastagHotlistedBottomSheet : BottomSheetDialogFragment() {

    private var vehicleNumber: String = ""
    private var issuerPhone: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_fastag_hotlisted, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            vehicleNumber = it.getString(ARG_VEHICLE_NUMBER, "")
            issuerPhone = it.getString(ARG_ISSUER_PHONE, "")
        }

        view.findViewById<TextView>(R.id.tvDescription).text =
            "Existing FASTag for \"$vehicleNumber\" is hotlisted.\nContact your FASTag issuer"

        view.findViewById<View>(R.id.ivClose).setOnClickListener {
            dismiss()
        }

        view.findViewById<View>(R.id.btnCallIssuer).setOnClickListener {
            if (issuerPhone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$issuerPhone"))
                startActivity(intent)
            }
            dismiss()
        }
    }

    companion object {
        const val TAG = "FastagHotlistedBottomSheet"
        private const val ARG_VEHICLE_NUMBER = "arg_vehicle_number"
        private const val ARG_ISSUER_PHONE = "arg_issuer_phone"

        fun newInstance(
            vehicleNumber: String,
            issuerPhone: String = ""
        ): FastagHotlistedBottomSheet {
            return FastagHotlistedBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_VEHICLE_NUMBER, vehicleNumber)
                    putString(ARG_ISSUER_PHONE, issuerPhone)
                }
            }
        }
    }
}

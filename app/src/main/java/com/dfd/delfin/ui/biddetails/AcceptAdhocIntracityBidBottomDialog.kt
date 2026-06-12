package com.dfd.delfin.ui.biddetails

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.Observer
import com.dfd.delfin.R
import com.dfd.delfin.data.home.bids.HomeBidsRequestItemData
import com.dfd.delfin.databinding.DialogBottomAcceptIntracityAdhocBidBinding
import com.dfd.delfin.ui.base.BaseViewModel
import com.dfd.delfin.ui.searchload.fragments.searchresults.SearchResultsFragment
import com.dfd.delfin.ui.searchload.fragments.searchresults.SearchResultsViewModel
import com.dfd.delfin.utils.AnalyticsUtil
import com.dfd.delfin.utils.EVENT_LOAD_INTRACITY_DRIVER_NAME
import com.dfd.delfin.utils.EVENT_LOAD_INTRACITY_DRIVER_NUMBER
import com.dfd.delfin.utils.EVENT_LOAD_INTRACITY_SUBMIT
import com.dfd.delfin.utils.EVENT_LOAD_INTRACITY_VEHICLE_NUMBER
import com.dfd.delfin.utils.PROPERTY_DRIVER_NAME
import com.dfd.delfin.utils.PROPERTY_DRIVER_NUMBER
import com.dfd.delfin.utils.PROPERTY_VEHICLE_NUMBER
import com.dfd.delfin.utils.UiUtils
import com.dfd.delfin.utils.extensions.isNotNullOrEmpty
import com.dfd.delfin.utils.prefs.UserPrefs
import java.util.regex.Pattern
import javax.inject.Inject


class AcceptAdhocIntracityBidBottomDialog @Inject constructor(
    context: Context,
    private val position: Int,
    private val transaction: HomeBidsRequestItemData,
    private val dialogInterface: AcceptAdhocIntracityBidBottomDialogInterface,
    private val analyticsUtil: AnalyticsUtil,
    private var userPrefs: UserPrefs,
    private var viewModel: BaseViewModel,
    private var uiUtils: UiUtils
) : AlertDialog(context) {

    /* dialog binding */
    private var isValidVehicleNumber = false
    private var isValidDriverNumber = false
    private var isValidDriverName = false
    private lateinit var binding: DialogBottomAcceptIntracityAdhocBidBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogBottomAcceptIntracityAdhocBidBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.request = transaction
        binding.dialog = true
        binding.layoutTransaction.request = transaction
        binding.layoutTransaction.dialog = true
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.attributes.windowAnimations = R.style.DialogAnimation
        window!!.setGravity(Gravity.BOTTOM)
        setCanceledOnTouchOutside(false)

        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        binding.progress.visibility = View.GONE
        binding.progressMsg.visibility = View.GONE
        //navigation listner
        setNavigationVisibleAndClickListner()
        //
        binding.close.setOnClickListener {
            dismiss()
        }
        binding.btnSubmit.setOnClickListener{
            submit()
        }
        val fragInstance =SearchResultsFragment._instance

        if(viewModel is SearchResultsViewModel){
            (viewModel as SearchResultsViewModel).acceptBidLiveData.observe(fragInstance, Observer {
                if(it!=null){
                    disableClicks(true)
                    enableSubmit()
                    binding.progress.visibility = View.GONE
                    binding.progressMsg.visibility = View.GONE
                    dismiss()


                }else{
                    disableClicks(true)
                    enableSubmit()
                    binding.progress.visibility = View.GONE
                    binding.progressMsg.visibility = View.GONE


                }
            })
        }

        binding.editTextVehicleNumber?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s != null && s.isNotEmpty() && s.isNotBlank()) {
                    val input = s.trim()
                        .toString()
                        if(validateTruckNumber(input)){
                            isValidVehicleNumber = true
                            binding.vehicleNumberError.visibility = View.GONE
                        } else {
                            binding.vehicleNumberError.visibility = View.VISIBLE
                            binding.vehicleNumberError.text ="Please enter valid vehicle Number"
                            isValidVehicleNumber = false

                        }
                        enableSubmit()

                }else{
                        isValidVehicleNumber = false
                        enableSubmit()

                }
            }
        })

        binding.editTextDriverName?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s != null && s.isNotEmpty() && s.isNotBlank()) {
                    val input = s.trim()
                        .toString()
                        if(input.length>=2){
                            isValidDriverName = true
                            binding.driverNameError.visibility = View.GONE
                        } else {
                            binding.driverNameError.visibility = View.VISIBLE
                            binding.driverNameError.text ="Please enter valid driver name"
                            isValidDriverName = false

                        }
                        enableSubmit()

                }else{

                        isValidDriverName = false
                        enableSubmit()

                }
            }
        })

        binding.editTextDriverNumber?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s != null && s.isNotEmpty() && s.isNotBlank()) {
                    val input = s.trim()
                        .toString()
                        if(input.length==10){
                            isValidDriverNumber = true
                            binding.driverNumberError.visibility = View.GONE
                        } else {
                            binding.driverNumberError.visibility = View.VISIBLE
                            binding.driverNumberError.text ="Please enter a valid driver number"
                            isValidDriverNumber = false

                        }
                        enableSubmit()

                }else{
                    isValidDriverNumber = false
                    enableSubmit()

                }
            }
        })

    }

    fun setNavigationVisibleAndClickListner(){
        if(transaction.pickupLocationCoordinates != null
            && transaction.pickupLocationCoordinates.lat.isNotNullOrEmpty()
            && transaction.pickupLocationCoordinates.lon.isNotNullOrEmpty()){
            binding.layoutTransaction.navigate.visibility = View.VISIBLE
            binding.layoutTransaction.navigate.setOnClickListener{
                openGoogleMapsWithCoordinates()
            }
        }
        
        // Set phone number click listener only if phone number exists
        if (transaction.demandType == "intracity_ops" && !transaction.requestCreatedByPhone.isNullOrEmpty()) {
            binding.layoutTransaction.contactPhoneNumber.setOnClickListener {
                dialPhoneNumber(transaction.requestCreatedByPhone!!)
            }
        }
    }
    
    private fun dialPhoneNumber(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AcceptIntracityDialog", "Error opening dialer: ${e.message}")
        }
    }

    private fun openGoogleMapsWithCoordinates() {
        try {
            val coordinates = transaction.pickupLocationCoordinates
            if (coordinates != null && coordinates.lat != null && coordinates.lon != null) {
                // Create Google Maps intent with coordinates and location name
                val gmmIntentUri = Uri.parse("geo:0,0?q=${coordinates.lat},${coordinates.lon}(${transaction.origin})")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                
                // Check if Google Maps app is available
                if (mapIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapIntent)
                } else {
                    // Fallback to web browser if Google Maps app is not installed
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${coordinates.lat},${coordinates.lon}"))
                    context.startActivity(webIntent)
                }
            } else {
                Toast.makeText(context, "Location information not available", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("NavigationError", "Error opening Google Maps: ${e.message}")
            Toast.makeText(context, "Unable to open map", Toast.LENGTH_SHORT).show()
        }
    }

    private fun  enableSubmit(){
       if(isValidDriverName&&isValidVehicleNumber&&isValidDriverNumber){
           enableSubmitButton()
       }else{
           disableSubmitButton()
       }
    }

    private fun disableSubmitButton(){
        binding.btnSubmit.isEnabled = false
    }
    private fun enableSubmitButton(){
        binding.btnSubmit.isEnabled = true
    }
    private fun validateTruckNumber(number: String): Boolean{
        val pattern = Pattern.compile(
            "^[a-zA-Z]{2}(((0?[1-9]{1}|[1-9]{1}[0-9]{1})[a-zA-Z]{1,3})|(0[1-9]{1}|[1-9]{1}[0-9]{1}))[0-9]{4}$|^[a-zA-Z]{3}[0-9]{4}$"
        )
        return pattern.matcher(number).matches()
    }

    private fun submit() {
        try {
            disableClicks(false)
//            window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            binding.btnSubmit.isEnabled = false
            binding.progress.visibility = View.VISIBLE
            binding.progressMsg.visibility = View.VISIBLE
            analyticsUtil.moEngageTrackEvent(EVENT_LOAD_INTRACITY_DRIVER_NAME, mutableListOf(PROPERTY_DRIVER_NAME),
                mutableListOf(binding.editTextDriverName.text.toString()))
            analyticsUtil.moEngageTrackEvent(EVENT_LOAD_INTRACITY_DRIVER_NUMBER, mutableListOf(PROPERTY_DRIVER_NUMBER),
                mutableListOf(binding.editTextDriverNumber.text.toString()))
            analyticsUtil.moEngageTrackEvent(EVENT_LOAD_INTRACITY_VEHICLE_NUMBER, mutableListOf(PROPERTY_VEHICLE_NUMBER),
                mutableListOf(binding.editTextVehicleNumber.text.toString()))
            analyticsUtil.moEngageTrackEvent(EVENT_LOAD_INTRACITY_SUBMIT, mutableListOf(PROPERTY_DRIVER_NAME, PROPERTY_DRIVER_NUMBER, PROPERTY_VEHICLE_NUMBER),
                mutableListOf(binding.editTextDriverName.text.toString(),binding.editTextDriverNumber.text.toString(), binding.editTextVehicleNumber.text.toString()))

            dialogInterface.acceptBid(
                    position,
                    transaction.key(), userPrefs.userId(),userPrefs.userName,transaction.targetPrice!!.toInt(),
                    transaction.biddingType
                        ?: "FTL", binding.editTextVehicleNumber.text.toString(),binding.editTextDriverNumber.text.toString(),binding.editTextDriverName.text.toString())

           //dismiss()

        } catch (e: IllegalArgumentException) {
            Log.e("AcceptAdhocBid", e.toString())
        }
    }

//    override fun dismissDialog(alertDialog:AcceptAdhocIntracityBidBottomDialog) {
//        alertDialog.dismiss()
//    }

    fun disableClicks(enabled:Boolean){
        binding.close.isEnabled = enabled
        binding.editTextDriverName.isEnabled = enabled
        binding.editTextDriverNumber.isEnabled = enabled
        binding.editTextVehicleNumber.isEnabled = enabled
    }
}

interface AcceptAdhocIntracityBidBottomDialogInterface {

    /**
     * Create bid
     */
    fun acceptBid(
        position: Int,
        transactionId: String,
        supplierId: String,
        supplierName: String,
        bidAmount: Int,
        commercialType: String,
        vehicleNumber: String,
        driverPhone:String,
        driverName: String
    )


}

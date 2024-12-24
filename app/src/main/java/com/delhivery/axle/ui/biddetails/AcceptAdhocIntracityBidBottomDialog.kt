package com.delhivery.axle.ui.biddetails

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.Observer
import com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread
import com.delhivery.axle.R
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.DialogBottomAcceptIntracityAdhocBidBinding
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsFragment
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsFragment.Companion._instance
import com.delhivery.axle.ui.home.fragments.loads.HomeLoadsViewModel
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsFragment
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsViewModel
import com.delhivery.axle.utils.AnalyticsUtil
import com.delhivery.axle.utils.EVENT_LOAD_INTRACITY_DRIVER_NAME
import com.delhivery.axle.utils.EVENT_LOAD_INTRACITY_DRIVER_NUMBER
import com.delhivery.axle.utils.EVENT_LOAD_INTRACITY_SUBMIT
import com.delhivery.axle.utils.EVENT_LOAD_INTRACITY_VEHICLE_NUMBER
import com.delhivery.axle.utils.PROPERTY_DRIVER_NAME
import com.delhivery.axle.utils.PROPERTY_DRIVER_NUMBER
import com.delhivery.axle.utils.PROPERTY_VEHICLE_NUMBER
import com.delhivery.axle.utils.UiUtils
import com.delhivery.axle.utils.prefs.UserPrefs
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
    private var homeFragInstance: HomeLoadsFragment?,
    private val searchFragInstance: SearchResultsFragment?,
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
        binding.layoutTransaction.request = transaction
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.attributes.windowAnimations = R.style.DialogAnimation
        window!!.setGravity(Gravity.BOTTOM)
        setCanceledOnTouchOutside(false)
        binding.progress.visibility = View.GONE
        binding.progressMsg.visibility = View.GONE
        binding.close.setOnClickListener {
            dismiss()
        }
        binding.btnSubmit.setOnClickListener{
            submit()
        }
        val fragInstance = if(homeFragInstance!=null){
            _instance
        }else SearchResultsFragment._instance

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
        }else{
            (viewModel as HomeLoadsViewModel).acceptBidLiveData.observe(fragInstance, Observer {
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

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
import androidx.core.content.ContextCompat
import com.delhivery.axle.R
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.DialogBottomAcceptIntracityAdhocBidBinding
import com.delhivery.axle.utils.AnalyticsUtil
import com.delhivery.axle.utils.prefs.UserPrefs
import java.util.regex.Pattern
import javax.inject.Inject

class AcceptAdhocIntracityBidBottomDialog @Inject constructor(
context: Context,
private val position:Int,
private val transaction: HomeBidsRequestItemData,
private val dialogInterface: AcceptAdhocIntracityBidBottomDialogInterface,
private val analyticsUtil: AnalyticsUtil,
private var userPrefs: UserPrefs,
) : AlertDialog(context) {

    /* dialog binding */
    private var isValidVehicleNumber = false
    private var isValidDriverNumber = false
    private var isValidDriverName = false
    private lateinit var binding: DialogBottomAcceptIntracityAdhocBidBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )
        binding = DialogBottomAcceptIntracityAdhocBidBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.request = transaction
        binding.layoutTransaction.request = transaction
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.attributes.windowAnimations = R.style.DialogAnimation
        window!!.setGravity(Gravity.BOTTOM)

        binding.close.setOnClickListener {
            dismiss()
        }

        binding.btnSubmit.setOnClickListener{
           submit()
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
                            binding.driverNumberError.text ="Please enter valid driver number"
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
                dialogInterface.acceptBid(
                    position,
                    transaction.key(), userPrefs.userId(),userPrefs.userName,transaction.targetPrice!!.toInt(),
                    transaction.biddingType
                        ?: "FTL", binding.editTextVehicleNumber.text.toString(),binding.editTextDriverName.text.toString(),binding.editTextDriverNumber.text.toString())

            dismiss()

        } catch (e: IllegalArgumentException) {
            Log.e("AcceptAdhocBid", e.toString())
        }
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

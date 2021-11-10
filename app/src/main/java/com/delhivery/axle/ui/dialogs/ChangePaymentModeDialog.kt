package com.delhivery.axle.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Toast
import com.delhivery.axle.R
import com.delhivery.axle.api.request.OMCRequest
import com.delhivery.axle.data.home.trips.FuelUserSpinnerOptions
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.databinding.DialogChangePaymentBinding
import com.delhivery.axle.databinding.DialogConfirmBidBinding
import com.delhivery.axle.ui.ledger.LedgerSpinnerAdapter
import com.delhivery.axle.ui.team.teamMembersIntent
import com.delhivery.axle.ui.tripdetails.FuelUserSpinnerAdapter
import com.delhivery.axle.utils.UiUtils
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.disposables.ArrayCompositeDisposable
import javax.inject.Inject

class ChangePaymentModeDialog @Inject constructor(
        context: Context,
        private val dialogInterface: ChangePaymentModeInterface,
        private val data: HomeTripsItemData,
        private val fuelUserSpinnerOptionsList: List<FuelUserSpinnerOptions>,
        private val userPrefs: UserPrefs,
        private val uiUtils: UiUtils,
        private val position: Int =0
) :AlertDialog(context) {

    private lateinit var binding: DialogChangePaymentBinding
    var advanceAmt = 0
    var fuelAmt = 0
    var userNumber = ""

    private val fuelUserSpinnerAdapter: FuelUserSpinnerAdapter by lazy { FuelUserSpinnerAdapter() }
    private  val fuelUserSpinnerOptions : MutableList<FuelUserSpinnerOptions> = mutableListOf<FuelUserSpinnerOptions>(
            FuelUserSpinnerOptions("Select Diesel Card No.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )

        binding = DialogChangePaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fuelUserSpinnerOptions.addAll(fuelUserSpinnerOptionsList)
        if(data.driverDetails?.driverPhoneNo != null){
            fuelUserSpinnerOptions.add(FuelUserSpinnerOptions(data.driverDetails.driverPhoneNo, "(Driver)"))
        }
        fuelUserSpinnerOptions.add(FuelUserSpinnerOptions("Different Number"))


        binding.apply {
            dialogTextVehicleNo.text = data.vehicleDetails.vehicleNo
            dialogTextDriverNo.text = data.formattedDriverDetails()
            amtAdvancePending.text = String.format(context.getString(R.string.label_with_sign_value),data.payment!!.paymentAmount!!.toInt())

            advanceAmt = (data.payment?.paymentAmount ?: 0.0).toInt()
            bankAcNumberText.text = "Bank A/c: " +userPrefs.accNumber
            advancePendingBankAmt.text = data.tripPayment()
        }

        if(data.payment != null && data.payment!!.fuelPayout != 0.0){
            binding.dieselPayoutIdentifier.visibility = View.GONE
            binding.layoutDieselAddAmt.visibility = View.VISIBLE
            binding.layoutAddPerson.visibility = View.VISIBLE
            binding.advancePendingDieselAmt.setText(data.payment!!.fuelPayout!!.toInt().toString())
        }

        binding.dieselPayoutIdentifier.setOnClickListener{
            binding.dieselPayoutIdentifier.visibility = View.GONE
            binding.layoutDieselAddAmt.visibility = View.VISIBLE

        }

        binding.selectMemberSpinner.apply {
            adapter = fuelUserSpinnerAdapter
            fuelUserSpinnerAdapter.setItems(fuelUserSpinnerOptions)
            setSelection(0)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>) = Unit
                override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View,
                        position: Int,
                        id: Long
                ) {
                    val item = parent.getItemAtPosition(position) as FuelUserSpinnerOptions
                    if(item.userName == "Different Number"){
                        if(userPrefs.isParent) {
                            context.startActivity(teamMembersIntent(context, true))
                        }
                        else{
                            Toast.makeText(context, R.string.msg_ask_admin , Toast.LENGTH_SHORT).show()
                        }
                        dismiss()

                    }
                    else if (item.userName == "Select Diesel Card No."){
                        userNumber =""
                        binding.layoutDieselCompany.visibility= View.GONE
                    }
                    else{
                        userNumber = item.userName
                        binding.layoutDieselCompany.visibility = View.VISIBLE
                    }
                }
            }

        }

        binding.advancePendingDieselAmt.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if(s!=null && s.toString()!=""){
                    val dieselAmt = s.toString().toInt()
                    if(dieselAmt > 0){
                        if((advanceAmt - dieselAmt) >= 0) {
                            binding.advancePendingBankAmt.text = String.format(context.getString(R.string.label_with_sign_value),(advanceAmt - dieselAmt))
                            binding.layoutAddPerson.visibility = View.VISIBLE
                        }
                        else{
                            binding.layoutAddPerson.visibility = View.GONE
                            binding.layoutDieselCompany.visibility = View.GONE
                            binding.selectMemberSpinner.setSelection(0)
                            userNumber = ""
                            binding.advancePendingBankAmt.text = String.format(context.getString(R.string.label_with_sign_value), advanceAmt)
                            binding.advancePendingDieselAmt.setText("0")

                            binding.advancePendingDieselAmt.setError("Can't be greater than Advance Pending Amount")

                        }

                    }
                    else if (dieselAmt == 0 ){
                        binding.layoutAddPerson.visibility = View.GONE
                        binding.layoutDieselCompany.visibility = View.GONE
                        binding.selectMemberSpinner.setSelection(0)
                        userNumber = ""
                    }

                }
                else{
                    binding.layoutAddPerson.visibility = View.GONE
                    binding.layoutDieselCompany.visibility = View.GONE
                    binding.selectMemberSpinner.setSelection(0)
                    binding.advancePendingBankAmt.text = String.format(context.getString(R.string.label_with_sign_value), advanceAmt)
                    userNumber = ""
                }
            }

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
            ) = Unit
        }
        )
        binding.doneButton.setOnClickListener{
            submit()
        }


    }

    private fun submit() {
        fuelAmt = if(binding.advancePendingDieselAmt.text.toString() != "") {
            binding.advancePendingDieselAmt.text.toString().toInt()
        }
        else{ 0 }

        if((fuelAmt > 0 && userNumber != "") || fuelAmt ==0 ){
            val omcRequest= OMCRequest(userNumber, "reliance", data.transactionId)
            dialogInterface.done(data.transactionId, omcRequest, "reliance", userNumber, fuelAmt.toString(),position)
            uiUtils.showProgress()
            dismiss()
        }
        else{
            Toast.makeText(context," Select Diesel Card Number",Toast.LENGTH_SHORT).show()
        }

    }
}
interface ChangePaymentModeInterface{
    fun done(
        transactionId: String,
        omcRequest : OMCRequest,
        omcType: String,
        fuelNumber: String,
        fuelAmt: String,
        position: Int
    )
}
package com.delhivery.axle.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Toast
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
        private val compositeDisposable: CompositeDisposable,
        private val dialogInterface: ChangePaymentModeInterface,
        private val data: HomeTripsItemData,
        private val uiUtils: UiUtils,
        private val userPrefs: UserPrefs
) :AlertDialog(context) {

    private lateinit var binding: DialogChangePaymentBinding
    var advanceAmt = 0

    private val fuelUserSpinnerAdapter: FuelUserSpinnerAdapter by lazy { FuelUserSpinnerAdapter() }
    private  val fuelUserSpinnerOptions : List<FuelUserSpinnerOptions> = mutableListOf<FuelUserSpinnerOptions>(
            FuelUserSpinnerOptions("Select Diesel Card No."),
            FuelUserSpinnerOptions("+91742704082", "(Admin)"),
            FuelUserSpinnerOptions("Different Number")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )

        binding = DialogChangePaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            dialogTextVehicleNo.text = data.vehicleDetails.vehicleNo
            dialogTextDriverNo.text = data.formattedDriverDetails()
            amtAdvancePending.text =  data.tripPayment()

            advanceAmt = (data.payment?.paymentAmount ?: 0.0).toInt()
            bankAcNumberText.text = "Bank A/c: " +userPrefs.accNumber
            advancePendingBankAmt.text = data.tripPayment()
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
                            Toast.makeText(context, "Ask Admin to Create New User", Toast.LENGTH_SHORT).show()
                        }
                        setSelection(0)

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
                            binding.advancePendingBankAmt.text = (advanceAmt - dieselAmt).toString()
                            binding.layoutAddPerson.visibility = View.VISIBLE
                        }
                        else{
                            binding.layoutAddPerson.visibility = View.GONE
                            binding.advancePendingBankAmt.text = advanceAmt.toString()
                            binding.advancePendingDieselAmt.setText("0")

                            binding.advancePendingDieselAmt.setError("Can't be greater than Advance Pending Amount")

                        }

                    }
                    else if (dieselAmt == 0 ){
                        binding.layoutAddPerson.visibility = View.GONE
                    }

                }
                else{
                    binding.layoutAddPerson.visibility = View.GONE
                    binding.advancePendingBankAmt.text = advanceAmt.toString()
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


    }
}
interface ChangePaymentModeInterface{
    fun done()
}
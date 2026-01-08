package com.delhivery.axle.ui.dialogs

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.RadioButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.delhivery.axle.R
import com.delhivery.axle.databinding.DialogDownloadLedgerBinding
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.prefs.UserPrefs
import java.text.SimpleDateFormat
import java.util.*

class DownloadLedgerDialog(
        context: Context,
        private val dialogInterface: DownloadLedgerInterface,
        private val analyticsUtil: AnalyticsUtil,
        private val userPrefs: UserPrefs
) : AlertDialog(context){
    /* dialog binding */
    private lateinit var binding: DialogDownloadLedgerBinding
    private lateinit var radioButton: RadioButton

    var startDate = -1
    var startMonth = -1
    var startYear = -1

    var endDate = -1
    var endMonth = -1
    var endYear = -1
    var optionFilter = ""

    /* dismiss timeout disposable */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )

        setCancelable(false)

        /* dialog binding */
        binding = DialogDownloadLedgerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* bind data to layout */
        binding.apply {
            radioGroup.check(binding.all.id)
            btnClose.setOnClickListener { dismissDialog() }
            editStartDate.setOnClickListener { openDatePicker("start") }
            editEndDate.setOnClickListener{ openDatePicker("end")}
            btnDownload.setOnClickListener { downloadDialog() }
            btnEmail.setOnClickListener { emailDialog() }
        }

        binding.btnClose.setOnClickListener { dismiss() }
    }

    /**
     * Dismiss dialog
     */
    private fun dismissDialog() {
        try {
            if (ownerActivity == null || ownerActivity!!.isDestroyed) {
                return
            }
            dismiss()
        } catch (e: Exception) {
            Log.d("Error Dialog", "Exception while closing dialog")
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun openDatePicker(editTextType: String){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(context, R.style.DatePickerTheme,{
            view, year, monthOfYear, dayOfMonth ->
                if(editTextType == "start"){
                    startDate = dayOfMonth
                    startMonth = monthOfYear
                    startYear = year
                    var month = monthOfYear + 1
                    binding.editStartDate.setText("$dayOfMonth/$month/${year.toString().substring(2)}")
                } else if(editTextType == "end"){
                    endDate = dayOfMonth
                    endMonth = monthOfYear
                    endYear = year
                    var month = monthOfYear + 1
                    binding.editEndDate.setText("$dayOfMonth/$month/${year.toString().substring(2)}")
                }
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun isDateCorrect() : Boolean{
        if(binding.editStartDate.text.toString().isEmpty() || binding.editEndDate.text.toString().isEmpty()){
            return false
        }
        try{
            var sdf = SimpleDateFormat("dd/MM/yyyy")
            var startDate = sdf.parse(binding.editStartDate.text.toString())
            var endDate = sdf.parse(binding.editEndDate.text.toString())

            return startDate < endDate
        }catch (exception: Exception){
            exception.printStackTrace()
        }
        return false
    }
    private fun calculateDuration() :String{
        try{
            var sdf = SimpleDateFormat("dd/MM/yyyy")
            var start = sdf.parse(binding.editStartDate.text.toString())
            var end = sdf.parse(binding.editEndDate.text.toString())

            var milli: Long = end.time - start.time


            val secondsInMilli: Long = 1000
            val minutesInMilli = secondsInMilli * 60
            val hoursInMilli = minutesInMilli * 60
            val daysInMilli = hoursInMilli * 24

            val elapsedDays: Long = milli / daysInMilli
            milli %= daysInMilli

            return "$elapsedDays days"


        }catch (exception: Exception){
            exception.printStackTrace()
        }

        return ""
    }

    private fun downloadDialog(){
        if(!isDateCorrect()){
            binding.errorLabel.text = "Please select valid dates"
            binding.errorLabel.visibility = View.VISIBLE
            Log.d("DownloadDialog","Please select valid dates")
        }else{
            //download report
            val duration = calculateDuration()
            val selectedOption: Int = binding.radioGroup.checkedRadioButtonId
            radioButton = findViewById(selectedOption)!!
            analyticsUtil.moEngageTrackEvent(
                    EVENT_DOWNLOAD_LEDGER,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_OPTION_SELECTED, PROPERTY_DURATION_SELECTED, PROPERTY_DOWNLOADED_EMAILED_SELECTED),
                    mutableListOf(userPrefs.userId(), radioButton.text.toString(), duration, VALUE_DOWNLOADED)
            )
            optionFilter = getFilterString(radioButton.text.toString())
            dialogInterface.onDownloadClick(startDate, startMonth, startYear, endDate, endMonth, endYear, optionFilter)
            dismiss()
        }
    }

    private fun emailDialog(){
        if (!isDateCorrect()){
            binding.errorLabel.text = "Please select valid dates"
            binding.errorLabel.visibility = View.VISIBLE

            Log.d("emailDialog","Please select valid dates")

        }else if(binding.editEmailId.text.toString().trim().length == 0){
            binding.errorLabel.text = "Please enter valid email id"
            binding.errorLabel.visibility = View.VISIBLE

            Log.d("emailDialog","Please enter valid email")

        }else{
            //email report
            val duration = calculateDuration()
            val selectedOption: Int = binding.radioGroup.checkedRadioButtonId
            radioButton = findViewById(selectedOption)!!
            analyticsUtil.moEngageTrackEvent(
                    EVENT_DOWNLOAD_LEDGER,
                    mutableListOf(PROPERTY_USER_ID, PROPERTY_OPTION_SELECTED, PROPERTY_DURATION_SELECTED, PROPERTY_EMAIL_ENTERED, PROPERTY_DOWNLOADED_EMAILED_SELECTED),
                    mutableListOf(userPrefs.userId(), radioButton.text.toString(), duration , binding.editEmailId.text.toString(), VALUE_EMAILED)
            )
            optionFilter = getFilterString(radioButton.text.toString())
            dialogInterface.onEmailClick(startDate, startMonth, startYear, endDate, endMonth, endYear, optionFilter, binding.editEmailId.text.toString())
            dismiss()
        }
    }

    private fun getFilterString(text: String = ""): String {
        return when (text) {
            "Payment Due (POD Submitted)" -> {
                "filter_trips_with_pending_payments"
            }
            "Trips with Recovery" -> {
                "filter_pending_recovery_trips"
            }
            "Settled" -> {
                "filter_settled_trips"
            }
            else -> {
                "all"
            }
        }
    }
}

interface DownloadLedgerInterface{
    fun onEmailClick(startDate: Int, startMonth: Int, startYear: Int, endDate: Int, endMonth: Int, endYear: Int, optionFilter: String, email: String)
    fun onDownloadClick(startDate: Int, startMonth: Int, startYear: Int, endDate: Int, endMonth: Int, endYear: Int, optionFilter: String)
}
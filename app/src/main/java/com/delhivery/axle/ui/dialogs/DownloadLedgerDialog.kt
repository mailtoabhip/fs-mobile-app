package com.delhivery.axle.ui.dialogs

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.delhivery.axle.databinding.DialogDownloadLedgerBinding
import java.text.SimpleDateFormat
import java.util.*

class DownloadLedgerDialog(
        context: Context,
        private val dialogInterface: DownloadLedgerInterface
) : AlertDialog(context) {
    /* dialog binding */
    private lateinit var binding: DialogDownloadLedgerBinding

    var startDate = -1
    var startMonth = -1
    var startYear = -1

    var endDate = -1
    var endMonth = -1
    var endYear = -1

    /* dismiss timeout disposable */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* dialog binding */
        binding = DialogDownloadLedgerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* bind data to layout */
        binding.apply {
            btnClose.setOnClickListener { dismissDialog() }
            editStartDate.setOnClickListener { openDatePicker("start") }
            editEndDate.setOnClickListener{ openDatePicker("end")}
            btnDownload.setOnClickListener { downloadDialog() }
            btnEmail.setOnClickListener { emailDialog() }
        }
    }

    /**
     * Dismiss dialog
     */
    private fun dismissDialog() {
        try {
            if (ownerActivity == null || ownerActivity!!.isDestroyed) {
                return
            }
            if (isShowing) {
                dismiss()
            }
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

        val datePickerDialog = DatePickerDialog(context, {
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

    private fun downloadDialog(){
        if(!isDateCorrect()){
            binding.errorLabel.text = "Please select valid dates"
            binding.errorLabel.visibility = View.VISIBLE
            Log.d("DownloadDialog","Please select valid dates")
        }else{
            //download report
            dialogInterface.onDownloadClick(startDate, startMonth, startYear, endDate, endMonth, endYear)
            dismissDialog()
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
            dialogInterface.onEmailClick(startDate, startMonth, startYear, endDate, endMonth, endYear, binding.editEmailId.text.toString())
            dismissDialog()
        }
    }
}

interface DownloadLedgerInterface{
    fun onEmailClick(startDate: Int, startMonth: Int, startYear: Int, endDate: Int, endMonth: Int, endYear: Int, email: String)
    fun onDownloadClick(startDate: Int, startMonth: Int, startYear: Int, endDate: Int, endMonth: Int, endYear: Int)
}
package com.delhivery.axle.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.delhivery.axle.R
import com.delhivery.axle.databinding.DialogErrorBinding
import com.delhivery.axle.databinding.DialogMonthSelectorBinding
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.safeDispose
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MonthSelectorDialog: DialogFragment() {

    private var months = mutableListOf<String>()
    private var monthName = arrayOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December")

    private fun initiateMonths(){
        val calendar = Calendar.getInstance()
        var monthNum = calendar.get((Calendar.MONTH))
        var year = calendar.get(Calendar.YEAR)
        while (year >= 2020 && monthNum >= 4) {
            if (monthNum == -1) {
                monthNum = 11
                year -= 1
            } else {
                val month = monthName[monthNum]
                val yy = year.toString().substring(2)
                val formattedMonth = "$month '$yy"
                months.add(formattedMonth)
                monthNum--
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val selectedItems = ArrayList<Int>() // Where we track the selected items
            val builder = AlertDialog.Builder(it)

            initiateMonths()
            // Set the dialog title
            builder.setTitle("Select Months")
                    // Specify the list array, the items to be selected by default (null for none),
                    // and the listener through which to receive callbacks when items are selected
                    .setMultiChoiceItems(months.toTypedArray(), null,
                            DialogInterface.OnMultiChoiceClickListener { dialog, which, isChecked ->
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    selectedItems.add(which)
                                } else if (selectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    selectedItems.remove(Integer.valueOf(which))
                                }
                            })
                    // Set the action buttons
                    .setPositiveButton("Download",
                            DialogInterface.OnClickListener { dialog, id ->
                                // User clicked OK, so save the selectedItems results somewhere
                                // or return them to the component that opened the dialog

                            })
                    .setPositiveButton("Email",
                            DialogInterface.OnClickListener { dialog, id ->

                            })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
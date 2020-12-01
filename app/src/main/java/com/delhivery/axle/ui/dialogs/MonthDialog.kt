package com.delhivery.axle.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.delhivery.axle.ui.ledger.ConsolidatedPageViewModel
import com.delhivery.axle.ui.ledger.consolidatedPageIntent
import java.util.*
import kotlin.collections.ArrayList

class MonthDialog: DialogFragment() {

    internal lateinit var listener: MonthDialogListener

    interface MonthDialogListener {
        fun onMonthClick(selectedMonth: Int)
    }

    var monthName = arrayOf(
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

    var selectedMonth = -1 // Where we track the selected items

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val selectedItems = ArrayList<Int>() // Where we track the selected items

            // Set the dialog title
            builder.setTitle("Select Month")
                    // Specify the list array, the items to be selected by default (null for none),
                    // and the listener through which to receive callbacks when items are selected
                    .setItems(monthName
                    ) { dialog, which ->
                        // The 'which' argument contains the index position
                        // of the selected item
                        selectedMonth = which
                        listener.onMonthClick(selectedMonth)
                    }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as MonthDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }
}
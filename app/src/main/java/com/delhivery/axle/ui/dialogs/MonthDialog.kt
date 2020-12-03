package com.delhivery.axle.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

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

        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

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
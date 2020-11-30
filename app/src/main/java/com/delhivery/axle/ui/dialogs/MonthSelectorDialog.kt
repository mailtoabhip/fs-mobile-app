package com.delhivery.axle.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.delhivery.axle.R
import com.delhivery.axle.databinding.DialogErrorBinding
import com.delhivery.axle.databinding.DialogMonthSelectorBinding
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.safeDispose
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class MonthSelectorDialog(
        context: Context
) : AlertDialog(context) {

    /* dialog binding */
    private lateinit var binding: DialogMonthSelectorBinding

    /* dismiss timeout disposable */
    private var timeoutDisposable: Disposable? = null

    private var selectedMonths = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* dialog binding */
        binding = DialogMonthSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        binding.checkboxSelectAll.setOnCheckedChangeListener{ v, selected ->
//            v.post{
//                selectedMonths.clear()
//
//                if (!selected) {
//                    binding.checkboxSelectAll.text = getString(R.string.action_select_all_states)
//                    adapter.itemsList()
//                            .forEach { state -> state.checked = false }
//                    adapter.notifyDataSetChanged()
//                    hide()
//                } else {
//                    binding.checkboxSelectAll.text = getString(R.string.action_deselect_all_states)
//                    adapter.itemsList()
//                            .forEach { state -> state.checked = true }
//                    selectedStates.addAll(states)
//                    adapter.notifyDataSetChanged()
//                    show()
//                }
//            }
//        }

        /* bind data to layout */
        binding.apply {
            btnClose.setOnClickListener { dismissDialog() }
            btnSelect.setOnClickListener { dismissDialog() }
        }

        /* dispose on dialog dismiss */
        setOnDismissListener { timeoutDisposable.safeDispose() }
    }

    /**
     * Dismiss dialog
     */
    private fun dismissDialog() {
        timeoutDisposable.safeDispose()
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
}
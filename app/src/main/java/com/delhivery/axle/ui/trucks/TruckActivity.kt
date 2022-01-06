package com.delhivery.axle.ui.trucks

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.telecom.Call
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityTruckBinding
import com.delhivery.axle.databinding.DialogBottomTruckOptionsBinding
import com.delhivery.axle.databinding.DialogBottomTruckValueBinding
import com.delhivery.axle.ui.base.BaseActivity


class TruckActivity : BaseActivity<ActivityTruckBinding, TruckViewModel>() {

    init {
        hasInlineProgress = true
    }

    override fun getViewModelClass()= TruckViewModel::class.java

    override fun layoutId() = R.layout.activity_truck

    override fun requireConnection() = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        /* setup toolbar */
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Enter Truck Details"

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
        }

        binding.editTruckCapacity.setOnClickListener{
            showTruckCapacityDialog()
        }

        binding.editTruckSize.setOnClickListener {
            showTruckSizeDialog()
        }

        binding.editCurrentCity.setOnClickListener {

        }
        binding.editUnloadingDestinations.setOnClickListener {
            showUnloadingLocationsDialog()
        }
    }

    private fun showUnloadingLocationsDialog() {

    }

    private fun showTruckSizeDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogBottomTruckValueBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)


        bindingDialog.selectText.text = getString(R.string.label_select_truck_size)

        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }



        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    private fun showTruckCapacityDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogBottomTruckValueBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.selectText.text = getString(R.string.label_select_truck_capacity)

        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }



        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)

    }


}


/**
 * Truck intent
 */
fun truckIntent(
    context: Context
) = Intent(context, TruckActivity::class.java).apply {

}
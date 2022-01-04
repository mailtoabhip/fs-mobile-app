package com.delhivery.axle.ui.trucks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityTruckBinding
import com.delhivery.axle.ui.base.BaseActivity


class TruckActivity : BaseActivity<ActivityTruckBinding, TruckViewModel>() {

    override fun getViewModelClass()= TruckViewModel::class.java

    override fun layoutId() = R.layout.activity_truck

    override fun requireConnection() = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }


}


/**
 * Truck intent
 */
fun truckIntent(
    context: Context
) = Intent(context, TruckActivity::class.java).apply {

}
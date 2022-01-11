package com.delhivery.axle.ui.trucks

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.DialogBottomActivateTruckBinding
import javax.inject.Inject

class ActivateTruckDialog @Inject constructor(
    context: Context,
    position:Int
) : AlertDialog(context){
    private lateinit var binding: DialogBottomActivateTruckBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )
        binding = DialogBottomActivateTruckBinding.inflate(layoutInflater)
        setContentView(binding.root)


        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.attributes.windowAnimations = R.style.DialogAnimation
        window!!.setGravity(Gravity.BOTTOM)

    }
}
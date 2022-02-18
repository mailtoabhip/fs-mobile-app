package com.delhivery.axle.ui.team

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.delhivery.axle.R
import com.delhivery.axle.databinding.DialogTeamMemberCreateBinding
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 4/1/21
 */

/**
 * Team member Create dialog
 */
class TeamMembersCreateDialog @Inject constructor(
        context: Context,
        private val dialogInterface: TeamMembersCreateDialogInterface,
        private val userPrefs: UserPrefs
) : AlertDialog(context) {

  /* dialog binding */
  private lateinit var binding: DialogTeamMemberCreateBinding
  private var name = ""
  private var number = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
    )

    setCancelable(false)

    /* dialog binding */
    binding = DialogTeamMemberCreateBinding.inflate(layoutInflater)
    setContentView(binding.root)

    window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    window!!.attributes.windowAnimations = R.style.DialogAnimation
    window!!.setGravity(Gravity.BOTTOM)

    /* set binding params */
    binding.apply {
      dieselReliance.isEnabled = false
      dieselIocl.isEnabled= false
    }

    binding.editName.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) = Unit
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
      ) {
        if (s != null) {
          try {
            val input = s.trim().toString()
            name = input
          } catch (e: Exception) {
           name = ""
          }
          binding.btnConfirm.isEnabled = s.length>0 && binding.editNumber.text?.length==10
        }else{
          binding.btnConfirm.isEnabled = false
        }
      }
    })

    binding.editNumber.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) = Unit
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
      ) {
        if (s != null) {
         // binding.tilNumber.error = null
         // binding.tilNumber.isErrorEnabled = false
          try {
            val input = s.trim().toString()
            binding.btnConfirm.isEnabled = input.length == 10 && !(binding.editName.text?.length==0)
            number = input
          } catch (e: Exception) {
           // binding.tilNumber.isErrorEnabled = true
           // binding.tilNumber.error = e.message
            number = ""
          }
        }else{
          binding.btnConfirm.isEnabled = false
        }
      }
    })

    binding.creteMemberDieselReferenceSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
      if(isChecked){
        binding.dieselReliance.isEnabled = true
        binding.dieselIocl.isEnabled= true
        binding.cardLayout.visibility = View.VISIBLE
      }
      else{
        binding.dieselReliance.isEnabled = false
        binding.dieselIocl.isEnabled= false
        binding.dieselReliance.isChecked = false
        binding.dieselIocl.isChecked = false
        binding.cardLayout.visibility = View.GONE
      }
    })

    if ((userPrefs.isLoadBoardClient || userPrefs.isLoadBoardSupplier)){
      if(userPrefs.userMode.equals("post_load")){
        binding.switchLay.visibility = View.GONE
      }else{
        binding.switchLay.visibility = View.VISIBLE
      }
    }else{
      binding.switchLay.visibility = View.VISIBLE
    }

    binding.btnConfirm.setOnClickListener {
      binding.editName.clearFocus()
      binding.editNumber.clearFocus()
      submit()
    }

    binding.btnClose.setOnClickListener { dismiss() }
  }

  private fun submit() {
    try {
      if (name.isNotEmpty() || number.isNotEmpty()) {
        require(number.length == 10) {
          "Please enter valid phone number"
        }
         var dieselPreference = "no"
         val dieselCompany = mutableListOf<String>()
        dieselPreference = if(binding.creteMemberDieselReferenceSwitch.isChecked) "yes" else "no"
        if(binding.creteMemberDieselReferenceSwitch.isChecked){
          if(binding.dieselReliance.isChecked) dieselCompany.add("reliance")
          if(binding.dieselIocl.isChecked) dieselCompany.add("iocl")
        }
        if(dieselPreference!="no" && dieselCompany.isNotEmpty() || dieselPreference == "no") {
          dialogInterface.createMember(name, number, dieselPreference, dieselCompany)
          dismiss()
        }
        else{
          Toast.makeText(context, "Select Diesel Company", Toast.LENGTH_SHORT).show()
        }

      } else {
        throw IllegalArgumentException("*Invalid text")
      }
    } catch (e: IllegalArgumentException) {
    }
  }
}

interface TeamMembersCreateDialogInterface {

  /**
   * Create team member
   */
  fun createMember(
          name: String,
          number: String,
          dieselPreference: String,
          dieselCompany: List<String>
  )

}
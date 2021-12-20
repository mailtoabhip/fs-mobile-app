package com.delhivery.axle.ui.team

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.delhivery.axle.R
import com.delhivery.axle.databinding.DialogTeamMemberCreateBinding
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
        private val dialogInterface: TeamMembersCreateDialogInterface
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

    /* set binding params */
    binding.apply {
      binding.tilName.hint = "Name"
      binding.tilNumber.hint = "Number"
      dieselReliance.isEnabled = false
      dieselIocl.isEnabled= false
    }

    binding.tilName.editText?.addTextChangedListener(object : TextWatcher {
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
          binding.tilName.error = null
          binding.tilName.isErrorEnabled = false
          try {
            val input = s.trim().toString()
            name = input
          } catch (e: Exception) {
            binding.tilName.isErrorEnabled = true
            binding.tilName.error = e.message
            name = ""
          }
        }
      }
    })

    binding.tilNumber.editText?.addTextChangedListener(object : TextWatcher {
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
          binding.tilNumber.error = null
          binding.tilNumber.isErrorEnabled = false
          try {
            val input = s.trim().toString()
            if (input.length != 10) {
              binding.tilNumber.isErrorEnabled = true
            }
            number = input
          } catch (e: Exception) {
            binding.tilNumber.isErrorEnabled = true
            binding.tilNumber.error = e.message
            number = ""
          }
        }
      }
    })
    binding.creteMemberDieselReferenceSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
      if(isChecked){
        binding.dieselReliance.isEnabled = true
        binding.dieselIocl.isEnabled= true
      }
      else{
        binding.dieselReliance.isEnabled = false
        binding.dieselIocl.isEnabled= false
        binding.dieselReliance.isChecked = false
        binding.dieselIocl.isChecked = false

      }
    })

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
      binding.tilNumber.isErrorEnabled = true
      binding.tilNumber.error = e.message
      val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
      binding.tilNumber.startAnimation(shake)
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
package com.delhivery.axle.ui.team

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
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
  private var dieselPreference = "no"
  private var dieselCompany = mutableListOf<String>()

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
        dieselPreference = "yes"
        binding.dieselReliance.isEnabled = true
        binding.dieselIocl.isEnabled= true
      }
      else{
        dieselPreference = "no"
        binding.dieselReliance.isEnabled = false
        binding.dieselIocl.isEnabled= false
        binding.dieselReliance.isChecked = false
        binding.dieselIocl.isChecked = false
        dieselCompany.clear()
      }
    })
    binding.dieselReliance.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
      if(isChecked){
        binding.dieselReliance.isChecked = true
        dieselCompany.add("reliance")
      }
      else{
        binding.dieselReliance.isChecked = false
        dieselCompany.remove("reliance")
      }
    })
    binding.dieselIocl.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
      if(isChecked){
        binding.dieselIocl.isChecked = true
        dieselCompany.add("iocl")
      }
      else{
        binding.dieselIocl.isChecked = false
        dieselCompany.remove("iocl")
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
        dialogInterface.createMember(name, number, dieselPreference, dieselCompany)
        dismiss()
      } else {
        throw IllegalArgumentException("*Invalid text")
      }
    } catch (e: IllegalArgumentException) {
      binding.tilName.isErrorEnabled = true
      binding.tilName.error = e.message
      val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
      binding.tilName.startAnimation(shake)
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
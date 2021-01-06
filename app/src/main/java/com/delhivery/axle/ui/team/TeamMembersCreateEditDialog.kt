package com.delhivery.axle.ui.team

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import com.delhivery.axle.R
import com.delhivery.axle.databinding.DialogTeamMemberCreateEditBinding
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import javax.inject.Inject

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 4/1/21
 */

/**
 * Bid Create/Edit dialog
 */
class TeamMembersCreateEditDialog @Inject constructor(
  context: Context,
  private val uuid: String,
  private val userNumber: String,
  private val dialogInterface: TeamMembersCreateEditDialogInterface
) : AlertDialog(context) {

  /* dialog binding */
  private lateinit var binding: DialogTeamMemberCreateEditBinding
  private var name = ""
  private var number = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window?.clearFlags(
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
    )

    setCancelable(false)

    /* dialog binding */
    binding = DialogTeamMemberCreateEditBinding.inflate(layoutInflater)
    setContentView(binding.root)

    /* set binding params */
    binding.apply {
      number = userNumber
      binding.tilName.hint = "Name"

      if (userNumber.isEmpty()) {
        binding.tilNumber.hint = "Number"
      }
    }

    if (uuid.isNotNullOrEmpty()) {
      number = userNumber
      binding.tilNumber.visibility = View.GONE
      binding.llNumber.visibility = View.VISIBLE
    } else {
      binding.tilNumber.visibility = View.VISIBLE
      binding.llNumber.visibility = View.GONE
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
        if (userNumber.isNotNullOrEmpty()) {
          dialogInterface.editMember(uuid, name)
        } else {
          dialogInterface.createMember(name, number)
        }
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

interface TeamMembersCreateEditDialogInterface {

  /**
   * Create team member
   */
  fun createMember(
    name: String,
    number: String
  )

  /**
   * Edit team member name
   */
  fun editMember(
    uuid: String,
    name: String
  )
}
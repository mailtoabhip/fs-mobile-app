package com.delhivery.axle.ui.team

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import com.delhivery.axle.R
import com.delhivery.axle.databinding.DialogTeamMemberEditBinding
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import kotlinx.android.synthetic.main.spinner_item.view.*
import javax.inject.Inject

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 4/1/21
 */

/**
 * Team member Create/Edit dialog
 */
class TeamMembersEditDialog @Inject constructor(
        context: Context,
        private val uuid: String,
        private val nameVal: String,
        private val userNumber: String,
        private val dieselCardPreference: Boolean,
        private val dieselCompany: List<String>,
        private val dialogInterface: TeamMembersEditDialogInterface
) : AlertDialog(context) {

    /* dialog binding */
    private lateinit var binding: DialogTeamMemberEditBinding
    private var name = ""
    private var number = ""
    private var dieselPreference = "no"
    private var dieselCompanyVal = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )

        setCancelable(false)

        /* dialog binding */
        binding = DialogTeamMemberEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* set binding params */
        binding.apply {
            editName.setText(nameVal)
            number = userNumber
            binding.tilNameEdit.hint = "Name"
            editMemberDieselReferenceSwitch.isChecked = dieselCardPreference
            dieselReliance.isEnabled = false
            if(dieselCompany.isNotEmpty()){
                dieselReliance.isChecked =true
            }

        }

        if (uuid.isNotNullOrEmpty()) {
            number = userNumber
            name = nameVal
            dieselPreference = if (dieselCardPreference) { "yes" } else { "no"}
            dieselCompanyVal = dieselCompany as MutableList<String>
            binding.llNumber.visibility = View.VISIBLE
        } else {
            binding.llNumber.visibility = View.GONE
        }

        binding.tilNameEdit.editText?.addTextChangedListener(object : TextWatcher {
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
                    binding.tilNameEdit.error = null
                    binding.tilNameEdit.isErrorEnabled = false
                    try {
                        val input = s.trim().toString()
                        name = input
                    } catch (e: Exception) {
                        binding.tilNameEdit.isErrorEnabled = true
                        binding.tilNameEdit.error = e.message
                        name = ""
                    }
                }
            }
        })

        binding.editMemberDieselReferenceSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                dieselPreference = "yes"
                binding.dieselReliance.isChecked = true
                dieselCompanyVal.clear()
                dieselCompanyVal.add("reliance")
            }
            else{
                dieselPreference = "no"
                binding.dieselReliance.isChecked = false
                dieselCompanyVal.clear()
            }
        })

        binding.btnConfirm.setOnClickListener {
            binding.editName.clearFocus()
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
                    dialogInterface.editMember(uuid, name ,dieselPreference ,dieselCompanyVal)
                }
                dismiss()
            } else {
                throw IllegalArgumentException("*Invalid text")
            }
        } catch (e: IllegalArgumentException) {
            binding.tilNameEdit.isErrorEnabled = true
            binding.tilNameEdit.error = e.message
            val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
            binding.tilNameEdit.startAnimation(shake)
        }
    }
}

interface TeamMembersEditDialogInterface {
    /**
     * Edit team member name
     */
    fun editMember(
            uuid: String,
            name: String,
            dieselPreference: String,
            dieselCompany: List<String>
    )
}
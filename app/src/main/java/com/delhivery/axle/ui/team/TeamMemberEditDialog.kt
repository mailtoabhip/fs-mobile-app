package com.delhivery.axle.ui.team

import android.app.Dialog
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
import com.delhivery.axle.databinding.DialogTeamMemberEditBinding
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
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
        private val dialogInterface: TeamMembersEditDialogInterface,
        private val userPrefs: UserPrefs
) : Dialog(context) {

    /* dialog binding */
    private lateinit var binding: DialogTeamMemberEditBinding
    private var name = ""
    private var number = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )

        setCancelable(true)

        /* dialog binding */
        binding = DialogTeamMemberEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.attributes.windowAnimations = R.style.DialogAnimation
        window!!.setGravity(Gravity.BOTTOM)

        if ((userPrefs.isLoadBoardClient == false || userPrefs.isLoadBoardSupplier == false)){
            binding.switchLay.visibility = View.VISIBLE
        }else{
            if(userPrefs.userMode.equals("post_load")){
                binding.switchLay.visibility = View.GONE
            }else{
                binding.switchLay.visibility = View.VISIBLE
            }
        }


        /* set binding params */
        binding.apply {
            editName.setText(nameVal)
            number = userNumber
            editMemberDieselReferenceSwitch.isChecked = dieselCardPreference
            if(editMemberDieselReferenceSwitch.isChecked){
                binding.cardLayout2.visibility = View.VISIBLE
            }
            dieselReliance.isEnabled = false
            dieselIocl.isEnabled =false
            if(dieselCompany.isNotEmpty()){
                dieselReliance.isEnabled = true
                dieselIocl.isEnabled= true
                dieselReliance.isChecked = dieselCompany.contains("reliance")
                dieselIocl.isChecked = dieselCompany.contains("iocl")

            }

        }

        if (uuid.isNotNullOrEmpty()) {
            number = userNumber
            name = nameVal
            binding.llNumber.visibility = View.VISIBLE
        } else {
            binding.llNumber.visibility = View.GONE
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
                    binding.btnConfirm.isEnabled = s.length>0 && binding.textNumber.text?.length==13
                }else{
                    binding.btnConfirm.isEnabled = false
                }
            }
        })

        binding.textNumber.addTextChangedListener(object : TextWatcher {
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
                        binding.btnConfirm.isEnabled = input.length == 10 && !(binding.editName.text?.length==0)
                        number = input
                    } catch (e: Exception) {
                        number = ""
                    }
                }else{
                    binding.btnConfirm.isEnabled = false
                }
            }
        })
        binding.dieselReliance.setOnCheckedChangeListener { compoundButton, ischecked -> if(ischecked){
            binding.btnConfirm.isEnabled = !(binding.editName.text?.length==0)
        }else{
            binding.btnConfirm.isEnabled = !(binding.editName.text?.length==0)&& binding.dieselIocl.isChecked
        } }
        binding.dieselIocl.setOnCheckedChangeListener { compoundButton, ischecked -> if(ischecked){
            binding.btnConfirm.isEnabled = !(binding.editName.text?.length==0)
        }else{
            binding.btnConfirm.isEnabled = !(binding.editName.text?.length==0)&& binding.dieselReliance.isChecked
        } }
        binding.editMemberDieselReferenceSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                binding.dieselReliance.isEnabled = true
                binding.dieselIocl.isEnabled= true
                binding.cardLayout2.visibility = View.VISIBLE
                binding.btnConfirm.isEnabled = !(binding.editName.text?.length==0)
            }
            else{
                binding.dieselReliance.isEnabled = false
                binding.dieselIocl.isEnabled= false
                binding.dieselReliance.isChecked = false
                binding.dieselIocl.isChecked = false
                binding.cardLayout2.visibility = View.GONE
                binding.btnConfirm.isEnabled = !(binding.editName.text?.length==0)
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
                require(number.length == 13) {
                    "Please enter valid phone number"
                }
                if (userNumber.isNotNullOrEmpty()) {
                    var dieselPreference = "no"
                    val dieselCompanyVal = mutableListOf<String>()
                    dieselPreference = if(binding.editMemberDieselReferenceSwitch.isChecked) "yes" else "no"
                    if(binding.editMemberDieselReferenceSwitch.isChecked){
                        if(binding.dieselReliance.isChecked) dieselCompanyVal.add("reliance")
                        if(binding.dieselIocl.isChecked) dieselCompanyVal.add("iocl")
                    }
                    if(dieselPreference!="no" && dieselCompanyVal.isNotEmpty() || dieselPreference == "no") {
                        dialogInterface.editMember(uuid, name, dieselPreference, dieselCompanyVal)
                        dismiss()
                    }
                    else{
                        Toast.makeText(context, "Select Diesel Company", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                throw IllegalArgumentException("*Invalid text")
            }
        } catch (e: IllegalArgumentException) {
            //binding.tilNameEdit.isErrorEnabled = true
           // binding.tilNameEdit.error = e.message
            val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
            //binding.tilNameEdit.startAnimation(shake)
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
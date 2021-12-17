package com.delhivery.axle.ui.team

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import com.delhivery.axle.data.UserModel
import com.delhivery.axle.databinding.DialogAdminMemberViewBinding
import com.delhivery.axle.databinding.DialogTeamMemberCreateBinding
import javax.inject.Inject

class AdminMemberDialog @Inject constructor(
        context: Context,
        private val user :UserModel,
        private val dialogInterface: AdminViewDialogInterface
) : AlertDialog(context)  {

    /* dialog binding */
    private lateinit var binding: DialogAdminMemberViewBinding

    private var dieselPreference = "no"
    private var dieselCompanyVal = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )

        setCancelable(false)

        /* dialog binding */
        binding = DialogAdminMemberViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.adminName.text = user.name
        binding.adminDieselReferenceSwitch.isChecked = user.getDieselPreferences()

        dieselPreference = user.dieselCardPreferences?: "no"

        if(user.dieselCompany != null){
            dieselCompanyVal = user.dieselCompany as MutableList<String>
        }

        binding.dieselReliance.isEnabled = false
        if(dieselCompanyVal.isNotEmpty()){
            binding.dieselReliance.isEnabled = true
            binding.dieselIocl.isEnabled= true
            if(dieselCompanyVal.contains("reliance"))
                binding.dieselReliance.isChecked = true
            else
                binding.dieselReliance.isChecked = false

            if(dieselCompanyVal.contains("iocl"))
                binding.dieselIocl.isChecked = true
            else
                binding.dieselIocl.isChecked = false

        }
        binding.adminDieselReferenceSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                dieselCompanyVal.clear()
                binding.dieselReliance.isEnabled = true
                binding.dieselIocl.isEnabled= true
            }
            else{
                dieselPreference = "no"
                binding.dieselReliance.isEnabled = false
                binding.dieselIocl.isEnabled= false
                binding.dieselReliance.isChecked = false
                binding.dieselIocl.isChecked = false
                dieselCompanyVal.clear()

            }
        })
        binding.dieselReliance.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                binding.dieselReliance.isChecked = true
                dieselCompanyVal.add("reliance")
            }
            else{
                binding.dieselReliance.isChecked = false
                dieselCompanyVal.remove("reliance")
            }
        })
        binding.dieselIocl.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                binding.dieselIocl.isChecked = true
                dieselCompanyVal.add("iocl")
            }
            else{
                binding.dieselIocl.isChecked = false
                dieselCompanyVal.remove("iocl")
            }
        })

        binding.btnConfirmAdmin.setOnClickListener {
            submit()
        }

        binding.btnClose.setOnClickListener { dismiss() }
    }

    private fun submit() {
        dialogInterface.updateAdminMember(user.userId, dieselPreference , dieselCompanyVal)
        dismiss()
    }
}

interface AdminViewDialogInterface {

    /**
     * Create team member
     */
    fun updateAdminMember(
        uuid: String,
        dieselPreference: String,
        dieselCompany: List<String>
    )
}
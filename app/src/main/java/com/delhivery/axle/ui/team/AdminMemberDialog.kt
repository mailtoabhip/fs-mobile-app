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
            binding.dieselReliance.isChecked =true
        }
        binding.adminDieselReferenceSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
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
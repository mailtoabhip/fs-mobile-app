package com.delhivery.axle.ui.team

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.Toast
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

        binding.adminName.text = user.userName
        binding.adminDieselReferenceSwitch?.isChecked = user.getDieselPreferences()

        dieselPreference = user.supplierDetails?.dieselCardPreferences?: "no"

        if(user.supplierDetails?.dieselCompany != null){
            dieselCompanyVal = user.supplierDetails?.dieselCompany as MutableList<String>
        }

        binding.dieselReliance.isEnabled = false
        if(dieselCompanyVal.isNotEmpty()){
            binding.dieselReliance.isEnabled = true
            binding.dieselIocl.isEnabled= true
            binding.dieselReliance.isChecked = dieselCompanyVal.contains("reliance")

            binding.dieselIocl.isChecked = dieselCompanyVal.contains("iocl")

        }
        binding.adminDieselReferenceSwitch?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                binding.dieselReliance.isEnabled = true
                binding.dieselIocl.isEnabled= true
                binding.cardLayout?.visibility = View.VISIBLE
            }
            else{
                binding.dieselReliance.isEnabled = false
                binding.dieselIocl.isEnabled= false
                binding.dieselReliance.isChecked = false
                binding.dieselIocl.isChecked = false
                binding.cardLayout?.visibility = View.GONE
            }
        })

        binding.btnConfirmAdmin?.setOnClickListener {
            submit()
        }

        binding.btnClose.setOnClickListener { dismiss() }
    }

    private fun submit() {

        var dieselPrefer = "no"
        val dieselCompany = mutableListOf<String>()
        dieselPrefer = if(binding.adminDieselReferenceSwitch?.isChecked == true) "yes" else "no"
        if(binding.adminDieselReferenceSwitch?.isChecked == true){
            if(binding.dieselReliance.isChecked) dieselCompany.add("reliance")
            if(binding.dieselIocl.isChecked) dieselCompany.add("iocl")
        }
        if(dieselPrefer!="no" && dieselCompany.isNotEmpty() || dieselPrefer == "no") {
            dialogInterface.updateAdminMember(user.userId, dieselPrefer , dieselCompany)
            dismiss()
        }
        else{
            Toast.makeText(context, "Select Diesel Company", Toast.LENGTH_SHORT).show()
        }

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
package com.delhivery.axle.ui.accountaction

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.delhivery.axle.R
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.databinding.ActivityAccountActionBinding
import com.delhivery.axle.ui.accountrole.accountRoleIntent
import com.delhivery.axle.ui.auth.AuthenticationActivity

import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject


class AccountActionActivity  : BaseActivity<ActivityAccountActionBinding, AccountActionViewModel>() {
    init {
        StatusBarColor = Color.parseColor("#ededff")
    }

    override fun getViewModelClass() = AccountActionViewModel::class.java

    override fun layoutId() = R.layout.activity_account_action

    override fun requireConnection() = false

    @Inject lateinit var userPrefs:UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Post loads selected
        binding.radioPostloads.setOnCheckedChangeListener { buttonView, isChecked ->
        clearRadioState()
            binding.radioPostloads.isChecked = isChecked
        }

        //post truck selected
        binding.radioPostTruck.setOnCheckedChangeListener { buttonView, isChecked ->
            clearRadioState()
            binding.radioPostTruck.isChecked = isChecked
        }

        //post both selected
        binding.radioPostBoth.setOnCheckedChangeListener { buttonView, isChecked ->
            clearRadioState()
            binding.radioPostBoth.isChecked = isChecked
        }

        binding.postbothLayout.setOnClickListener {
            setBothLoad()
        }

        binding.postloadsLayout.setOnClickListener {
           setPostLoad()
        }

        binding.posttrucksLayout.setOnClickListener {
           setPostTruck()
        }

        getUserMode()

        //set up the mode
        binding.btnProceed.setOnClickListener {
            if (binding.radioPostBoth.isChecked) {
                redirectRole(AccountType.Both)
            } else if (binding.radioPostTruck.isChecked) {
                redirectRole(AccountType.PostTruck)
            } else if (binding.radioPostloads.isChecked) {
                 redirectRole(AccountType.PostLoad)
            }
        }
    }

    //redirect to account role page
    private fun redirectRole(mode:AccountType){
        viewModel.updateUser(UpdateUserRequest(userMode = mode.title))
        startActivity(accountRoleIntent(this, mode))
    }

    //clear previous selection
    private fun clearRadioState(){
        binding.btnProceed.isEnabled = true
        binding.radioPostloads.isChecked = false
        binding.radioPostBoth.isChecked = false
        binding.radioPostTruck.isChecked = false
    }

    //clear layout background
    private fun clearBackground(){
        binding.postbothLayout.isSelected = false
        binding.postloadsLayout.isSelected = false
        binding.posttrucksLayout.isSelected = false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.updateUserStatusLiveData.postValue(null)
        navigationUtils.navigate(AuthenticationActivity::class.java, true)
    }

    private fun setPostLoad(){
        clearBackground()
        binding.postloadsLayout.isSelected = true
        clearRadioState()
        binding.radioPostloads.isChecked = true
    }

    private fun setPostTruck(){
        clearBackground()
        binding.posttrucksLayout.isSelected = true
        clearRadioState()
        binding.radioPostTruck.isChecked = true
    }

    private fun setBothLoad(){
        clearBackground()
        binding.postbothLayout.isSelected = true
        clearRadioState()
        binding.radioPostBoth.isChecked = true
    }

    private fun getUserMode() {
        if (intent == null || !intent.hasExtra(IntentExtraModeTypeKey)) {
            if(userPrefs.userMode.isNotNullOrEmpty()){
                if(userPrefs.userMode.equals(AccountType.Both.title)){
                    setBothLoad()
                } else if(userPrefs.userMode.equals(AccountType.PostTruck.title)){
                    setPostTruck()
                } else if(userPrefs.userMode.equals(AccountType.PostLoad.title)){
                    setPostLoad()
                }
            }
        }else{
            if(AccountType.byTypeId(intent.getIntExtra(IntentExtraModeTypeKey,AccountType.Unknown.typeId)).equals(AccountType.Both)){
                setBothLoad()
            } else if(AccountType.byTypeId(intent.getIntExtra(IntentExtraModeTypeKey,AccountType.Unknown.typeId)).equals(AccountType.PostTruck)){
                setPostTruck()
            } else if(AccountType.byTypeId(intent.getIntExtra(IntentExtraModeTypeKey,AccountType.Unknown.typeId)).equals(AccountType.PostLoad)){
                setPostLoad()
            }
        }
    }
}

/*  */
/*  */
const val IntentExtraRoleTypeKey = "role_type"
const val IntentExtraModeTypeKey = "mode_type"

/**
 * Get [AccountModeActivity] for specific [Mode] as [type]
 */
fun accountModeIntent(
        context: Context,
        type: AccountType?
) = Intent(context, AccountActionActivity::class.java).apply {
    if(type?.typeId!=-1)putExtra(IntentExtraModeTypeKey, type?.typeId)
}




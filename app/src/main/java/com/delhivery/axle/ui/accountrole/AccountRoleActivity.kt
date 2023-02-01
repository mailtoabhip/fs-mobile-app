package com.delhivery.axle.ui.accountrole

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.api.request.UpdateUserRequest
import com.delhivery.axle.databinding.ActivityAccountRoleBinding
import com.delhivery.axle.databinding.ActivityVerifyPanBinding
import com.delhivery.axle.ui.accountaction.*
import com.delhivery.axle.ui.accountdetails.AccountDetailsActivity
import com.delhivery.axle.ui.accountdetails.accountDetailIntent
import com.delhivery.axle.ui.auth.AuthenticationActivity
import com.delhivery.axle.ui.auth.AuthenticationUIState

import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.*
import com.delhivery.axle.utils.extensions.isNotNullOrEmpty
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject


class AccountRoleActivity  : BaseActivity<ActivityAccountRoleBinding, AccountRoleViewModel>() {
    init {
        StatusBarColor = Color.parseColor("#ededff")
    }
    override fun getViewModelClass() = AccountRoleViewModel::class.java

    override fun layoutId() = R.layout.activity_account_role

    override fun requireConnection() = false

    @Inject
    lateinit var userPrefs: UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getUserMode()

        /* observe and update ui state */
       viewModel.modeLiveData.observe(this, StateObserver())

        viewModel.uiStateLiveData.observe(this, RoleUIObserver())

        viewModel.roleLiveData.observe(this, Observer {
            binding.btnProceed.isEnabled = it.title.isNotNullOrEmpty()
        })

        //set up the mode
        binding.btnProceed.setOnClickListener {
            viewModel.updateUser(UpdateUserRequest(userRole = viewModel.roleLiveData.value?.title))
            startActivity(accountDetailIntent(this, viewModel.modeLiveData.value, viewModel.roleLiveData.value))
        }

        binding.shipperLayout.setOnClickListener {
            viewModel.uiStateLiveData.postValue(RoleUIState.PostLoadShipper)
        }

        binding.shipperLayout3.setOnClickListener {
            viewModel.uiStateLiveData.postValue(RoleUIState.BothShipper)
        }

        binding.brokerLayout.setOnClickListener {
            viewModel.uiStateLiveData.postValue(RoleUIState.PostLoadBroker)
        }

        binding.brokerLayout2.setOnClickListener {
            viewModel.uiStateLiveData.postValue(RoleUIState.PostTruckBroker)
        }

        binding.brokerLayout3.setOnClickListener {
            viewModel.uiStateLiveData.postValue(RoleUIState.BothBroker)
        }

        binding.ownerLayout2.setOnClickListener {
            viewModel.uiStateLiveData.postValue(RoleUIState.PostTruckOwner)
        }

        binding.ownerLayout3.setOnClickListener {
            viewModel.uiStateLiveData.postValue(RoleUIState.BothOwner)
        }

        binding.transporterLayout.setOnClickListener {
            viewModel.uiStateLiveData.postValue(RoleUIState.PostLoadTransporter)
        }

        binding.transporterLayout2.setOnClickListener {
            viewModel.uiStateLiveData.postValue(RoleUIState.PostTruckTransporter)
        }

        binding.transporterLayout3.setOnClickListener {
            viewModel.uiStateLiveData.postValue(RoleUIState.BothTransporter)
        }

    }

    private fun setTransporterRole(){
        viewModel.roleLiveData.postValue(AccountRole.Transporter)
    }

    private fun setShipperRole(){
        viewModel.roleLiveData.postValue(AccountRole.Shipper)
    }

    private fun setBrokerRole(){
        viewModel.roleLiveData.postValue(AccountRole.Broker)
    }

    private fun setOwnerRole(){
        viewModel.roleLiveData.postValue(AccountRole.FleetOwner)
    }

    private fun resetRoleLayout(){
        binding.shipperLayout.isSelected = false
        binding.brokerLayout.isSelected = false
        binding.transporterLayout.isSelected = false
        binding.shipperLayout3.isSelected = false
        binding.brokerLayout2.isSelected = false
        binding.transporterLayout2.isSelected = false
        binding.brokerLayout3.isSelected = false
        binding.transporterLayout3.isSelected = false
        binding.ownerLayout2.isSelected = false
        binding.ownerLayout3.isSelected = false
    }

    private fun getUserMode() {
        if (intent == null || !intent.hasExtra(IntentExtraModeTypeKey)) {
            if(userPrefs.userMode.isNotNullOrEmpty()){
                if(userPrefs.userMode.equals(AccountType.Both.title)){
                    viewModel.modeLiveData.postValue(AccountType.Both)
                } else if(userPrefs.userMode.equals(AccountType.PostTruck.title)){
                   viewModel.modeLiveData.postValue(AccountType.PostTruck)
                } else if(userPrefs.userMode.equals(AccountType.PostLoad.title)){
                   viewModel.modeLiveData.postValue(AccountType.PostLoad)
                }
            }else{
                navigationUtils.navigate(AuthenticationActivity::class.java, true)
            }
        }else{
           viewModel.modeLiveData.postValue(AccountType.byTypeId(intent.getIntExtra(IntentExtraModeTypeKey, AccountType.Unknown.typeId)))
        }
    }

    inner class StateObserver : Observer<AccountType> {
        override fun onChanged(it: AccountType?) {
            it?.let { state ->
                when (state) {
                  AccountType.PostLoad->{
                        binding.shipperSection.visibility = View.VISIBLE
                        binding.transporterSection.visibility = View.GONE
                        binding.bothSection.visibility = View.GONE

                      if (intent == null || !intent.hasExtra(IntentExtraRoleTypeKey)) {
                          if(userPrefs.userRole.isNotNullOrEmpty()){
                              if(userPrefs.userRole.equals(AccountRole.Shipper.title)){
                                  viewModel.uiStateLiveData.postValue(RoleUIState.PostLoadShipper)
                              }else  if(userPrefs.userRole.equals(AccountRole.Broker.title)){
                                  viewModel.uiStateLiveData.postValue(RoleUIState.PostLoadBroker)
                              } else if(userPrefs.userRole.equals(AccountRole.Transporter.title)){
                                  viewModel.uiStateLiveData.postValue(RoleUIState.PostLoadTransporter)
                              }
                          }
                      }else{
                          if(AccountRole.byTypeId(intent.getIntExtra(IntentExtraRoleTypeKey, AccountRole.Unknown.typeId)).equals(AccountRole.Shipper)){
                              viewModel.uiStateLiveData.postValue(RoleUIState.PostLoadShipper)
                          }else if(AccountRole.byTypeId(intent.getIntExtra(IntentExtraRoleTypeKey, AccountRole.Unknown.typeId)).equals(AccountRole.Broker)){
                              viewModel.uiStateLiveData.postValue(RoleUIState.PostLoadBroker)
                          } else if(AccountRole.byTypeId(intent.getIntExtra(IntentExtraRoleTypeKey, AccountRole.Unknown.typeId)).equals(AccountRole.Transporter)){
                              viewModel.uiStateLiveData.postValue(RoleUIState.PostLoadTransporter)
                          }
                      }

                  } AccountType.PostTruck->{
                    binding.shipperSection.visibility = View.GONE
                    binding.transporterSection.visibility = View.VISIBLE
                    binding.bothSection.visibility = View.GONE

                    if (intent == null || !intent.hasExtra(IntentExtraRoleTypeKey)) {
                        if(userPrefs.userRole.isNotNullOrEmpty()){
                            if(userPrefs.userRole.equals(AccountRole.FleetOwner.title)){
                                viewModel.uiStateLiveData.postValue(RoleUIState.PostTruckOwner)
                            }else  if(userPrefs.userRole.equals(AccountRole.Broker.title)){
                                viewModel.uiStateLiveData.postValue(RoleUIState.PostTruckBroker)
                            } else if(userPrefs.userRole.equals(AccountRole.Transporter.title)){
                                viewModel.uiStateLiveData.postValue(RoleUIState.PostTruckTransporter)
                            }
                        }
                    }else{
                        if(AccountRole.byTypeId(intent.getIntExtra(IntentExtraRoleTypeKey, AccountRole.Unknown.typeId)).equals(AccountRole.FleetOwner)){
                            viewModel.uiStateLiveData.postValue(RoleUIState.PostTruckOwner)
                        }else if(AccountRole.byTypeId(intent.getIntExtra(IntentExtraRoleTypeKey, AccountRole.Unknown.typeId)).equals(AccountRole.Broker)){
                            viewModel.uiStateLiveData.postValue(RoleUIState.PostTruckBroker)
                        } else if(AccountRole.byTypeId(intent.getIntExtra(IntentExtraRoleTypeKey, AccountRole.Unknown.typeId)).equals(AccountRole.Transporter)){
                            viewModel.uiStateLiveData.postValue(RoleUIState.PostTruckTransporter)
                        }
                    }

                  } AccountType.Both->{
                    binding.shipperSection.visibility = View.GONE
                    binding.transporterSection.visibility = View.GONE
                    binding.bothSection.visibility = View.VISIBLE

                    if (intent == null || !intent.hasExtra(IntentExtraRoleTypeKey)) {
                        if(userPrefs.userRole.isNotNullOrEmpty()){
                            if(userPrefs.userRole.equals(AccountRole.FleetOwner.title)){
                                viewModel.uiStateLiveData.postValue(RoleUIState.BothOwner)
                            }else  if(userPrefs.userRole.equals(AccountRole.Broker.title)){
                                viewModel.uiStateLiveData.postValue(RoleUIState.BothBroker)
                            } else if(userPrefs.userRole.equals(AccountRole.Transporter.title)){
                                viewModel.uiStateLiveData.postValue(RoleUIState.BothTransporter)
                            }else if(userPrefs.userRole.equals(AccountRole.Shipper.title)){
                                viewModel.uiStateLiveData.postValue(RoleUIState.BothShipper)
                            }
                        }
                    }else{
                        if(AccountRole.byTypeId(intent.getIntExtra(IntentExtraRoleTypeKey, AccountRole.Unknown.typeId)).equals(AccountRole.FleetOwner)){
                            viewModel.uiStateLiveData.postValue(RoleUIState.BothOwner)
                        }else if(AccountRole.byTypeId(intent.getIntExtra(IntentExtraRoleTypeKey, AccountRole.Unknown.typeId)).equals(AccountRole.Broker)){
                            viewModel.uiStateLiveData.postValue(RoleUIState.BothBroker)
                        } else if(AccountRole.byTypeId(intent.getIntExtra(IntentExtraRoleTypeKey, AccountRole.Unknown.typeId)).equals(AccountRole.Transporter)){
                            viewModel.uiStateLiveData.postValue(RoleUIState.BothTransporter)
                        }else if(AccountRole.byTypeId(intent.getIntExtra(IntentExtraRoleTypeKey, AccountRole.Unknown.typeId)).equals(AccountRole.Shipper)){
                            viewModel.uiStateLiveData.postValue(RoleUIState.BothShipper)
                        }
                    }
                  }
                  else ->{}
                }
            }
        }
    }

    inner class RoleUIObserver : Observer<RoleUIState> {
        override fun onChanged(it: RoleUIState?) {
            it?.let { state ->
                when (state) {
                    RoleUIState.PostLoadShipper -> {
                        resetRoleLayout()
                        binding.shipperLayout.isSelected = true
                        setShipperRole()
                    }
                    RoleUIState.PostLoadBroker -> {
                        resetRoleLayout()
                        binding.brokerLayout.isSelected = true
                        setBrokerRole()
                    }
                    RoleUIState.PostLoadTransporter -> {
                        resetRoleLayout()
                        binding.transporterLayout.isSelected = true
                        setTransporterRole()
                    }
                    RoleUIState.PostTruckOwner -> {
                        resetRoleLayout()
                        binding.ownerLayout2.isSelected = true
                        setOwnerRole()
                    }
                    RoleUIState.PostTruckBroker -> {
                        resetRoleLayout()
                        binding.brokerLayout2.isSelected = true
                        setBrokerRole()
                    }
                    RoleUIState.PostTruckTransporter -> {
                        resetRoleLayout()
                        binding.transporterLayout2.isSelected = true
                        setTransporterRole()
                    }
                    RoleUIState.BothShipper -> {
                        resetRoleLayout()
                        binding.shipperLayout3.isSelected = true
                        setShipperRole()
                    }
                    RoleUIState.BothBroker -> {
                        resetRoleLayout()
                        binding.brokerLayout3.isSelected = true
                        setBrokerRole()
                    }
                    RoleUIState.BothTransporter -> {
                        resetRoleLayout()
                        binding.transporterLayout3.isSelected = true
                        setTransporterRole()
                    }
                    RoleUIState.BothOwner -> {
                        resetRoleLayout()
                        binding.ownerLayout3.isSelected = true
                        setOwnerRole()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.updateUserStatusLiveData.postValue(null)
        startActivity(accountModeIntent(this, viewModel.modeLiveData.value))}
}

/**
 * Get [AccountRoleActivity] for specific [Role] as [type]
 */
fun accountRoleIntent(
        context: Context,
        type: AccountType?
) = Intent(context, AccountRoleActivity::class.java).apply {
    if(type?.typeId!=-1) putExtra(IntentExtraModeTypeKey, type?.typeId)
}






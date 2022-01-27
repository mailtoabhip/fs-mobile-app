package com.delhivery.axle.ui.accountsetup.fragments

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.database.entity.SearchLoadHistoryEntity
import com.delhivery.axle.databinding.FragmentPrimaryActionBinding
import com.delhivery.axle.databinding.FragmentSearchLoadBinding
import com.delhivery.axle.databinding.ViewSearchLoadHistoryItemBinding
import com.delhivery.axle.ui.accountsetup.AccountSetupBaseFragment
import com.delhivery.axle.ui.accountsetup.AccountSetupViewModel
import com.delhivery.axle.ui.accountsetup.RoleAccountSetupAction
import com.delhivery.axle.ui.custom.AnimationType.RevealOpen
import com.delhivery.axle.ui.searchload.fragments.ProgressSearchLoadAction
import com.delhivery.axle.ui.searchload.fragments.SearchLoadAction
import com.delhivery.axle.ui.searchload.fragments.SearchLoadBaseFragment
import com.delhivery.axle.utils.AutoCompleteUtils
import com.delhivery.axle.utils.EVENT_SEARCH_ERROR
import com.delhivery.axle.utils.extensions.errorVibrate
import com.delhivery.axle.utils.extensions.setup
import com.delhivery.axle.utils.extensions.visible
import com.github.florent37.kotlin.pleaseanimate.please
import kotlinx.android.synthetic.main.fragment_primary_action.*
import javax.inject.Inject

/**
 * Account Primary action
 */
class PrimaryActionFragment : AccountSetupBaseFragment<FragmentPrimaryActionBinding, AccountSetupViewModel>() {

  companion object {
    /* singleton instance */
    val _instance: PrimaryActionFragment by lazy { PrimaryActionFragment() }
  }

  override fun getViewModelClass() = AccountSetupViewModel::class.java

  override fun layoutId() = R.layout.fragment_primary_action

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.progressLiveData.observe(this, ProgressObserver())

    //Post loads selected
    binding.radioPostloads.setOnClickListener {
      clearRadioState()
      binding.radioPostloads.isChecked = true
    }

    //post truck selected
    binding.radioPostTruck.setOnClickListener {
      clearRadioState()
      binding.radioPostTruck.isChecked = true
    }

    //post both selected
    binding.radioPostBoth.setOnClickListener {
      clearRadioState()
      binding.radioPostBoth.isChecked = true
    }

    //set up the role
    binding.btnProceed.setOnClickListener {
      if(radioPostBoth.isChecked){
        AccountRoleFragment.roleData = "both"
      }else if (radioPostTruck.isChecked){
        AccountRoleFragment.roleData = "truck"
      }else if (radioPostloads.isChecked){
         AccountRoleFragment.roleData = "load"
      }
      action(RoleAccountSetupAction(true))

    }
  }

  //clear previous selection
  private fun clearRadioState(){
    binding.btnProceed.isEnabled = true
    binding.radioPostloads.isChecked = false
    binding.radioPostBoth.isChecked = false
    binding.radioPostTruck.isChecked = false
  }

  /**
   * Progress observer
   */
  inner class ProgressObserver : Observer<Boolean> {
    override fun onChanged(t: Boolean?) {
      t?.let {
        when (t) {
          true -> uiUtils.showDelhiveryProgress(
              "Getting details", "This usually takes few seconds to load. please be patient.",
              "This usually takes few seconds to load. please be patient."
          )
          false -> uiUtils.hideDelhiveryProgress()
        }
      }
    }
  }
}
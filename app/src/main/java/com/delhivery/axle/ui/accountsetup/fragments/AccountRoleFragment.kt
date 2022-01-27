package com.delhivery.axle.ui.accountsetup.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.data.home.bids.HomeBidsRequestItemData
import com.delhivery.axle.databinding.FragmentAccountRoleBinding
import com.delhivery.axle.databinding.FragmentPrimaryActionBinding
import com.delhivery.axle.ui.accountsetup.AccountSetupBaseFragment
import com.delhivery.axle.ui.accountsetup.AccountSetupViewModel
import com.delhivery.axle.ui.accountsetup.DetailsAccountSetupAction
import com.delhivery.axle.ui.accountsetup.RoleAccountSetupAction
import com.delhivery.axle.ui.biddetails.BidDetailsCreateEditDialog
import com.delhivery.axle.ui.searchload.fragments.SearchLoadBaseFragment

/**
 * Account Role action
 */
class AccountRoleFragment : AccountSetupBaseFragment<FragmentAccountRoleBinding, AccountSetupViewModel>() {

  companion object {
    /* singleton instance */
    val _instance: AccountRoleFragment by lazy { AccountRoleFragment() }
    var roleData:String? = null
  }

  override fun getViewModelClass() = AccountSetupViewModel::class.java

  override fun layoutId() = R.layout.fragment_account_role

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.progressLiveData.observe(this, ProgressObserver())

      if(roleData.equals("load")){
        binding.ownerLayout.visibility = View.GONE
        binding.shipperLayout.visibility = View.VISIBLE
        binding.brokerLayout.setBackground(resources.getDrawable(R.drawable.bg_all_round_corner_white))
      }else if(roleData.equals("truck")){
        binding.shipperLayout.visibility = View.GONE
        binding.ownerLayout.visibility = View.VISIBLE
        binding.brokerLayout.setBackground(resources.getDrawable(R.drawable.bg_rounded_blue))
      }else if(roleData.equals("both")){
        binding.shipperLayout.visibility = View.VISIBLE
        binding.ownerLayout.visibility = View.VISIBLE
        binding.brokerLayout.setBackground(resources.getDrawable(R.drawable.bg_all_round_corner_white))
      }

    binding.ownerLayout.setOnClickListener {
      binding.ownerLayout.setBackground(resources.getDrawable(R.drawable.bg_rounded_blue))
      binding.shipperLayout.setBackground(resources.getDrawable(R.drawable.bg_all_round_corner_white))
      binding.transporterLayout.setBackground(resources.getDrawable(R.drawable.bg_all_round_corner_white))
      binding.brokerLayout.setBackground(resources.getDrawable(R.drawable.bg_all_round_corner_white))
    }

    binding.shipperLayout.setOnClickListener {
      binding.ownerLayout.setBackground(resources.getDrawable(R.drawable.bg_all_round_corner_white))
      binding.shipperLayout.setBackground(resources.getDrawable(R.drawable.bg_rounded_blue))
      binding.transporterLayout.setBackground(resources.getDrawable(R.drawable.bg_all_round_corner_white))
      binding.brokerLayout.setBackground(resources.getDrawable(R.drawable.bg_all_round_corner_white))
    }

    binding.transporterLayout.setOnClickListener {
      binding.ownerLayout.setBackground(resources.getDrawable(R.drawable.bg_all_round_corner_white))
      binding.shipperLayout.setBackground(resources.getDrawable(R.drawable.bg_all_round_corner_white))
      binding.transporterLayout.setBackground(resources.getDrawable(R.drawable.bg_rounded_blue))
      binding.brokerLayout.setBackground(resources.getDrawable(R.drawable.bg_all_round_corner_white))
    }

    binding.brokerLayout.setOnClickListener {
      binding.ownerLayout.setBackground(resources.getDrawable(R.drawable.bg_all_round_corner_white))
      binding.shipperLayout.setBackground(resources.getDrawable(R.drawable.bg_all_round_corner_white))
      binding.transporterLayout.setBackground(resources.getDrawable(R.drawable.bg_all_round_corner_white))
      binding.brokerLayout.setBackground(resources.getDrawable(R.drawable.bg_rounded_blue))
    }

    //set up the role
    binding.btnProceed.setOnClickListener {
      action(DetailsAccountSetupAction(true))
    }
  }

  override fun onPause() {
    super.onPause()
    uiUtils.toggleKeyboard()
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
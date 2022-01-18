package com.delhivery.axle.ui.accountsetup

import android.graphics.Color
import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityAccountSetupBinding
import com.delhivery.axle.databinding.ActivityAuthenticationBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.searchload.fragments.*
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * Account setup screen
 */
class AccountSetupActivity : BaseActivity<ActivityAccountSetupBinding, AccountSetupViewModel>(){

  override fun getViewModelClass() = AccountSetupViewModel::class.java

  override fun layoutId() = R.layout.activity_account_setup

  /* current Fragment type */
  private var currentFragmentType: AccountSetupFragmentType? = null

  override fun requireConnection() = true

  @Inject lateinit var userPrefs: UserPrefs

  init {
    StatusBarColor = Color.parseColor("#ededff")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = ""
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /* start with priamry action fragment */
    navigate(AccountSetupFragmentType.PrimaryFragment)
  }

  /**
   * Navigate to [SearchLoadFragmentType] fragment
   */
  private fun navigate(fragmentType: AccountSetupFragmentType) {
    if (currentFragmentType == fragmentType) return
    currentFragmentType = fragmentType
    navigationUtils.addReplaceFragment(
            R.id.container, fragmentType.fragment, PrimaryActionFragmentTag
    )
    title = currentFragmentType?.title
  }

  override fun onBackPressed() {
    when (currentFragmentType) {
     AccountSetupFragmentType.RoleFragment -> navigate(AccountSetupFragmentType.PrimaryFragment)
      AccountSetupFragmentType.DetailsFragment -> navigate(AccountSetupFragmentType.RoleFragment)
      else -> super.onBackPressed()
    }
  }

  /**
   * Fragment action observer
   */
  fun fragmentAction(action: BaseAccountSetupFragmentAction) {
    when (action.type) {
      /* shoe/hide progress */
      AccountSetupFragmentActionType.Progress -> {
        (action as ProgressAccountSetupAction).apply {
          if (show) {
            uiUtils.showDelhiveryProgress(action.title, action.message, action.protip)
          } else {
            uiUtils.hideDelhiveryProgress()
          }
        }
      }
      AccountSetupFragmentActionType.Role-> navigate(AccountSetupFragmentType.RoleFragment)
      AccountSetupFragmentActionType.Details-> navigate(AccountSetupFragmentType.DetailsFragment)
      AccountSetupFragmentActionType.PrimaryAction-> navigate(AccountSetupFragmentType.PrimaryFragment)
    }
  }
}

/* Primary fragment tag */
private const val PrimaryActionFragmentTag = "priamry_action_fragment_tag"


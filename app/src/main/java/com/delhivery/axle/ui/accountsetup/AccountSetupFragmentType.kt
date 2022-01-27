package com.delhivery.axle.ui.accountsetup

import com.delhivery.axle.ui.accountsetup.fragments.AccountDetailsFragment
import com.delhivery.axle.ui.accountsetup.fragments.AccountRoleFragment
import com.delhivery.axle.ui.accountsetup.fragments.PrimaryActionFragment
import com.delhivery.axle.ui.searchload.fragments.searchload.SearchLoadFragment
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsFragment

/**
 * Account set up fragment type
 */
enum class AccountSetupFragmentType(
  val fragment: AccountSetupBaseFragment<*, *>,
  val title: String
) {
    PrimaryFragment(PrimaryActionFragment._instance, ""),
    RoleFragment(AccountRoleFragment._instance,""),
    DetailsFragment(AccountDetailsFragment._instance,"")
}
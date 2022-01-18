package com.delhivery.axle.ui.accountsetup

import com.delhivery.axle.data.CityModel
import com.delhivery.axle.ui.searchload.fragments.SearchLoadFragmentActionType.Search

/**
 * Account set up fragment actions type
 */
enum class AccountSetupFragmentActionType {
  Progress,
  PrimaryAction,
  Role,
  Details
}

/**
 * Account set up fragment action base class
 */
abstract class BaseAccountSetupFragmentAction(val type: AccountSetupFragmentActionType)

/**
 * Show/hide inline - progress
 */
class ProgressAccountSetupAction(
  val show: Boolean,
  val title: String = "Saving details",
  val message: String = "This usually takes few seconds to load. please be patient.",
  val protip: String = "Some tip regarding how to bid, or whats to be considered while bidding."
) : BaseAccountSetupFragmentAction(
        AccountSetupFragmentActionType.Progress
)

/**
 * Show/hide inline - progress
 */
class RoleAccountSetupAction(
        val show: Boolean,
        val title: String = "Saving details",
        val message: String = "This usually takes few seconds to load. please be patient.",
        val protip: String = "Some tip regarding how to bid, or whats to be considered while bidding."
) : BaseAccountSetupFragmentAction(
        AccountSetupFragmentActionType.Role
)
package com.delhivery.axle.ui.team

import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter.ItemClickListener

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 30/12/20
 */
interface TeamMembersRVAdapterInterface : ItemClickListener<BaseTeamMembersRVAdapterItem<*>> {
  override fun onItemClicked(item: BaseTeamMembersRVAdapterItem<*>) {

  }

  /**
   * Handle specific action
   */
  fun handleAction(
    actionId: String,
    item: BaseTeamMembersRVAdapterItem<*>
  )

  /**
   * Handle specific action with item position
   */
  fun handleAction(
    actionId: String,
    item: BaseTeamMembersRVAdapterItem<*>,
    position: Int
  )
}
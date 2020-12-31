package com.delhivery.axle.ui.team

import android.view.View
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ViewHomeBidsProgressItemBinding
import com.delhivery.axle.databinding.ViewHomeLoadsProgressItemBinding
import com.delhivery.axle.databinding.ViewTeamMemberAdminItemBinding
import com.delhivery.axle.databinding.ViewTeamMemberSubuserItemBinding
import com.delhivery.axle.databinding.ViewTeamMembersProgressItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 30/12/20
 */

/**
 * Base Home bids RV adapter view holder
 */
abstract class BaseTeamMembersRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseTeamMembersRVAdapterItem<*>>(binding: B) :
    BaseViewHolder<B>(binding) {
  abstract fun bind(
    item: IT,
    _interface: TeamMembersRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    _interface: TeamMembersRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Add on click listener for action with position
   */
  protected fun View.clickToAction(
    actionId: String,
    item: IT,
    position: Int,
    _interface: TeamMembersRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, position, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    _interface: TeamMembersRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }

  /**
   * Post action to UI with position
   */
  protected fun View.action(
    actionId: String,
    item: IT,
    position: Int,
    _interface: TeamMembersRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item, position) }
}

/**
 * admin user item view holder
 */
class TeamMembersAdminUserItemVH(binding: ViewTeamMemberAdminItemBinding) :
    BaseTeamMembersRVAdapterViewHolder<ViewTeamMemberAdminItemBinding, TeamMemberAdminUserItem>(
        binding
    ) {
  override fun bind(
    item: TeamMemberAdminUserItem,
    _interface: TeamMembersRVAdapterInterface
  ) {

  }
}

/**
 * sub user item view holder
 */
class TeamMembersSubUserItemVH(binding: ViewTeamMemberSubuserItemBinding) :
    BaseTeamMembersRVAdapterViewHolder<ViewTeamMemberSubuserItemBinding, TeamMemberSubUserItem>(
        binding
    ) {
  override fun bind(
    item: TeamMemberSubUserItem,
    _interface: TeamMembersRVAdapterInterface
  ) {

  }
}

/**
 * Progress inline view holder
 */
internal class TeamMembersProgressItemVH(binding: ViewTeamMembersProgressItemBinding) :
    BaseTeamMembersRVAdapterViewHolder<ViewTeamMembersProgressItemBinding, TeamMembersProgressItem>(
        binding
    ) {
  override fun bind(
    item: TeamMembersProgressItem,
    _interface: TeamMembersRVAdapterInterface
  ) {
  }
}
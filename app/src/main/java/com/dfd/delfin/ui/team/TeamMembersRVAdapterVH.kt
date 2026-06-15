package com.dfd.delfin.ui.team

import android.view.View
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.R
import com.dfd.delfin.api.request.TeamMemberAction_options
import com.dfd.delfin.api.request.ViewAdminMember
import com.dfd.delfin.databinding.*
import com.dfd.delfin.ui.base.BaseViewHolder
import com.dfd.delfin.utils.extensions.isNotNullOrEmpty

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
    binding.user = item.data
    if(item.data.userName.isNotNullOrEmpty()) {
      binding.logoTeamMember.text = item.data.userName?.get(0)?.uppercase().toString()
    }
    binding.adminCardContainer.clickToAction(ViewAdminMember, item, _interface)
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
    binding.user = item.data
    if(item.data.userName.isNotNullOrEmpty()) {
      binding.logoTeamMember.text = item.data.userName?.get(0)?.uppercase().toString()
    }
    binding.iconOptionsTeamMember.clickToAction(TeamMemberAction_options, item, bindingAdapterPosition, _interface)
    //binding.iconEditUser.clickToAction(EditTeamMemberAction_Edit, item, _interface)
//    binding.iconRemoveUser.clickToAction(DeleteTeamMemberAction_Delete, item, _interface)
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

/**
 * Team warning item view holder
 */
internal class TeamWarningItemVH(binding: ViewNoTeamMemberBinding) :
  BaseTeamMembersRVAdapterViewHolder<ViewNoTeamMemberBinding,TeamWarningItem>(
    binding
  ) {
  override fun bind(
    item: TeamWarningItem,
    _interface: TeamMembersRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
    binding.iconImage = R.drawable.ic_no_member_warning
  }
}
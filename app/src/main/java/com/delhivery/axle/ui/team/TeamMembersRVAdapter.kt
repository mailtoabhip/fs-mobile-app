package com.delhivery.axle.ui.team

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.delhivery.axle.databinding.ViewNoTeamMemberBinding
import com.delhivery.axle.databinding.ViewTeamMemberAdminItemBinding
import com.delhivery.axle.databinding.ViewTeamMemberSubuserItemBinding
import com.delhivery.axle.databinding.ViewTeamMembersProgressItemBinding
import com.delhivery.axle.ui.base.BaseViewHolder
import com.delhivery.axle.ui.base.adapter.BaseDataRVAdapter
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.AddUpdate
import com.delhivery.axle.ui.base.adapter.DataRVAdapterOperationType.Remove
import com.delhivery.axle.ui.team.TeamMembersRVAdapterItemType.*

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 31/12/20
 */
class TeamMembersRVAdapter(private val _interface: TeamMembersRVAdapterInterface) :
    BaseDataRVAdapter<BaseTeamMembersRVAdapterItem<*>, ViewDataBinding, BaseViewHolder<*>>(
        _interface
    ) {

  override fun getItemViewType(position: Int) = itemsList()[position].type.typeId

  override fun getBinding(
    inflater: LayoutInflater,
    parent: ViewGroup,
    viewType: Int
  ) = when (TeamMembersRVAdapterItemType.byTypeId(viewType)) {
    AdminUser -> ViewTeamMemberAdminItemBinding.inflate(inflater, parent, false)
    SubUser -> ViewTeamMemberSubuserItemBinding.inflate(inflater, parent, false)
    Warning -> ViewNoTeamMemberBinding.inflate(inflater, parent,false)
    else -> ViewTeamMembersProgressItemBinding.inflate(inflater, parent, false)
  }

  override fun createVH(binding: ViewDataBinding) = when (binding) {
    is ViewTeamMemberAdminItemBinding -> TeamMembersAdminUserItemVH(binding)
    is ViewTeamMemberSubuserItemBinding -> TeamMembersSubUserItemVH(binding)
    is ViewNoTeamMemberBinding -> TeamWarningItemVH(binding)
    else -> TeamMembersProgressItemVH(binding as ViewTeamMembersProgressItemBinding)
  }

  override fun bindVH(
    holder: BaseViewHolder<*>,
    item: BaseTeamMembersRVAdapterItem<*>
  ) {
    when (holder) {
      is TeamMembersAdminUserItemVH -> holder.bind(item as TeamMemberAdminUserItem, _interface)
      is TeamMembersSubUserItemVH -> holder.bind(item as TeamMemberSubUserItem, _interface)
      is TeamMembersProgressItemVH -> holder.bind(item as TeamMembersProgressItem, _interface)
      is TeamWarningItemVH -> holder.bind(item as TeamWarningItem, _interface)
    }
  }

  /**
   *
   * Reset to empty state with progress bar
   *
   */
  fun resetStaticData() {
    mutableListOf<Pair<BaseTeamMembersRVAdapterItem<*>, DataRVAdapterOperationType>>().apply {
      add(Pair(TeamMembersProgressItem(), AddUpdate))
      items.filter { it.type == AdminUser || it.type == SubUser || it.type == Warning }
          .map { Pair(it, Remove) }
          .let {
            addAll(it)
          }
    }
        .let {
          operation(it)
        }
  }

}
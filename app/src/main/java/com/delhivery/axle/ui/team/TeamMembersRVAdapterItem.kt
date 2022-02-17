package com.delhivery.axle.ui.team

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.UserModel
import com.delhivery.axle.data.home.bids.HomeBidsProgressItemData
import com.delhivery.axle.data.teammembers.TeamMembersWarningItemData
import com.delhivery.axle.ui.team.TeamMembersRVAdapterItemType.SubUser
import com.delhivery.axle.ui.team.TeamMembersRVAdapterItemType.AdminUser
import com.delhivery.axle.ui.team.TeamMembersRVAdapterItemType.Progress
import com.google.firebase.dynamiclinks.ShortDynamicLink

/**
 * Created by Vibhor for Delhivery Pvt Ltd
 * on 30/12/20
 */

/**
 * RV item type for [TeamMembersRVAdapter]
 */
enum class TeamMembersRVAdapterItemType(val typeId: Int) {
  AdminUser(0),
  SubUser(1),
  Progress(2),
  Warning(3);


  companion object {
    /**
     * Get [TeamMembersRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().filter { typeId == it.typeId }.firstOrNull()
  }
}

/**
 * Base team member type adapter item
 */
abstract class BaseTeamMembersRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: TeamMembersRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Team Member admin user item
 */
class TeamMemberAdminUserItem(data: UserModel) :
    BaseTeamMembersRVAdapterItem<UserModel>(AdminUser, data)

/**
 * Team Member child user item
 */
class TeamMemberSubUserItem(data: UserModel) :
    BaseTeamMembersRVAdapterItem<UserModel>(SubUser, data)

/**
 * Team Member progress item
 */
class TeamMembersProgressItem(data: HomeBidsProgressItemData = HomeBidsProgressItemData()) :
    BaseTeamMembersRVAdapterItem<HomeBidsProgressItemData>(Progress, data)

/**
 * Team members warning item
 */
class TeamWarningItem(data: TeamMembersWarningItemData):
    BaseTeamMembersRVAdapterItem<TeamMembersWarningItemData>(TeamMembersRVAdapterItemType.Warning, data)
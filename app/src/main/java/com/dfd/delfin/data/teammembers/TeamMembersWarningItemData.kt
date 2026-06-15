package com.dfd.delfin.data.teammembers

import com.dfd.delfin.data.BaseKeyTypeModel

data class TeamMembersWarningItemData (
    val title: String,
    val subtitle: String,
    val actionLabel: String,
    val actionId: String
) : BaseKeyTypeModel<String>() {
    override fun key() = TeamWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val TeamWarningItemDataKeyPrefix = "warning_"

/* actions */
const val WarningAction_NoMembers = "no_members"
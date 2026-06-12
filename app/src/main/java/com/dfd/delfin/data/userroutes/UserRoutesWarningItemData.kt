package com.dfd.delfin.data.userroutes

import com.dfd.delfin.data.BaseKeyTypeModel

data class UserRoutesWarningItemData(
    val title: String,
    val subtitle: String,
    val actionLabel: String,
    val actionId: String
) : BaseKeyTypeModel<String>() {
    override fun key() = UserRouteWarningItemDataKeyPrefix + actionId
}

/* unique key for diff */
const val UserRouteWarningItemDataKeyPrefix = "warning_"

/* actions */
const val WarningAction_NoRoutes = "no_routes"

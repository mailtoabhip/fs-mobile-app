package com.dfd.delfin.data.home.loads

import com.dfd.delfin.data.BaseKeyTypeModel

data class HomeLoadsKycPendingItemData(
  val title: String = "KYC Pending!",
  val subtitle: String = "Please complete it to place your bid"
) : BaseKeyTypeModel<String>() {
  override fun key() = HomeLoadsKycPendingItemDataKey
}

private const val HomeLoadsKycPendingItemDataKey = "kyc_pending"

// actions
const val HomeLoadsKycPendingAction = "click_kyc_pending"


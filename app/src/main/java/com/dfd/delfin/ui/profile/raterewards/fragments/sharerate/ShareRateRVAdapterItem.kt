package com.dfd.delfin.ui.profile.raterewards.fragments.sharerate

import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.data.sharerates.ShareRateRoutesItemData
import com.dfd.delfin.data.sharerates.ShareRatesProgressItemData
import com.dfd.delfin.data.sharerates.ShareRatesTimeOutItemData
import com.dfd.delfin.data.sharerates.ShareRatesWarningItemData
//import com.delhivery.axle.database.entity.OffersEntity
import com.dfd.delfin.ui.profile.raterewards.fragments.sharerate.ShareRateRVAdapterItemType.Progress
import com.dfd.delfin.ui.profile.raterewards.fragments.sharerate.ShareRateRVAdapterItemType.ShareRate
import com.dfd.delfin.ui.profile.raterewards.fragments.sharerate.ShareRateRVAdapterItemType.Timeout
import com.dfd.delfin.ui.profile.raterewards.fragments.sharerate.ShareRateRVAdapterItemType.Warning


enum class ShareRateRVAdapterItemType(val typeId: Int) {
  ShareRate(0),
  Progress(1),
  Warning(2),
  Timeout(3);

  companion object {
    /**
     * Get [ShareRateRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().firstOrNull { typeId == it.typeId }
  }
}

/**
 * Base MyTrucks adapter item
 */
abstract class BaseShareRateRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: ShareRateRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * ShareRate data item
 */
class ShareRatesItem(data: ShareRateRoutesItemData) :
  BaseShareRateRVAdapterItem<ShareRateRoutesItemData>(ShareRate, data)

/**
 * Inline progress item
 */
class ShareRatesProgressItem(data: ShareRatesProgressItemData = ShareRatesProgressItemData()) :
  BaseShareRateRVAdapterItem<ShareRatesProgressItemData>(Progress, data)

/**
 * Warning/action item
 */
class ShareRatesWarningItem(data: ShareRatesWarningItemData) :
  BaseShareRateRVAdapterItem<ShareRatesWarningItemData>(Warning, data)

/**
 * Timeout item
 */
class ShareRatesTimeoutItem(data: ShareRatesTimeOutItemData) :
  BaseShareRateRVAdapterItem<ShareRatesTimeOutItemData>(Timeout, data)
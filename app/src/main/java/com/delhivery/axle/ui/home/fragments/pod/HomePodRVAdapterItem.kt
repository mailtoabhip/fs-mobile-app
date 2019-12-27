package com.delhivery.axle.ui.home.fragments.pod

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.bids.HomeBidsProgressItemData
import com.delhivery.axle.data.home.pod.HomePodChildItemData
import com.delhivery.axle.data.home.pod.HomePodHeaderItemData
import com.delhivery.axle.data.home.pod.HomePodParentItemData
import com.delhivery.axle.data.home.pod.HomePodSearchItemData
import com.delhivery.axle.data.home.pod.HomePodTimeOutItemData
import com.delhivery.axle.data.home.pod.HomePodWarningItemData
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Child
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Header
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Parent
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Progress
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Search
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Timeout
import com.delhivery.axle.ui.home.fragments.pod.HomePodRVAdapterItemType.Warning

/**
 * RV item type for [HomePodRVAdapter]
 */
enum class HomePodRVAdapterItemType(val typeId: Int) {
  Header(0),
  Search(1),
  Parent(2),
  Child(3),
  Warning(4),
  Progress(5),
  Timeout(6);

  companion object {
    /**
     * Get [HomePodRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().filter { typeId == it.typeId }.firstOrNull()
  }
}

/**
 * Base Home Pod type adapter item
 */
abstract class BaseHomePodRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: HomePodRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Pod Header item with my bids and confirmed bids
 */
class HomePodHeaderItem(
  data: HomePodHeaderItemData = HomePodHeaderItemData()
) : BaseHomePodRVAdapterItem<HomePodHeaderItemData>(Header, data)

/**
 * Pod Search item with live load requests
 */
class HomePodSearchItem(
  data: HomePodSearchItemData = HomePodSearchItemData()
) : BaseHomePodRVAdapterItem<HomePodSearchItemData>(Search, data)

/**
 * Pod parent request item
 */
class HomePodParentItem(data: HomePodParentItemData) :
    BaseHomePodRVAdapterItem<HomePodParentItemData>(Parent, data)

/**
 * Pod cfild request item
 */
class HomePodChildItem(data: HomePodChildItemData) :
    BaseHomePodRVAdapterItem<HomePodChildItemData>(Child, data)

/**
 * Pod Warning/action item
 */
class HomePodWarningItem(data: HomePodWarningItemData) :
    BaseHomePodRVAdapterItem<HomePodWarningItemData>(Warning, data)

/**
 * Pod Timeout item
 */
class HomePodTimeoutItem(data: HomePodTimeOutItemData) :
    BaseHomePodRVAdapterItem<HomePodTimeOutItemData>(Timeout, data)

/**
 * Pod Inline progress item
 */
class HomePodProgressItem(data: HomeBidsProgressItemData = HomeBidsProgressItemData()) :
    BaseHomePodRVAdapterItem<HomeBidsProgressItemData>(Progress, data)
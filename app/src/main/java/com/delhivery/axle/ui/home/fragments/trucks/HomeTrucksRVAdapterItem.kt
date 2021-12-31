package com.delhivery.axle.ui.home.fragments.trucks

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.trucks.*

enum class HomeTrucksRVAdapterItemType(val typeId: Int){
    Request(0),
    Progress(1),
    Search(2),
    Warning(3),
    Timeout(4),
    Info(5),
    MoreInfo(6),
    Filters(7);

    companion object {
        /**
         * Get [HomeTrucksRVAdapterItemType] by typeId
         */
        fun byTypeId(typeId: Int) = values().filter { typeId == it.typeId }.firstOrNull()
    }
}

/**
 * Base Home trucks type adapter item
 */
abstract class BaseHomeTrucksRVAdapterItem<D : BaseKeyTypeModel<String>>(
    val type: HomeTrucksRVAdapterItemType,
    val data: D
) : BaseKeyTypeModel<String>() {
    override fun key() = data.key()
}

/**
 * Truck request item
 */
class HomeTrucksRequestItem(data: HomeTrucksRequestItemData) :
    BaseHomeTrucksRVAdapterItem<HomeTrucksRequestItemData>(HomeTrucksRVAdapterItemType.Request, data)

/**
 * Inline progress item
 */
class HomeTrucksProgressItem(
    data: HomeTrucksProgressItemData = HomeTrucksProgressItemData()
) : BaseHomeTrucksRVAdapterItem<HomeTrucksProgressItemData>(HomeTrucksRVAdapterItemType.Progress, data)

/**
 * Search item with live truck requests
 */
class HomeTrucksSearchItem(
    data: HomeTrucksSearchItemData = HomeTrucksSearchItemData()
) : BaseHomeTrucksRVAdapterItem<HomeTrucksSearchItemData>(HomeTrucksRVAdapterItemType.Search, data)

/**
 * Warning/action item
 */
class HomeTrucksWarningItem(data: HomeTrucksWarningItemData) :
    BaseHomeTrucksRVAdapterItem<HomeTrucksWarningItemData>(HomeTrucksRVAdapterItemType.Warning, data)

/**
 * Timeout item
 */
class HomeTrucksTimeoutItem(data: HomeTrucksTimeoutItemData) :
    BaseHomeTrucksRVAdapterItem<HomeTrucksTimeoutItemData>(HomeTrucksRVAdapterItemType.Timeout, data)

/**
 * Inline Info item
 */
class HomeTrucksInfoItem(
    data: HomeTrucksInfoItemData = HomeTrucksInfoItemData()
) : BaseHomeTrucksRVAdapterItem<HomeTrucksInfoItemData>(HomeTrucksRVAdapterItemType.Info, data)


/**
 * Inline more info item
 */
class HomeTrucksMoreInfoItem(
    data: HomeTrucksMoreInfoItemData = HomeTrucksMoreInfoItemData(
        "To get more relevant loads - change your \n preferences here!"
    )
) : BaseHomeTrucksRVAdapterItem<HomeTrucksMoreInfoItemData>(HomeTrucksRVAdapterItemType.MoreInfo, data)

/**
 * Filter item
 */
class HomeTrucksFilterItem(
    data: HomeTrucksFilterItemData = HomeTrucksFilterItemData(
        false
    )
) : BaseHomeTrucksRVAdapterItem<HomeTrucksFilterItemData>(HomeTrucksRVAdapterItemType.Filters, data)
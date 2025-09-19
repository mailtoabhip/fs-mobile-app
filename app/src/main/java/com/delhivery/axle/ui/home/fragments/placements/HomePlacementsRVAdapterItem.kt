package com.delhivery.axle.ui.home.fragments.placements

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.home.placements.HomePlacementNoDelayItemData
import com.delhivery.axle.data.home.placements.HomePlacementsDurationItemData
import com.delhivery.axle.data.home.placements.HomePlacementsFilterItemData
import com.delhivery.axle.data.home.placements.HomePlacementsItemData
import com.delhivery.axle.data.home.placements.HomePlacementsProgressItemData
import com.delhivery.axle.data.home.placements.HomePlacementsTimeoutItemData
import com.delhivery.axle.data.home.placements.HomePlacementsTypeItemData
import com.delhivery.axle.data.home.placements.HomePlacementsWarningItemData

enum class HomePlacementsRVAdapterItemType(val typeId: Int) {
   PlacementItem(1),
    NonDelay(2),
    Duration(3),
    Header(4),
    Filters(5),
    Progress(6),
    Warning(7),
    Timeout(8);

    companion object {
        /**
         * Get [HomePlacementsRVAdapterItemType] by typeId
         */
        fun byTypeId(typeId: Int) = HomePlacementsRVAdapterItemType.values()
            .filter { typeId == it.typeId }.firstOrNull()
    }
}
enum class PlacementTypes {
    Delayed,
    MissingDetails,
    Expected
}

enum class LoadTypes {
    ftlAdhoc,
    ftlRegular,
    intracityAdhoc,
    intracityRegular,
    orionFixed,
    orionSpot
}
/**
 * Base Home Placements type adapter item
 */
abstract class BaseHomePlacementsRVAdapterItem<D : BaseKeyTypeModel<String>>(
    val type: HomePlacementsRVAdapterItemType,
    val data: D
) : BaseKeyTypeModel<String>() {
    override fun key() = data.key()
}

/**
 * Load request item
 */
class HomeVehiclePlacementsRequestItem(data: HomePlacementsItemData) :
    BaseHomePlacementsRVAdapterItem<HomePlacementsItemData>(HomePlacementsRVAdapterItemType.PlacementItem, data)
class HomePlacementsDurationItem(
    data: HomePlacementsDurationItemData = HomePlacementsDurationItemData()
) : BaseHomePlacementsRVAdapterItem<HomePlacementsDurationItemData>(HomePlacementsRVAdapterItemType.Duration, data)


class HomePlacementsNoDelayItem(
    data: HomePlacementNoDelayItemData = HomePlacementNoDelayItemData()
) : BaseHomePlacementsRVAdapterItem<HomePlacementNoDelayItemData>(HomePlacementsRVAdapterItemType.NonDelay, data)

class HomePlacementsTypeItem(
    data: HomePlacementsTypeItemData = HomePlacementsTypeItemData()
) : BaseHomePlacementsRVAdapterItem<HomePlacementsTypeItemData>(HomePlacementsRVAdapterItemType.Header, data)

/**
 * Inline progress item
 */
class HomePlacementsProgressItem(
    data: HomePlacementsProgressItemData = HomePlacementsProgressItemData()
) : BaseHomePlacementsRVAdapterItem<HomePlacementsProgressItemData>(HomePlacementsRVAdapterItemType.Progress, data)

/**
 * Warning/action item
 */
class HomePlacementsWarningItem(data: HomePlacementsWarningItemData) :
    BaseHomePlacementsRVAdapterItem<HomePlacementsWarningItemData>(HomePlacementsRVAdapterItemType.Warning, data)

/**
 * Timeout item
 */
class HomePlacementsTimeoutItem(data: HomePlacementsTimeoutItemData) :
    BaseHomePlacementsRVAdapterItem<HomePlacementsTimeoutItemData>(HomePlacementsRVAdapterItemType.Timeout, data)

/**
 * Filter item
 */
class HomePlacementsFilterItem(
    data: HomePlacementsFilterItemData = HomePlacementsFilterItemData(
        "", "0","0","0"
    )
) : BaseHomePlacementsRVAdapterItem<HomePlacementsFilterItemData>(HomePlacementsRVAdapterItemType.Filters, data)

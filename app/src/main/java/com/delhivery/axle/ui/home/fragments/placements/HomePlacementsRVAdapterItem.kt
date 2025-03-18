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
    IntracityContracts(0),
    IntracityAdhoc(1),
    IntercityContracts(2),
    IntercityAdhoc(3),
    NonDelay(4),
    Duration(5),
    Header(6),
    Filters(7),
    Progress(8),
    Warning(9),
    Timeout(10);

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
    intracityRegular
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
class HomePlacementsIntracityContractsRequestItem(data: HomePlacementsItemData) :
    BaseHomePlacementsRVAdapterItem<HomePlacementsItemData>(HomePlacementsRVAdapterItemType.IntracityContracts, data)

class HomePlacementsIntercityContractsRequestItem(data: HomePlacementsItemData) :
    BaseHomePlacementsRVAdapterItem<HomePlacementsItemData>(HomePlacementsRVAdapterItemType.IntercityContracts, data)

class HomePlacementsIntracityAdhocRequestItem(data: HomePlacementsItemData) :
    BaseHomePlacementsRVAdapterItem<HomePlacementsItemData>(HomePlacementsRVAdapterItemType.IntracityAdhoc, data)

class HomePlacementsIntercityAdhocRequestItem(data: HomePlacementsItemData) :
    BaseHomePlacementsRVAdapterItem<HomePlacementsItemData>(HomePlacementsRVAdapterItemType.IntercityAdhoc, data)

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

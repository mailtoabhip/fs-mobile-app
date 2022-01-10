package com.delhivery.axle.ui.searchCity

import com.delhivery.axle.data.BaseKeyTypeModel
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.search.SearchProgressItemData
import com.delhivery.axle.data.search.SearchTimeOutItemData
import com.delhivery.axle.data.search.SearchWarningItemData

enum class SearchCityRvAdapterItemType(val typeId: Int){
    CityItem(0),
    Warning(1),
    Progress(2),
    Timeout(3);


    companion object {
        /**
         * Get [SearchCityRvAdapterItemType] by typeId
         */
        fun byTypeId(typeId: Int) = values().firstOrNull { typeId == it.typeId }
    }
}

/**
 * Base City Item
 */
abstract class BaseCityRVAdapterItem<D : BaseKeyTypeModel<String>>(
    val type: SearchCityRvAdapterItemType,
    val data: D
) : BaseKeyTypeModel<String>() {
    override fun key() = data.key()
}

/**
 * Search item
 */
class SearchDataItem(data: CityModel) :
    BaseCityRVAdapterItem<CityModel>(SearchCityRvAdapterItemType.CityItem, data)

/**
 * Warning/action item
 */
class SearchWarningItem(data: SearchWarningItemData) :
    BaseCityRVAdapterItem<SearchWarningItemData>(SearchCityRvAdapterItemType.Warning, data)

/**
 * Timeout item
 */
class SearchTimeoutItem(data: SearchTimeOutItemData) :
    BaseCityRVAdapterItem<SearchTimeOutItemData>(SearchCityRvAdapterItemType.Timeout, data)

/**
 * Inline progress item
 */
class SearchProgressItem(data: SearchProgressItemData = SearchProgressItemData()) :
    BaseCityRVAdapterItem<SearchProgressItemData>(SearchCityRvAdapterItemType.Progress, data)
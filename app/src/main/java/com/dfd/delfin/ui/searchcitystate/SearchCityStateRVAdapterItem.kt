package com.dfd.delfin.ui.searchcitystate

import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.data.CityModel
import com.dfd.delfin.data.search.SearchProgressItemData
import com.dfd.delfin.data.search.SearchTimeOutItemData
import com.dfd.delfin.data.search.SearchWarningItemData

enum class SearchCityStateRvAdapterItemType(val typeId: Int){
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
abstract class BaseCityStateRVAdapterItem<D : BaseKeyTypeModel<String>>(
    val type: SearchCityStateRvAdapterItemType,
    val data: D
) : BaseKeyTypeModel<String>() {
    override fun key() = data.key()
}

/**
 * Search item
 */
class SearchDataItem(data: CityModel) :
    BaseCityStateRVAdapterItem<CityModel>(SearchCityStateRvAdapterItemType.CityItem, data)

/**
 * Warning/action item
 */
class SearchWarningItem(data: SearchWarningItemData) :
    BaseCityStateRVAdapterItem<SearchWarningItemData>(SearchCityStateRvAdapterItemType.Warning, data)

/**
 * Timeout item
 */
class SearchTimeoutItem(data: SearchTimeOutItemData) :
    BaseCityStateRVAdapterItem<SearchTimeOutItemData>(SearchCityStateRvAdapterItemType.Timeout, data)

/**
 * Inline progress item
 */
class SearchProgressItem(data: SearchProgressItemData = SearchProgressItemData()) :
    BaseCityStateRVAdapterItem<SearchProgressItemData>(SearchCityStateRvAdapterItemType.Progress, data)


package com.dfd.delfin.ui.searchload.fragments.searchresults

import com.dfd.delfin.data.BaseKeyTypeModel
import com.dfd.delfin.data.home.bids.HomeBidsProgressItemData
import com.dfd.delfin.data.home.bids.HomeBidsRequestItemData
import com.dfd.delfin.data.home.bids.HomeBidsSearchSpinnerItemData
import com.dfd.delfin.data.home.bids.HomeBidsWarningItemData
import com.dfd.delfin.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.ContractProgress
import com.dfd.delfin.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Contracts
import com.dfd.delfin.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.LoadProgress
import com.dfd.delfin.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Request
import com.dfd.delfin.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.SearchSpinner
import com.dfd.delfin.ui.searchload.fragments.searchresults.SearchResultsRVAdapterItemType.Warning

/**
 * RV item type for [SearchLoadsRVAdapter]
 */
enum class SearchResultsRVAdapterItemType(val typeId: Int) {
  Request(0),
  Contracts(1),
  SearchSpinner(2),
  Warning(3),
  LoadProgress(4),
  ContractProgress(5);

  companion object {
    /**
     * Get [SearchResultsRVAdapterItemType] by typeId
     */
    fun byTypeId(typeId: Int) = values().filter { typeId == it.typeId }.firstOrNull()
  }
}

/**
 * Base Home search type adapter item
 */
abstract class BaseSearchLoadsRVAdapterItem<D : BaseKeyTypeModel<String>>(
  val type: SearchResultsRVAdapterItemType,
  val data: D
) : BaseKeyTypeModel<String>() {
  override fun key() = data.key()
}

/**
 * Search request item
 */
class SearchLoadsRequestItem(data: HomeBidsRequestItemData) :
    BaseSearchLoadsRVAdapterItem<HomeBidsRequestItemData>(Request, data)

/**
 * Inline progress item
 */
class SearchContractsProgressItem(
  data: HomeBidsProgressItemData = HomeBidsProgressItemData()
) : BaseSearchLoadsRVAdapterItem<HomeBidsProgressItemData>(ContractProgress, data)

/**
 * Inline progress item
 */
class SearchLoadsProgressItem(
  data: HomeBidsProgressItemData = HomeBidsProgressItemData()
) : BaseSearchLoadsRVAdapterItem<HomeBidsProgressItemData>(LoadProgress, data)

/**
 * Search contracts item
 */
class SearchContractsRequestItem(data: HomeBidsRequestItemData) :
    BaseSearchLoadsRVAdapterItem<HomeBidsRequestItemData>(Contracts, data)

/**
 * Search load screen dummy view
 */
class SearchLoadsSearchSpinnerItem(data: HomeBidsSearchSpinnerItemData = HomeBidsSearchSpinnerItemData()) :
    BaseSearchLoadsRVAdapterItem<HomeBidsSearchSpinnerItemData>(SearchSpinner, data)

/**
 * Search warning item
 */
class SearchLoadsWarningItem(data: HomeBidsWarningItemData) :
    BaseSearchLoadsRVAdapterItem<HomeBidsWarningItemData>(Warning, data)

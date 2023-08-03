package com.delhivery.axle.ui.searchload.fragments

import com.delhivery.axle.data.CityModel
import com.delhivery.axle.ui.searchload.fragments.SearchLoadFragmentActionType.Search

/**
 * Search load fragment actions type
 */
enum class SearchLoadFragmentActionType {
  Progress,
  Search
}

/**
 * Search load fragment action base class
 */
abstract class BaseSearchLoadFragmentAction(val type: SearchLoadFragmentActionType)

/**
 * Show/hide inline - progress
 */
class ProgressSearchLoadAction(
  val show: Boolean,
  val title: String = "Searching loads",
  val message: String = "This usually takes few seconds to load. please be patient.",
  val protip: String = "Some tip regarding how to bid, or whats to be considered while bidding."
) : BaseSearchLoadFragmentAction(
    SearchLoadFragmentActionType.Progress
)

/**
 * Search Load action
 */
class SearchLoadAction(
  val originCity: CityModel,
  val destinationCity: CityModel?,
  val truckType: String?,
  val truckDisplayName: String?,
  val status: String?,
  val requestType:String?,
  val contractType:String?,
  val truckDisplayNames: ArrayList<String>,
  val saveToHistory: Boolean = false,
  val isFlexible:Boolean?=null,
  val isNewVersion:Boolean?=null
) : BaseSearchLoadFragmentAction(Search)
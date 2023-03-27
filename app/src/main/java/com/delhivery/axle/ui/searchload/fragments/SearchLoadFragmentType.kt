package com.delhivery.axle.ui.searchload.fragments

import com.delhivery.axle.ui.searchload.fragments.searchload.SearchLoadFragment
import com.delhivery.axle.ui.searchload.fragments.searchresults.IntracitySearchResultsFragment
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsFragment

/**
 * Search load fragment type
 */
enum class  SearchLoadFragmentType(
  val fragment: SearchLoadBaseFragment<*, *>,
  val title: String
) {
  LoadFragment(SearchLoadFragment._instance, "Search load"),
  IntracityLoadFragment(IntracitySearchLoadFragment._instance,"Search contract"),
  ResultsFragment(SearchResultsFragment._instance, "Search results"),
  IntracityResultsFragment(IntracitySearchResultsFragment._instance,"Search results")
}
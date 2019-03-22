package com.delhivery.orion.ui.searchload.fragments

import com.delhivery.orion.ui.searchload.fragments.searchload.SearchLoadFragment
import com.delhivery.orion.ui.searchload.fragments.searchresults.SearchResultsFragment

/**
 * Search load fragment type
 */
enum class SearchLoadFragmentType(
  val fragment: SearchLoadBaseFragment<*, *>,
  val title: String
) {
  LoadFragment(SearchLoadFragment._instance, "Search load"),
  ResultsFragment(SearchResultsFragment._instance, "Search results")
}
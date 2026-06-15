package com.dfd.delfin.ui.searchload.fragments

import com.dfd.delfin.ui.searchload.fragments.searchload.SearchLoadFragment
import com.dfd.delfin.ui.searchload.fragments.searchresults.SearchResultsFragment

/**
 * Search load fragment type
 */
enum class  SearchLoadFragmentType(
  val fragment: SearchLoadBaseFragment<*, *>,
  val title: String
) {
  LoadFragment(SearchLoadFragment._instance, "Search load"),
  ResultsFragment(SearchResultsFragment._instance, "Search results")
}
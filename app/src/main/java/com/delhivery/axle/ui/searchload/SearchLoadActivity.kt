package com.delhivery.axle.ui.searchload

import android.os.Bundle
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivitySearchLoadBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.searchload.fragments.BaseSearchLoadFragmentAction
import com.delhivery.axle.ui.searchload.fragments.ProgressSearchLoadAction
import com.delhivery.axle.ui.searchload.fragments.SearchLoadAction
import com.delhivery.axle.ui.searchload.fragments.SearchLoadFragmentActionType.Progress
import com.delhivery.axle.ui.searchload.fragments.SearchLoadFragmentActionType.Search
import com.delhivery.axle.ui.searchload.fragments.SearchLoadFragmentType
import com.delhivery.axle.ui.searchload.fragments.SearchLoadFragmentType.LoadFragment
import com.delhivery.axle.ui.searchload.fragments.SearchLoadFragmentType.ResultsFragment
import com.delhivery.axle.ui.searchload.fragments.searchresults.SearchResultsFragment

class SearchLoadActivity : BaseActivity<ActivitySearchLoadBinding, SearchLoadViewModel>() {

  override fun getViewModelClass() = SearchLoadViewModel::class.java

  override fun layoutId() = R.layout.activity_search_load

  override fun requireConnection() = true

  /* current Fragment type */
  private var currentFragmentType: SearchLoadFragmentType? = null

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Search Load"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    /* start with load fragment */
    navigate(LoadFragment)
  }

  /**
   * Navigate to [SearchLoadFragmentType] fragment
   */
  private fun navigate(fragmentType: SearchLoadFragmentType) {
    if (currentFragmentType == fragmentType) return
    currentFragmentType = fragmentType
    navigationUtils.addReplaceFragment(
        R.id.container, fragmentType.fragment, SearchLoadFragmentTag
    )
    title = currentFragmentType?.title
  }

  /**
   * Fragment action observer
   */
  fun fragmentAction(action: BaseSearchLoadFragmentAction) {
    when (action.type) {
      /* shoe/hide progress */
      Progress -> {
        (action as ProgressSearchLoadAction).apply {
          if (show) {
            uiUtils.showDelhiveryProgress(action.title, action.message, action.protip)
          } else {
            uiUtils.hideDelhiveryProgress()
          }
        }
      }
      Search -> {
        (action as SearchLoadAction).apply {
          /* navigate to search results fragment */
          navigate(ResultsFragment)
          /* search query */
          (ResultsFragment.fragment as SearchResultsFragment).search(
              originCity, destinationCity, truckType, saveToHistory, false
          )
        }
      }
    }
  }

  override fun onBackPressed() {
    when (currentFragmentType) {
      ResultsFragment -> navigate(LoadFragment)
      else -> super.onBackPressed()
    }
  }
}

/* Search load fragment tag */
private const val SearchLoadFragmentTag = "search_load_fragment_tag"
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
import com.delhivery.axle.utils.EVENT_NAVIGATION_PODS
import com.delhivery.axle.utils.EVENT_SEARCH_DETAILS_SUBMIT
import com.delhivery.axle.utils.PROPERTY_SEARCH_BODY_TYPE
import com.delhivery.axle.utils.PROPERTY_SEARCH_DESTINATION_CITY
import com.delhivery.axle.utils.PROPERTY_SEARCH_ORIGIN_CITY
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

/**
 * Search load screen
 */
class SearchLoadActivity : BaseActivity<ActivitySearchLoadBinding, SearchLoadViewModel>() {

  @Inject lateinit var userPrefs: UserPrefs
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
          analyticsUtil.moEngageTrackEvent(
              EVENT_SEARCH_DETAILS_SUBMIT,
              mutableListOf(PROPERTY_SEARCH_ORIGIN_CITY, PROPERTY_SEARCH_DESTINATION_CITY,
                  PROPERTY_SEARCH_BODY_TYPE),
              mutableListOf( originCity.cityName() ?: "Anywhere",
                  destinationCity?.cityName() ?: "Anywhere",truckType)
          )
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
    userPrefs.setPreviousScreen(this.javaClass.name)
    when (currentFragmentType) {
      ResultsFragment -> navigate(LoadFragment)
      else -> super.onBackPressed()
    }
  }
}

/* Search load fragment tag */
private const val SearchLoadFragmentTag = "search_load_fragment_tag"
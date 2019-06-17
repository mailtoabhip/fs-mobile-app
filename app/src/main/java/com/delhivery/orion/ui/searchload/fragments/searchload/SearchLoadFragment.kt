package com.delhivery.orion.ui.searchload.fragments.searchload

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.data.CityModel
import com.delhivery.orion.database.entity.SearchLoadHistoryEntity
import com.delhivery.orion.databinding.FragmentSearchLoadBinding
import com.delhivery.orion.databinding.ViewSearchLoadHistoryItemBinding
import com.delhivery.orion.ui.custom.AnimationType.RevealOpen
import com.delhivery.orion.ui.searchload.fragments.ProgressSearchLoadAction
import com.delhivery.orion.ui.searchload.fragments.SearchLoadAction
import com.delhivery.orion.ui.searchload.fragments.SearchLoadBaseFragment
import com.delhivery.orion.utils.AutoCompleteUtils
import com.delhivery.orion.utils.extensions.setup
import com.delhivery.orion.utils.extensions.visible
import com.github.florent37.kotlin.pleaseanimate.please
import javax.inject.Inject

class SearchLoadFragment : SearchLoadBaseFragment<FragmentSearchLoadBinding, SearchLoadFragmentViewModel>() {

  companion object {
    /* singleton instance */
    val _instance: SearchLoadFragment by lazy { SearchLoadFragment() }
  }

  override fun getViewModelClass() = SearchLoadFragmentViewModel::class.java

  override fun layoutId() = R.layout.fragment_search_load

  @Inject lateinit var autoCompleteUtils: AutoCompleteUtils

  private var origin: CityModel? = null
  private var destination: CityModel? = null

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    /* animate open */
    binding.arcView.animate(RevealOpen) {
      please {
        animate(binding.containerHistory) toBe {
          visible()
        }
        animate(binding.textHistoryTitle) toBe {
          visible()
        }
        animate(binding.containerSearchLoadHeaderForm) toBe {
          visible()
        }
      }.setStartDelay(200)
          .start()

      setupSearchScreen()
    }
  }

  override fun onPause() {
    super.onPause()
    uiUtils.toggleKeyboard()
  }

  private fun setupSearchScreen() {
    autoCompleteUtils.autoCompleteCity(binding.editOriginCity) {
      origin = it
      binding.editDestinationCity.requestFocus()
    }

    autoCompleteUtils.autoCompleteCity(binding.editDestinationCity) {
      destination = it
      uiUtils.toggleKeyboard()
    }

    /* truck type */
    binding.spinnerTruckType.setup(R.array.array_truck_type) { p, v ->

    }

    /* submit */
    binding.btnAction.setOnClickListener {
      searchLoad()
    }

    /* reverse origin/destination cities */
    binding.imgForward.setOnClickListener {
      please {
        animate(it) toBe {
          toBeRotated(180f)
        }
      }.thenCouldYou {
        please {
          animate(it) toBe {
            originalRotation()
          }
        }.withEndAction {
          val _origin = binding.editOriginCity.text.toString()
          binding.editOriginCity.setText(binding.editDestinationCity.text.toString())
          binding.editDestinationCity.setText(_origin)
        }
            .setStartDelay(300)
            .start()
      }
          .start()
    }

    /* init */
    initObservers()
  }

  /**
   * Search load as per user selections
   */
  private fun searchLoad(
    saveToHistory: Boolean = true,
    origin: CityModel? = this@SearchLoadFragment.origin,
    destination: CityModel? = this@SearchLoadFragment.destination
  ) {
    uiUtils.toggleKeyboard(true)

    if (origin == null) {
      binding.editOriginCity.setError("Please select origin")
      binding.editOriginCity.errorAnimate()
      return
    }

    /* form data */
    val type = binding.spinnerTruckType.selectedItem.toString()

    /* save to history if needed */
    if (saveToHistory) {
      viewModel.saveToHistory(
          origin, destination ?: CityModel(
          "", "",
          "", "", "", ""
      ), type
      )
    }
    /* searching progress */
    action(ProgressSearchLoadAction(true))

    /* delay and search for better UX */
    Handler().postDelayed({
      action(SearchLoadAction(origin, destination, type))
    }, 200)
  }

  private fun validate() = origin != null

  /**
   * init observers
   */
  private fun initObservers() {
    /* observe live data for search history */
    viewModel.searchLoadHistoryLiveData()
        .observe(this, SearchLoadHistoryObserver())
  }

  /**
   * Search load history observer
   */
  inner class SearchLoadHistoryObserver : Observer<List<SearchLoadHistoryEntity>> {
    override fun onChanged(t: List<SearchLoadHistoryEntity>?) {
      t?.let { items ->
        binding.containerHistory.removeAllViews()
        items.forEachIndexed { index, item ->
          val itemBinding = historyItemBinding()
          itemBinding.data = item
          itemBinding.root.setOnClickListener {
            searchLoad(false, item.originCity, item.destinationCity)
          }
          itemBinding.root.setOnLongClickListener {
            viewModel.deleteSearchResult(item)
            true
          }
          binding.containerHistory.addView(itemBinding.root, index)
        }
      }
      /* title as per search results */
      binding.textHistoryTitle.visible(t != null && t.isNotEmpty())
    }
  }

  /**
   * Create new history item binding
   */
  private fun historyItemBinding() = ViewSearchLoadHistoryItemBinding.inflate(
      layoutInflater, binding.containerHistory, false
  )
}
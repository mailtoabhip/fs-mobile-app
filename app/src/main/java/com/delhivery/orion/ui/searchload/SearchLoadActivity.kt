package com.delhivery.orion.ui.searchload

import android.arch.lifecycle.Observer
import android.os.Bundle
import com.delhivery.orion.R
import com.delhivery.orion.database.entity.SearchLoadHistoryEntity
import com.delhivery.orion.databinding.ActivitySearchLoadBinding
import com.delhivery.orion.databinding.ViewSearchLoadHistoryItemBinding
import com.delhivery.orion.ui.base.BaseActivity
import com.delhivery.orion.ui.custom.AnimationType.RevealOpen
import com.delhivery.orion.utils.extensions.setup
import com.delhivery.orion.utils.extensions.visible
import com.github.florent37.kotlin.pleaseanimate.please

class SearchLoadActivity : BaseActivity<ActivitySearchLoadBinding, SearchLoadViewModel>() {
  override fun getViewModelClass() = SearchLoadViewModel::class.java

  override fun layoutId() = R.layout.activity_search_load

  override fun requireConnection() = true

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    /* setup toolbar */
    setSupportActionBar(binding.toolbar)
    title = "Search Load"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    binding.arcView.animate(RevealOpen) {
      setupSearchScreen()
    }

    /* init */
    initObservers()
  }

  /**
   * init observers
   */
  private fun initObservers() {
    /* observe live data for search history */
    viewModel.searchLoadHistoryLiveData()
        .observe(this, SearchLoadHistoryObserver())
  }

  private fun setupSearchScreen() {
    /* truck type */
    binding.spinnerTruckType.setup(R.array.array_truck_type) { p, v ->

    }

    /* truck size */
    binding.spinnerTruckSize.setup(R.array.array_truck_size) { p, v ->

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
  }

  /**
   * Search load as per user selections
   */
  private fun searchLoad(saveToHistory: Boolean = true) {
    uiUtils.toggleKeyboard(true)
    viewModel.searchLoad(
        binding.editOriginCity.text.toString(), binding.editDestinationCity.text.toString(),
        binding.spinnerTruckType.selectedItem.toString(),
        binding.spinnerTruckSize.selectedItem.toString(),
        saveToHistory
    )
  }

  /**
   * Search load history observer
   */
  inner class SearchLoadHistoryObserver : Observer<List<SearchLoadHistoryEntity>> {
    override fun onChanged(t: List<SearchLoadHistoryEntity>?) {
      t?.let { items ->
        binding.containerHistory.removeAllViews()
        items.forEachIndexed { index, item ->
          val itemBinding = ViewSearchLoadHistoryItemBinding.inflate(
              layoutInflater, binding.containerHistory, false
          )
          itemBinding.data = item
          itemBinding.root.setOnClickListener {
            //            searchLoad(false)
            viewModel.deleteSearchResult(item)
          }
          binding.containerHistory.addView(itemBinding.root, index)
        }
      }
      /* title as per search results */
      binding.textHistoryTitle.visible(t != null && t.isNotEmpty())
    }

  }
}
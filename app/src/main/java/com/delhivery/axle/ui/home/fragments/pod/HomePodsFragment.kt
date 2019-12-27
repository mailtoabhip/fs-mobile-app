package com.delhivery.axle.ui.home.fragments.pod

import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.R
import com.delhivery.axle.data.home.pod.HomePodChildAction
import com.delhivery.axle.data.home.pod.HomePodParentAction
import com.delhivery.axle.data.home.pod.HomePodParentItemData
import com.delhivery.axle.databinding.FragmentHomePodBinding
import com.delhivery.axle.ui.custom.DelhiveryAnimatedSearchBar.ToolbarElevationChangeListener
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment
import com.delhivery.axle.ui.home.fragments.pod.GenreDataFactory.makeGenres
import com.delhivery.axle.utils.DialogUtils
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class HomePodsFragment : HomeBaseFragment<FragmentHomePodBinding, HomePodViewModel>(),
    HomePodRVAdapterInterface, ToolbarElevationChangeListener {

  var _title: String = "Pods"

  override val title: CharSequence
    get() = _title

  init {
    toolbarElevationLiveData = MutableLiveData()
    hasInlineProgress = true
  }

  private val MINIMUM = 25
  var scrollDist = 0
  var visible = false

  @Inject lateinit var dialogUtils: DialogUtils
  @Inject lateinit var userPrefs: UserPrefs

  init {
    toolbarElevationLiveData = MutableLiveData()
    hasInlineProgress = true
  }

  companion object {
    /* singleton instance */
    val _instance: HomePodsFragment by lazy { HomePodsFragment() }
  }

  override fun getViewModelClass() = HomePodViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_pod

  /* RV adapter */
  private val adapter: HomePodRVAdapter by lazy {
    HomePodRVAdapter(this)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    binding.rvPod.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
      adapter = this@HomePodsFragment.adapter
//      addOnScrollListener(HomeBidsRVScrollListener(binding.editStickySearch))
//      addOnScrollListener(PaginationInterface())
    }

    binding.refreshLayout.setOnRefreshListener {
      binding.refreshLayout.isRefreshing = false
      refreshData()
    }

    refreshData()
  }

  private fun refreshData() {
    val list = makeGenres()
    val converted = mutableListOf<BaseHomePodRVAdapterItem<*>>()
    converted.add(HomePodHeaderItem())
    converted.add(HomePodSearchItem())
    for (itemData in list) {
      converted.add(HomePodParentItem(itemData))
    }
    adapter.setItems(converted)
  }

  override fun handleAction(
    actionId: String,
    position: Int,
    item: BaseHomePodRVAdapterItem<*>
  ) {
    // handle actions here
    when (actionId) {
      HomePodParentAction -> {
        val data = item.data as HomePodParentItemData
        adapter.toggle(position, data)
      }
      HomePodChildAction -> {

      }

    }
  }

  override fun postElevation(elevation: Float) {
    toolbarElevationLiveData!!.postValue(elevation)
  }

}
package com.delhivery.orion.ui.home.fragments.bids

import android.arch.lifecycle.MutableLiveData
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.databinding.FragmentHomeBidsBinding
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.orion.ui.custom.DelhiveryFabCardMenuItem
import com.delhivery.orion.ui.home.fragments.HomeBaseFragment

class HomeBidsFragment : HomeBaseFragment<FragmentHomeBidsBinding, HomeBidsViewModel>(),
    ItemClickListener<BaseHomeBidsRVAdapterItem<*>> {

  init {
    toolbarElevationLiveData = MutableLiveData()
  }

  companion object {
    /* singleton instance */
    val _instance: HomeBidsFragment by lazy { HomeBidsFragment() }
  }

  override fun getViewModelClass() = HomeBidsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_bids

  /* RV adapter */
  private val adapter: HomeBidsRVAdapter by lazy {
    HomeBidsRVAdapter(this)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    /* setup recycler view */
    binding.rvBids.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@HomeBidsFragment.adapter
      addOnScrollListener(HomeBidsRVAdapterScrollListener(binding.editStickySearch))
    }

    adapter.setItems(getDummyData())

    /* Use this logic to create our own menu as per  */
    binding.fabSort.setOnClickListener { fab ->
      uiUtils.fabCardMenu(fab as FloatingActionButton, HomeBidsFabCardMenuItems) {
        onFabMenuItemSelected(it)
      }
    }
  }

  private fun getDummyData() = mutableListOf<BaseHomeBidsRVAdapterItem<*>>().apply {
    add(0, HomeBidsHeaderItem())
    add(1, HomeBidsSearchItem())
    for (i in 0..50) {
      add(HomeBidsRequestItem())
    }
  }

  override fun onItemClicked(item: BaseHomeBidsRVAdapterItem<*>) {
    /* Literally most useless function here, remove it asap */
  }

  private fun onFabMenuItemSelected(item: DelhiveryFabCardMenuItem) {
    /* todo - handle sorting here */
  }

  /**
   * Home bids rv adapter scroll listener for search bar animation related stuff
   */
  inner class HomeBidsRVAdapterScrollListener(
    private val stickyView: View,
    private val elevation: Float = 12f
  ) : OnScrollListener() {
    /* Current toolbar elevation */
    private var toolbarElevation = -1f

    override fun onScrolled(
      recyclerView: RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)

      val layoutManager = (recyclerView.layoutManager as LinearLayoutManager)

      val pos = layoutManager.findFirstVisibleItemPosition()
      val viewVisibility = if (pos >= 1) {
        val _toolbarElevation = if (pos == 1) {
          val childView = recyclerView.findViewHolderForAdapterPosition(1)!!.itemView

          val viewTopGap = childView.height - stickyView.height * 1f
          val viewTop = childView.top + viewTopGap
          if (viewTop > 0) {
            val factor = viewTop / viewTopGap
            val invFactor = 1f - factor
            stickyView.translationY = viewTop
            stickyView.alpha = invFactor
            ViewCompat.setElevation(stickyView, elevation * invFactor)
            factor * defToolbarElevation
          } else {
            stickyView.translationY = stickyView.top * 1f
            stickyView.alpha = 1f
            ViewCompat.setElevation(stickyView, elevation)
            0f
          }
        } else {
          stickyView.translationY = 0f
          stickyView.alpha = 1f
          0f
        }
        if (_toolbarElevation != toolbarElevation) {
          toolbarElevation = _toolbarElevation
          toolbarElevationLiveData!!.postValue(toolbarElevation)
        }
        View.VISIBLE
      } else {
        if (toolbarElevation != defToolbarElevation) {
          toolbarElevation = defToolbarElevation
          toolbarElevationLiveData!!.postValue(toolbarElevation)
        }
        View.GONE
      }
      if (stickyView.visibility != viewVisibility) {
        if (stickyView.visibility == View.GONE) {
          binding.fabSort.hide()
        } else {
          binding.fabSort.show()
        }
        uiUtils.toggleKeyboard()
        stickyView.visibility = viewVisibility
      }
    }
  }
}
package com.delhivery.orion.ui.home.fragments.trips

import android.arch.lifecycle.MutableLiveData
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.View
import com.delhivery.orion.R
import com.delhivery.orion.databinding.FragmentHomeTripsBinding
import com.delhivery.orion.ui.base.adapter.BaseDataRVAdapter.ItemClickListener
import com.delhivery.orion.ui.home.fragments.HomeBaseFragment

class HomeTripsFragment : HomeBaseFragment<FragmentHomeTripsBinding, HomeTripsViewModel>(),
    ItemClickListener<BaseHomeTripsRVAdapterItem<*>> {

  init {
    toolbarElevationLiveData = MutableLiveData()
  }

  companion object {
    /* singleton instance */
    val _instance: HomeTripsFragment by lazy { HomeTripsFragment() }
  }

  /* RV adapter */
  private val adapter: HomeTripsRVAdapter by lazy {
    HomeTripsRVAdapter(this)
  }

  override fun getViewModelClass() = HomeTripsViewModel::class.java

  override fun layoutId() = R.layout.fragment_home_trips

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    /* setup recycler view */
    binding.rvTrips.apply {
      layoutManager = LinearLayoutManager(context)
      adapter = this@HomeTripsFragment.adapter
//      addOnScrollListener(HomeTripsRVScrollListener(binding.editStickySearch))
    }

    adapter.setItems(getDummyData())

    /* fab menu */
    binding.fabFilter.setOnClickListener { fab ->
      uiUtils.fabCardMenu(fab as FloatingActionButton, HomeTripsFabCardMenuItems) {
        /* handle filter type here */
      }
    }
  }

  private fun getDummyData() = mutableListOf<BaseHomeTripsRVAdapterItem<*>>().apply {
    add(0, HomeTripsSearchItem())
    for (i in 0..50) {
      add(HomeTripsItem())
    }
  }

  override fun onItemClicked(item: BaseHomeTripsRVAdapterItem<*>) {

  }

  inner class HomeTripsRVScrollListener(
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
      if (pos >= 1) {
        stickyView.alpha = 1f
//        please {
//          animate(stickyView) {
//            originalPosition()
//          }
//        }.start()
//        if (stickyView.translationY < 0) {
//        stickyView.animate()
//            .translationY(0f)
//            .start()
//        }
        toolbarElevationLiveData!!.postValue(0f)
      } else {
        val searchView = recyclerView.findViewHolderForAdapterPosition(0)!!.itemView
        val factor =
          (searchView.height.toFloat() - searchView.bottom.toFloat()) / searchView.height.toFloat()
        stickyView.alpha = factor
//        stickyView.alpha = 0f
//        if (stickyView.translationY == 0f) {
//        stickyView.animate()
//            .translationY(-stickyView.height.toFloat())
//            .start()
//        please {
//          animate(stickyView) {
//            outOfScreen(Gravity.TOP)
//          }
//        }.start()
//        }
        toolbarElevationLiveData!!.postValue((1 - factor) * elevation)
      }
    }
  }
}
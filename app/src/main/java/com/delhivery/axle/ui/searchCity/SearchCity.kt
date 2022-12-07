package com.delhivery.axle.ui.searchCity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.delhivery.axle.R
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.CitySelected
import com.delhivery.axle.data.search.SearchTimeOutAction
import com.delhivery.axle.data.search.SearchWarningAction_NoResult
import com.delhivery.axle.database.entity.SearchCityEntity
import com.delhivery.axle.database.entity.SearchLoadHistoryEntity
import com.delhivery.axle.databinding.ActivitySearchCityBinding
import com.delhivery.axle.databinding.ViewSearchCityItemBinding
import com.delhivery.axle.databinding.ViewSearchLoadHistoryItemBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.searchongoingtrip.SearchOngoingTripRVAdapter
import com.delhivery.axle.utils.REQCODE_SELECT_CITY
import com.delhivery.axle.utils.extensions.visible
import com.delhivery.axle.utils.prefs.UserPrefs
import javax.inject.Inject

class SearchCity : BaseActivity<ActivitySearchCityBinding, SearchCityViewModel>() ,
  SearchCityRVAdapterInterface{

    init {
        hasInlineProgress = true
    }

    override fun getViewModelClass() = SearchCityViewModel::class.java

    override fun layoutId()= R.layout.activity_search_city

    override fun requireConnection()= true

    var isLoadingData = true

    @Inject lateinit var userPrefs: UserPrefs

    init {
        hasInlineProgress = true
    }

    private val adapter by lazy { SearchCityRvAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.cityType = intent.getStringExtra(CityType) ?: ""
        viewModel.fromDialog = intent.getBooleanExtra(FromDialog, false)
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)
        title = if(viewModel.cityType == "origin") "Search Origin City" else "Search Destination City"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.rvCityItems.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@SearchCity.adapter

        }

        viewModel.searchLiveData.observe(this, Observer {
            adapter.resetStaticData()
            if (it != null) {
                adapter.operation(it)
            }
        })

        binding.editQuery.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s != null) {
                    binding.historyLayout.visibility= View.GONE
                    try {
                        viewModel.searchText = s.trim().toString()
                        Log.d("prefix", s.trim().toString())

                        if (viewModel.searchText.length in 3..10) {
                            refreshData()
                        } else {
                            adapter.resetStaticData()
                        }
                    }  catch (e: Exception) {
                        binding.editQuery.error = e.message
                    }
                }
            }
        })

        if (viewModel.searchText.length in 3..10) {
            refreshData()
        }

        initObservers()
    }

    /**
     * init observers
     */
    private fun initObservers() {
        /* observe live data for search history */
        viewModel.searchCityHistoryLiveData()
            .observe(this, SearchCityHistoryObserver() )
    }

    /**
     * Search load history observer
     */
    inner class SearchCityHistoryObserver : Observer<List<SearchCityEntity>> {
        override fun onChanged(t: List<SearchCityEntity>?) {
            t?.let { items ->
                binding.containerHistory.removeAllViews()
                items.forEachIndexed { index, item ->
                    val itemBinding = historyItemBinding()
                    itemBinding.request = item.originCity
                    itemBinding.root.setOnClickListener {
                        val data = item.originCity as CityModel
                        if(viewModel.fromDialog){
                            LocalBroadcastManager.getInstance(this@SearchCity)
                                .sendBroadcast(Intent("get_selected_city").apply {
                                    putExtra("City",data)
                                    putExtra(CityType,viewModel.cityType)
                                }
                                )
                        }
                        else {
                            setResult(REQCODE_SELECT_CITY, Intent().apply {
                                putExtra("City", data)
                                putExtra(CityType, viewModel.cityType)
                            })
                        }
                        finish()
                    }
                    itemBinding.root.setOnLongClickListener {
                        viewModel.deleteCity(item)
                        true
                    }
                    binding.containerHistory.addView(itemBinding.root, index)
                }
            }
            /* title as per search results */
            binding.textHistoryTitle.visible(!t.isNullOrEmpty())
        }
    }

    override fun handleAction(
        actionId: String,
        item: BaseCityRVAdapterItem<*>) {
        when(actionId){
            CitySelected -> {
                val data = item.data as CityModel
                viewModel.saveToHistory(data)
                if(viewModel.fromDialog){
                    LocalBroadcastManager.getInstance(this@SearchCity)
                        .sendBroadcast(Intent("get_selected_city").apply {
                            putExtra("City",data)
                            putExtra(CityType,viewModel.cityType)
                        }
                        )
                }
                else {
                    setResult(REQCODE_SELECT_CITY, Intent().apply {
                        putExtra("City", data)
                        putExtra(CityType, viewModel.cityType)
                    })
                }
                finish()
            }
            SearchWarningAction_NoResult -> {
                refreshData()
            }

            SearchTimeOutAction -> {
                refreshData()
            }
        }
    }

    private fun refreshData() {
        adapter.resetStaticData()
        viewModel.searchCity(viewModel.searchText)
    }

    /**
     * Create new history item binding
     */
    private fun historyItemBinding() = ViewSearchCityItemBinding.inflate(
        layoutInflater, binding.containerHistory, false
    )
}


private const val FromDialog = "from_dialog"
private const val CityType = "city_type"

/**
 * Search City Intent
  */
fun searchCityIntent(
    context: Context,
    cityType: String,
    fromDialog: Boolean = false
) = Intent( context, SearchCity::class.java).apply {
    putExtra(CityType, cityType)
    putExtra(FromDialog, fromDialog)
}
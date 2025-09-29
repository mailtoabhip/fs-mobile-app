package com.delhivery.axle.ui.searchcitystate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.data.CityModel
import com.delhivery.axle.data.CitySelected
import com.delhivery.axle.data.search.SearchTimeOutAction
import com.delhivery.axle.data.search.SearchWarningAction_NoResult
import com.delhivery.axle.databinding.ActivitySearchOriginCityBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.REQCODE_DESTINATION_SELECT_CITY
import com.delhivery.axle.utils.REQCODE_SELECT_CITY
import com.delhivery.axle.utils.WindowInsetsUtils
import com.delhivery.axle.utils.extensions.focusClick
import com.delhivery.axle.utils.extensions.getQueryTextChangeObservable
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchOriginCityActivity : BaseActivity<ActivitySearchOriginCityBinding, SearchCityStateViewModel>() ,
    SearchCityStateRVAdapterInterface{

    init {
        hasInlineProgress = true
    }

    override fun getViewModelClass() = SearchCityStateViewModel::class.java

    override fun layoutId()= R.layout.activity_search_origin_city

    override fun requireConnection()= true

    var isLoadingData = true
    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true

    @Inject
    lateinit var userPrefs: UserPrefs

    init {
        hasInlineProgress = true
    }

    private val adapter by lazy { SearchOriginCityRvAdapter(this) }
   private val popularAdapter by lazy{ PopularOriginLocationAdapter(this)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("SearchOriginCityActivity_SetupTime")
        activitySetupTrace?.start()
        viewModel.cityType = intent.getStringExtra(CityType) ?: ""
        viewModel.selectedData = intent.getStringExtra(SelectedData)?:""
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
        title = if(viewModel.cityType == "origin") "Origin Location" else "Destination Location"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.rvCityItems.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@SearchOriginCityActivity.adapter

        }
        binding.rvPopularCityItems.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@SearchOriginCityActivity.popularAdapter

        }

        viewModel.searchLiveData.observe(this, Observer {
            if (it != null) {
                    adapter.clearItems()
                    adapter.operation(it)
            }
        })
        binding.closeIcon.setOnClickListener{
            binding.editQuery.setQuery("",false)
        }
        binding.editQuery.getQueryTextChangeObservable()
            .debounce(300, TimeUnit.MILLISECONDS)
            .filter { text ->
                if (text.length<2) {
                    this?.runOnUiThread {
                        binding.popularLl.visibility = View.VISIBLE
                        binding.rvCityItems.visibility = View.GONE
                        binding.searchIcon.visibility = View.VISIBLE
                        binding.closeIcon.visibility = View.GONE
                    }
                    return@filter false
                } else {
                    return@filter true
                }
            }
            .distinctUntilChanged()
            .switchMap { query ->
                dataFromNetwork(query)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result ->
                if(result.length>1)
                    this?.runOnUiThread {
                        binding.rvCityItems.visibility = View.VISIBLE
                        binding.popularLl.visibility = View.GONE
                        binding.searchIcon.visibility = View.GONE
                        binding.closeIcon.visibility = View.VISIBLE
                    }
                viewModel.searchCity(result)
            }
        if(viewModel.selectedData.isNotEmpty()){
            binding.editQuery.setQuery(viewModel.selectedData,false)
            viewModel.searchText = viewModel.selectedData
            binding.popularLl.visibility= View.GONE
            binding.searchIcon.visibility = View.GONE
            binding.closeIcon.visibility = View.VISIBLE
        }else{
            binding.editQuery.focusClick()
        }
        if (viewModel.searchText.length in 2..10) {
            refreshData()
        }
        viewModel.getPopularCities()
        viewModel.popularCitiesLiveData.observe(this, Observer {
            popularAdapter.operation(it)
        })
    }
    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }
    override fun handleAction(
        actionId: String,
        item: BaseCityStateRVAdapterItem<*>) {
        when(actionId){
            CitySelected -> {
                val data = item.data as CityModel
                if(viewModel.cityType.equals("origin")){
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
        adapter.clearItems()
        viewModel.searchCity(viewModel.searchText)
    }


    private fun dataFromNetwork(query: String): Observable<String> {
        return Observable.just(true)
            .delay(0, TimeUnit.SECONDS)
            .map {
                query
            }
    }

}

/**
 * Search City Intent
 */
fun searchOriginCityIntent(
    context: Context
) = Intent( context, SearchOriginCityActivity::class.java)
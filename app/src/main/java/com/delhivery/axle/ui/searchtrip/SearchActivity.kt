package com.delhivery.axle.ui.searchtrip

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.delhivery.axle.R
import com.delhivery.axle.R.string
import com.delhivery.axle.api.repository.UserSearchLimit
import com.delhivery.axle.api.request.SearchAction_ResetTrip
import com.delhivery.axle.api.request.SearchAction_SearchTrip
import com.delhivery.axle.api.request.SearchRequest
import com.delhivery.axle.data.home.trips.HomeTripsItemData
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadEpod
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_UploadTracking
import com.delhivery.axle.data.home.trips.HomeTripsRequestAction_ViewDetails
import com.delhivery.axle.data.search.SearchTimeOutAction
import com.delhivery.axle.data.search.SearchWarningAction_NoResult
import com.delhivery.axle.databinding.ActivitySearchBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.ui.home.activity.docket.docketUpdateIntent
import com.delhivery.axle.ui.searchload.fragments.SearchLoadFragmentType.LoadFragment
import com.delhivery.axle.ui.searchload.fragments.SearchLoadFragmentType.ResultsFragment
import com.delhivery.axle.ui.tripdetails.tripDetailsIntent
import com.delhivery.axle.ui.tripdetails.uploadImageIntent
import com.delhivery.axle.utils.AWSUtils
import com.delhivery.axle.utils.AWSUtils.AWSProgressInterface
import com.delhivery.axle.utils.PaginationScrollListener
import com.delhivery.axle.utils.REQCODE_UPLOAD_DOCKET
import com.delhivery.axle.utils.REQCODE_UPLOAD_POD
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import java.io.File
import javax.inject.Inject

class SearchActivity : BaseActivity<ActivitySearchBinding, SearchViewModel>(),
    SearchRVAdapterInterface, AWSProgressInterface {

  override fun getViewModelClass() = SearchViewModel::class.java

  override fun layoutId() = R.layout.activity_search

  override fun requireConnection() = true

  var isLoadingData = true

  init {
    hasInlineProgress = true
  }

  private val adapter by lazy { SearchRVAdapter(this) }

  @Inject lateinit var awsUtils: AWSUtils

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    setSupportActionBar(binding.toolbar)
    title = "Search Trips"
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        if (viewModel.total > 0) {
          adapter.resetStaticData()
          viewModel.total = 0
        } else {
        }
        finish()
      }
    })

    binding.rvSearch.apply {
      layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
      adapter = this@SearchActivity.adapter
      addOnScrollListener(PaginationInterface())
    }

    viewModel.searchLiveData.observe(this, Observer {
      it?.let { _items ->
        adapter.operation(_items)
      }
    })

    viewModel.dataLoadingLiveData.observe(this, Observer {
      isLoadingData = it ?: false
    })

    viewModel.delegationLiveData.observe(this, Observer {
      if (it != null) {
        awsUtils.startDownload(it.first, it.second, it.third, this)
      } else {
        uiUtils.showSnackbar("Please try again")
      }
    })

    adapter.resetStaticData()
  }

  private fun searchTrips(data: SearchRequest? = null) {
    val vehicleNumber = data?.vehicleNumber
    val lrNumber = data?.lr
    if(!vehicleNumber.isNullOrEmpty() && !lrNumber.isNullOrEmpty()){
      viewModel.request = SearchRequest(vehicleNumber = vehicleNumber, lr = lrNumber)
    }else if(!vehicleNumber.isNullOrEmpty()){
      viewModel.request = SearchRequest(vehicleNumber = vehicleNumber)
    }else if(!lrNumber.isNullOrEmpty()){
      viewModel.request = SearchRequest(lr = lrNumber)
    }else{
      uiUtils.showSnackbar("Please enter search parameters")
      return
    }
    adapter.refresh()
    uiUtils.toggleKeyboard()
    viewModel.searchTrips()
  }

  override fun onAWSSuccess(path: String) {
    if (!isFinishing) {
      uiUtils.hideProgress()
      uiUtils.showSnackbar("Downloaded")
      val file = getFile()
      if (file != null) {
        openFile(file)
      } else {
        uiUtils.showSnackbar("Can't process POD")
      }
    }
  }

  override fun onAWSFailure() {
    if (!isFinishing) {
      uiUtils.hideProgress()
      uiUtils.showSnackbar("Couldn't complete download, please try after sometime")
    }
  }

  private fun openFile(file: File) {
    try {
      val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
      val intent = Intent(Intent.ACTION_VIEW)
      if (file.toString().contains(".pdf")) {
        intent.setDataAndType(uri, "application/pdf")
      } else if (file.toString().contains(".jpg") ||
          file.toString().contains(".jpeg") || file.toString().contains(".png")
      ) {
        intent.setDataAndType(uri, "image/jpeg")
      } else {
        intent.setDataAndType(uri, "*/*")
      }
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      startActivity(intent)
    } catch (e: java.lang.Exception) {
      uiUtils.showSnackbar("No application found which can open the file")
    }
  }

  override fun handleAction(
    actionId: String,
    item: BaseSearchRVAdapterItem<*>
  ) {
    when (actionId) {
      SearchAction_SearchTrip -> {
        val data = item.data as SearchRequest
        searchTrips(data)
      }

      HomeTripsRequestAction_ViewDetails -> {
        val data = item.data as HomeTripsItemData
        startActivity(tripDetailsIntent(data.key(), this))
      }

      HomeTripsRequestAction_UploadEpod -> {
        val data = item.data as HomeTripsItemData
        viewModel.transactionId = data.transactionId
        viewModel.podUrl = data.podUrl ?: ""
        if (data.podUrl.isNullOrEmpty()) {
          startActivityForResult(uploadImageIntent(this, data.transactionId, data.reachedTime!!, data.unloadingTime!!), REQCODE_UPLOAD_POD)
        } else {
          if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            compositeDisposable += requestPermission(arrayOf(WRITE_EXTERNAL_STORAGE))
              .onBackground()
              .subscribe { granted, error ->
                if (error == null && granted) {
                  uiUtils.showSnackbar("Downloading POD....")
                  val file = getFile()
                  if (file != null && !TextUtils.isEmpty(data.podUrl)) {
                    uiUtils.showProgress()
                    data.podUrl.let {
                      viewModel.podUrl = it
                      viewModel.getDelegationToken(it, file)
                    }
                  } else {
                    uiUtils.showSnackbar("Can't process POD")
                  }
                } else {
                  uiUtils.showSnackbar(getString(string.storage_permission))
                }
              }
        }
      }

      HomeTripsRequestAction_UploadTracking -> {
        val data = item.data as HomeTripsItemData
        when {
          data.hasPODTracking() ->
            startActivityForResult(
                docketUpdateIntent(context = this, trip = data), REQCODE_UPLOAD_DOCKET
            )
          else -> {
            val list = arrayListOf<String>()
            list.add(data.transactionId)
            startActivityForResult(
                docketUpdateIntent(context = this, transactionIds = list), REQCODE_UPLOAD_DOCKET
            )
          }
        }
      }

      SearchAction_ResetTrip, SearchWarningAction_NoResult -> {
        viewModel.request = SearchRequest()
        adapter.resetStaticData()
        viewModel.total = 0
      }

      SearchTimeOutAction -> {
        searchTrips()
      }
    }
  }

  private fun getFile(): File? {
    val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val basePath = "$storageDir/" + viewModel.transactionId
    if (viewModel.podUrl != null) {
      return when {
        (viewModel.podUrl).endsWith("pdf") -> File(basePath + "_pod.pdf")
        (viewModel.podUrl).endsWith("png") -> File(basePath + "_pod.png")
        (viewModel.podUrl).endsWith("jpg") || (viewModel.podUrl).endsWith("jpeg") -> File(
            basePath + "_pod.jpg"
        )
        else -> {
          return null
        }
      }
    }
    return null
  }

/*  override fun onBackPressed() {
    if (viewModel.total > 0) {
      adapter.resetStaticData()
      viewModel.total = 0
    } else {
      super.onBackPressed()
    }
  }*/

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      REQCODE_UPLOAD_POD, REQCODE_UPLOAD_DOCKET -> {
        if (resultCode == Activity.RESULT_OK) {
          searchTrips(viewModel.request)
        }
      }
    }
  }

  /**
   * Pagination interface
   */
  inner class PaginationInterface : PaginationScrollListener(
      UserSearchLimit
  ) {
    override fun loadMore() = viewModel.searchTrips(true)

    override fun hasMore() = viewModel.hasMoreData

    override fun isLoading() = isLoadingData
  }

}

/**
 * Trip details intent
 */
fun searchIntent(
  context: Context
) = Intent(context, SearchActivity::class.java).apply {
}

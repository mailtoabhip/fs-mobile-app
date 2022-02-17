package com.delhivery.axle.ui.profile.kycdetails.fragments

import android.Manifest
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.delhivery.axle.R
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.data.doc.DocDetailData
import com.delhivery.axle.databinding.FragmentKycDocumentsBinding
import com.delhivery.axle.injection.module.GlideApp
import com.delhivery.axle.ui.kyc.aadhaar.DownloadtemRVAdapterInterface
import com.delhivery.axle.ui.kyc.gst.GstRVAdapter
import com.delhivery.axle.utils.AWSUtils
import com.delhivery.axle.utils.BitmapUtils
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import java.io.File
import javax.inject.Inject


class KycDocumentsFragment : ProfileKYCBaseFragment<FragmentKycDocumentsBinding, KYCDocumentsViewModel>(), DownloadtemRVAdapterInterface, AWSUtils.AWSProgressInterface, DocRVAdapterInterface {

    init {
        hasInlineProgress = true
    }

    companion object {
        /* singleton instance */
        val _instance: KycDocumentsFragment by lazy { KycDocumentsFragment() }
    }

    lateinit var path:String
    var docArray = ArrayList<Pair<String, String?>>()
    var showProg:Boolean = false
    var dList:HashSet<String> = HashSet()

    @Inject lateinit var awsUtils:AWSUtils

    @Inject lateinit var bitmapUtils:BitmapUtils

    override fun getViewModelClass()= KYCDocumentsViewModel::class.java
    val awsURl = "https://orion-service.s3.ap-southeast-1.amazonaws.com/"

    /* RV adapter */
    private val docRVAdapter: DocRVAdapter by lazy { DocRVAdapter(this) }

    override fun layoutId() = R.layout.fragment_kyc_documents

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.attachmentList.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@KycDocumentsFragment.docRVAdapter
        }

        viewModel.setDataDoc()

        viewModel.docLiveData.observe(this, Observer {
            it?.let { _items ->
                docRVAdapter.operation(_items)
            }
        })

        viewModel.delegationDownloadLiveData.observe(this, Observer {
            if (it != null) {
                awsUtils.startDownload(it.first, it.second, it.third, this)
                viewModel.imagePath = it.third.path
            } else {
                uiUtils.showSnackbar("Please try again")
            }
        })

    }

    private fun downloadLogo(item: String) {
        compositeDisposable += requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onBackground()
                .subscribe { granted, error ->
                    if (error == null && granted) {
                        val file = getFile(item)
                        if (file != null) {
                            viewModel.getDownloadDelegationToken(item, file)
                        } else {
                            uiUtils.showSnackbar("Can't process image")
                        }
                    } else {
                        uiUtils.showSnackbar(getString(R.string.storage_permission))
                    }
                }
    }

    private fun downloadLogo(item: Pair<String?, String?>, prt:String) {
        compositeDisposable += requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onBackground()
                .subscribe { granted, error ->
                    if (error == null && granted) {
                        val file = getFile(prt)
                        if (file != null) {
                            viewModel.getDownloadDelegationToken(prt, file)
                        } else {
                            uiUtils.showSnackbar("Can't process image")
                        }
                    } else {
                        uiUtils.showSnackbar(getString(R.string.storage_permission))
                    }
                }
    }

    private fun getFile(item: String): File? {
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        val basePath = "$storageDir/" + System.currentTimeMillis()
        val arrString = item.split("/")
        return File(basePath + arrString[arrString.size-1])
    }

    override fun onAWSSuccess(path: String) {
        uiUtils.hideProgress()
        if(showProg) {
            uiUtils.showSnackbar("Document downloaded successfully")
        }else {
            if (docArray.contains(Pair(awsURl+ path, null))) {
                docArray.remove(Pair(awsURl + path, null))
            }
            docArray.add(Pair(awsURl + path, viewModel.imagePath))
        }
        showProg = false
    }

    override fun onAWSFailure() {
        if(showProg) {
            uiUtils.showSnackbar("Document download failed!")
        }
        showProg = false
        uiUtils.hideProgress()
    }

    override fun handleAction(item: String) {
        uiUtils.showProgress()
        showProg= true
        downloadLogo(item.replace(awsURl, ""))
    }

    override fun handleImageAction(item: Pair<String, String?>) {
        if(!dList.contains(item.first)){
            dList.add(item.first)
            downloadLogo(item,item.first.replace(awsURl, ""))
        }
     }

    override fun handleAction(actionId: String, item: BaseDocRVAdapterItem<*>) {
        downloadLogo(Pair(actionId, null),actionId.replace(awsURl, ""))
    }

    override fun fetchDetails(data: DocDetailData) {
    }

    private fun loadImage(
            path: String?,
            view: ImageView,
            textView: TextView
    ) {
        view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                view.viewTreeObserver.removeOnPreDrawListener(this)
                val imageViewHeight = view.measuredHeight
                val imageViewWidth = view.measuredWidth
                path?.let {
                    GlideApp.with(view.context)
                            .load(bitmapUtils.decodeSampledBitmap(path, imageViewWidth, imageViewHeight))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                            .listener(object : RequestListener<Drawable?> {
                                override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                                        isFirstResource: Boolean
                                ): Boolean {
                                    return false
                                }

                                override fun onResourceReady(
                                        resource: Drawable?,
                                        model: Any?,
                                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                ): Boolean {
                                    var byteCount = Int.MIN_VALUE
                                    byteCount = (resource as BitmapDrawable).bitmap.byteCount / 1024
                                    textView.text = byteCount.toString() + " KB"
                                    return false
                                }

                            }).circleCrop().into(view)
                }
                return true
            }
        })
    }
}
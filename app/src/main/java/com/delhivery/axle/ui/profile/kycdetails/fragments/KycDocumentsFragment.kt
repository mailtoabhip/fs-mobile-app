package com.delhivery.axle.ui.profile.kycdetails.fragments

import android.Manifest
import android.R.attr.bitmap
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
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
import com.delhivery.axle.data.doc.DocAction_ViewDetails
import com.delhivery.axle.data.doc.DocDetailData
import com.delhivery.axle.data.transactions.TransactionTimeOutAction
import com.delhivery.axle.databinding.FragmentKycDocumentsBinding
import com.delhivery.axle.injection.module.GlideApp
import com.delhivery.axle.utils.AWSUtils
import com.delhivery.axle.utils.BitmapUtils
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject


class KycDocumentsFragment : ProfileKYCBaseFragment<FragmentKycDocumentsBinding, KYCDocumentsViewModel>(),  AWSUtils.AWSProgressInterface, DocRVAdapterInterface {

    init {
        hasInlineProgress = true
    }

    companion object {
        /* singleton instance */
        val _instance: KycDocumentsFragment by lazy { KycDocumentsFragment() }
    }

    lateinit var path:String
    var showProg:Boolean = false
    var dList:HashMap<String, DocDetailData?> = HashMap()
    var docItem:DocDetailData = DocDetailData("", null)

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

        dList.clear()
        dList = HashMap()
        docItem = DocDetailData("", null)
        showProg = false
        refreshData()

        viewModel.docLiveData.reobserve(this, Observer {
            it?.let { _items ->
                docRVAdapter.operation(_items)
            }
        })

        viewModel.docDetailsLiveData.reobserve(this, Observer {
            docRVAdapter.notifyDataSetChanged()
        })

        viewModel.delegationDownloadLiveData.reobserve(this, Observer {
            if (it != null) {
                awsUtils.startDownload(it.first, it.second, it.third, this)
                viewModel.imagePath = it.third.path
            }
        })

    }

    private fun downloadLogo(item: String) {
        compositeDisposable += requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onBackground()
                .subscribe { granted, error ->
                    if (error == null && granted) {
                        showProg = true
                        uiUtils.showProgress()
                        val file = getFile(item)
                        if (file != null) {
                            viewModel.getDownloadDelegationToken(item, file)
                        } else {
                            uiUtils.showSnackbar("Can't process image")
                        }
                    } else {
                        uiUtils.hideProgress()
                        uiUtils.showSnackbar(getString(R.string.storage_permission))
                    }
                }
    }

    private fun downloadImage(data: DocDetailData, item: String) {
        val file = getImageFile(item)
        if (file != null) {
            docItem.docPath = file.path
            docItem.docUrl = data.docUrl
            dList.get(data.docUrl)?.docPath = file.path
            viewModel.getDownloadDelegationToken(item, file)
        } else {
            uiUtils.showSnackbar("Can't process image")
        }
    }

    private fun getFile(item: String): File? {
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        val basePath = "$storageDir/"+System.currentTimeMillis()
        val arrString = item.split("/")
        return File(basePath + arrString[arrString.size - 1])
    }

    private fun getImageFile(item: String): File? {
        val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val basePath = "$storageDir/"+System.currentTimeMillis()
        val arrString = item.split("/")
        Log.d("asmdaklalaaa", basePath)
        return File(basePath + arrString[arrString.size - 1])
    }

    override fun onAWSSuccess(path: String) {
        uiUtils.hideProgress()
        if(showProg) {
            uiUtils.showSnackbar("Document downloaded successfully")
        }else {
            val fullPath = awsURl+path
            dList.get(fullPath)?.let { viewModel.fetchDetails(it, viewModel.imagePath) }
        }
        showProg = false
    }

    override fun onAWSFailure() {
        if(showProg) {
            uiUtils.showSnackbar("Document download failed!")
        }
//        else {
//            val fullPath = awsURl+path
//            dList.get(fullPath)?.let { viewModel.fetchDetails(it, viewModel.imagePath) }
//        }
        showProg = false
        uiUtils.hideProgress()
    }

    override fun handleAction(actionId: String, item: BaseDocRVAdapterItem<*>) {
        when (actionId) {
            DocAction_ViewDetails -> {
                downloadLogo(item.data.key().replace(awsURl, ""))
            }
            TransactionTimeOutAction -> {
                refreshData()
            }
        }
    }

    private fun refreshData() {
        docRVAdapter.resetStaticData()
        viewModel.setDataDoc()
    }

    override fun fetchDetails(data: DocDetailData) {
        if(!dList.contains(data.docUrl)){
            dList.put(data.docUrl, data)
            showProg = false
            downloadImage(data, data.docUrl.replace(awsURl, ""))
        }
    }

    override fun showImage(data: DocDetailData, textView: TextView, imageView: ImageView) {
        try {
            if(data.docPath?.endsWith(".pdf") == true &&  !renderToBitmap(context, data.docPath).isNullOrEmpty()){
                val bitmap = renderToBitmap(context, data.docPath)?.get(0)
                imageView.setImageBitmap(bitmap)
                val stream = ByteArrayOutputStream()
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val imageInByte: ByteArray = stream.toByteArray()
                val lengthbmp = imageInByte.size.toLong()/ 1024
                textView.text = lengthbmp.toString() +" KB"
            }else{
                loadImage(data.docPath, imageView, textView)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    fun renderToBitmap(context: Context?, filePath: String?): List<Bitmap>? {
        val images: MutableList<Bitmap> = ArrayList()
        val pdfiumCore = PdfiumCore(context)
        try {
            val f: File = File(filePath)
            val fd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfDocument: PdfDocument = pdfiumCore.newDocument(fd)
            val pageCount = pdfiumCore.getPageCount(pdfDocument)
            for (i in 0 until pageCount) {
                pdfiumCore.openPage(pdfDocument, i)
                val width = pdfiumCore.getPageWidthPoint(pdfDocument, i)
                val height = pdfiumCore.getPageHeightPoint(pdfDocument, i)
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                pdfiumCore.renderPageBitmap(pdfDocument, bmp, i, 0, 0, width, height)
                images.add(bmp)
            }
            pdfiumCore.closeDocument(pdfDocument)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return images
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
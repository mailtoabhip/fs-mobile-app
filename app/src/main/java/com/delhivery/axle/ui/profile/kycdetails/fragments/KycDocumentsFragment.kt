package com.delhivery.axle.ui.profile.kycdetails.fragments

import android.Manifest
import android.R.attr.bitmap
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
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
import com.delhivery.axle.api.response.DocumentFile
import com.delhivery.axle.api.response.FileData
import com.delhivery.axle.utils.BitmapUtils
import com.delhivery.axle.utils.DocumentUtils
import com.delhivery.axle.utils.NavigationUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import com.delhivery.axle.utils.StepKey
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject


class KycDocumentsFragment : ProfileKYCBaseFragment<FragmentKycDocumentsBinding, KYCDocumentsViewModel>(),
    DocRVAdapterInterface, DocumentUtils.DocumentListInterface {

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
    var docItem:DocDetailData = DocDetailData("", null, null, null,null, null, null)
    private var fragmentSetupTrace: Trace? = null
    private var isFirstResume = true
    // ✅ Map s3_path -> original URL to handle concurrent downloads
    private val s3PathToUrlMap: HashMap<String, String> = HashMap()
    private var currentDownloadUrl: String? = null
    private var currentDownloadData: DocDetailData? = null

    @Inject lateinit var bitmapUtils:BitmapUtils
    @Inject lateinit var documentUtils: DocumentUtils

    override fun getViewModelClass()= KYCDocumentsViewModel::class.java

    /* RV adapter */
    private val docRVAdapter: DocRVAdapter by lazy { DocRVAdapter(this) }

    override fun layoutId() = R.layout.fragment_kyc_documents

    @Inject lateinit var userPrefs: UserPrefs

    @Inject lateinit var navigationUtils: NavigationUtils

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSetupTrace = FirebasePerformance.getInstance().newTrace("KycDocumentsFragment_SetupTime")
        fragmentSetupTrace?.start()
        binding.attachmentList.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = this@KycDocumentsFragment.docRVAdapter
        }

        if(userPrefs.verificationStatus.equals("failed")){
            binding.btnRetry.visibility = View.VISIBLE
        }else{
            binding.btnRetry.visibility = View.GONE
        }
        if(userPrefs.noOfVerificationIssues.equals("1")&&userPrefs.isBankDetailsRejected==true){
            binding.btnRetry.isEnabled=false
        }else{
            binding.btnRetry.isEnabled=true
        }
        binding.btnRetry.setOnClickListener {
            userPrefs.retryVerification = true
            userPrefs.retryVerificationOnBack=false
            val bundle = Bundle()
            bundle.putInt(StepKey, 0)
            context?.let { it1 -> navigationUtils.navigateKyc(it1, true, bundle) }
        }

        dList.clear()
        dList = HashMap()
        docItem = DocDetailData("", null, null, null,null, null, null)
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

    }

    override fun onResume() {
        super.onResume()
        if (fragmentSetupTrace != null && isFirstResume) {
            fragmentSetupTrace?.stop()
            isFirstResume = false
        }
    }

    private fun downloadLogo(docUrl: String) {
        compositeDisposable += requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .onBackground()
            .subscribe { granted, error ->
                if (error == null && granted) {
                    showProg = true
                    currentDownloadUrl = docUrl
                    uiUtils.showProgress()
                    // Extract s3_path from full S3 URL
                    val s3Path = extractS3PathFromUrl(docUrl)
                    if (s3Path != null) {
                        Log.d("KycDocumentsFragment", "downloadByS3Path: $s3Path")
                        documentUtils.downloadByS3Path(s3Path, this)
                    } else {
                        uiUtils.hideProgress()
                        uiUtils.showSnackbar("Unable to extract document path from URL")
                    }
                } else {
                    uiUtils.hideProgress()
                    uiUtils.showSnackbar(getString(R.string.storage_permission))
                }
            }
    }

    private fun downloadImage(data: DocDetailData, docUrl: String) {
        // Store reference in dList before starting download
        if (!dList.containsKey(docUrl)) {
            dList[docUrl] = data
        }
        currentDownloadData = data
        currentDownloadUrl = docUrl
        Log.d("KycDocumentsFragment", "downloadImage: docUrl=$docUrl, data.docUrl=${data.docUrl}")
        // Extract s3_path from full S3 URL
        val s3Path = extractS3PathFromUrl(docUrl)
        Log.d("KycDocumentsFragment", "downloadByS3Path: $s3Path")
        if (s3Path != null) {
            // ✅ Map s3_path to original URL for lookup in callback
            s3PathToUrlMap[s3Path] = docUrl
            // Use secure Document API: GET /document/download?s3_path=...
            documentUtils.downloadByS3Path(s3Path, this)
        } else {
            uiUtils.showSnackbar("Unable to extract document path from URL")
        }
    }

    /**
     * Extract s3_path from full S3 URL
     * Example: "https://orion-service-prod-mum.s3.ap-south-1.amazonaws.com/documents/user-supplier/pancard/1735911510372.png"
     * Returns: "documents/user-supplier/pancard/1735911510372.png"
     * 
     * Also handles: "https://orion-service-prod-mum.s3.ap-south-1.amazonaws.com/trips/vendor_pod/docket/docket_1764233770054.jpg"
     * Returns: "trips/vendor_pod/docket/docket_1764233770054.jpg"
     */
    private fun extractS3PathFromUrl(docUrl: String): String? {
        return try {
            // Construct AWS base path from config
            val bucket = com.delhivery.axle.config.AWSConfig.Bucket.value()
            val region = com.delhivery.axle.config.AWSConfig.ServerRegion.value()
            val awsBasePath = "https://$bucket.s3.$region.amazonaws.com/"
            
            // Remove base URL from full S3 URL
            if (docUrl.startsWith(awsBasePath)) {
                docUrl.replace(awsBasePath, "").split("?")[0] // Remove query parameters if any
            } else {
                // Fallback: Try to extract path from URL pattern (bucket.s3.region.amazonaws.com/path)
                val parts = docUrl.split(".amazonaws.com/")
                if (parts.size > 1) {
                    parts[1].split("?")[0] // Remove query parameters if any
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("KycDocumentsFragment", "Error extracting s3_path: ${e.message}")
            null
        }
    }


    /**
     * Create file for downloads: {timestamp}{filename} in Downloads directory
     */
    private fun getFile(url: String): File? {
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val arrString = url.split("/")
        val fileName = arrString[arrString.size - 1].split("?")[0] // Remove query parameters
        val timestamp = System.currentTimeMillis()
        val file = File(storageDir, "${timestamp}${fileName}")
        // Ensure directory exists
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return file
    }

    /**
     * Create file for viewing: {timestamp}/{filename} in app's documents directory
     */
    private fun getImageFile(url: String): File? {
        val storageDir = activity?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (storageDir == null) return null
        val arrString = url.split("/")
        val fileName = arrString[arrString.size - 1].split("?")[0] // Remove query parameters
        val timestamp = System.currentTimeMillis()
        val timestampDir = File(storageDir, timestamp.toString())
        // Ensure directory exists
        if (!timestampDir.exists()) {
            timestampDir.mkdirs()
        }
        return File(timestampDir, fileName)
    }


    override fun handleAction(actionId: String, item: BaseDocRVAdapterItem<*>) {
        when (actionId) {
            DocAction_ViewDetails -> {
                // docUrl is already a complete URL from the backend
                downloadLogo(item.data.key())
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
            // docUrl is already a complete URL from the backend
            Log.d("KycDocumentsFragment", "fetchDetails: ${data.docUrl}")
            downloadImage(data, data.docUrl)
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

    // DocumentListInterface implementation for secure download API
    override fun onDocumentListSuccess(files: List<FileData>) {
        if (files.isNotEmpty()) {
            val documentFile = files.first()
            // ✅ Use s3_path from response to find the original URL
            val s3Path = documentFile.s3Path
            val originalUrl = s3Path?.let { s3PathToUrlMap[it] }
            val dataFromList = originalUrl?.let { dList[it] }
            
            Log.d("KycDocumentsFragment", "onDocumentListSuccess: s3Path=$s3Path, originalUrl=$originalUrl, dataFromList=${dataFromList?.docUrl}, mapKeys=${s3PathToUrlMap.keys}")
            
            if (originalUrl == null || dataFromList == null) {
                uiUtils.hideProgress()
                uiUtils.showSnackbar("Unable to determine original document URL")
                // Clean up
                s3Path?.let { s3PathToUrlMap.remove(it) }
                currentDownloadUrl = null
                currentDownloadData = null
                return
            }
            
            // Download file from pre-signed URL
            downloadFileFromUrl(documentFile.downloadUrl, originalUrl)
        } else {
            uiUtils.hideProgress()
            uiUtils.showSnackbar("No documents found")
            // Clean up
            currentDownloadUrl?.let { 
                val s3Path = extractS3PathFromUrl(it)
                s3Path?.let { s3PathToUrlMap.remove(it) }
            }
            currentDownloadUrl = null
            currentDownloadData = null
        }
    }

    override fun onDocumentListFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showSnackbar("Download failed: $error")
        // ✅ Clean up mapping
        currentDownloadUrl?.let { 
            val s3Path = extractS3PathFromUrl(it)
            s3Path?.let { s3PathToUrlMap.remove(it) }
        }
        currentDownloadUrl = null
        currentDownloadData = null
    }

    /**
     * Download file from pre-signed URL using OkHttpClient
     */
    private fun downloadFileFromUrl(downloadUrl: String, originalDocUrl: String) {
        compositeDisposable += io.reactivex.Observable.fromCallable {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(downloadUrl)
                .build()
            
            val response: Response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val file = if (showProg) {
                    // For downloadLogo: save to Downloads folder
                    getFile(originalDocUrl)
                } else {
                    // For downloadImage: save to app documents folder
                    getImageFile(originalDocUrl)
                }
                
                if (file != null) {
                    response.body()?.byteStream()?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    file.absolutePath
                } else {
                    null
                }
            } else {
                null
            }
        }
        .onBackground()
        .subscribe({ filePath ->
            if (filePath != null) {
                if (showProg) {
                    // For downloadLogo
                    uiUtils.showSnackbar("File downloaded successfully")
                    showProg = false
                } else {
                    // For downloadImage - ✅ Look up correct data from dList using originalDocUrl
                    val data = originalDocUrl?.let { dList[it] }
                    if (data != null) {
                        data.docPath = filePath
                        dList[originalDocUrl] = data  // ✅ Update dList
                        Log.d("KycDocumentsFragment", "Downloaded: $originalDocUrl -> $filePath")
                        docRVAdapter.notifyDataSetChanged()
                    } else {
                        Log.e("KycDocumentsFragment", "Could not find data for URL: $originalDocUrl")
                    }
                }
            } else {
                uiUtils.showSnackbar("Download failed")
            }
            uiUtils.hideProgress()
            // ✅ Clean up mapping
            val s3Path = extractS3PathFromUrl(originalDocUrl)
            s3Path?.let { s3PathToUrlMap.remove(it) }
            currentDownloadUrl = null
            currentDownloadData = null
        }, { error ->
            uiUtils.hideProgress()
            uiUtils.showSnackbar("Download error: ${error.message}")
            // ✅ Clean up mapping on error
            val s3Path = extractS3PathFromUrl(originalDocUrl)
            s3Path?.let { s3PathToUrlMap.remove(it) }
            currentDownloadUrl = null
            currentDownloadData = null
        })
    }
}
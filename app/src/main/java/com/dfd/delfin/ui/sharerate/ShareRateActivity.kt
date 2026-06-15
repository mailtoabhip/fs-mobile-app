package com.dfd.delfin.ui.sharerate

import android.Manifest
import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.AllCaps
import android.text.TextWatcher
import android.view.*
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.dfd.delfin.BuildConfig
import com.dfd.delfin.R
import com.dfd.delfin.R.string
import com.dfd.delfin.api.response.FileData
import com.dfd.delfin.api.response.TruckResponseArray
import com.dfd.delfin.data.CityModel
import com.dfd.delfin.data.yourrewards.YourRewardsItemData
import com.dfd.delfin.databinding.ActivityShareRateBinding
import com.dfd.delfin.databinding.DialogBottomTruckValueBinding
import com.dfd.delfin.databinding.DialogRateUploadSuccessBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.ui.businessverification.DocUploadAdapter
import com.dfd.delfin.ui.loadAlert.HomeLoadAlertRequestItemData
import com.dfd.delfin.ui.searchCity.searchCityIntent
import com.dfd.delfin.ui.trucks.TruckSizeAdapter
import com.dfd.delfin.utils.*
import com.dfd.delfin.utils.constants.FileType
import com.dfd.delfin.utils.extensions.*
import com.dfd.delfin.utils.prefs.UserPrefs
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject


/**
 * Share rate screen
 */
class ShareRateActivity : BaseActivity<ActivityShareRateBinding, ShareRateViewModel>(), DatePickerDialog.OnDateSetListener, DocumentUtils.DocumentProgressInterface, DocumentUtils.DocumentListInterface {

    companion object {
        val __INSTANCE: ShareRateActivity by lazy { ShareRateActivity() }
    }

    override fun getViewModelClass() = ShareRateViewModel::class.java

    override fun layoutId() = R.layout.activity_share_rate

    override fun requireConnection() = false

    var capacityArr = mutableListOf<String>()
    var sourcedAs : String = ""
    var truckItems = mutableListOf<TruckResponseArray>()
    var homeLoadAlertRequestItemData= HomeLoadAlertRequestItemData(inventoryUuid = "")
    val adapter : TruckSizeAdapter by lazy { TruckSizeAdapter() }
    private var calendar: Calendar = Calendar.getInstance()
    var proofTypeFilled = false
    var docUploadProof = false
    var offerId:String? = ""
    var startTime: Long = 0
    var endTime: Long = 0
    private val CityType = "city_type"

    private var isCamera: Boolean = false
    private var mPhotoFile: File? = null
    private lateinit var uploadImageName: String
    private lateinit var localImageName: String
    val awsPath = "loadboard/sharerate/"
    val docUploadAdapter : DocUploadAdapter by lazy { DocUploadAdapter() }
    var uploadArray:ArrayList<Pair<String, String>> = ArrayList()

    @Inject
    lateinit var imageUtils: ImageUtils
    @Inject
    lateinit var documentUtils: DocumentUtils
    @Inject
    lateinit var fileCompressor: FileCompressor
    @Inject
    lateinit var bitmapUtils: BitmapUtils
    @Inject
    lateinit var userPrefs: UserPrefs

    var itemTD:String? = null
    var offerTD:String? = null
    var priceId:String? = null
    var priceSortKey:String? = null

    private var activitySetupTrace: Trace? = null
    private var isFirstResume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySetupTrace = FirebasePerformance.getInstance().newTrace("ShareRateActivity_SetupTime")
        activitySetupTrace?.start()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        viewModel.origin = intent?.extras?.getString("originname")?.let { CityModel(it, intent?.extras?.getString("occ")) }
        viewModel.destination = intent?.extras?.getString("destname")?.let { CityModel(it, intent?.extras?.getString("dcc")) }

        if( intent?.extras?.getString("amt")!=null) {
            viewModel.bannerText = "Earn ₹"+intent?.extras?.getString("amt")+" on this lane offer"
        }

        startTime = System.currentTimeMillis()

        binding.editTruckNumber.setFilters(arrayOf<InputFilter>(AllCaps()))


        if( intent?.extras?.getString("offerid")!=null) {
            offerId = intent?.extras?.getString("offerid")
        }

        if( intent?.extras?.getString("itemTD")!=null) {
            itemTD = intent?.extras?.getString("itemTD")
        }

        if( intent?.extras?.getString("offerTD")!=null) {
            offerTD = intent?.extras?.getString("offerTD")
        }

        if(itemTD!=null){
            if(itemTD.equals(offerTD)){
                viewModel.selected_truck_type = itemTD as String
                if(intent?.extras?.getString("truckCapacity")!=null && !intent?.extras?.getString("truckCapacity").toString().equals("null")) {
                    viewModel.selected_truck_capacity = intent?.extras?.getString("truckCapacity")
                }
            }
        }

        if(viewModel.selected_truck_capacity.isNotNullOrEmpty() &&  viewModel.selected_truck_type.isNotNullOrEmpty()) {
                binding.textTruckSize.text = viewModel.selected_truck_type + " " + viewModel.selected_truck_capacity.toString()
        }else {
                binding.textTruckSize.text = viewModel.selected_truck_type
        }

        if(intent?.extras?.getString("truckNumber")!=null) {
            viewModel.selected_vehicle_number = intent?.extras?.getString("truckNumber")!!
            binding.editTruckNumber.setText(viewModel.selected_vehicle_number)
        }



        /* setup toolbar */
        setSupportActionBar(binding.toolbar)
    
    /* Handle window insets for edge-to-edge display (API 35+) */
    if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
      WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
    }
        title = "Share rate & earn reward"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setOriginDestinationTruck()

        binding.editTruckSize.setOnClickListener {
            if(viewModel.selected_truck_type.isNotNullOrEmpty() && viewModel.selected_truck_capacity.isNullOrEmpty()) {
                showTruckSizeDialog("", "fill")
            }else{
                showTruckSizeDialog("", null)
            }
        }

        binding.uploadDocLay.setOnClickListener {
            val imageName = "IMG_" + System.currentTimeMillis()+".jpg"
            captureImage(imageName, imageName)
        }
        binding.docRemove.setOnClickListener {
            showUploadImage()
        }

        binding.editDate.setOnClickListener {
            dialogUtils.datePicker(listener = this@ShareRateActivity, maxDate = 0, minDate = -6)
        }

        viewModel.truckGetLiveData.observe(this, Observer {
            if (it != null) {
                truckItems.addAll(it)
                if (!homeLoadAlertRequestItemData.truckSpecifications.isNullOrEmpty()) {
                    if (homeLoadAlertRequestItemData.truckSpecifications?.get(0)!!.truck_uuid.isNotNullOrEmpty()
                    ) {
                        showTruckSizeDialog(
                                homeLoadAlertRequestItemData.truckSpecifications?.get(0)!!.truck_uuid, null
                        )
                    }
                }
            }
        })

        viewModel.pricingLiveData.observe(this, Observer {
            if (it != null) {
                fillODVTData(it.pricingData.get(0))
            }
        })


        binding.spinnerProof.setup(R.array.array_rate_proof) { p, v ->
            if(p>0){
                proofTypeFilled = true
                viewModel.proofType = v
               enableSaveAlertButton()
            }else{
                proofTypeFilled =false
                viewModel.proofType = null
                enableSaveAlertButton()
            }
        }

        binding.editPriceAddTruck.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null && p0.toString().trim().isNotNullOrEmpty()) {
                    viewModel.expectedPrice = p0.toString().toDouble()
                } else {
                    viewModel.expectedPrice = null
                }
                enableSaveAlertButton()
            }
        })

        binding.editTruckNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                viewModel.selected_vehicle_number = p0.toString()
                enableSaveAlertButton()
            }
        })

        initializeData()

        viewModel.priceUnit= "PMT"

        binding.btnRadioPerTon.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                viewModel.priceUnit= "PMT"
            }
        }

        binding.btnRadioFixed.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                viewModel.priceUnit= "FTL"
            }
        }

        binding.layOrigin.setOnClickListener {
            startActivityForResult(searchCityIntent(this, "origin"), REQCODE_SELECT_CITY)

        }
        binding.layDest.setOnClickListener {
            startActivityForResult(searchCityIntent(this, "destination"), REQCODE_SELECT_CITY)
        }

        binding.btnSubmitDetails.setOnClickListener {
            binding.errorLane.visibility = View.GONE
            if(!viewModel.selected_vehicle_number?.let { it1 -> validateTruckNumber(it1) }) {
                binding.editTruckNumber.error = "Invalid vehicle number entered"
                uiUtils.showToast("Invalid vehicle number entered")
            }else {
                    endTime = System.currentTimeMillis()
                    val ttl = endTime - startTime
                    analyticsUtil.moEngageTrackEvent(
                            EVENT_SUBMIT_OFFER,
                            mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_TTL, PROPERTY_OFFER_ID),
                            mutableListOf(userPrefs.userId(), userPrefs.phoneNumber
                                    ?: "dummy", ttl.toString(), offerId ?: "")
                    )

                    if(viewModel.selected_truck_capacity.isNotNullOrEmpty() && !viewModel.selected_truck_capacity.toString().equals("null")){
                       // viewModel.sharerate()
                        binding.errorTruck.visibility = View.GONE
                    }else{
                        uiUtils.showToast("Please choose both truck size and capacity")
                        binding.errorTruck.text = "Please choose both truck size and capacity"
                        binding.errorTruck.visibility = View.VISIBLE
                    }
            }
        }

        viewModel.rateUpdatedLiveData.observe(this, Observer {
            if (it != null && it) {
                showSuccessDialog()
            }
        })

        viewModel.errorrateUpdatedLiveData.observe(this, Observer {
            if (it != null && it.isNotNullOrEmpty()) {
                if (it.equals("The selection is not applicable for current offer period")) {
                    binding.errorLane.text = it
                    binding.errorLane.visibility = View.VISIBLE
                }
                uiUtils.showToast(it)
                viewModel.errorrateUpdatedLiveData.postValue(null)
            }
        })

        // Removed delegation token logic - direct upload now
    }

    override fun onResume() {
        super.onResume()
        if (activitySetupTrace != null && isFirstResume) {
            activitySetupTrace?.stop()
            isFirstResume = false
        }
    }

   private fun setOriginDestinationTruck(){
       try {
           binding.editOrigin.setText(viewModel.origin!!.cityName())
           binding.editOrigin.isEnabled = false
           binding.editDestination.setText(viewModel.destination!!.cityName())
           binding.editDestination.isEnabled = false
           viewModel.selected_vehicle_number = binding.editTruckNumber.text.toString()
       }catch (e: Exception){

       }
    }

    private fun fillODVTData(it:YourRewardsItemData){
        viewModel.origin = CityModel(it.originCity!!,it.originCityCode!!)
        viewModel.destination= CityModel(it.destinationCity!!,it.destinationCityCode!!)
        if(it.sortKey!=null) {
            offerId = it.sortKey
        }

        if(it.truckDisplayName!=null) {
            offerTD = it.truckDisplayName
            itemTD = it.truckDisplayName
        }

        if(itemTD!=null){
            if(itemTD.equals(offerTD)){
                viewModel.selected_truck_type = itemTD as String
                if(it.truckCapacity!=null && !it.truckCapacity.toString().equals("null")) {
                    viewModel.selected_truck_capacity = it.truckCapacity
                }
            }
        }

        if(viewModel.selected_truck_capacity.isNotNullOrEmpty() &&  viewModel.selected_truck_type.isNotNullOrEmpty()) {
            binding.textTruckSize.text = viewModel.selected_truck_type + " " + viewModel.selected_truck_capacity.toString()+" MT"
        }else {
            binding.textTruckSize.text = viewModel.selected_truck_type
        }

        if(it.vehicleNumber!=null) {
            viewModel.selected_vehicle_number =it.vehicleNumber
            binding.editTruckNumber.setText(viewModel.selected_vehicle_number)
        }
        if(it.rejectionReason!=null) {
            var rejectedReason = getString(string.rejected_due)+" "+it.rejectionReason.replace("_"," ")
            binding.errorLane.setText(rejectedReason)
            binding.errorLane.visibility = View.VISIBLE
        }
        setOriginDestinationTruck()
    }
    private fun showSuccessDialog() {
        runOnUiThread {
                val dialog = Dialog(this)
                val bindingDialog= DialogRateUploadSuccessBinding.inflate(layoutInflater)

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(bindingDialog.root)
                dialog.setCancelable(false)

                bindingDialog.buttonCancel.setOnClickListener {
                   onBackPressedDispatcher.onBackPressed()
                    dialog.dismiss()
                }

                dialog.show()
                dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val back = ColorDrawable(Color.TRANSPARENT)
                val inset = InsetDrawable(back, 30)
                dialog.window!!.setBackgroundDrawable(inset)
                dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
                dialog.window!!.setGravity(Gravity.BOTTOM)
        }
    }

    private fun initializeData() {
       // viewModel.fetchTruckType()
       // viewModel.fetchSupplierTrucks()
    }

    private fun showUploadImage() {
        binding.uploadDocLay.visibility=View.VISIBLE
        binding.docUploadedLay.visibility=View.GONE
        if(uploadArray.size>0){
            uploadArray.clear()
        }
        viewModel.documentProofUrl.clear()
        docUploadProof= false
        enableSaveAlertButton()
    }

    private fun validateTruckNumber(number: String): Boolean{
        val pattern = Pattern.compile(
                "[a-zA-Z]{2}((([0-9]{1,2}|[1-9]{1}[0-9]{1})[a-zA-Z]{1,3})|(0[1-9]{1}|[1-9]{1}[0-9]{1}))[0-9]{4}\$|^[a-zA-Z]{3}[0-9]{4}"
        )
        return pattern.matcher(number).matches()
    }

    private fun enableSaveAlertButton() {
                binding.btnSubmitDetails.isEnabled = viewModel.tripDate.isNotNullOrEmpty()
                        && viewModel.origin != null
                        && viewModel.destination != null
                        && viewModel.selected_truck_type.isNotNullOrEmpty() &&
                        viewModel.selected_vehicle_number.isNotNullOrEmpty()
                        && viewModel.expectedPrice != null
                        && viewModel.expectedPrice.toString().trim().isNotNullOrEmpty()
                        && viewModel.expectedPrice!! > 0
                        && viewModel.selected_truck_type.isNotNullOrEmpty()
                        && viewModel.proofType.isNotNullOrEmpty()
                        && docUploadProof
    }

    private fun showTruckSizeDialog(type1: String, typ: String?) {
        val dialog = Dialog(this)
        val bindingDialog= DialogBottomTruckValueBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)


        bindingDialog.selectText.text = "Choose truck size & Capacity"
        bindingDialog.selectText2.visibility = View.VISIBLE
        bindingDialog.selectText2.text = "Choose Size \u2022 Step 1 of 2"

        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        val truckSizeList = mutableListOf<TruckResponseArray>()

        for(truck in (truckItems.sortedByDescending { it.truckUuid }.reversed()).sortedBy { it.defaultMG }){
                truckSizeList.add(truck)
        }
        adapter.setItems(truckSizeList)
        bindingDialog.truckList.adapter = this@ShareRateActivity.adapter

        bindingDialog.truckList.setOnItemClickListener { parent, view, position, id ->
            binding.textTruckSize.text = adapter.getItem(position).truckUuid
            sourcedAs = adapter.getItem(position).sourcedAs?: ""
            var min = adapter.getItem(position).minCapacity
            val max = adapter.getItem(position).maxCapacity
            viewModel.selected_truck_capacity = ""
            viewModel.selected_truck_type = adapter.getItem(position).truckUuid.toString()
            capacityArr.clear()
            if(min !=null &&  max!=null){
                while (min <= max) {
                    capacityArr.add("$min MT")
                    min += (1.0)
                }
            }
            adapter.notifyDataSetChanged()
            dialog.dismiss()
            showTruckCapacityDialog()
            enableSaveAlertButton()
        }


        if(type1.isNotNullOrEmpty()){
            var pos:Int=0
            for (truckSize in truckSizeList){
                if(truckSize.truckUuid.equals(type1)){
                    break
                }
                pos=pos+1

            }
            binding.textTruckSize.text = viewModel.selected_truck_type +" "+viewModel.selected_truck_capacity.toString()
            sourcedAs = adapter.getItem(pos).sourcedAs?: ""
            var min = adapter.getItem(pos).minCapacity
            val max = adapter.getItem(pos).maxCapacity
            capacityArr.clear()
            if(min !=null &&  max!=null){
                while (min <= max) {
                    capacityArr.add("$min MT")
                    min += (1.0)
                }
            }
            dialog.dismiss()
            enableSaveAlertButton()
        }

        if(typ!=null && typ.equals("fill")){
            binding.textTruckSize.text = viewModel.selected_truck_type.toString()
            var itemData:TruckResponseArray? = null
            for(r in truckSizeList){
                if(r.truckDisplayName.equals(viewModel.selected_truck_type)){
                    itemData = r
                }
            }
            sourcedAs = itemData?.sourcedAs?: ""
            var min = itemData?.minCapacity
            val max = itemData?.maxCapacity
            viewModel.selected_truck_capacity = ""
            //viewModel.selected_truck_type = adapter.getItem(position).truckUuid.toString()
            capacityArr.clear()
            if(min !=null &&  max!=null){
                while (min <= max) {
                    capacityArr.add("$min MT")
                    min += (1.0)
                }
            }
            adapter.notifyDataSetChanged()
            dialog.dismiss()
            showTruckCapacityDialog()
            enableSaveAlertButton()
        }else{
            if (!type1.isNotNullOrEmpty()) {
                dialog.show()
            }
        }

        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 800)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }

    private fun showTruckCapacityDialog() {
        val dialog = Dialog(this)
        val bindingDialog= DialogBottomTruckValueBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        bindingDialog.selectText.text = "Choose truck size & Capacity"
        bindingDialog.selectText2.visibility = View.VISIBLE
        bindingDialog.selectText2.text = "Choose Capacity \u2022 Step 2 of 2"

        bindingDialog.closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, capacityArr);
        bindingDialog.truckList.adapter = adapter
        bindingDialog.truckList.setOnItemClickListener { parent, view, position, id ->
            var str = binding.textTruckSize.text
            binding.textTruckSize.text = "$str "+adapter.getItem(position)
            viewModel.selected_truck_capacity =  if(adapter.getItem(position)?.isNotEmpty() == true)
                adapter.getItem(position).toString().split("\\s+".toRegex())[0].toDouble().toString() else "0.0"
            dialog.dismiss()
            enableSaveAlertButton()
        }
        adapter.notifyDataSetChanged();

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)

    }

    override fun onDateSet(
            p0: DatePicker?,
            year: Int,
            month: Int,
            day: Int
    ){
        calendar.set(year, month, day)
        val format = "%1$02d"
        val dy = String.format(format, day)
        val yr = String.format(format, year)
        val mt = String.format(format, month + 1)
        val dateTime = "$dy/$mt/$yr"
        binding.textDate.text = dateTime

        val time: Date = calendar.getTime()
        val outputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        outputFmt.setTimeZone(TimeZone.getTimeZone("UTC"))
        val dateAsString: String = outputFmt.format(time)
        viewModel.tripDate = dateAsString
        enableSaveAlertButton()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            mPhotoFile = createImageFile()
            val photoURI = FileProvider.getUriForFile(
                    this, BuildConfig.APPLICATION_ID + ".provider", mPhotoFile!!
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQCODE_TAKE_PHOTO)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(localImageName, ".jpg", storageDir)
    }

    private fun dispatchGalleryIntent() {

        val intent = Intent()
        intent.type = "*/*"
        val mimetypes = arrayOf("image/*", "application/pdf")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(
                intent,
                REQCODE_FILE_ATTACHMENTS
        )
    }

    private fun uploadImage(file: File) {
        uiUtils.showProgress()
        // Use document type based on context - this appears to be for rate sharing proof
        val docType = "letterhead" // or appropriate document type
        documentUtils.uploadDocument(file, FileType.IMAGE, docType, this)
    }

    private fun captureImage(
            uploadImageName: String,
            localImageName: String
    ) {
        this.uploadImageName = uploadImageName
        this.localImageName = localImageName

        val items = arrayOf<CharSequence>("Take Photo", "Choose from file", "Cancel")
        val builder = AlertDialog.Builder(this, R.style.DatePickerTheme)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Take Photo" -> requestImageCapturePermissions(true)
                items[item] == "Choose from file" -> requestImageCapturePermissions(false)
                items[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun requestImageCapturePermissions(isCamera: Boolean) {
        this.isCamera = isCamera
        compositeDisposable += requestPermission(
                arrayOf(
                        Manifest.permission.CAMERA
                ).apply {
                  if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                    plus(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
        )
                .onBackground()
                .subscribe { granted, error ->
                    if (error == null && granted) {
                        if (isCamera) {
                            dispatchTakePictureIntent()
                        } else {
                            dispatchGalleryIntent()
                        }
                    } else {
                        uiUtils.showSnackbar(getString(R.string.storage_camera_permission))
                    }
                }

    }


    private fun showFileSelected() {
        binding.uploadDocLay.visibility=View.GONE
        binding.docUploadedLay.visibility=View.VISIBLE
        docUploadProof=true
        binding.docTitle.setText(uploadArray.get(0).first)
        binding.docSize.setText(uploadArray.get(0).second + " KB")
        enableSaveAlertButton()
    }




    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQCODE_CAMERA -> {
                requestImageCapturePermissions(isCamera)
            }
        }
    }

    override fun onDocumentSuccess(downloadUrl: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Document uploaded successfully")
        // downloadUrl is the download URL returned from /document/upload API
        uploadArray.add(Pair(downloadUrl, (mPhotoFile?.length()?.div(1024)).toString()))
        showFileSelected()
        enableSaveAlertButton()
        resetUploadData()
    }

    override fun onDocumentFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Upload failed: $error")
        resetUploadData()
    }

    // Download functionality
    override fun onDocumentListSuccess(files: List<FileData>) {
        uiUtils.hideProgress()
        if (files.isNotEmpty()) {
            // Handle successful document list - show files to user
            showDocumentList(files)
        } else {
            uiUtils.showToast("No documents found")
        }
    }

    override fun onDocumentListFailure(error: String) {
        uiUtils.hideProgress()
        uiUtils.showToast("Failed to load documents: $error")
    }

    private fun showDocumentList(files: List<FileData>) {
        // Show list of available documents for download
        // This could be implemented as a dialog or list view
        val fileNames = files.map { it.filename }
        uiUtils.showToast("Found ${files.size} document(s): ${fileNames.joinToString(", ")}")
    }

    private fun downloadDocuments() {
        uiUtils.showProgress()
        val docType = "letterhead" // Document type for rate sharing
        documentUtils.listDocuments(docType, this)
    }
    private fun resetUploadData() {
        mPhotoFile = null
        uploadImageName = ""
        localImageName = ""
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQCODE_TAKE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (mPhotoFile == null) {
                        uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                        return
                    }

                    try {
                        mPhotoFile = imageUtils.compressToFile(mPhotoFile!!, localImageName)
                        uiUtils.showProgress()
                        uploadImage(mPhotoFile!!)
                    } catch (e: IOException) {
                        uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                    }
                } else {
                    uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                }
            }

            REQCODE_SELECT_CITY -> {
                if (data != null) {
                    val type = data.getStringExtra(CityType)
                    val city = data.getSerializable("City",CityModel::class.java)!!
                    if (type == "origin") {
                        viewModel.origin = city
                        binding.editOrigin.setText(city.cityName().trim())
                    } else if (type == "destination") {
                        viewModel.destination = city
                        binding.editDestination.setText(city.cityName().trim())
                    }
                }
            }

            REQCODE_FILE_ATTACHMENTS -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        val selectedFile = data?.data
                        require(selectedFile != null)
                        val parcelFileDescriptor =
                                contentResolver?.openFileDescriptor(selectedFile, "r", null)
                        require(parcelFileDescriptor != null)
                        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                        require(
                                contentResolver != null && contentResolver?.getFileName(selectedFile) != null
                        )
                        val imageScopedFile =
                                File(cacheDir, contentResolver?.getFileName(selectedFile)!!)
                        val outputStream = FileOutputStream(imageScopedFile)
                        inputStream.copyTo(outputStream, bufferSize = 16384)
                        this.uploadImageName = "IMG_" + System.currentTimeMillis() + "." + imageScopedFile.extension
                        this.localImageName = "IMG_" + System.currentTimeMillis() + "." + imageScopedFile.extension

                        if (imageScopedFile.extension == "jpg" || imageScopedFile.extension == "png" || imageScopedFile.extension == "jpeg") {
                            mPhotoFile = fileCompressor.compressToFile(File(imageScopedFile.path), localImageName)
                        } else if (imageScopedFile.extension == "pdf") {
                            mPhotoFile = imageScopedFile
                        } else {
                             // Block invalid file types
                             analyticsUtil.moEngageTrackEvent(
                                EVENT_DOC_UPLOADED_WITH_WRONG_EXTENSION,
                                mutableListOf(PROPERTY_USER_ID, PROPERTY_PHONE_NO, PROPERTY_TYPE_OF_DOC, PROPERTY_SOURCE_PAGE),
                                mutableListOf(userPrefs.userId(), userPrefs.phoneNumber.toString(), imageScopedFile.extension, "share_rate")
                            )
                            uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                            return
                        }

                        if (mPhotoFile == null) {
                            uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                            return
                        }
                        uiUtils.showProgress()
                        uploadImage(mPhotoFile!!)
                    } catch (e: IOException) {
                        uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                    }
                } else {
                    uiUtils.showToast(getString(R.string.msg_image_capture_failed))
                }
            }
        }
    }
}


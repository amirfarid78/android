package com.coheser.app.activitesfragments.videorecording

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AbsListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.location.AddressPlacesModel
import com.coheser.app.activitesfragments.location.SearchAddressActivity
import com.coheser.app.activitesfragments.shoping.SelectProductA
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.activitesfragments.videorecording.videothum.ThumbyActivity
import com.coheser.app.adapters.HashTagAdapter
import com.coheser.app.apiclasses.ApiClient
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.apiclasses.InterfaceFileUpload
import com.coheser.app.databinding.ActivityPostVideoBinding
import com.coheser.app.databinding.ItemProductBinding
import com.coheser.app.mainmenu.MainMenuActivity
import com.coheser.app.models.HashTagModel
import com.coheser.app.models.HomeModel
import com.coheser.app.models.UploadVideoModel
import com.coheser.app.models.UserModel
import com.coheser.app.services.VideoUploadWorker
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.DataHolder
import com.coheser.app.simpleclasses.FileUtils
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.simpleclasses.VideoThumbnailExtractor
import com.volley.plus.VPackages.VolleyRequest
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale


class PostVideoActivity : AppCompatLocaleActivity(), View.OnClickListener {

    var videoPath: String? = null
    var width: String? = null
    var height: String? = null
    var draftFile: String? = null
    var duetVideoId: String? = null
    var duetVideoUsername: String? = null
    var duetOrientation: String? = null
    var homeModel:HomeModel?=null
    var privcyType = "Public"
    var counter = -1
    var tagedUser = ArrayList<UserModel>()

    var placesModel=AddressPlacesModel()
    private var mFunctions: FirebaseFunctions? = null
    lateinit var binding: ActivityPostVideoBinding
    var editor:SharedPreferences.Editor?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Functions.setLocale(
            Functions.getSharedPreference(this)
                .getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE),
            this,
            javaClass,
            false
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_post_video)
        mFunctions = FirebaseFunctions.getInstance()
        binding.postBtn.setOnClickListener(this)
        binding.locLayout.setOnClickListener(this)

        editor=Functions.getSharedPreference(this).edit()
        editor?.putString(Variables.default_video_thumb,"")
        editor?.putString(Variables.selected_video_thumb,"")
        editor?.commit()



        val intent = intent
        if (intent != null) {
            if(intent.hasExtra("from")){
                if(intent.getStringExtra("from").equals("edit")) {
                    homeModel = DataHolder.instance?.data?.getParcelable("data")
                    setData()
                }
            }
            else {

                draftFile = intent.getStringExtra("draft_file")
                duetVideoId = intent.getStringExtra("duet_video_id")
                duetOrientation = intent.getStringExtra("duet_orientation")
                duetVideoUsername = intent.getStringExtra("duet_video_username")
                if (duetVideoUsername != null && duetVideoUsername != "") {
                    binding.duetLayoutUsername.visibility = View.VISIBLE
                    binding.duetUsername.text = duetVideoUsername

                    binding.duetLayout.visibility = View.GONE
                    binding.saveDraftBtn.visibility = View.GONE
                    binding.duetSwitch.setChecked(false)

                }

                videoPath = FileUtils.getAppFolder(this) + Variables.output_filter_file
                makeThumbnailOfVideo()

                getVideoSize()

                placesModel.title =  Functions.getSettingsPreference(binding.root.context).getString(Variables.currentLocation,"Location")
                placesModel.address =   Functions.getSettingsPreference(binding.root.context).getString(Variables.currentLocation,"Location")
                placesModel.lat=Functions.getSettingsPreference(binding.root.context).getString(Variables.DEVICE_LAT,"0.0")!!.toDouble()
                placesModel.lng = Functions.getSettingsPreference(binding.root.context).getString(Variables.DEVICE_LNG,"0.0")!!.toDouble()
                placesModel.latLng = LatLng(placesModel.lat,placesModel.lng)
                placesModel.placeId ="0"
                binding.locTitle.text=placesModel.title

            }
        }


        setAdapterForHashtag()
        binding.goBack.setOnClickListener(this)
        binding.privacyTypeLayout.setOnClickListener(this)
        binding.saveDraftBtn.setOnClickListener(this)
        binding.hashtagBtn.setOnClickListener(this)
        binding.tagUserBtn.setOnClickListener(this)
        binding.editCoverBtn.setOnClickListener(this)
        binding.selectProductLayout.setOnClickListener(this)

        KeyboardVisibilityEvent.setEventListener(
            this
        ) { isOpen ->
            if (isOpen) {
                binding.kayboardLayout.visibility = View.VISIBLE
            } else {
                binding.kayboardLayout.visibility = View.GONE
            }
        }

        binding.kayboardLayout.setOnClickListener(this)
        binding.aditionalDetailsTextCount.text = "0" + "/" + Constants.VIDEO_DESCRIPTION_CHAR_LIMIT
        binding.descriptionEdit.filters =
            arrayOf<InputFilter>(LengthFilter(Constants.VIDEO_DESCRIPTION_CHAR_LIMIT))
        binding.descriptionEdit.addTextChangedListener(textWatcher)


    }



    fun openEditThumb(){
        val intent = Intent(this, ThumbyActivity::class.java)
        intent.putExtra(ThumbyActivity.EXTRA_URI, Uri.parse(videoPath))
        intent.putExtra(ThumbyActivity.EXTRA_THUMBNAIL_POSITION, 0)
        editCoverCallback.launch(intent)
    }


    var editCoverCallback = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data

            val thumbnail = FileUtils.base64ToBitmap(Functions.getSharedPreference(this).getString(Variables.selected_video_thumb,""))

            if (duetVideoId != null) {

                VideoThumbnailExtractor.getThumbnailFromVideoFilePath(Functions.getAppFolder(this) + duetVideoId + ".mp4","1000") { thumbnailDuet ->
                    if (thumbnailDuet != null) {

                        val combined = combineImages(thumbnailDuet,thumbnail!!)
                        val bitmap = Bitmap.createScaledBitmap(combined, combined.width / 6, combined.height / 3, false)

                        thumbnail?.recycle()
                        combined.recycle()

                        binding!!.videoThumbnail.setImageBitmap(bitmap)
                        Functions.getSharedPreference(this).edit()
                            .putString(Variables.selected_video_thumb, FileUtils.bitmapToBase64(bitmap))
                            .commit()

                    }

                }

            }
            else {
                val bitmap = Bitmap.createScaledBitmap(
                    thumbnail!!,
                    thumbnail!!.width / 4,
                    thumbnail!!.height / 4,
                    false
                )
                thumbnail!!.recycle()
                binding!!.videoThumbnail.setImageBitmap(bitmap)

            }

        }
    }


    fun setData(){

        if(homeModel!=null) {

            Glide.with(this)
                .load(homeModel?.default_thumbnail)
                .thumbnail(0.4f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.image_placeholder)
                .into(binding.videoThumbnail)

            binding.editCoverBtn.visibility=View.GONE

            binding.descriptionEdit.setText(homeModel?.getVideoDescription())


            binding.privcyTypeTxt.text=homeModel?.privacy_type

            if(homeModel?.allow_comments.equals("1")){
                binding.commentSwitch.isChecked=true
            }
            else{
                binding.commentSwitch.isChecked=false
            }

            if(homeModel?.allow_duet.equals("1")){
                binding.duetSwitch.isChecked=true
            }
            else{
                binding.duetSwitch.isChecked=false
            }


            if(!TextUtils.isEmpty(homeModel?.lat) && (!homeModel?.lat.equals("null") && !homeModel?.lat.equals("0.0"))) {
                placesModel.title=homeModel?.location_name
                placesModel.address=homeModel?.location_string
                placesModel.lat = homeModel?.lat?.toDouble() ?: 0.0
                placesModel.lng = homeModel?.lng?.toDouble() ?: 0.0
                placesModel.latLng = LatLng(placesModel.lat, placesModel.lng);
                placesModel.placeId = homeModel?.placeId
                binding.locTitle.text=placesModel.title
            }


            binding.saveDraftBtn.visibility=View.GONE
            binding.postBtnTxt.text="Edit"

        }

    }

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, count: Int) {

                if (binding!!.descriptionEdit.length() > counter) {
                    counter = binding!!.descriptionEdit.length()
                    if (binding!!.descriptionEdit.length() > 0) {
                        val lastChar = charSequence.toString().substring(charSequence.length - 1)
                        if (lastChar == " ") {
                            findViewById<View>(R.id.hashtag_layout).visibility = View.GONE
                        } else if (lastChar == "#") {
                            findViewById<View>(R.id.hashtag_layout).visibility = View.VISIBLE
                        } else if (lastChar == "@") {
                            openFriends("@friends")
                        }
                        val hash_tags = binding!!.descriptionEdit.text.toString()
                        val separated =
                            hash_tags.split("#".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        for (item in separated) {
                            if (item != null && item != "") {
                                if (item.contains(" ")) {
                                    //stop calling api
                                } else {
                                    val string1 = item.replace("#", "")
                                    pageCount = 0
                                    callApiForHashTag(string1)
                                }
                            }
                        }
                    } else {
                        binding.hashtagLayout.visibility = View.GONE
                    }
                } else {
                    if (binding!!.descriptionEdit.length() == 1) {
                        counter = -1
                    } else {
                        counter--
                    }
                }

            binding!!.aditionalDetailsTextCount.text =
                binding!!.descriptionEdit.text!!.length.toString() + "/" + Constants.VIDEO_DESCRIPTION_CHAR_LIMIT
        }

        override fun afterTextChanged(editable: Editable) {}
    }


    fun getVideoSize() {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoPath)
        width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
    }

    private fun makeThumbnailOfVideo() {
        VideoThumbnailExtractor.getThumbnailFromVideoFilePath(videoPath,"1000") { thumbnail ->
            if (thumbnail != null) {
                makeDifferentTypeThumbnail(thumbnail)
            }
        }
    }

    private fun makeDifferentTypeThumbnail(thumbnail: Bitmap) {

        if (duetVideoId != null) {
            VideoThumbnailExtractor.getThumbnailFromVideoFilePath(Functions.getAppFolder(this) + duetVideoId + ".mp4","1000") { thumbnailDuet ->
                if (thumbnailDuet != null) {

                    val combined = combineImages(thumbnailDuet,thumbnail)
                    val bitmap = Bitmap.createScaledBitmap(combined, combined.width / 6, combined.height / 3, false)

                    thumbnail.recycle()
                    combined.recycle()

                    binding!!.videoThumbnail.setImageBitmap(bitmap)


                    Functions.getSharedPreference(this@PostVideoActivity)
                        .edit()
                        .putString(Variables.default_video_thumb, Functions.bitmapToBase64(bitmap))
                        .commit()

                }

            }
        }
        else {

            val bitmap = Bitmap.createScaledBitmap(
                thumbnail,
                thumbnail.width / 3,
                thumbnail.height / 3,
                false
            )
            thumbnail.recycle()
            binding!!.videoThumbnail.setImageBitmap(bitmap)

            Functions.getSharedPreference(this).edit()
                .putString(Variables.default_video_thumb, FileUtils.bitmapToBase64(bitmap))
                .commit()
        }

    }



    fun combineImages(c: Bitmap, s: Bitmap): Bitmap {
        val width: Int
        val height: Int

        if (c.width > s.width) {
            width = c.width + s.width
            height = c.height
        } else {
            width = s.width + s.width
            height = c.height
        }

        val cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val comboImage = Canvas(cs)

        comboImage.drawBitmap(c, 0f, 0f, null)
        comboImage.drawBitmap(s, c.width.toFloat(), 0f, null)

        return cs
    }




    override fun onClick(v: View) {
        when (v.id) {
            R.id.goBack -> onBackPressed()

            R.id.editCoverBtn -> {
                openEditThumb()
            }

            R.id.privacy_type_layout -> privacyDialog()
            R.id.kayboardLayout -> Functions.hideSoftKeyboard(this@PostVideoActivity)
            R.id.save_draft_btn -> saveFileInDraft()
            R.id.post_btn -> {
                makeMentionArrays()

                if(homeModel!=null){
                    callApiForEditVideo()
                }
                else {
                    enqueueVideoUpload()
                }

            }

            R.id.hashtag_btn -> {
                binding.hashtagLayout.visibility = View.VISIBLE
                binding!!.descriptionEdit.setText(binding!!.descriptionEdit.text.toString() + " #")
                binding!!.descriptionEdit.setSelection(binding!!.descriptionEdit.text!!.length)
                pageCount = 0
                callApiForHashTag("")
            }

            R.id.tag_user_btn -> openFriends("@friends")

            R.id.loc_layout -> {
                val intent = Intent(this, SearchAddressActivity::class.java)
                intent.putExtra("from","select")
                someActivityResultLauncher.launch(intent)
            }

            R.id.select_product_layout->{
                val intent = Intent(this@PostVideoActivity, SelectProductA::class.java)
                resultCallback.launch(intent)
            }

        }
    }


    var selectList = ArrayList<ProductModel>()
    val resultCallback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data

            val productModel : ProductModel ?=data?.getParcelableExtra("data")
            productModel?.let { newProduct->
                val index = selectList.indexOfFirst { it.product.id == newProduct.product.id }
                if (index != -1){
                    selectList[index] = newProduct
                }else{
                    selectList.add(newProduct)
                }
            }
            populateDataList()
        }
    }

    private fun populateDataList() {

        binding.chipGroup.visibility=View.VISIBLE
        binding.chipGroup.removeAllViews()

        for (i in selectList.indices) {
            val itemModel = selectList[i]

            val itemBinding=ItemProductBinding.inflate(LayoutInflater.from(binding.root.context))
            itemBinding.tvTag.text = "" + itemModel.product.taggedName
            itemBinding.tabTag.tag = i
            itemBinding.tvTag.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.black
                )
            )
            itemBinding.crossBtn.setOnClickListener { v: View? ->

                if (selectList.contains(itemModel)) {
                    selectList.remove(itemModel)
                    populateDataList()
                }
            }

            binding.chipGroup.addView(itemBinding.root)
        }

    }


    var someActivityResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null) {
                 placesModel= data!!.getParcelableExtra<AddressPlacesModel>("data")!!
                binding!!.locTitle.text = placesModel.title
                printLog(Constants.tag, "${placesModel.lat},${placesModel.lng}, ${placesModel.title}")
            }
        }
    }


    var hashList = ArrayList<HashTagModel>()
    var recyclerView: RecyclerView? = null
    var pageCount = 0
    var ispostFinsh = false
    var loadMoreProgress: ProgressBar? = null
    var linearLayoutManager: LinearLayoutManager? = null
    var hashtag_adapter: HashTagAdapter? = null
    // call api for get all the hash tag that can be selected before post the video
    fun callApiForHashTag(lastChar: String) {
        val params = JSONObject()
        try {
            params.put("type", "hashtag")
            params.put("keyword", lastChar)
            params.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this,
            ApiLinks.search,
            params,
            Functions.getHeaders(this)
        ) { resp ->
            Functions.checkStatus(this@PostVideoActivity, resp)
            Functions.cancelLoader()
            try {
                val response = JSONObject(resp)
                val code = response.optString("code")
                if (code.equals("200", ignoreCase = true)) {
                    val msgArray = response.optJSONArray("msg")
                    val temp_list = ArrayList<HashTagModel>()
                    for (i in 0 until msgArray.length()) {
                        val itemdata = msgArray.optJSONObject(i)
                        val Hashtag = itemdata.optJSONObject("Hashtag")
                        val item = HashTagModel()
                        item.id = Hashtag.optString("id")
                        item.name = Hashtag.optString("name")
                        item.videos_count = Hashtag.optString("videos_count")
                        temp_list.add(item)
                    }
                    if (pageCount == 0) {
                        hashList.clear()
                        hashList.addAll(temp_list)
                    } else {
                        hashList.addAll(temp_list)
                    }
                    hashtag_adapter!!.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loadMoreProgress!!.visibility = View.GONE
            }
        }
    }


    // set the hashtag adapter to recycler view
    private fun setAdapterForHashtag() {
        loadMoreProgress = findViewById(R.id.load_more_progress)
        recyclerView = findViewById(R.id.hashtag_recylerview)
        linearLayoutManager = LinearLayoutManager(this@PostVideoActivity)
        linearLayoutManager!!.orientation = RecyclerView.VERTICAL
        binding.hashtagRecylerview.setLayoutManager(linearLayoutManager)
        binding.hashtagRecylerview.setHasFixedSize(true)
        hashtag_adapter = HashTagAdapter(this@PostVideoActivity, hashList) { view, pos, `object` ->
            val item = `object` as HashTagModel
            findViewById<View>(R.id.hashtag_layout).visibility = View.GONE
            val sb = StringBuilder()
            val desc = binding!!.descriptionEdit.text.toString()
            if (desc.length > 0) {
                val bits = desc.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val lastOne = bits[bits.size - 1]
                val newString = lastOne.replace(lastOne, item.name)
                for (i in 0 until bits.size - 1) {
                    sb.append(bits[i] + " ")
                }
                sb.append("#$newString ")
            }
            binding!!.descriptionEdit.setText(sb)
            binding!!.descriptionEdit.setSelection(binding!!.descriptionEdit.text!!.length)
        }
        binding.hashtagRecylerview.setAdapter(hashtag_adapter)
        binding.hashtagRecylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var userScrolled = false
            var scrollOutitems = 0
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrollOutitems = linearLayoutManager!!.findLastVisibleItemPosition()
                Functions.printLog("resp", "" + scrollOutitems)
                if (userScrolled && scrollOutitems == hashList.size - 1) {
                    userScrolled = false
                    if (binding.loadMoreProgress.getVisibility() != View.VISIBLE && !ispostFinsh) {
                        binding.loadMoreProgress.setVisibility(View.VISIBLE)
                        pageCount = pageCount + 1
                        callApiForHashTag("")
                    }
                }
            }
        })
    }


    // open the follower list of the profile for mention them during post video
    fun openFriends(from: String?) {
        val intent = Intent(this@PostVideoActivity, FriendsActivity::class.java)
        intent.putExtra("id", Functions.getSharedPreference(this).getString(Variables.U_ID, ""))
        intent.putExtra("from", from)
        resultFriendsCallback.launch(intent)

        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)

    }


    var resultFriendsCallback = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
                val arrayList = data.getSerializableExtra("data") as ArrayList<UserModel>?
                for (i in arrayList!!.indices) {
                    val item = arrayList[i]
                    tagedUser.add(item)
                    var lastChar: String? = null
                    if (!TextUtils.isEmpty(binding!!.descriptionEdit.text.toString())) lastChar =
                        binding!!.descriptionEdit.text.toString().substring(
                            binding!!.descriptionEdit.text!!.length - 1
                        )
                    if (lastChar != null && lastChar.contains("@")) binding!!.descriptionEdit.setText(
                        binding!!.descriptionEdit.text.toString() + item.username + " "
                    ) else binding!!.descriptionEdit.setText(
                        binding!!.descriptionEdit.text.toString() + "@" + item.username + " "
                    )
                    binding!!.descriptionEdit.setSelection(binding!!.descriptionEdit.text!!.length)
                }
            }
        }
    }

    // show the option that is you want to make video public or private
    private fun privacyDialog() {
        val options =
            arrayOf<CharSequence>(getString(R.string.public_), getString(R.string.private_))
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        builder.setTitle(null)
        builder.setItems(options) { dialog, item ->
            binding!!.privcyTypeTxt.text = options[item]
            privcyType = if (item == 0) {
                "Public"
            } else {
                "Private"
            }
        }
        builder.show()
    }

    var hashTag: JSONArray? = null
    var friendsTag: JSONArray? = null
    var product_json: JSONArray? = null
    fun makeMentionArrays() {
        hashTag = JSONArray()
        friendsTag = JSONArray()
        val tagList = HashMap<String, String>()
        val separated = binding!!.descriptionEdit.text.toString().split(" ".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        for (item in separated) {
            if (item != null && item != "") {
                if (item.contains("#")) {
                    val string1 = item.replace("#", "")
                    val tag_object = JSONObject()
                    try {
                        if (!tagList.containsKey(("" + string1).lowercase(Locale.getDefault()))) {
                            tagList[("" + string1).lowercase(Locale.getDefault())] =
                                ("" + string1).lowercase(
                                    Locale.getDefault()
                                )
                            tag_object.put("name", ("" + string1).lowercase(Locale.getDefault()))
                            hashTag!!.put(tag_object)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (item.contains("@")) {
                    val string1 = item.replace("@", "")
                    val tag_object = JSONObject()
                    try {
                        for (user_model in tagedUser) {
                            if (user_model.username!!.contains(string1)) {
                                tag_object.put("user_id", user_model.id)
                                friendsTag!!.put(tag_object)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        product_json = JSONArray()
        Log.d(Constants.tag,"datasize :${selectList.size}" )
        try {
            if (selectList.size > 0)
                for (productModel in selectList) {
                    val productjsonObj = JSONObject()
                    productjsonObj.put("product_id", productModel.product.id)
                    productjsonObj.put("title", productModel.product.taggedName)
                    product_json!!.put(productjsonObj)
                }
        } catch (e: Exception) {
            Log.d(Constants.tag,e.printStackTrace().toString())
        }
        printLog(Constants.tag, "product Json :$product_json")
        printLog(Constants.tag, hashTag.toString())
        printLog(Constants.tag, friendsTag.toString())

    }


    fun setDataToModel():UploadVideoModel{

        val uploadModel = UploadVideoModel()
        uploadModel.userId = Functions.getSharedPreference(applicationContext)
            .getString(Variables.U_ID, "0")
        uploadModel.soundId = Variables.selectedSoundId
        uploadModel.description = binding!!.descriptionEdit.text.toString()
        uploadModel.privacyPolicy = privcyType
        if (binding!!.commentSwitch.isChecked) {
            uploadModel.allowComments = "1"
        } else {
            uploadModel.allowComments = "0"
        }

        if (binding!!.duetSwitch.isChecked) {
            uploadModel.allowDuet = "1"
        } else {
            uploadModel.allowDuet = "0"
        }

        uploadModel.hashtagsJson = hashTag.toString()
        uploadModel.usersJson = friendsTag.toString()
        uploadModel.product_json = product_json.toString()
        uploadModel.placesModel=placesModel;
        uploadModel.allowDuet = "0"
        uploadModel.width = width
        uploadModel.height = height
        if (duetVideoId != null) {
            uploadModel.videoId = duetVideoId
            uploadModel.duet = "" + duetOrientation
        }
        else {
            uploadModel.videoId = "0"
        }
        uploadModel.videoType = "0"


        return uploadModel;
    }


    fun callApiForEditVideo(){
        val uploadModel=setDataToModel();

        val gson = Gson()
        val jsonString = gson.toJson(uploadModel)
        printLog(Constants.tag, jsonString.toString())
        printLog(Constants.tag,"product json"+ uploadModel!!.product_json)
        Functions.copyTextToClipboard(this@PostVideoActivity,""+uploadModel!!.product_json)

        val interfaceFileUpload = ApiClient.getRetrofitInstance(this)
            .create<InterfaceFileUpload>(InterfaceFileUpload::class.java)

        val PrivacyType: RequestBody = RequestBody.create(MultipartBody.FORM, uploadModel!!.privacyPolicy!!)
        val UserId: RequestBody = RequestBody.create(MultipartBody.FORM, uploadModel!!.userId!!)
        val AllowComments: RequestBody = RequestBody.create(MultipartBody.FORM, uploadModel!!.allowComments!!)
        val Description: RequestBody = RequestBody.create(MultipartBody.FORM, uploadModel!!.description!!)
        val AllowDuet: RequestBody = RequestBody.create(MultipartBody.FORM, uploadModel!!.allowDuet!!)
        val UsersJson: RequestBody = RequestBody.create(MultipartBody.FORM, uploadModel!!.usersJson!!)
        val HashtagsJson: RequestBody = RequestBody.create(MultipartBody.FORM, uploadModel!!.hashtagsJson!!)
        val productJson: RequestBody = RequestBody.create(MultipartBody.FORM, uploadModel!!.product_json!!)
        val locationString: RequestBody = RequestBody.create(MultipartBody.FORM,"" + uploadModel!!.placesModel!!.address)
        val lat: RequestBody = RequestBody.create(MultipartBody.FORM, "" + uploadModel!!.placesModel!!.lat)
        val lng: RequestBody = RequestBody.create(MultipartBody.FORM, "" + uploadModel!!.placesModel!!.lng)
        val placeId: RequestBody = RequestBody.create(MultipartBody.FORM, "" + uploadModel!!.placesModel!!.placeId)
        val locationName: RequestBody = RequestBody.create(MultipartBody.FORM,"" + uploadModel!!.placesModel!!.title)
        val videoId: RequestBody = RequestBody.create(MultipartBody.FORM, homeModel!!.video_id!!)
        val tagStoreId: RequestBody = RequestBody.create(MultipartBody.FORM, uploadModel!!.tagStoreId!!)

        var fileUpload = interfaceFileUpload.editVideo(
                PrivacyType,
                UserId,
                AllowComments,
                Description,
                AllowDuet,
                UsersJson,
                HashtagsJson,
                videoId,
                locationString,
                lat,
                lng,
                placeId,
                locationName,
                productJson,
               tagStoreId
            )

        Functions.showLoader(this,false,false)
        fileUpload.enqueue(object : Callback<Any?> {
            override fun onResponse(
                call: Call<Any?>,
                response: Response<Any?>
            ) {
                Functions.cancelLoader()
                val bodyRes = Gson().toJson(response.body())
                Log.d(Constants.tag, "Responce: $bodyRes")
                try {
                    val jsonObject = JSONObject(bodyRes)
                    val code = jsonObject.optInt("code", 0)
                    if (code == 200) {

                        val intent = Intent()
                        intent.putExtra("isShow", true)
                        setResult(RESULT_OK, intent)
                        finish()

                    }
                } catch (e: java.lang.Exception) {
                    Log.d(Constants.tag, "Exception :$e")
                }
            }

            override fun onFailure(call: Call<Any?>, t: Throwable) {
                Functions.cancelLoader()
                Log.d(Constants.tag, "Exception onFailure :$t")

            }
        })


    }


    fun enqueueVideoUpload() {

        val uploadModel=setDataToModel()

        val bundle=Bundle()
        bundle.putString("uri", videoPath)
        bundle.putString("draft_file", draftFile)
        bundle.putParcelable("data", uploadModel)
        DataHolder.instance?.data=bundle

        val inputData = Data.Builder()
            .build()

        val uploadWorkRequest = OneTimeWorkRequestBuilder<VideoUploadWorker>()
            .setInputData(inputData)
            .addTag("videoUpload")
            .build()
        WorkManager.getInstance(this).enqueue(uploadWorkRequest)

        runOnUiThread {
            val intent = Intent(this@PostVideoActivity, MainMenuActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

    }

    override fun onBackPressed() {
        val count = this.supportFragmentManager.backStackEntryCount
        if (count > 0) {
            this.supportFragmentManager.popBackStack()
        } else {
            finish()
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right)
        }
    }

    // save the file into the draft
    fun saveFileInDraft() {
        val source = File(videoPath)
        val destination =
            File(FileUtils.getAppFolder(this) + Variables.DRAFT_APP_FOLDER + Functions.getRandomString(5) + ".mp4")
        try {
            if (source.exists()) {
                val `in`: InputStream = FileInputStream(source)
                val out: OutputStream = FileOutputStream(destination)
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                `in`.close()
                out.close()
                Toast.makeText(
                    this@PostVideoActivity,
                    getString(R.string.file_save_in_draft),
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this@PostVideoActivity, MainMenuActivity::class.java))
            } else {
                Toast.makeText(this@PostVideoActivity, getString(R.string.file_save_in_draft), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}

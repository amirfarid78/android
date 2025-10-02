package com.coheser.app.activitesfragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AbsListView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.adapters.MyVideosAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.ActivityLocationVideosBinding
import com.coheser.app.databinding.LayoutForMapTagedVideosBottomTabsBinding
import com.coheser.app.interfaces.onDrawableCallback
import com.coheser.app.models.HomeModel
import com.coheser.app.simpleclasses.*
import com.coheser.app.simpleclasses.mapclasses.MapWorker
import com.coheser.app.viewModels.NearByVideoViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.math.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class NearbyVideosActivity : AppCompatLocaleActivity(), OnMapReadyCallback,
    GoogleMap.OnCameraMoveListener {
    var dataList= mutableListOf<HomeModel>()
    lateinit var adapter: MyVideosAdapter

    var linearLayoutManager: GridLayoutManager? = null
    lateinit var binding: ActivityLocationVideosBinding
    lateinit var tabsBinding: LayoutForMapTagedVideosBottomTabsBinding

    var mGoogleMap: GoogleMap? = null
    var mapWorker: MapWorker? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    var videoMarkersList: ArrayList<Marker> = ArrayList()
    private val earthradius = 6366198.0


    private val viewModel: NearByVideoViewModel  by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Functions.setLocale(
            Functions.getSharedPreference(this@NearbyVideosActivity)
                .getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE),
            this,
            javaClass,
            false
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_videos)

        binding.lifecycleOwner = this

        tabsBinding = binding.bottomSheet.includedLayout
        tabsBinding.shimmerList.shimmerViewContainer.startShimmer()
        binding.mapView.onCreate(savedInstanceState)

        viewModel.placeId = intent.getStringExtra("placeId").toString()
        viewModel.lat = intent.getStringExtra("lat").toString()
        viewModel.lng = intent.getStringExtra("lng").toString()
        viewModel.locImage = intent.getStringExtra("locImage")!!

        binding.bottomSheet.locationNameTxt.text = intent.getStringExtra("locationName")
        binding.bottomSheet.locationTxt.text = intent.getStringExtra("subCategory")
        linearLayoutManager = GridLayoutManager(binding.root.context, 3)
        tabsBinding.recylerview.layoutManager = linearLayoutManager
        tabsBinding.recylerview.setHasFixedSize(true)

        adapter =
            MyVideosAdapter(binding.root.context, dataList, "location") { view, pos, `object` ->
                openWatchVideo(pos)
            }
        tabsBinding.recylerview.adapter = adapter
        tabsBinding.recylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                linearLayoutManager?.let {
                    scrollOutitems = it.findLastVisibleItemPosition()
                }
                Functions.printLog("resp", "" + scrollOutitems)

                if (userScrolled && scrollOutitems == dataList.size - 1) {
                    userScrolled = false
                    if (tabsBinding.loadMoreProgress.visibility != View.VISIBLE && !viewModel.ispostFinsh) {
                        tabsBinding.loadMoreProgress.visibility = View.VISIBLE
                        viewModel.pageCount.set(viewModel.pageCount.get()+1)
                        viewModel.showVideosAgainstLocation()
                    }
                }
            }
        })

        binding.mapView.onResume()
        binding.mapView.getMapAsync(this@NearbyVideosActivity)
        setupMapIfNeeded()
        methodInitLayouts()
        setObserver()
        viewModel.showVideosAgainstLocation()

        actionControl()

    }

    fun setObserver(){
        viewModel.videosLiveData.observe(this,{
            when(it){
                is ApiResponce.Loading ->{}
                is ApiResponce.Success ->{
                    it.data?.let {
                        if (it != null) {
                            if(viewModel.pageCount.get()==0){
                                dataList.clear()
                            }
                            dataList.addAll(it)
                            adapter.notifyDataSetChanged()
                        }
                        changeUi()
                    }
                }
                is ApiResponce.Error ->{
                    changeUi()
                }
            }
        })
    }


    fun changeUi(){
        if (dataList.isEmpty()) {
            tabsBinding.noDataLayout.visibility = View.VISIBLE
        } else {
            tabsBinding.noDataLayout.visibility = View.GONE
        }
        tabsBinding.shimmerList.shimmerViewContainer.visibility = View.GONE
        tabsBinding.loadMoreProgress.visibility = View.GONE
        loadPins()
    }



    private fun actionControl() {
        binding.ivBack.setOnClickListener(DebounceClickHandler {
            onBackPressed()
        })
    }

    private fun setupMapIfNeeded() {
        if (mGoogleMap == null) {
            MapsInitializer.initialize(this@NearbyVideosActivity)
            binding.mapView.onResume()
            binding.mapView.getMapAsync(this@NearbyVideosActivity)
        }
    }

    private fun methodInitLayouts() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.root)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    Log.d(Constants.tag, "STATE_DRAGGING")
                    val screenHeight = resources.displayMetrics.heightPixels
                    bottomSheetBehavior.peekHeight = (screenHeight * 0.2f).toInt()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset > 0.25 && slideOffset < 0.75) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                } else if (slideOffset >= 0.75) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                } else {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }

                val customWidth =
                    binding.root.context.resources.getDimension(R.dimen._250sdp).toInt()
                val newHeight = customWidth + ((1 - slideOffset) * customWidth).toInt()
                if (binding.mapView.layoutParams.height < newHeight) {
                    binding.mapView.layoutParams.height = newHeight
                    binding.mapView.requestLayout()
                }
            }
        })
        bottomSheetBehavior.halfExpandedRatio = 0.7f
    }


    private fun loadPins() {
        for (item in dataList) {
            mapWorker?.let { worker ->
                Log.d(
                    Constants.tag, "abc maker${(item.lat)!!.toDouble()}==${(item.lng)!!.toDouble()}"
                )
                addMarkerPin(item, object : onDrawableCallback {
                    override fun onResult(drawable: Drawable) {
                        val userMarker = getMyMarkerPinView(drawable, "${item.userModel?.username}")
                        val marker: Marker = worker.addMarker(
                            dataList.indexOf(item),
                            item.userModel?.username,
                            LatLng((item.lat)!!.toDouble(), (item.lng)!!.toDouble()),
                            userMarker
                        )
                        videoMarkersList.add(marker)
                        if (dataList.indexOf(dataList.last()) == dataList.indexOf(item)) {
                            showLatLngBoundZoom(videoMarkersList)
                        }
                    }
                })
            }
        }
    }

    fun getMyMarkerPinView(drawableImage: Drawable?, userName: String): Bitmap {
        val customMarkerView =
            (binding.root.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                    R.layout.view_custom_current_marker,
                    null
                )
        val profileImg = customMarkerView.findViewById<CircleImageView>(R.id.img_pin_profile)
        val tvUsername = customMarkerView.findViewById<TextView>(R.id.tvUsername)
        val tvPinProfile = customMarkerView.findViewById<TextView>(R.id.tv_pin_profile)
        tvUsername.text = userName
        if (drawableImage == null) {
            profileImg.visibility = View.GONE
            tvPinProfile.visibility = View.VISIBLE
            tvPinProfile.text = userName
        } else {
            profileImg.setImageDrawable(drawableImage)
            profileImg.visibility = View.VISIBLE
            tvPinProfile.visibility = View.GONE
        }

        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        customMarkerView.layout(
            0, 0, customMarkerView.measuredWidth, customMarkerView.measuredHeight
        )
        customMarkerView.buildDrawingCache()

        val returnedBitmap = Bitmap.createBitmap(
            customMarkerView.measuredWidth, customMarkerView.measuredHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(returnedBitmap)
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)

        val drawable = customMarkerView.background
        drawable?.draw(canvas)
        customMarkerView.draw(canvas)

        Log.d(Constants.tag, "URL : 22")
        return returnedBitmap
    }

    private fun addMarkerPin(item: HomeModel, callback: onDrawableCallback) {
        Glide.with(binding.root.context).load(item.getThum())
            .placeholder(R.drawable.ic_profile_gray).error(R.drawable.ic_profile_gray)
            .diskCacheStrategy(DiskCacheStrategy.ALL).dontAnimate()
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable, transition: Transition<in Drawable>?
                ) {
                    callback.onResult(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    placeholder?.let {
                        callback.onResult(it)
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    errorDrawable?.let {
                        callback.onResult(it)
                    }
                }
            })
    }

    private fun showLatLngBoundZoom(videoMarkersList: ArrayList<Marker>) {
        if (videoMarkersList.isEmpty()) {
            return
        }

        val latlngBuilder = LatLngBounds.Builder()
        for (mrk in videoMarkersList) {
            try {
                mrk.position?.let { latlngBuilder.include(it) }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
            }
        }

        val bounds = latlngBuilder.build()

        val center = bounds.center
        val northEast = move(center, 709.0, 709.0)
        val southWest = move(center, -709.0, -709.0)
        latlngBuilder.include(southWest)
        latlngBuilder.include(northEast)

        if (areBoundsTooSmall(bounds, 300)) {
            mGoogleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    bounds.center, Constants.mapZoomLevel
                )
            )
        } else {
            mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
        }
    }

    private fun areBoundsTooSmall(bounds: LatLngBounds, minDistanceInMeter: Int): Boolean {
        val result = FloatArray(1)
        Location.distanceBetween(
            bounds.southwest.latitude,
            bounds.southwest.longitude,
            bounds.northeast.latitude,
            bounds.northeast.longitude,
            result
        )
        return result[0] < minDistanceInMeter
    }

    private fun move(startLL: LatLng, toNorth: Double, toEast: Double): LatLng {
        val lonDiff = meterToLongitude(toEast, startLL.latitude)
        val latDiff = meterToLatitude(toNorth)
        return LatLng(startLL.latitude + latDiff, startLL.longitude + lonDiff)
    }

    private fun meterToLongitude(meterToEast: Double, latitude: Double): Double {
        val latArc = Math.toRadians(latitude)
        val radius = Math.cos(latArc) * earthradius
        val rad = meterToEast / radius
        return Math.toDegrees(rad)
    }

    private fun meterToLatitude(meterToNorth: Double): Double {
        val rad = meterToNorth / earthradius
        return Math.toDegrees(rad)
    }

    private fun openWatchVideo(postion: Int) {
        val intent = Intent(this@NearbyVideosActivity, WatchVideosActivity::class.java)
        val args = Bundle()
        args.putParcelableArrayList("arraylist",  ArrayList(dataList))
        DataHolder.instance?.data = args

        intent.putExtra("position", postion)
        intent.putExtra("locationID", viewModel.placeId)
        intent.putExtra("pageCount", viewModel.pageCount.get())
        intent.putExtra(
            "userId",
            Functions.getSharedPreference(this@NearbyVideosActivity).getString(Variables.U_ID, "")
        )
        intent.putExtra("whereFrom", "location")
        try {
            resultCallback.launch(intent)
        } catch (e: Exception) {
            startActivity(intent)
        }
    }

    var resultCallback = registerForActivityResult(StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult?> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result!!.resultCode == RESULT_OK) {
                    val data = result!!.data
                    if (data!!.getBooleanExtra("isShow", false)) {
                        val bundle = DataHolder.instance?.data
                        if (bundle != null) {
                            val arrayList =
                                bundle.getSerializable("arraylist") as ArrayList<HomeModel>?
                            dataList.clear()
                            dataList.addAll(arrayList!!)
                        }
                        viewModel.pageCount.set(data.getIntExtra("pageCount", 0))
                        adapter!!.notifyDataSetChanged()
                    }
                }
            }
        })

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap?.let {
            mapWorker = MapWorker(binding.root.context, it)
        }

        mGoogleMap?.apply {
            setMapStyle(MapStyleOptions.loadRawResourceStyle(binding.root.context, R.raw.gray_map))

            if (ActivityCompat.checkSelfPermission(
                    binding.root.context, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    binding.root.context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Handle permission request
                return
            }

            isMyLocationEnabled = true
            uiSettings.apply {
                isZoomControlsEnabled = false
                isMapToolbarEnabled = false
                isMyLocationButtonEnabled = false
                isRotateGesturesEnabled = false
                isTiltGesturesEnabled = false
                isMyLocationButtonEnabled = false
            }

            setOnCameraMoveListener(this@NearbyVideosActivity)
            setOnMapLoadedCallback {
                // Additional map setup
            }
        }
    }

    private fun haversineDistance(start: LatLng, end: LatLng): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLng = Math.toRadians(end.longitude - start.longitude)

        val a =
            sin(dLat / 2).pow(2.0) + cos(Math.toRadians(start.latitude)) * cos(Math.toRadians(end.latitude)) * sin(
                dLng / 2
            ).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }


    override fun onCameraMove() {
        val zoom = mGoogleMap!!.cameraPosition.zoom
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}

package com.coheser.app.activitesfragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.adapters.ShowPlaylistAdapter
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentShowHomePlaylistBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.HomeModel
import com.coheser.app.models.PlaylistHomeModel
import com.coheser.app.simpleclasses.DataParsing.parseVideoData
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import com.volley.plus.interfaces.Callback
import org.json.JSONException
import org.json.JSONObject

class ShowHomePlaylistF : BottomSheetDialogFragment, View.OnClickListener {
    var dataList: ArrayList<HomeModel> = ArrayList()
    var adapter: ShowPlaylistAdapter? = null
    var platlistId: String? = null
    var userId: String? = null
    var videoId: String? = null
    var playlistName: String? = null

    var callback: FragmentCallBack? = null
    var playlistMapList: HashMap<String?, HomeModel> = HashMap()
    var binding: FragmentShowHomePlaylistBinding? = null


    constructor(
        videoId: String?,
        platlistId: String?,
        userId: String?,
        playlistName: String?,
        callback: FragmentCallBack?
    ) {
        this.videoId = videoId
        this.platlistId = platlistId
        this.userId = userId
        this.playlistName = playlistName
        this.callback = callback
    }

    constructor()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentShowHomePlaylistBinding.inflate(
            layoutInflater, container, false
        )

        binding!!.ivOption.setOnClickListener(this)
        binding!!.ivBack.setOnClickListener(this)


        setupScreenData()
        return binding!!.root
    }


    // api for get the videos list from server
    private fun callApiForPlaylistVideos(platlistId: String?) {
        val parameters = JSONObject()
        try {
            parameters.put("id", platlistId)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.showPlaylists, parameters, getHeaders(
                activity
            ), Callback { resp ->
                checkStatus(activity, resp)
                binding!!.progressBar.visibility = View.GONE
                parsePlalistVideoData(resp)
            })
    }

    fun parsePlalistVideoData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if ((code == "200")) {
                val msg = jsonObject.optJSONObject("msg")
                val temp_list = ArrayList<HomeModel>()


                val public_array = msg.optJSONArray("PlaylistVideo")


                for (i in 0 until public_array.length()) {
                    val itemdata = public_array.optJSONObject(i)

                    val video = itemdata.optJSONObject("Video")
                    val sound = video.optJSONObject("Sound")
                    val user = video.optJSONObject("User")
                    val location = video.optJSONObject("Location")
                    val store = video.optJSONObject("Store")
                    val videoProduct = video.optJSONObject("Product")
                    val userPrivacy = user.optJSONObject("PrivacySetting")
                    val pushNotification = user.optJSONObject("PushNotification")
                    val item = parseVideoData(
                        user,
                        sound,
                        video,
                        location,
                        store,
                        videoProduct,
                        userPrivacy,
                        pushNotification
                    )
                    item.playlistVideoId = itemdata.optString("id")
                    item.playlistId = msg.getJSONObject("Playlist").optString("id")
                    item.playlistName = msg.getJSONObject("Playlist").optString("name")

                    if ((item.user_id != null) && item.user_id != "null" && item.user_id != "0") {
                        playlistMapList[item.video_id] = item
                        temp_list.add(item)
                    }
                }
                dataList.addAll(temp_list)
            }
        } catch (e: JSONException) {
            Log.d(Constants.tag, "Error: Exception: $e")
        } finally {
            setupAdapter()
            if (dataList.isEmpty()) {
                binding!!.noDataLayout.visibility = View.VISIBLE
            } else {
                binding!!.noDataLayout.visibility = View.GONE
            }
        }
    }


    private fun setupScreenData() {
        binding!!.tvPlaylist.text = playlistName
        if (userId.equals(
                getSharedPreference(context).getString(Variables.U_ID, ""),
                ignoreCase = true
            )
        ) {
            binding!!.ivOption.visibility = View.VISIBLE
        } else {
            binding!!.ivOption.visibility = View.GONE
        }

        binding!!.progressBar.visibility = View.VISIBLE
        callApiForPlaylistVideos(platlistId)
    }

    private fun setupAdapter() {
        val playlist = ArrayList<PlaylistHomeModel>()
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.VERTICAL
        binding!!.recylerview.layoutManager = layoutManager
        for (itemModel: HomeModel in dataList) {
            val item = PlaylistHomeModel()
            item.model = itemModel
            if ((itemModel.video_id == videoId)) {
                item.isSelection = true
            } else {
                item.isSelection = false
            }

            playlist.add(item)
        }
        adapter = ShowPlaylistAdapter(playlist, userId, object : AdapterClickListener {
            override fun onItemClick(view: View, pos: Int, `object`: Any) {
                val itemUpdate = playlist[pos]

                if (view.id == R.id.ivOption) {
                    showDeleteVideo(view, itemUpdate, pos)
                } else {
                    if (!(itemUpdate.isSelection)) {
                        val bundle = Bundle()
                        bundle.putBoolean("isShow", true)
                        bundle.putString("type", "videoPlay")
                        bundle.putInt("position", pos)
                        callback!!.onResponce(bundle)
                        dismiss()
                    }
                }
            }
        })
        binding!!.recylerview.adapter = adapter
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivBack -> {
                dismiss()
            }

            R.id.ivOption -> {
                showSetting()
            }
        }
    }

    private fun showDeleteVideo(view: View, itemUpdate: PlaylistHomeModel, pos: Int) {
        val wrapper: Context = ContextThemeWrapper(context, R.style.AlertDialogCustom)
        val popup = PopupMenu(wrapper, view)

        popup.menuInflater.inflate(R.menu.menu_playlist, popup.menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popup.gravity = Gravity.TOP or Gravity.RIGHT
        }

        popup.show()
        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.menuDelete -> {
                        deletePlaylistVideo(itemUpdate, pos)
                    }
                }
                return true
            }
        })
    }

    private fun showSetting() {
        val wrapper: Context = ContextThemeWrapper(context, R.style.AlertDialogCustom)
        val popup = PopupMenu(wrapper, binding!!.ivOption)

        popup.menuInflater.inflate(R.menu.menu_playlist, popup.menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popup.gravity = Gravity.TOP or Gravity.RIGHT
        }

        popup.show()
        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.menuDelete -> {
                        deletePlaylist()
                    }
                }
                return true
            }
        })
    }

    private fun deletePlaylist() {
        val parameters = JSONObject()
        try {
            parameters.put("id", platlistId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.deletePlaylist, parameters, getHeaders(
                activity
            ), object : Callback {
                override fun onResponce(resp: String) {
                    checkStatus(activity, resp)
                    cancelLoader()
                    try {
                        val jsonObject = JSONObject(resp)
                        val code = jsonObject.optString("code")
                        if ((code == "200")) {
                            val bundle = Bundle()
                            bundle.putBoolean("isShow", true)
                            bundle.putString("type", "deletePlaylist")
                            callback!!.onResponce(bundle)
                            dismiss()
                        }
                    } catch (e: Exception) {
                        Log.d(Constants.tag, "Exception: $e")
                    }
                }
            })
    }


    private fun deletePlaylistVideo(itemUpdate: PlaylistHomeModel, pos: Int) {
        val parameters = JSONObject()
        try {
            parameters.put("id", itemUpdate.model.playlistVideoId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.deletePlaylistVideo, parameters, getHeaders(
                activity
            ), object : Callback {
                override fun onResponce(resp: String) {
                    checkStatus(activity, resp)
                    cancelLoader()
                    try {
                        val jsonObject = JSONObject(resp)
                        val code = jsonObject.optString("code")
                        if ((code == "200")) {
                            val bundle = Bundle()
                            bundle.putBoolean("isShow", true)
                            bundle.putString("type", "deletePlaylistVideo")
                            bundle.putInt("position", pos)
                            callback!!.onResponce(bundle)
                            dismiss()
                        }
                    } catch (e: Exception) {
                        Log.d(Constants.tag, "Exception: $e")
                    }
                }
            })
    }
}
package com.coheser.app.activitesfragments.search

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.adapters.SoundListAdapter
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentSearchBinding
import com.coheser.app.models.SoundsModel
import com.coheser.app.simpleclasses.FileUtils.getAppFolder
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.isStringHasValue
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Functions.showToast
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.MainSearchViewModel
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.request.DownloadRequest
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 */
class SearchSoundFragment : Fragment(), Player.Listener {
    var type: String? = null
    var dataList = ArrayList<Any>()
    var adapter: SoundListAdapter? = null
    var prDownloader: DownloadRequest? = null
    var pageCount = 0
    var ispostFinsh = false
    var previousView: View? = null
    var thread: Thread? = null
    var player: ExoPlayer? = null
    var previousUrl = "none"
    var position = 0

    private val viewModel : MainSearchViewModel by viewModel()
    var itemModel = SoundsModel()

    lateinit var binding : FragmentSearchBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_search, container, false)

       binding.lifecycleOwner = this

        type = if (arguments != null && isStringHasValue(
                requireArguments().getString("type")
            )
        ) {
            requireArguments().getString("type")
        } else {
            "sound"
        }
        var linearLayoutManager = LinearLayoutManager(context)
        binding.recylerview.setLayoutManager(linearLayoutManager)
        dataList = ArrayList()
        adapter = SoundListAdapter(context, dataList) { view, pos, `object` ->
            val item = `object` as SoundsModel
            if (view.id == R.id.done) {
                stopPlaying()
                downLoadMp3(item.id, item.name, item.audio)
            } else if (view.id == R.id.fav_btn) {
                viewModel.addFavSound(item)
                position = pos
                itemModel = item
            } else {
                if (thread != null && !thread!!.isAlive) {
                    stopPlaying()
                    playaudio(view, item)
                } else if (thread == null) {
                    stopPlaying()
                    playaudio(view, item)
                }
            }
        }
        binding.recylerview.setAdapter(adapter)
        binding.recylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                printLog("resp", "" + scrollOutitems)
                if (userScrolled && scrollOutitems == dataList!!.size - 1) {
                    userScrolled = false
                    if (binding.loadMoreProgress.visibility != View.VISIBLE && !ispostFinsh) {
                        binding.loadMoreProgress.visibility = View.VISIBLE
                        pageCount = pageCount + 1
                        viewModel.getSearchData(pageCount,SearchMainActivity.searchEdit.text.toString(),type!!)
                    }
                }
            }
        })
        pageCount = 0

        initObserver()
        return binding.root
    }
    fun initObserver(){
        viewModel.soundLiveData.observe(requireActivity()){response ->
            when(response){
                is ApiResponce.Success ->{
                    response.data?.let { list ->
                        dataList.addAll(list)
                        adapter?.updateData(dataList)
                        viewModel.showDataView()
                        binding.shimmerViewContainer.visibility = View.GONE
                        binding.loadMoreProgress.visibility = View.GONE
                        Log.d(Constants.tag,"datasizeUser : ${dataList.size}")
                    }
                }
                is ApiResponce.Error ->{
                    if (pageCount > 0){
                        binding.loadMoreProgress.visibility = View.GONE
                    }else{
                        binding.shimmerViewContainer.visibility = View.GONE
//                        viewModel.hideDataView()
                        binding.noDataLayout.visibility = View.VISIBLE
                        binding.nodataTxt.text = requireContext().getString(R.string.no_result_found_for) + SearchMainActivity.searchEdit.text.toString() + "\""

                    }
                }
                is ApiResponce.Loading ->{
                    if (pageCount == 0){
                        binding.shimmerViewContainer.visibility = View.VISIBLE
                        binding.shimmerViewContainer.startShimmer()
                    }
                }

                else ->{}
            }
        }
        viewModel.addSoundLiveData.observe(requireActivity()){
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        if (itemModel.favourite == "1") itemModel.favourite = "0" else itemModel.favourite = "1"
                        dataList.remove(itemModel)
                        dataList.add(position, itemModel)
                        adapter!!.notifyDataSetChanged()
                    }
                }
                is ApiResponce.Error -> {
                    showToast(context,it.message)
                }
                is ApiResponce.Loading -> {

                }
                else ->{}
            }
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible && dataList.isEmpty()) {
            viewModel.getSearchData(pageCount,SearchMainActivity.searchEdit.text.toString(),type!!)
        }
    }

    // get the list of sounds against typed keyword
    fun callApi() {
        val params = JSONObject()
        try {
            if (getSharedPreference(context).getString(Variables.U_ID, null) != null) {
                params.put("user_id", getSharedPreference(context).getString(Variables.U_ID, "0"))
            }
            params.put("type", type)
            params.put("keyword", SearchMainActivity.searchEdit.text.toString())
            params.put("starting_point", "" + pageCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.search, params, getHeaders(
                activity
            )
        ) { resp ->
            checkStatus(activity, resp)
            if (type.equals("sound", ignoreCase = true)) {
                parseSounds(resp)
            }
        }
    }

    // parse the sound list date into model
    private fun parseSounds(responce: String) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code == "200") {
                val msgArray = jsonObject.getJSONArray("msg")
                val temp_list = ArrayList<Any>()
                for (i in 0 until msgArray.length()) {
                    val itemdata = msgArray.optJSONObject(i)
                    val sound = itemdata.optJSONObject("Sound")
                    val item = SoundsModel()
                    item.id = sound.optString("id")
                    item.audio = sound.optString("audio")
                    item.name = sound.optString("name")
                    item.description = sound.optString("description")
                    item.section = sound.optString("section")
                    item.thum = sound.optString("thum")
                    item.duration = sound.optString("duration")
                    item.created = sound.optString("created")
                    item.favourite = sound.optString("favourite")
                    temp_list.add(item)
                }
                if (pageCount == 0) {
                    dataList!!.addAll(temp_list)
                    if (dataList!!.isEmpty()) {
                        binding.noDataLayout.visibility = View.VISIBLE
                    } else {
                        binding.noDataLayout.visibility = View.GONE
                        binding.recylerview.adapter = adapter
                    }
                } else {
                    if (temp_list.isEmpty()) ispostFinsh = true else {
                        dataList!!.addAll(temp_list)
                        adapter!!.notifyDataSetChanged()
                    }
                }
            } else {
                if (dataList!!.isEmpty()) {
                    binding.noDataLayout.visibility = View.VISIBLE
                    binding.nodataTxt.text =
                        getString(R.string.no_result_found_for) + SearchMainActivity.searchEdit.text.toString() + "\""
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            binding.loadMoreProgress.visibility = View.GONE
        }
    }

    private fun callApiForFavSound(pos: Int, item: SoundsModel) {
        val parameters = JSONObject()
        try {
            parameters.put("user_id", getSharedPreference(context).getString(Variables.U_ID, "0"))
            parameters.put("sound_id", item.id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity, ApiLinks.addSoundFavourite, parameters, getHeaders(
                activity
            )
        ) { resp ->
            checkStatus(activity, resp)
            cancelLoader()
            if (item.favourite == "1") item.favourite = "0" else item.favourite = "1"
            dataList!!.remove(item)
            dataList!!.add(pos, item)
            adapter!!.notifyDataSetChanged()
        }
    }

    fun playaudio(view: View?, item: SoundsModel) {
        previousView = view
        if (previousUrl == item.audio) {
            previousUrl = "none"
            runningSoundId = "none"
        } else {
            previousUrl = item.audio
            runningSoundId = item.id
            player = ExoPlayer.Builder(requireContext()).setTrackSelector(DefaultTrackSelector(requireContext()))
                .build()
            player!!.setMediaItem(MediaItem.fromUri(item.audio))
            player!!.prepare()
            player!!.addListener(this)
            player!!.playWhenReady = true
            try {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build()
                player!!.setAudioAttributes(audioAttributes, true)
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception audio focus : $e")
            }
        }
    }

    fun stopPlaying() {
        if (player != null) {
            player!!.playWhenReady = false
            player!!.removeListener(this)
            player!!.release()
        }
        showStopState()
    }

    override fun onStart() {
        super.onStart()
        active = true
    }

    override fun onStop() {
        super.onStop()
        active = false
        runningSoundId = "null"
        if (player != null) {
            player!!.playWhenReady = false
            player!!.removeListener(this)
            player!!.release()
        }
        showStopState()
    }

    fun showRunState() {
        if (previousView != null) {
            previousView!!.findViewById<View>(R.id.loading_progress).visibility =
                View.GONE
            previousView!!.findViewById<View>(R.id.pause_btn).visibility =
                View.VISIBLE
            previousView!!.findViewById<View>(R.id.play_btn).visibility =
                View.GONE
            val imgDone = previousView!!.findViewById<View>(R.id.done)
            val imgFav = previousView!!.findViewById<View>(R.id.fav_btn)
            imgFav.animate().translationX(0f).setDuration(400).start()
            imgDone.animate().translationX(0f).setDuration(400).start()
        }
    }

    fun showLoadingState() {
        previousView!!.findViewById<View>(R.id.play_btn).visibility = View.GONE
        previousView!!.findViewById<View>(R.id.loading_progress).visibility = View.VISIBLE
        previousView!!.findViewById<View>(R.id.pause_btn).visibility =
            View.GONE
    }

    fun showStopState() {
        if (previousView != null) {
            previousView!!.findViewById<View>(R.id.play_btn).visibility =
                View.VISIBLE
            previousView!!.findViewById<View>(R.id.loading_progress).visibility =
                View.GONE
            previousView!!.findViewById<View>(R.id.pause_btn).visibility = View.GONE
            val imgDone = previousView!!.findViewById<View>(R.id.done)
            val imgFav = previousView!!.findViewById<View>(R.id.fav_btn)
            imgDone.animate()
                .translationX(java.lang.Float.valueOf("" + resources.getDimension(R.dimen._80sdp)))
                .setDuration(400).start()
            imgFav.animate()
                .translationX(java.lang.Float.valueOf("" + resources.getDimension(R.dimen._50sdp)))
                .setDuration(400).start()
        }
        runningSoundId = "none"
    }

    fun downLoadMp3(id: String, sound_name: String, url: String?) {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Please Wait...")
        progressDialog.show()
        prDownloader = PRDownloader.download(
            url, getAppFolder(
                requireActivity()
            ) + Variables.APP_HIDED_FOLDER, sound_name + id
        )
            .build()
        prDownloader?.start(object : OnDownloadListener {
            override fun onDownloadComplete() {
                progressDialog.dismiss()
                showToast(context, binding.root.context.getString(R.string.audio_saved_in_your_phone))
            }

            override fun onError(error: Error) {
                progressDialog.dismiss()
            }
        })
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING) {
            showLoadingState()
        } else if (playbackState == Player.STATE_READY) {
            showRunState()
        } else if (playbackState == Player.STATE_ENDED) {
            showStopState()
        }
    }

    companion object {
        var runningSoundId: String? = null
        var active = false
        @JvmStatic
        fun newInstance(type: String?): SearchSoundFragment {
            val fragment = SearchSoundFragment()
            val args = Bundle()
            args.getString("type", type)
            fragment.arguments = args
            return fragment
        }
    }
}

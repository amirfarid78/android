package com.coheser.app.activitesfragments.livestreaming.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.adapter.LiveUserAdapter
import com.coheser.app.activitesfragments.livestreaming.model.LiveUserModel
import com.coheser.app.activitesfragments.livestreaming.utils.StreamingFirebaseManager
import com.coheser.app.databinding.ActivityLiveUsersBinding
import com.coheser.app.simpleclasses.AppCompatLocaleActivity
import com.coheser.app.simpleclasses.Functions.checkLoginUser
import com.coheser.app.simpleclasses.Functions.getPermissionStatus
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.setLocale
import com.coheser.app.simpleclasses.Functions.showPermissionSetting
import com.coheser.app.simpleclasses.PermissionUtils
import com.coheser.app.simpleclasses.Variables
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LiveUsersActivity : AppCompatLocaleActivity(), View.OnClickListener {
    lateinit var context: Context
    var dataList: ArrayList<LiveUserModel> = ArrayList()
    var adapter: LiveUserAdapter? = null
    var rootref: DatabaseReference? = null
    var takePermissionUtils: PermissionUtils? = null
    var selectLiveModel: LiveUserModel? = null
    var position: Int = 0
    lateinit var binding: ActivityLiveUsersBinding

    private val mPermissionResult = registerForActivityResult<Array<String>, Map<String, Boolean>>(
        ActivityResultContracts.RequestMultiplePermissions(), object : ActivityResultCallback<Map<String, Boolean>> {
            @RequiresApi(api = Build.VERSION_CODES.M)
            override fun onActivityResult(result: Map<String, Boolean>) {
                var allPermissionClear = true
                val blockPermissionCheck: MutableList<String> = ArrayList()
                for (key in result.keys) {
                    if (!result[key]!!) {
                        allPermissionClear = false
                        blockPermissionCheck.add(
                            getPermissionStatus(
                                this@LiveUsersActivity, key
                            )
                        )
                    }
                }
                if (blockPermissionCheck.contains("blocked")) {
                    showPermissionSetting(
                        this@LiveUsersActivity,
                        getString(R.string.we_need_camera_and_recording_permission_for_live_streaming)
                    )
                } else if (allPermissionClear) {
                    joinStream()
                }
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(getSharedPreference(this)
            .getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE),
            this, javaClass, false
        )
        binding = ActivityLiveUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this@LiveUsersActivity
        rootref = FirebaseDatabase.getInstance().reference
        takePermissionUtils = PermissionUtils(this@LiveUsersActivity, mPermissionResult)
        binding.backBtn.setOnClickListener(this)

        binding.recylerview.setLayoutManager(GridLayoutManager(context, 3))
        binding.recylerview.setHasFixedSize(true)
        adapter = LiveUserAdapter(context, dataList) { view, pos, `object` ->
            if (!(dataList.isEmpty())) {
                position = pos
                val itemUpdate = dataList[pos]
                selectLiveModel = itemUpdate

                if (checkLoginUser(this@LiveUsersActivity)) {
                    if (takePermissionUtils!!.isCameraRecordingPermissionGranted) {
                        joinStream()
                    } else {
                        takePermissionUtils!!.showCameraRecordingPermissionDailog(getString(R.string.we_need_camera_and_recording_permission_for_live_streaming))
                    }
                }
            }
        }
        binding.recylerview.setAdapter(adapter)

        val userList=StreamingFirebaseManager.getInstance(this)?.userList
        userList?.let { dataList.addAll(it) }

        userList?.onAdd={it,index->
            dataList.add(it)
            adapter?.notifyItemInserted((dataList.size-1))
            notifylist()
        }
        userList?.onRemove={it,index->
            dataList.removeAt(index)
            adapter?.notifyItemRemoved(index)
            notifylist()
        }

        userList?.onUpdate={old,new,index->
            dataList.set(index,new)
        }
        notifylist()

    }

    fun notifylist(){
        if (dataList.isEmpty()) {
            binding.noDataFound.visibility = View.VISIBLE
        } else {
            binding.noDataFound.visibility = View.GONE
        }
    }

    private fun joinStream() {
        val intent = Intent()
        intent.putParcelableArrayListExtra("dataList", dataList)
        intent.putExtra("position", position)
        intent.setClass(this@LiveUsersActivity, MultiViewLiveActivity::class.java)
        startActivity(intent)
    }



    public override fun onDestroy() {
        mPermissionResult.unregister()
        super.onDestroy()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.back_btn -> {
                finish()
                com.google.android.exoplayer2.util.Log.d(Constants.tag, "Click")
            }

            else -> {}
        }
    }


}

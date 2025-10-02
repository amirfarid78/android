package com.coheser.app.activitesfragments.spaces

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.coheser.app.R
import com.coheser.app.activitesfragments.spaces.models.GroupModel
import com.coheser.app.activitesfragments.spaces.models.TopicModel
import com.coheser.app.databinding.FragmentCreateRoomBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Dialogs.showError
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.facebook.drawee.view.SimpleDraweeView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.realpacific.clickshrinkeffect.applyClickShrink

class CreateRoomFragment(var fragmentCallBack: FragmentCallBack) : Fragment(), View.OnClickListener {
    lateinit var binding: FragmentCreateRoomBinding
    var groupModel: GroupModel? = null
    var width: Int = 0

    var reference: DatabaseReference? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_create_room,container,false)
        InitControl()
        return binding?.root
    }




    private fun InitControl() {
        width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        groupModel = GroupModel()
        reference = FirebaseDatabase.getInstance().reference
        binding.tabGenrateGroup.setOnClickListener(this)
        binding.tabGenrateGroup.applyClickShrink()
        binding.tabChoosePeople.setOnClickListener(this)
        binding.tabChoosePeople.applyClickShrink()
        binding.addTopicTxt.setOnClickListener(this)
        binding.addTopicbtn.setOnClickListener(this)
        setUpScreenData()
    }

    private fun setUpScreenData() {
        groupModel!!.privacyType = "0"
        groupModel!!.name = ""
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tabGenrateGroup -> {
                groupModel!!.name = binding.titleEdit.text.toString()
                if (TextUtils.isEmpty(groupModel!!.name)) {
                    showError(requireActivity(), "Please enter room title!")
                } else if (selectedTopics!!.isEmpty()) {
                    showError(requireActivity(), "Please select the topic!")
                } else {
                    val bundleGenrate = Bundle()
                    bundleGenrate.putBoolean("isShow", false)
                    bundleGenrate.putString("action", "genrateRoom")
                    bundleGenrate.putSerializable("groupModel", groupModel)
                    bundleGenrate.putString("roomName", "" + groupModel!!.name)
                    bundleGenrate.putString("privacyType", "" + groupModel!!.privacyType)
                    bundleGenrate.putSerializable("selectedFriends", selectedFriends)
                    bundleGenrate.putSerializable("topics", selectedTopics)
                    fragmentCallBack.onResponce(bundleGenrate)
                    parentFragmentManager.popBackStack()
                }
            }

            R.id.tabChoosePeople -> {
                addFriendsToRoom()
            }

            R.id.addTopicbtn, R.id.addTopicTxt -> addTopics()
        }
    }


    var selectedTopics: ArrayList<TopicModel>? = ArrayList()
    private fun addTopics() {
        val intent = Intent(requireActivity(), InterestPreferenceA::class.java)
        resultCallback.launch(intent)
    }

    var resultCallback: ActivityResultLauncher<Intent> =
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(), object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    if (result.resultCode == Activity.RESULT_OK) {
                        val data = result.data
                        if (data!!.getBooleanExtra("isShow", false)) {
                            selectedTopics =
                                data.getSerializableExtra("dataList") as ArrayList<TopicModel>?

                            if (selectedTopics!!.size > 0) {
                                binding.topicListLayout.visibility = View.VISIBLE
                                binding.addTopicTxt.visibility = View.GONE
                            } else {
                                binding.topicListLayout.visibility = View.GONE
                                binding.addTopicTxt.visibility = View.VISIBLE
                            }

                            addTopicItem()
                        }
                    }
                }
            })


    fun addTopicItem() {
        binding.topicList.removeAllViews()
        for (i in selectedTopics!!.indices) {
            val itemModel = selectedTopics!![i]

            val tabTag = LayoutInflater.from(binding.root.context)
                .inflate(R.layout.item_topic, null) as RelativeLayout
            val innerView = tabTag.findViewById<LinearLayout>(R.id.innerView)
            val ivTag = innerView.findViewById<SimpleDraweeView>(R.id.ivTag)
            val ivFrameTag = innerView.findViewById<View>(R.id.ivFrameTag)
            val tvTag = innerView.findViewById<TextView>(R.id.tvTag)
            tvTag.text = "" + itemModel.title

            tabTag.tag = i
            ivTag.controller = frescoImageLoad(
                binding.root.context,
                "" + itemModel.title,
                binding.root.context.resources.getDimension(R.dimen._9sdp).toInt(),
                itemModel.image, ivTag
            )

            tvTag.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.white
                )
            )
            tabTag.isActivated = true
            ivFrameTag.backgroundTintList = ContextCompat.getColorStateList(
                binding.root.context, R.color.appColor
            )

            binding.topicList.addView(tabTag)
        }
    }


    var selectedFriends: ArrayList<UserModel> = ArrayList()
    private fun addFriendsToRoom() {
        val fragment = AddFriendsSelectionF({ bundle: Bundle ->
            if (bundle.getBoolean("isShow", false)) {
                selectedFriends = bundle.getSerializable("UserList") as ArrayList<UserModel>

                if (selectedFriends!!.size > 0) {
                    binding.tabGenrateGroup.visibility = View.VISIBLE
                    binding.tabChoosePeople.visibility = View.GONE
                } else {
                    binding.tabGenrateGroup.visibility = View.GONE
                    binding.tabChoosePeople.visibility = View.VISIBLE
                }
            }
        }, false)
        fragment.show(childFragmentManager, "AddFriendsSelectionF")
    }

    override fun onDetach() {
        super.onDetach()

    }
}
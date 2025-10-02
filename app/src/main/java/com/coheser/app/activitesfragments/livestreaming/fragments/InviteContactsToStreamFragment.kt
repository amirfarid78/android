package com.coheser.app.activitesfragments.livestreaming.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.StreamingConstants
import com.coheser.app.activitesfragments.livestreaming.adapter.Contacts_Adapter
import com.coheser.app.activitesfragments.livestreaming.model.ContactsDataModel
import com.coheser.app.activitesfragments.livestreaming.model.Group_member_GetSet
import com.coheser.app.adapters.ProfileSharingAdapter
import com.coheser.app.apiclasses.ApiLinks
import com.coheser.app.databinding.FragmentInviteContactsToStreamBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.ShareAppModel
import com.coheser.app.models.StreamInviteModel
import com.coheser.app.simpleclasses.DataParsing.getUserDataModel
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.appInstalledOrNot
import com.coheser.app.simpleclasses.Functions.cancelLoader
import com.coheser.app.simpleclasses.Functions.checkStatus
import com.coheser.app.simpleclasses.Functions.getHeaders
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.hideSoftKeyboard
import com.coheser.app.simpleclasses.Functions.printLog
import com.coheser.app.simpleclasses.Functions.showLoader
import com.coheser.app.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONArray
import org.json.JSONObject
import java.util.Random

class InviteContactsToStreamFragment : BottomSheetDialogFragment {
    
    lateinit var binding: FragmentInviteContactsToStreamBinding

    var userlist: ArrayList<ContactsDataModel> = ArrayList()
    var followerlist: HashMap<String, ContactsDataModel> = HashMap()
    var contactsAdapter: Contacts_Adapter? = null
    var adapterShareToSocial: ProfileSharingAdapter? = null
    var linearLayoutManager: LinearLayoutManager? = null
    lateinit var androidColors: IntArray
    var rootref: DatabaseReference? = null
    var pageCount: Int = 0
    var refreshCallback: FragmentCallBack? = null
    var allMembersArrylist: ArrayList<Group_member_GetSet> = ArrayList()
    var streamingId: String? = null
    var streamType: String? = null
    var inviteUserMapList: HashMap<String, ContactsDataModel?> = HashMap()
    private val mBehavior: BottomSheetBehavior<*>? = null

    constructor(streamingId: String?, streamType: String?, refreshCallback: FragmentCallBack?) {
        this.streamingId = streamingId
        this.streamType = streamType
        this.refreshCallback = refreshCallback
    }

    constructor()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInviteContactsToStreamBinding.inflate(inflater, container, false)
        
        rootref = FirebaseDatabase.getInstance().reference
        androidColors = getResources().getIntArray(R.array.bg_color_array)
        binding.ivBack.setOnClickListener(View.OnClickListener { dismiss() })
        binding.ivInviteAll.setOnClickListener(View.OnClickListener { sendStreamingMultipleInvite() })

        if (streamType == "single") {
            binding.ivInviteAll.setVisibility(View.GONE)
        } else {
            binding.ivInviteAll.setVisibility(View.VISIBLE)
        }
        ownSharedApp
        setupRecyclerAdapter()
        setupSearchEditText()

        binding.loadMoreProgress.setVisibility(View.VISIBLE)
        pageCount = 0
        callApi()

        return binding.root
    }

    val ownSharedApp: Unit
        get() {
           val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.recylerviewShareToSocial.setLayoutManager(layoutManager)
            binding.recylerviewShareToSocial.setHasFixedSize(false)
            adapterShareToSocial =
                ProfileSharingAdapter(context, appShareDataList) { view, pos, `object` ->
                    val item = `object` as ShareAppModel
                    shareProfile(item)
                }
            binding.recylerviewShareToSocial.setAdapter(adapterShareToSocial)
        }

    fun shareProfile(item: ShareAppModel?) {
        val streamingLink = (Variables.https + "://" + getString(R.string.domain)
                + getString(R.string.share_stream_endpoint_second)
                + Functions.removeAtSymbol(
            Functions.getSharedPreference(context).getString(Variables.U_NAME, "")!!
        )
                ) + "&live=" + Functions.getRandomString(6) + streamingId + Functions.getRandomString(
            10
        )
        try {
            when {
                item?.getName().equals(getString(R.string.whatsapp), ignoreCase = true) -> {
                    try {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, streamingLink)
                            `package` = "com.whatsapp"
                        }
                        startActivity(sendIntent)
                    } catch (e: Exception) {
                        Log.d(Constants.tag, "Exception: $e")
                    }
                }
                item?.getName().equals(getString(R.string.facebook), ignoreCase = true) -> {
                    try {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, streamingLink)
                            `package` = "com.facebook.katana"
                        }
                        startActivity(sendIntent)
                    } catch (e: Exception) {
                        Log.d(Constants.tag, "Exception: $e")
                    }
                }
                item?.getName().equals(getString(R.string.messenger), ignoreCase = true) -> {
                    try {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, streamingLink)
                            `package` = "com.facebook.orca"
                        }
                        startActivity(sendIntent)
                    } catch (e: Exception) {
                        Log.d(Constants.tag, "Exception: $e")
                    }
                }
                item?.getName().equals(getString(R.string.sms), ignoreCase = true) -> {
                    try {
                        val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                            type = "vnd.android-dir/mms-sms"
                            putExtra("sms_body", streamingLink)
                        }
                        startActivity(smsIntent)
                    } catch (e: Exception) {
                        Log.d(Constants.tag, "Exception: $e")
                    }
                }
                item?.getName().equals(getString(R.string.copy_link), ignoreCase = true) -> {
                    try {
                        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Copied Text", streamingLink)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, getString(R.string.link_copy_in_clipboard), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.d(Constants.tag, "Exception: $e")
                    }
                }
                item?.getName().equals(getString(R.string.email), ignoreCase = true) -> {
                    try {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, streamingLink)
                            `package` = "com.google.android.gm"
                        }
                        startActivity(sendIntent)
                    } catch (e: Exception) {
                        Log.d(Constants.tag, "Exception: $e")
                    }
                }
                item?.getName().equals(getString(R.string.other), ignoreCase = true) -> {
                    try {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, streamingLink)
                        }
                        startActivity(sendIntent)
                    } catch (e: Exception) {
                        Log.d(Constants.tag, "Exception: $e")
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }
    }


    private val appShareDataList: ArrayList<ShareAppModel>
        get() {
            val dataList = ArrayList<ShareAppModel>()
            run {
                if (appInstalledOrNot(requireContext(), "com.whatsapp")) {
                    val item = ShareAppModel()
                    item.name = getString(R.string.whatsapp)
                    item.icon = R.drawable.ic_share_whatsapp
                    dataList.add(item)
                }
            }
            run {
                if (appInstalledOrNot(requireContext(), "com.facebook.katana")) {
                    val item = ShareAppModel()
                    item.name = getString(R.string.facebook)
                    item.icon = R.drawable.ic_share_facebook
                    dataList.add(item)
                }
            }
            run {
                if (appInstalledOrNot(requireContext(), "com.facebook.orca")) {
                    val item = ShareAppModel()
                    item.name = getString(R.string.messenger)
                    item.icon = R.drawable.ic_share_messenger
                    dataList.add(item)
                }
            }
            run {
                val item = ShareAppModel()
                item.name = getString(R.string.sms)
                item.icon = R.drawable.ic_share_sms
                dataList.add(item)
            }
            run {
                val item = ShareAppModel()
                item.name = getString(R.string.copy_link)
                item.icon = R.drawable.ic_share_copy_link
                dataList.add(item)
            }
            run {
                if (appInstalledOrNot(requireContext(), "com.whatsapp")) {
                    val item = ShareAppModel()
                    item.name = getString(R.string.email)
                    item.icon = R.drawable.ic_share_email
                    dataList.add(item)
                }
            }
            run {
                val item = ShareAppModel()
                item.name = getString(R.string.other)
                item.icon = R.drawable.ic_share_other
                dataList.add(item)
            }
            return dataList
        }


    private fun callApi() {
        val parameters = JSONObject()
        try {
            if (binding.searchEdit!!.text.toString().length > 0) {
                parameters.put("type", "user")
                parameters.put("keyword", binding.searchEdit!!.text.toString())
                parameters.put("starting_point", "" + pageCount)
            } else {
                parameters.put(
                    "user_id",
                    getSharedPreference(context).getString(Variables.U_ID, "")
                )
                parameters.put("starting_point", "" + pageCount)
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
        }

        var url: String? = ""
        url = if (binding.searchEdit!!.text.toString().length > 0) {
            ApiLinks.search
        } else {
            ApiLinks.showFollowers
        }


        VolleyRequest.JsonPostRequest(
            activity, url, parameters, getHeaders(
                activity
            )
        ) { resp ->
            binding.loadMoreProgress!!.visibility = View.GONE
            checkStatus(activity, resp)
            parseFansData(resp)
        }
    }

    private fun setupRecyclerAdapter() {
        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager!!.orientation = RecyclerView.VERTICAL
        binding.usersrecylerview.setLayoutManager(linearLayoutManager)
        contactsAdapter = Contacts_Adapter(requireContext(), userlist, object:Contacts_Adapter.OnItemClickListener{
            override fun onItemClick(itemUpdate: ContactsDataModel, positon: Int) {
                val item = userlist[positon]
                if (streamType == "single") {
                    if (item?.isexits == true) {
                        Toast.makeText(
                            context,
                            requireContext().getString(R.string.already_a_member),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        add_member_Dialog(item, positon)
                    }
                }
                else if (streamType == "multiple") {
                    if (inviteUserMapList.containsKey(item!!.userId)) {
                        itemUpdate.isexits = false
                        userlist[positon] = itemUpdate
                        contactsAdapter!!.notifyItemChanged(positon)
                        inviteUserMapList.remove(item.userId)
                    } else {
                        itemUpdate.isexits = true
                        userlist[positon] = itemUpdate
                        contactsAdapter!!.notifyItemChanged(positon)
                        inviteUserMapList[item.userId!!] = item
                    }
                    updateButtonStatus()
                }
            }
        })
        binding.usersrecylerview.setAdapter(contactsAdapter)


        binding.usersrecylerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var userScrolled: Boolean = false
            var scrollOutitems: Int = 0

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
                if (userScrolled && (scrollOutitems == userlist.size - 1)) {
                    userScrolled = false

                    if (binding.loadMoreProgress!!.visibility != View.VISIBLE) {
                        binding.loadMoreProgress!!.visibility = View.VISIBLE
                        pageCount = pageCount + 1
                        callApi()
                    }
                }
            }
        })
    }

    private fun setupSearchEditText() {
        binding.searchEdit.addTextChangedListener(
            object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {
                    if (binding.searchEdit.getText().toString().length > 0) {
                        binding.tvSuggested.text = requireContext().getString(R.string.search_)
                        binding.searchEdit.visibility = View.VISIBLE
                    } else {
                        binding.tvSuggested.text = requireContext().getString(R.string.suggested)
                        binding.searchEdit.visibility = View.GONE
                        setlistwithFollower()
                    }
                }
            }
        )
        binding.searchBtn!!.setOnClickListener {
            if (binding.searchEdit.getText().toString().length > 0) {
                hideSoftKeyboard(activity)
                pageCount = 0
                callApi()
            }
        }
    }

    private fun setlistwithFollower() {
        userlist.clear()
        for (key in followerlist.keys) {
            val userItem = followerlist[key]
            userItem?.let { userlist.add(it) }
        }
        contactsAdapter!!.notifyDataSetChanged()


        if (userlist.isEmpty()) {
            binding.noDataLayout.visibility = View.VISIBLE
        } else {
            binding.noDataLayout.visibility = View.GONE
        }
    }

    private fun updateButtonStatus() {
        if (inviteUserMapList.keys.size > 0) {
            binding.ivInviteAll!!.visibility = View.VISIBLE
        } else {
            binding.ivInviteAll!!.visibility = View.GONE
        }
    }

    fun parseFansData(responce: String?) {
        try {
            val jsonObject = JSONObject(responce)
            val code = jsonObject.optString("code")
            if (code.equals("200", ignoreCase = true)) {
                val msg = jsonObject.optJSONArray("msg")
                val temp_list = ArrayList<ContactsDataModel>()
                for (i in 0 until msg.length()) {
                    val data = msg.optJSONObject(i)


                    val userDetailModel = getUserDataModel(data.optJSONObject("User"))

                    val user = ContactsDataModel()
                    user.username = userDetailModel.username
                    user.picture = userDetailModel.getProfilePic()
                    user.userId = userDetailModel.id
                    user.email = userDetailModel.email
                    user.firstName = userDetailModel.first_name
                    user.lastName = userDetailModel.last_name
                    user.uid = ""
                    user.verified = userDetailModel.verified
                    user.imagecolor = androidColors[Random().nextInt(androidColors.size)]
                    user.isexits = false

                    if (binding.searchEdit!!.text.toString().length < 1) {
                        followerlist[user.userId!!] = user
                    }

                    temp_list.add(user)
                }

                if (pageCount == 0) {
                    userlist.clear()
                    userlist.addAll(temp_list)

                    if (userlist.isEmpty()) {
                        binding.noDataLayout.visibility = View.VISIBLE
                    } else {
                        binding.noDataLayout.visibility = View.GONE
                    }
                }
                else {
                    if (temp_list.isEmpty()) {
                    } else {
                        userlist.addAll(temp_list)
                    }
                }

            }
            else {
                if (userlist.isEmpty()) binding.noDataLayout.visibility =
                    View.VISIBLE
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
        } finally {
            contactsAdapter!!.notifyDataSetChanged()
            binding.loadMoreProgress!!.visibility = View.GONE
        }
    }

    private fun isUserExist(user: ContactsDataModel): Boolean {
        var isExist = false
        for (member in allMembersArrylist) {
            if (user.userId.equals(member.user_id, ignoreCase = true)) {
                isExist = true
            }
        }
        return isExist
    }


    // below three method is used for delete the message fro the groupchar\t
    private fun add_member_Dialog(contactsData: ContactsDataModel?, positon: Int) {
        val options = arrayOf<CharSequence>(
            requireContext().getString(R.string.add) + " " + contactsData!!.username + " " + requireContext().getString(
                R.string.in_this_concert
            ), requireContext().getString(R.string.cancel_)
        )

        val builder = AlertDialog.Builder(
            requireContext(), R.style.AlertDialogCustom
        )

        builder.setTitle(null)

        builder.setItems(options) { dialog, item ->
            if (options[item] == requireContext().getString(R.string.add) + " " + contactsData.username + " " + requireContext().getString(
                    R.string.in_this_concert
                )
            ) {
                rootref!!.child(StreamingConstants.liveStreamingUsers)
                    .child(streamingId!!)
                    .child("StreamInvite")
                    .child(getSharedPreference(context).getString(Variables.U_ID, "")!!)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            activity!!.runOnUiThread {
                                if (snapshot.exists()) {
                                    val itemUpdate = snapshot.getValue(
                                        StreamInviteModel::class.java
                                    )
                                    if (snapshot.childrenCount > 0) {
                                    } else {
                                        sendStreamingSingleInvite(contactsData, positon)
                                    }
                                } else {
                                    sendStreamingSingleInvite(contactsData, positon)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
            } else if (options[item] == requireContext().getString(R.string.cancel_)) {
                dialog.dismiss()
            }
        }

        builder.show()
    }

    private fun sendStreamingSingleInvite(contactsData: ContactsDataModel?, positon: Int) {
        val parameters = JSONObject()
        try {
            val userArray = JSONArray()
            val userObj = JSONObject()
            userObj.put("user_id", contactsData!!.userId)
            userArray.put(userObj)
            parameters.put("users", userArray)
            parameters.put("live_streaming_id", streamingId)
            parameters.put("type", streamType)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.inviteUserToStreaming,
            parameters,
            getHeaders(context)
        ) { resp ->
            checkStatus(activity, resp)
            cancelLoader()
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code == "200") {
                    Add_member(positon)
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
            }
        }
    }


    private fun sendStreamingMultipleInvite() {
        val parameters = JSONObject()
        try {
            val userArray = JSONArray()
            for (key in inviteUserMapList.keys) {
                val itemModel = inviteUserMapList[key]
                val userObj = JSONObject()
                userObj.put("user_id", itemModel!!.userId)
                userArray.put(userObj)
            }
            parameters.put("users", userArray)
            parameters.put("live_streaming_id", streamingId)
            parameters.put("type", streamType)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(
            activity,
            ApiLinks.inviteUserToStreaming,
            parameters,
            getHeaders(context)
        ) { resp ->
            checkStatus(activity, resp)
            cancelLoader()
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code == "200") {
                    Toast.makeText(
                        context,
                        requireContext().getString(R.string.invitation_send_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception : $e")
            }
        }
    }

    private fun addMultipleUsersIntoList() {
    }


    fun Add_member(position: Int) {
        val itemUpdate = userlist[position]
        itemUpdate!!.isexits = true
        userlist[position] = itemUpdate
        contactsAdapter!!.notifyItemChanged(position)
        Toast.makeText(context, requireContext().getString(R.string.invitation_sended), Toast.LENGTH_SHORT)
            .show()
    }


    override fun onDetach() {
        super.onDetach()
        if (refreshCallback != null) {
            val bundle = Bundle()
            bundle.putBoolean("isShow", false)
            refreshCallback!!.onResponce(bundle)
        }
    }
}

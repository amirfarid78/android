package com.coheser.app.activitesfragments.profile

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.coheser.app.R
import com.coheser.app.activitesfragments.InboxActivity
import com.coheser.app.activitesfragments.WebviewActivity
import com.coheser.app.activitesfragments.accounts.AccountUtils
import com.coheser.app.activitesfragments.accounts.LoginActivity
import com.coheser.app.activitesfragments.accounts.ManageAccountsFragment
import com.coheser.app.activitesfragments.profile.likedvideos.LikedVideoFragment
import com.coheser.app.activitesfragments.profile.privatevideos.PrivateVideoFragment
import com.coheser.app.activitesfragments.profile.usersstory.ViewStoryA
import com.coheser.app.activitesfragments.profile.uservideos.UserVideoFragment
import com.coheser.app.activitesfragments.shoping.ShopA
import com.coheser.app.activitesfragments.walletandwithdraw.MyWallet
import com.coheser.app.adapters.ViewPagerAdapter
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.databinding.FragmentProfileTabBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.StoryModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.FileUtils
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.hideSoftKeyboard
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.MyProfileViewModel
import io.paperdb.Paper
import io.paperdb.PaperDbException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import org.koin.androidx.viewmodel.ext.android.viewModel


class ProfileTabFragment : Fragment() {
    protected var tabLayout: TabLayout? = null
    protected var pager: ViewPager2? = null
    var profileReceiver: ProfileBroadCast? = null
    lateinit var binding: FragmentProfileTabBinding
    private var adapter: ViewPagerAdapter? = null

    private val viewModel: MyProfileViewModel by viewModel()

    var fragmentUserVides: UserVideoFragment? = null

    var resultCallback = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
                setProfileData(true)
            }
        }
    }


    var resultUserDetailCallback = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
                viewModel.isRefreshTabs.set(false)
                viewModel.getUserDetails()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileTabBinding.inflate(inflater, container, false)

        return init()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setObserveAble()

        viewModel.getUserDetails()

    }

    fun setObserveAble(){

        viewModel.userDetailLiveData.observe(viewLifecycleOwner,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        if (it != null) {
                            setData(it)
                        }
                    }

                }
                else -> {}
            }
        })

        viewModel.playListLiveData.observe(viewLifecycleOwner,{
            when(it){
                is ApiResponce.Success ->{
                    it.data?.let {
                        if (it != null) {
                            if (fragmentUserVides != null) {

                                    fragmentUserVides?.updateUserPlaylist(it,
                                        viewModel.userModel.value?.verified.toString(),
                                        FragmentCallBack { bundle ->
                                            if (bundle.getBoolean("isShow", false)) {
                                                viewModel.getUserDetails()
                                            }
                                        })
                                }
                            }
                        }

                    }
                else -> {}
            }
        })

    }

    fun setData(userDetailModel: UserModel) {

        viewModel.setData(userDetailModel!!)

        AccountUtils.updateUserModel(userDetailModel)
        setProfileData(false)

                if(viewModel.isRefreshTabs.get()){
                    SetTabs()
                }

                if (userDetailModel.pushNotificationModel != null) {
                    Paper.book(Variables.PrivacySetting)
                        .write(Variables.PushSettingModel, userDetailModel.pushNotificationModel!!)
                }

                if (userDetailModel.privacySettingModel != null) {
                    Paper.book(Variables.PrivacySetting)
                        .write(Variables.PrivacySettingModel, userDetailModel.privacySettingModel!!)
                }

        if (userDetailModel.storyModel!=null) {

            binding.circleStatusBar.visibility = View.VISIBLE
            binding.circleStatusBar.counts=userDetailModel.storyModel?.videoList?.size!!
        } else {
            binding.circleStatusBar.visibility = View.GONE
        }

                val editor = getSharedPreference(binding.root.context).edit()
                editor.putString(Variables.U_PIC, userDetailModel.getProfilePic())
                editor.putString(Variables.U_GIF, userDetailModel.getProfileGif())
                editor.putString(Variables.U_PROFILE_VIEW, userDetailModel.profile_view)
                editor.putString(Variables.U_WALLET, "" + userDetailModel.wallet)
                editor.putString(Variables.U_total_balance_usd, ""+userDetailModel.total_balance_usd)
                editor.putString(Variables.U_total_coins_all_time, "" + userDetailModel.total_all_time_coins)
                editor.putString(Variables.U_PHONE_NO,userDetailModel.phone)
                editor.putString(Variables.REFERAL_CODE, userDetailModel.referral_code)
                editor.putInt(Variables.IS_VERIFIED, userDetailModel.verified)
                editor.putLong(Variables.U_Followers, userDetailModel.followers_count)
                editor.putLong(Variables.U_Followings, userDetailModel.following_count)

                editor.commit()


        fragmentUserVides?.updatePlaylistCreate()
        updateProfileVisitorCount(userDetailModel)
        binding.refreshLayout.isRefreshing = false

    }

    fun openStoryDetail() {
        viewModel.userModel?.value?.storyModel?.let {
            val storyList=ArrayList<StoryModel>()
            storyList.add(it)
            val myIntent = Intent(context, ViewStoryA::class.java)
            myIntent.putExtra("storyList", storyList )
            myIntent.putExtra("position", 0)
            startActivity(myIntent)
            requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
        }
    }

    private fun openProfileViewHistory() {
        val intent = Intent(binding.root.context, ViewProfileHistoryActivity2::class.java)
        resultUserDetailCallback.launch(intent)
        requireActivity()?.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }


    private fun openWallet() {
        val intent = Intent(binding.root.context, MyWallet::class.java)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openShop() {
        val intent = Intent(binding.root.context, ShopA::class.java)
        try {
            intent.putExtra("name",viewModel.userModel.value!!.username)
            intent.putExtra("profile",viewModel.userModel.value!!.getProfilePic())
            intent.putExtra("id",viewModel.userModel.value!!.id)
            resultCallback.launch(intent)
        }catch (e:Exception){
            startActivity(intent)
        }
        activity?.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    fun openWebUrl(title: String?, url: String?) {
        val intent = Intent(binding.root.context, WebviewActivity::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun openManageMultipleAccounts() {
        val f = ManageAccountsFragment { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                hideSoftKeyboard(activity)
                val intent = Intent(activity, LoginActivity::class.java)
                startActivity(intent)
                activity?.overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
            }
        }
        f.show(childFragmentManager, "")
    }

    override fun setMenuVisibility(visible: Boolean) {
        super.setMenuVisibility(visible)
        if (visible) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(200)
                if (getSharedPreference(binding.root.context).getBoolean(Variables.IS_LOGIN, false)) {
                    setProfileData(true)
                    viewModel.isRefreshTabs.set(false)
                    viewModel.getUserDetails()
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateCounts()
        viewModel.getInboxCountData()
        showDraftCount()
    }


    private fun init(): View {
        binding.appbarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (Math.abs(verticalOffset) == appBarLayout.totalScrollRange) {
                binding.refreshLayout.isEnabled = false
            } else binding.refreshLayout.isEnabled = verticalOffset == 0
        }
        binding.refreshLayout.setOnRefreshListener {
            viewModel.isRefreshTabs.set(true)
            viewModel.getUserDetails()
        }
        binding.userImage.setOnClickListener(DebounceClickHandler {
            if (binding.circleStatusBar.visibility === View.VISIBLE) {
                openStoryDetail()
            } else {
                openEditProfile()
            }
        })

        showDraftCount()
        binding.editProfileBtn.setOnClickListener(DebounceClickHandler { openEditProfile() })
        binding.tabAccount.setOnClickListener(DebounceClickHandler { openManageMultipleAccounts() })
        binding.tabLink.setOnClickListener(DebounceClickHandler {
            openWebUrl(
                binding.root.context?.getString(R.string.web_browser),
                binding.tvLink.text.toString()
            )
        })


        binding.inboxbtn.setOnClickListener {
            if (Functions.checkLoginUser(activity)) {
                val intent = Intent(binding.root.context, InboxActivity::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }
        binding.menuBtn.setOnClickListener(DebounceClickHandler {
            val intent = Intent(binding.root.context, SettingAndPrivacyActivity::class.java)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
        })
        binding.tabViewsHistory.setOnClickListener(DebounceClickHandler { openProfileViewHistory() })
        binding.followingLayout.setOnClickListener(DebounceClickHandler { openFollowing() })
        binding.fansLayout.setOnClickListener(DebounceClickHandler { openFollowers() })
        binding.shopBtn.setOnClickListener(DebounceClickHandler { openShop() })
        binding.shareProfileBtn.setOnClickListener(DebounceClickHandler { openWallet() })

        profileReceiver = ProfileBroadCast()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.registerReceiver(
                profileReceiver,
                IntentFilter(Variables.profileBroadCastAction),
                Context.RECEIVER_NOT_EXPORTED
            )

        } else {
            activity?.registerReceiver(profileReceiver, IntentFilter(Variables.profileBroadCastAction))
        }
        return binding.root
    }

    fun SetTabs() {
        if(adapter!=null){
            val fragment=adapter!!.getFragments(0) as UserVideoFragment
            if(fragment!=null){
                fragment.refreshData()
            }
        }
        else {
            adapter = ViewPagerAdapter(this)
            pager = binding.root.findViewById(R.id.pager)
            tabLayout = binding.root.findViewById(R.id.tabs)
            pager?.setOffscreenPageLimit(2)
            registerFragmentWithPager()
            pager?.setAdapter(adapter)
            addTabs()
            setupTabIcons()

            pager?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    tabLayout?.getTabAt(position)?.select()
                }
            })
        }
    }

    private fun addTabs() {
        val tabLayoutMediator = TabLayoutMediator(
            tabLayout!!, pager!!
        ) { tab, position ->
            if (position == 0) {
                tab.setText(binding.root.context?.getString(R.string.my_videos))
            } else if (position == 1) {
                tab.setText(binding.root.context?.getString(R.string.liked_videos))
            } else if (position == 2) {
                tab.setText(binding.root.context?.getString(R.string.repost))
            }

            else if (position == 3) {
                tab.setText(binding.root.context?.getString(R.string.favourite))
            }
        }
        tabLayoutMediator.attach()
    }

    private fun registerFragmentWithPager() {
        fragmentUserVides = UserVideoFragment.newInstance(
            true,
            getSharedPreference(binding.root.context).getString(Variables.U_ID, "").toString(),
            getSharedPreference(binding.root.context).getString(Variables.U_NAME, "").toString(),
            ""
        )
        adapter!!.addFrag(fragmentUserVides)

        adapter!!.addFrag(
            LikedVideoFragment.newInstance(
                true,
                getSharedPreference(binding.root.context).getString(Variables.U_ID, "").toString(),
                getSharedPreference(binding.root.context).getString(Variables.U_NAME, "").toString(),
                true,
                ""
            )
        )

        adapter!!.addFrag(
            RepostVideoFragment.newInstance(
                true,
                getSharedPreference(binding.root.context).getString(Variables.U_ID, "").toString(),
                getSharedPreference(binding.root.context).getString(Variables.U_NAME, "").toString(),
                ""
            )
        )



        adapter!!.addFrag(PrivateVideoFragment.newInstance())
    }

    fun showDraftCount() {
        try {
            val path = activity?.let { FileUtils.getAppFolder(it) } + Variables.DRAFT_APP_FOLDER
            val directory = File(path)
            val files = directory.listFiles()
            if (files.size <= 0) {
                //draf gone
            } else {
                //draf visible
            }
        } catch (e: Exception) {
        }
    }

    // place the profile data
    private fun setProfileData(isLoadImage:Boolean) {

        try {
            getSharedPreference(binding.root.context).getString(Variables.U_ID,"")
                ?.let { AccountUtils.getUserModel(it)?.let { it1 -> viewModel.setData(it1) } }
        } catch (e: PaperDbException) {
            Paper.book().destroy()
        }


        if(viewModel.userModel.value!=null) {

            if(adapter==null || pager==null) {
                SetTabs()
            }


            if(isLoadImage) {
                if (viewModel.userModel.value?.getProfileGif()!!.isEmpty()) {
                    binding.userImage.controller =
                        frescoImageLoad(
                            viewModel.userModel.value?.getProfilePic(),
                            binding.userImage,
                            false
                        )

                }
                else {
                    binding.userImage.controller = frescoImageLoad(
                        viewModel.userModel.value?.getProfileGif(),
                        R.drawable.ic_user_icon,
                        binding.userImage,
                        true
                    )

                }
            }

            if (viewModel.userModel.value?.video_count!!<1) {

                    binding.createPopupLayout.visibility = View.VISIBLE
                    val aniRotate =
                        AnimationUtils.loadAnimation(
                            binding.root.context,
                            R.anim.up_and_down_animation
                        )
                    binding.createPopupLayout.startAnimation(aniRotate)
            } else {
                binding.createPopupLayout.visibility = View.GONE
                binding.createPopupLayout.clearAnimation()
            }

        }

    }

    // change the icons of the tab
    private fun setupTabIcons() {
        val view1 = LayoutInflater.from(binding.root.context).inflate(R.layout.item_tabs_profile_menu, null)
        val imageView1 = view1.findViewById<ImageView>(R.id.image)
        imageView1.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_my_video_select
            )
        )
        imageView1.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.black),
            PorterDuff.Mode.SRC_IN
        )
        tabLayout!!.getTabAt(0)!!.setCustomView(view1)

        val view2 = LayoutInflater.from(binding.root.context).inflate(R.layout.item_tabs_profile_menu, null)
        val imageView2 = view2.findViewById<ImageView>(R.id.image)
        imageView2.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_liked_video_gray
            )
        )
        imageView2.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.darkgray),
            PorterDuff.Mode.SRC_IN
        )
        tabLayout!!.getTabAt(1)!!.setCustomView(view2)

        val view3 = LayoutInflater.from(binding.root.context).inflate(R.layout.item_tabs_profile_menu, null)
        val imageView3 = view3.findViewById<ImageView>(R.id.image)
        imageView3.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_repost_gray))
        imageView3.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.darkgray),
            PorterDuff.Mode.SRC_IN
        )
        tabLayout!!.getTabAt(2)!!.setCustomView(view3)


            val view5 = LayoutInflater.from(binding.root.context).inflate(R.layout.item_tabs_profile_menu, null)
            val imageView5 = view5.findViewById<ImageView>(R.id.image)
            imageView5.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_lock_gray
                )
            )
            imageView5.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.darkgray),
                PorterDuff.Mode.SRC_IN
            )
            tabLayout!!.getTabAt(3)!!.setCustomView(view5)

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                pager!!.setCurrentItem(tab.position, true)
                val v = tab.customView
                val image = v!!.findViewById<ImageView>(R.id.image)
                image.setColorFilter(
                    ContextCompat.getColor(binding.root.context!!, R.color.black),
                    PorterDuff.Mode.SRC_IN
                )
                when (tab.position) {
                    0 -> if (viewModel.userModel.value?.video_count!! <1) {
                        binding.createPopupLayout.visibility = View.VISIBLE
                        val aniRotate =
                            AnimationUtils.loadAnimation(binding.root.context, R.anim.up_and_down_animation)
                        binding.createPopupLayout.startAnimation(aniRotate)
                    } else {
                        binding.createPopupLayout.visibility = View.GONE
                    }

                    1 -> {
                        binding.createPopupLayout.clearAnimation()
                        binding.createPopupLayout.visibility = View.GONE
                    }

                    2 -> {
                        binding.createPopupLayout.clearAnimation()
                        binding.createPopupLayout.visibility = View.GONE
                    }

                    3 -> {
                        binding.createPopupLayout.clearAnimation()
                        binding.createPopupLayout.visibility = View.GONE
                    }
                }
                tab.setCustomView(v)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val v = tab.customView
                val image = v!!.findViewById<ImageView>(R.id.image)
                image.setColorFilter(
                    ContextCompat.getColor(binding.root.context!!, R.color.darkgray),
                    PorterDuff.Mode.SRC_IN
                )
                tab.setCustomView(v)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }


    private fun updateProfileVisitorCount(userDetailModel: UserModel) {
        if (userDetailModel.profile_visit_count > 0) {
            binding.tabVisitorCount.visibility = View.VISIBLE
            if (userDetailModel.profile_visit_count > 99) {
                binding.tvVisitorPlus.visibility = View.VISIBLE
                binding.tvVisitorCount.text = "99"
            } else {
                binding.tvVisitorPlus.visibility = View.GONE
                binding.tvVisitorCount.text = "" + userDetailModel.profile_visit_count
            }
        } else {
            binding.tabVisitorCount.visibility = View.GONE
        }
    }

    private fun openEditProfile() {
        val intent = Intent(binding.root.context, EditProfileActivity::class.java)
        try {
            resultCallback.launch(intent)
        }catch (e:Exception){
            startActivity(intent)
        }
        activity?.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    // open the following fragment
    private fun openFollowing() {
        val intent = Intent(binding.root.context, FollowsMainTabActivity::class.java)
        intent.putExtra("id", getSharedPreference(binding.root.context).getString(Variables.U_ID, ""))
        intent.putExtra("from_where", "following")
        intent.putExtra("userName", getSharedPreference(binding.root.context).getString(Variables.U_NAME, ""))
        intent.putExtra("followingCount", viewModel.userModel.value?.following_count)
        intent.putExtra("followerCount", viewModel.userModel.value?.followers_count)
        resultUserDetailCallback.launch(intent)
        activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    // open the followers fragment
    private fun openFollowers() {
        val intent = Intent(binding.root.context, FollowsMainTabActivity::class.java)
        intent.putExtra("id", getSharedPreference(binding.root.context).getString(Variables.U_ID, ""))
        intent.putExtra("from_where", "fan")
        intent.putExtra("userName",viewModel.userModel.value?.username)
        intent.putExtra("followingCount", viewModel.userModel.value?.following_count)
        intent.putExtra("followerCount", viewModel.userModel.value?.followers_count)
        resultUserDetailCallback.launch(intent)
        activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }


    override fun onDetach() {
        super.onDetach()
        if (profileReceiver != null) {
            activity?.unregisterReceiver(profileReceiver)
            profileReceiver = null
        }

    }

    inner class ProfileBroadCast : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            viewModel.isRefreshTabs.set(true)
            viewModel.getUserDetails()
        }
    }


    companion object {
        fun newInstance(): ProfileTabFragment {
            val fragment = ProfileTabFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}

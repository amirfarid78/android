package com.coheser.app.activitesfragments.accounts

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.coheser.app.R
import com.coheser.app.adapters.ViewPagerAdapter
import com.coheser.app.databinding.FragmentSignUpBinding
import com.coheser.app.models.UserRegisterModel
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.google.android.material.tabs.TabLayoutMediator

// Use EmailPhoneFragment to sign up / log in with email or phone number
class EmailPhoneFragment : Fragment(){

    lateinit var binding:FragmentSignUpBinding
    var fromWhere =""
    var isBussiness=false
    var userRegisterModel: UserRegisterModel? = UserRegisterModel()
    private var adapter: ViewPagerAdapter? = null

    companion object {
        fun newInstance(fromWhere: String,isBusiness:Boolean,userRegisterModel: UserRegisterModel?): EmailPhoneFragment {
            val fragment = EmailPhoneFragment()
            val args = Bundle()
            args.putString("fromWhere",fromWhere)
            args.putSerializable("user_model",userRegisterModel)
            args.putBoolean("isBusiness",isBusiness)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        arguments?.let {
            fromWhere = it.getString("fromWhere","")
            userRegisterModel = it.getSerializable("user_model") as UserRegisterModel?
            isBussiness = it.getBoolean("isBusiness")
        }

        if(!isBussiness) {
            activity?.setTheme(R.style.whiteStatus)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                activity?.window?.setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                )
            }
        }

        binding =DataBindingUtil.inflate(inflater,R.layout.fragment_sign_up, container, false)
        initControl()
        actionControl()
        return binding.root
    }

    private fun actionControl() {
        binding.goBack.setOnClickListener(DebounceClickHandler{
            activity?.onBackPressed()
        })
    }

    private fun initControl() {
        if (fromWhere == AccountUtils.typeLogin) {
            binding.signupTxt.setText(binding.root.context.getString(R.string.login))
        }

        SetTabs()
    }

    fun SetTabs() {
        adapter = ViewPagerAdapter(this)
        binding.pager.setOffscreenPageLimit(2)
        registerFragmentWithPager()
        binding.pager.setAdapter(adapter)
        addTabs()
        binding.pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabs.getTabAt(position)!!.select()
            }
        })
    }

    private fun addTabs() {
        val tabLayoutMediator = TabLayoutMediator(
            binding.tabs, binding.pager
        ) { tab, position ->
            if (position == 0) {
                tab.text = binding.root.context.getString(R.string.phone)
            } else if (position == 1) {
                tab.text = binding.root.context.getString(R.string.email)
            }
        }
        tabLayoutMediator.attach()
    }

    private fun registerFragmentWithPager() {
        adapter?.addFrag(PhoneFragment.newInstance(fromWhere,userRegisterModel))
        adapter?.addFrag(EmailFragment.newInstance( fromWhere,userRegisterModel))
    }
}
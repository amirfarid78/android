package com.coheser.app.activitesfragments.accounts

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coheser.app.R
import com.coheser.app.adapters.SwitchAccountAdapter
import com.coheser.app.databinding.FragmentManageAccountsBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.DebounceClickHandler
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Variables
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.paperdb.Paper

class ManageAccountsFragment(var callback: FragmentCallBack?) : BottomSheetDialogFragment() {

    lateinit var binding:FragmentManageAccountsBinding
    private var adapter: SwitchAccountAdapter? = null
    private val list = ArrayList<UserModel>()


    companion object {
        fun newInstance(callback: FragmentCallBack?): ManageAccountsFragment {
            val fragment = ManageAccountsFragment(callback)
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =DataBindingUtil.inflate(inflater,R.layout.fragment_manage_accounts, container, false)
        initControl()
        actionControl()
        return binding.root
    }

    private fun actionControl() {
        binding.ivClose.setOnClickListener(DebounceClickHandler{
            dismiss()
        })
        binding.tabAddAccount.setOnClickListener(DebounceClickHandler{
            openAddNewAccount()
        })
    }

    private fun initControl() {
        val layoutManager = LinearLayoutManager(binding.root.context)
        layoutManager.orientation = RecyclerView.VERTICAL
        binding.recyclerview.setLayoutManager(layoutManager)
        adapter = SwitchAccountAdapter(list, AdapterClickListener { view, pos, `object` ->
            val item = `object` as UserModel
            if (view.id == R.id.mainLayout) {
                if (item.isSelected) {
                    // nothing to do because we are already login
                } else {
                    AccountUtils.setUpNewSelectedAccount(view.context, item)
                }
            }
        })
        binding.recyclerview.setAdapter(adapter)


        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                accountList
            }
        }, 300)
    }

    private val accountList: Unit
        private get() {
            list.clear()
            run {
                for (key: String in Paper.book(Variables.MultiAccountKey).allKeys) {
                    val item: UserModel = Paper.book(Variables.MultiAccountKey).read(key)?:continue
                    if(item.id!!.equals(item.id.equals(getSharedPreference(binding.root.getContext()).getString(Variables.U_ID, "")))) {
                            item.isSelected=true
                        }
                    list.add(item)
                    adapter?.notifyDataSetChanged()
                }
            }
        }

    private fun openAddNewAccount() {
        val bundle = Bundle()
        bundle.putBoolean("isShow", true)
        callback?.onResponce(bundle)
        dismiss()
    }
}
package com.coheser.app.activitesfragments.shoping

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.coheser.app.activitesfragments.shoping.adapter.ProfileProductsAdapter
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.databinding.FragmentTaggedProductListBinding
import com.coheser.app.interfaces.AdapterClickListener
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.models.HomeModel

/**
 * A simple [Fragment] subclass.
 */
class TaggedProductsListFragment : BottomSheetDialogFragment {
    var adapter: ProfileProductsAdapter? = null
    var dataList = ArrayList<ProductModel>()
    var homeModel: HomeModel? = null
    var linearLayoutManager: LinearLayoutManager? = null
    var rootref: DatabaseReference? = null
    var binding: FragmentTaggedProductListBinding? = null
    var callback: FragmentCallBack?= null

    constructor()

    constructor(callback:FragmentCallBack) {
        this.callback = callback
    }

    companion object {
        fun newInstance(callback:FragmentCallBack): TaggedProductsListFragment {
            val fragment = TaggedProductsListFragment(callback)
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTaggedProductListBinding.inflate(layoutInflater, container, false)

        val bundle = arguments
        if (bundle != null) {
            homeModel = bundle.getParcelable<Parcelable>("data") as HomeModel?
        }
        rootref = FirebaseDatabase.getInstance().reference
        dataList.addAll(homeModel!!.tagProductList!!)

        linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager!!.orientation = RecyclerView.VERTICAL
        binding!!.recylerview.layoutManager = linearLayoutManager
        binding!!.recylerview.setHasFixedSize(true)
        adapter = ProfileProductsAdapter(requireContext(), dataList, AdapterClickListener { view, pos, `object` ->
            val item = `object` as ProductModel
            val intent = Intent(activity, ShopItemDetailA::class.java)
            intent.putExtra("data", item)
            startActivity(intent)
            })
        binding!!.recylerview.adapter = adapter

        return binding!!.root
    }


    override fun onDetach() {
        val bundle = Bundle()
        bundle.putBoolean("isShow",true)
        callback?.onResponce(bundle)
        super.onDetach()
    }

}

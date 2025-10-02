package com.coheser.app.activitesfragments.shoping;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.coheser.app.R;
import com.coheser.app.activitesfragments.shoping.adapter.ProfileProductsAdapter;
import com.coheser.app.activitesfragments.shoping.models.ProductModel;
import com.coheser.app.databinding.FragmentShopAllBinding;
import com.coheser.app.interfaces.AdapterClickListener;

public class ShopAllF extends Fragment {

    public ShopAllF() {

    }

    public static ShopAllF newInstance(ArrayList<ProductModel> dataList) {
        ShopAllF fragment = new ShopAllF();
        Bundle args = new Bundle();
        args.putSerializable("data",dataList);
        fragment.setArguments(args);
        return fragment;
    }

    FragmentShopAllBinding binding;
    ArrayList<ProductModel>dataList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentShopAllBinding.inflate(inflater,container,false);

        dataList= (ArrayList<ProductModel>) getArguments().getSerializable("data");
        setAdapter();
        return binding.getRoot();
    }



    ProfileProductsAdapter adapter;
    void setAdapter(){
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 2);
        binding.recyclerview.setLayoutManager(linearLayoutManager);

        adapter = new ProfileProductsAdapter(getContext(), dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                ProductModel productModel=(ProductModel) object;
                switch (view.getId()){
                    case R.id.shop_item:
                        Intent intent=new Intent(getActivity(), ShopItemDetailA.class);
                        intent.putExtra("data",  productModel);
                        startActivity(intent);
                        break;
                }
            }
        });
        binding.recyclerview.setAdapter(adapter);


        binding.recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean userScrolled;
            int scrollOutitems,scrollInItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollInItem=linearLayoutManager.findFirstVisibleItemPosition();
                scrollOutitems = linearLayoutManager.findLastVisibleItemPosition();

                if (scrollInItem == 0)
                {
                    recyclerView.setNestedScrollingEnabled(true);
                }
                else
                {
                    recyclerView.setNestedScrollingEnabled(false);
                }
                if (userScrolled && (scrollOutitems == dataList.size() - 1)) {
                    userScrolled = false;

                }


            }

        });

    }


}
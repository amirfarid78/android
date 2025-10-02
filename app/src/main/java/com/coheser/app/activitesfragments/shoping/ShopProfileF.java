package com.coheser.app.activitesfragments.shoping;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.coheser.app.simpleclasses.Variables;
import com.coheser.app.viewModels.ShopViewModel;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.activitesfragments.shoping.adapter.ProfileProductsAdapter;
import com.coheser.app.activitesfragments.shoping.models.ProductModel;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.databinding.FragmentShopProfileBinding;
import com.coheser.app.interfaces.AdapterClickListener;
import com.coheser.app.simpleclasses.Functions;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopProfileF#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopProfileF extends Fragment {

   // RecyclerView recyclerView;
    ArrayList<ProductModel> dataList=new ArrayList<>();
    ProfileProductsAdapter adapter;
 //   View view;
    Context context;
    int pageCount = 0;
    boolean ispostFinsh;

    GridLayoutManager linearLayoutManager;
 //   ProgressBar loadMoreProgress;

    String userId;
    boolean isMyProfile;

    FragmentShopProfileBinding binding;
    ShopViewModel shopViewModel;


    public ShopProfileF() {
        // Required empty public constructor
    }

    public static ShopProfileF newInstance(boolean is_my_profile, String userId) {
        ShopProfileF fragment = new ShopProfileF();
        Bundle args = new Bundle();
        args.putString("userId",userId);
        args.putBoolean("isMyProfile",is_my_profile);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_shop_profile, container, false);

        context = getContext();

        Bundle bundle=getArguments();
        userId=bundle.getString("userId");
        isMyProfile = bundle.getBoolean("isMyProfile");

        shopViewModel = new ViewModelProvider(this).get(ShopViewModel.class);
        setObserver();

        linearLayoutManager = new GridLayoutManager(context, 2);
        binding.recylerview.setLayoutManager(linearLayoutManager);

        adapter = new ProfileProductsAdapter(getContext(), dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                ProductModel productModel=(ProductModel) object;
                switch (view.getId()){
                    case R.id.shop_item:
                        Intent intent=new Intent(getActivity(), ShopItemDetailA.class);
                        intent.putExtra("data",  productModel);
                        shopItemResultLauncher.launch(intent);
                        break;
                }
            }
        });
        binding.recylerview.setAdapter(adapter);


        binding.recylerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                    if (binding.loadMoreProgress.getVisibility() != View.VISIBLE && !ispostFinsh) {
                        binding.loadMoreProgress.setVisibility(View.VISIBLE);
                        pageCount = pageCount + 1;
                        callApiShowProducts();
                    }
                }


            }

        });


        return binding.getRoot();
    }
    private final ActivityResultLauncher<Intent> shopItemResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->{
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        boolean isDelete = data.getBooleanExtra("delete",false);
                        if (isDelete){
                            String pos = data.getStringExtra("productId");
                            assert pos != null;
                            shopViewModel.setDeletedItemPosition(pos);
                        }
                    }
                }
            });
    public void setObserver(){
        shopViewModel.getDeletedItemPosition().observe(getViewLifecycleOwner(), ProductId -> {
            if (ProductId != null) {
                Log.d(Constants.tag,"ProductId to remove :"+ProductId);
                Iterator<ProductModel> iterator = dataList.iterator();
                while (iterator.hasNext()) {
                    ProductModel product = iterator.next();
                    if (product.getProduct().getId().equals(ProductId)) {
                        iterator.remove();
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
                if (dataList.isEmpty()){
                    binding.noDataLayout.setVisibility(View.VISIBLE);
                }else{
                    binding.noDataLayout.setVisibility(View.GONE);
                }
            }
        });
    }


    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (binding!=null && visible) {
            callApiShowProducts();
        }
    }



    Boolean isApiRun = false;
    //this will get the all videos data of user and then parse the data
    private void callApiShowProducts() {
        if (dataList == null)
            dataList = new ArrayList<>();

        isApiRun = true;
        JSONObject parameters = new JSONObject();
        try {
            if (userId.equals(Functions.getSharedPreference(getContext()).getString(Variables.U_ID,""))){
                parameters.put("starting_point", "" + pageCount);
            }else{
                parameters.put("user_id", userId);
                parameters.put("starting_point", "" + pageCount);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(pageCount==0){
            binding.pbar.setVisibility(View.VISIBLE);
            binding.noDataLayout.setVisibility(View.GONE);
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showProducts, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                isApiRun = false;
                parseData(resp);
            }
        });


    }

    public void parseData(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");


            if (code.equals("200")) {
                JSONArray msg = jsonObject.optJSONArray("msg");
                ArrayList<ProductModel> temp_list = new ArrayList<>();

                for (int i = 0; i < msg.length(); i++) {
                    JSONObject itemdata = msg.optJSONObject(i);

                    ProductModel model = new Gson().fromJson(String.valueOf(itemdata), ProductModel.class);
                    temp_list.add(model);
                }

                if (pageCount == 0) {
                    dataList.clear();
                    dataList.addAll(temp_list);
                } else {
                    dataList.addAll(temp_list);
                }

            }


            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.d(Constants.tag,"Exception: "+e);
        } finally {
            binding.pbar.setVisibility(View.GONE);
            binding.loadMoreProgress.setVisibility(View.GONE);

            if (dataList.isEmpty()) {
                binding.noDataLayout.setVisibility(View.VISIBLE);
            } else {
                binding.noDataLayout.setVisibility(View.GONE);
            }
        }
    }

}
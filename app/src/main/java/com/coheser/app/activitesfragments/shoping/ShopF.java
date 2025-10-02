package com.coheser.app.activitesfragments.shoping;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.coheser.app.activitesfragments.WebviewActivity;
import com.coheser.app.activitesfragments.search.SearchMainActivity;
import com.coheser.app.activitesfragments.shoping.models.ProductCategory;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.activitesfragments.shoping.adapter.HorizontalProductsAdapter;
import com.coheser.app.activitesfragments.shoping.models.Product;
import com.coheser.app.activitesfragments.shoping.models.ProductModel;
import com.coheser.app.adapters.SlidingAdapter;
import com.coheser.app.adapters.ViewPagerAdapter;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.databinding.FragmentShopBinding;
import com.coheser.app.interfaces.AdapterClickListener;
import com.coheser.app.models.SliderModel;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;

public class ShopF extends Fragment implements View.OnClickListener {

    public ShopF() {

    }
    public static ShopF newInstance() {
        ShopF fragment = new ShopF();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    FragmentShopBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentShopBinding.inflate(inflater,container,false);

        binding.cartBtn.setOnClickListener(this);
        binding.searchEdit.setOnClickListener(this);
        binding.searchLayout.setOnClickListener(this);
        binding.backBtn.setOnClickListener(this);

        callApiAppSlider();

        callApiTopViewedList();

        callApiPromotedList();

        SetTabs();

        callApigetProductCategory();

        return  binding.getRoot();
    }
    public void openSearch() {
        Intent intent=new Intent(getActivity(), SearchMainActivity.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_layout:
                openSearch();
                break;
            case R.id.search_edit:
                openSearch();
                break;

            case R.id.cartBtn:
                if (Functions.checkLoginUser(requireActivity())) {
                    startActivity(new Intent(getActivity(), YourCartA.class));
                }
                break;

            case R.id.backBtn:
                getActivity().onBackPressed();
                break;

            default:
                return;

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        int count=Functions.getSettingsPreference(getActivity()).getInt(Variables.cartCount,0);
        if(count>0){
            binding.tabCartCount.setVisibility(View.VISIBLE);
            binding.tvCartCount.setText(""+count);
        }else {
            binding.tabCartCount.setVisibility(View.GONE);
        }
    }

    public void callApiAppSlider(){

        JSONObject parameters = new JSONObject();
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showCartSlider, parameters, Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                parseSliderData(resp);
            }
        });

    }

    ArrayList<SliderModel> slider_list = new ArrayList<>();
    public void parseSliderData(String resp) {
        try {
            JSONObject jsonObject = new JSONObject(resp);

            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                slider_list.clear();

                JSONArray msg = jsonObject.optJSONArray("msg");
                for (int i = 0; i < msg.length(); i++) {
                    JSONObject object = msg.optJSONObject(i);
                    JSONObject AppSlider = object.optJSONObject("CartSlider");

                    SliderModel sliderModel = new SliderModel();
                    sliderModel.id = AppSlider.optString("id");
                    sliderModel.setImage(AppSlider.optString("image"));
                    sliderModel.setUrl(AppSlider.optString("url"));

                    slider_list.add(sliderModel);
                }

                setSliderAdapter();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSliderAdapter() {
        binding.imageSlider.setAdapter(new SlidingAdapter(getActivity(), slider_list, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                String slider_url = slider_list.get(pos).getUrl();
                if (slider_url != null && !slider_url.equals("")) {

                    Intent intent=new Intent(view.getContext(), WebviewActivity.class);
                    intent.putExtra("url", slider_url);
                    intent.putExtra("title", "Link");
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
            }
        }));

    }


    ArrayList<ProductModel> dataListTopPic=new ArrayList<>();
    public void setTopPicksAdapter(){

        HorizontalProductsAdapter topPicksAdapter = new HorizontalProductsAdapter(getContext(), dataListTopPic, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

                switch (view.getId()){
                    case R.id.mainLayout:
                        ProductModel productModel=(ProductModel) dataListTopPic.get(pos);
                        Intent intent=new Intent(getActivity(), ShopItemDetailA.class);
                        intent.putExtra("data",  productModel);
                        startActivity(intent);
                        break;
                }

            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false);
        binding.recyclerViewTopPick.setLayoutManager(linearLayoutManager);
        binding.recyclerViewTopPick.setAdapter(topPicksAdapter);

    }

    public void callApiTopViewedList(){

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("starting_point", "0");


        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showTopViewedProducts, parameters, Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {

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

                    dataListTopPic.clear();
                    dataListTopPic.addAll(temp_list);

            }

            setTopPicksAdapter();


        }
        catch (Exception e) {
            Log.d(Constants.tag,"Exception: "+e);
        }
        finally {
            if(dataListTopPic.isEmpty()){
                binding.topPickMainLayout.setVisibility(View.GONE);
            }
            else {
                binding.topPickMainLayout.setVisibility(View.VISIBLE);
            }

            binding.topPickPbar.setVisibility(View.GONE);
        }
    }






    ArrayList<ProductModel> dataListPromoted=new ArrayList<>();
    public void setPromotedProductsAdapter(){
        HorizontalProductsAdapter promotedProductsAdapter = new HorizontalProductsAdapter(getContext(), dataListPromoted, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

                switch (view.getId()){
                    case R.id.mainLayout:
                        Intent intent=new Intent(getActivity(), ShopItemDetailA.class);
                        intent.putExtra("data",  dataListPromoted.get(pos));
                        startActivity(intent);
                        break;
                }


            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false);
        binding.recyclerViewPromoted.setLayoutManager(linearLayoutManager);
        binding.recyclerViewPromoted.setAdapter(promotedProductsAdapter);
    }

    public void callApiPromotedList(){
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("starting_point", "0");


        }
        catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showPromotedProducts, parameters, Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                parseDataPromoted(resp);
            }
        });
    }



    public void parseDataPromoted(String responce) {

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

                dataListPromoted.clear();
                dataListPromoted.addAll(temp_list);

            }

            setPromotedProductsAdapter();


        } catch (Exception e) {
            Log.d(Constants.tag,"Exception: "+e);
        }finally {

            if(dataListPromoted.isEmpty()){
                binding.promotedMainLayout.setVisibility(View.GONE);
            }
            else {
                binding.promotedMainLayout.setVisibility(View.VISIBLE);
            }

            binding.promotedPbar.setVisibility(View.GONE);


        }
    }






    public void callApigetProductCategory(){

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("starting_point", "0");


        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showCategories, parameters, Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                parseDataProductCategory(resp);
            }
        });


    }


    ArrayList<ProductCategory> categoryArrayList = new ArrayList<>();
    public void parseDataProductCategory(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");

            if (code.equals("200")) {

                JSONArray msg = jsonObject.optJSONArray("msg");


                for (int i = 0; i < msg.length(); i++) {
                    JSONObject itemdata = msg.optJSONObject(i);
                    ProductCategory item=new ProductCategory();

                   JSONObject productCategory = itemdata.optJSONObject("ProductCategory");
                   item.name=productCategory.getString("title");


                    JSONArray products= itemdata.optJSONArray("Product");
                    for (int j = 0; j < products.length(); j++) {
                        JSONObject productJson = products.getJSONObject(j);

                        if(productJson!=null) {
                            Product product = new Product();
                            product.setId(productJson.getString("id"));
                            product.setcategory_id(productJson.getString("category_id"));
                            product.setUser_id(productJson.getString("user_id"));
                            product.setTitle(productJson.getString("title"));
                            product.setDescription(productJson.getString("description"));
                            product.setPrice(productJson.getString("price"));
                            product.setSale_price(productJson.getString("sale_price"));
                            product.setPromote(productJson.getString("promote"));
                            product.setView(productJson.getString("view"));
                            product.setUpdated(productJson.getString("updated"));
                            product.setCreated(productJson.getString("created"));

                            ProductModel model = new Gson().fromJson(String.valueOf(productJson), ProductModel.class);
                            model.setProduct(product);

                            item.productModels.add(model);
                        }
                    }

                    categoryArrayList.add(item);
                    adapter.addFrag(ShopAllF.newInstance(item.productModels));

                }

                adapter.notifyDataSetChanged();
                addTabs();

            }
        } catch (Exception e) {
            Log.d(Constants.tag,"Exception: "+e);
        }

    }



    ViewPagerAdapter adapter;
    public void SetTabs() {
        adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
    }


    private void addTabs() {
        TabLayoutMediator tabLayoutMediator=new TabLayoutMediator(binding.tabLayout, binding.viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(categoryArrayList.get(position).name);
            }
        });
        tabLayoutMediator.attach();

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.tabLayout.getTabAt(position).select();
            }
        });

    }



}
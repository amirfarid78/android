package com.coheser.app.activitesfragments.shoping;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.coheser.app.activitesfragments.chat.ChatActivity;
import com.coheser.app.activitesfragments.profile.ReportTypeActivity;
import com.coheser.app.activitesfragments.shoping.AddProducts.AddDetailsA;
import com.coheser.app.activitesfragments.shoping.adapter.ProductVariationAdapter;
import com.coheser.app.databinding.ShopBottomsheetMenuBinding;
import com.coheser.app.simpleclasses.Dialogs;
import com.coheser.app.simpleclasses.ShowMoreLess;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.activitesfragments.shoping.models.ProductAttribute;
import com.coheser.app.activitesfragments.shoping.models.ProductAttributeVariation;
import com.coheser.app.activitesfragments.shoping.models.ProductModel;
import com.coheser.app.adapters.SlidingAdapter;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.databinding.ActivityShopItemDetailBinding;
import com.coheser.app.interfaces.AdapterClickListener;
import com.coheser.app.models.SliderModel;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;

public class ShopItemDetailA extends AppCompatActivity implements View.OnClickListener{
    ActivityShopItemDetailBinding binding;

    ProductModel item=new ProductModel();
    String id;
    ProductModel selectedItem=new ProductModel();

    DatabaseReference rootref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopItemDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        rootref=FirebaseDatabase.getInstance().getReference();

        Intent intent=getIntent();
        if(intent.hasExtra("data")) {
            item = (ProductModel) intent.getParcelableExtra("data");
        }
        else if(intent.hasExtra("id")){
            id=intent.getStringExtra("id");
        }



        binding.addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkValidation();
            }
        });

        binding.buyNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkValidation();
            }
        });

        binding.buyNowBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                checkValidation();
                ItemPuchaseSheet bottomSheet = ItemPuchaseSheet.Companion.newInstance(selectedItem);
                bottomSheet.show(getSupportFragmentManager(), "ItemPuchaseSheet");
            }
        });


        callApiProductDetails();


        binding.cartBtn.setOnClickListener(this);
        binding.favBtn.setOnClickListener(this);
        binding.backBtn.setOnClickListener(this);
        binding.menuBtn.setOnClickListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        int count=Functions.getSettingsPreference(this).getInt(Variables.cartCount,0);
        if(count>0){
            binding.tabCartCount.setVisibility(View.VISIBLE);
            binding.tvCartCount.setText(""+count);
        }
        else {
            binding.tabCartCount.setVisibility(View.GONE);
        }
    }


    private void callApiProductDetails() {
        JSONObject parameters = new JSONObject();
        try {

            if(item.getProduct()!=null) {
                parameters.put("product_id", item.getProduct().getId());
            }
            else {
                parameters.put("product_id",id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

         Functions.showLoader(this,false,false);
        VolleyRequest.JsonPostRequest(this, ApiLinks.showProductDetail, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.cancelLoader();
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");


                    if (code.equals("200")) {
                        JSONObject msg = jsonObject.getJSONObject("msg");
                       item = new Gson().fromJson(String.valueOf(msg), ProductModel.class);
                       Log.d(Constants.tag,"category :"+item.getCategory().getTitle());
                       setData();
                    }

                } catch (JSONException e) {
                    Log.d(Constants.tag,"Exception: "+e);
                }

            }
        });

    }


    private void callApiFavProduct() {
        JSONObject parameters = new JSONObject();
        try {
           parameters.put("product_id",item.getProduct().getId());

        } catch (Exception e) {
            e.printStackTrace();
        }
         Functions.showLoader(this,false,false);
        VolleyRequest.JsonPostRequest(this, ApiLinks.addProductFavourite, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.cancelLoader();
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {

                        if(item.getProductFavouriteObject().getFavourite().equals("1")){
                            item.getProductFavouriteObject().setFavourite("0");
                        }else {
                            item.getProductFavouriteObject().setFavourite("1");
                        }
                        setData();

                    }
                } catch (Exception e) {
                    Log.d(Constants.tag,"Exception: "+e);
                }

            }
        });
    }





    public void setData(){
        setSliderAdapter();
        setDataToVariationList();

        selectedItem.clone(item);
        if(selectedItem.getProductAttribute().size()>0) {
            for (int i = 0; i < selectedItem.getProductAttribute().size(); i++) {
                selectedItem.getProductAttribute().get(i).getProductAttributeVariation().clear();
            }
        }

        if(item.getProduct().getUser_id().equals(Functions.getSharedPreference(this).getString(Variables.U_ID,""))){
            binding.chatBtn.setVisibility(View.GONE);
        }else {
            binding.chatBtn.setVisibility(View.VISIBLE);
            binding.chatBtn.setOnClickListener(this);
        }

        if(TextUtils.isEmpty(item.getProduct().getCondition())){
            binding.conditionLayout.setVisibility(View.GONE);
        }else {
            binding.conditionLayout.setVisibility(View.VISIBLE);
            binding.conditionTxt.setText(Functions.capitalizeEachWord(item.getProduct().getCondition()));
        }

        if (item.getCategory().getTitle() == null){
            binding.categoryText.setVisibility(View.GONE);
        }else{
            binding.categoryText.setVisibility(View.VISIBLE);
            binding.categoryText.setText(item.getCategory().getTitle());
        }


        binding.productTitleTxt.setText(item.getProduct().getTitle());
        binding.discriptionTxt.setText(item.getProduct().getDescription());
        ShowMoreLess builder =  new ShowMoreLess.Builder(this)
                .textLengthAndLengthType(15, ShowMoreLess.TYPE_LINE)
                .showMoreLabel(getString(R.string.show_more))
                .showLessLabel(getString(R.string.show_less))
                .showMoreLabelColor(getColor(R.color.appColor))
                .showLessLabelColor(getColor(R.color.appColor))
                .labelUnderLine(false)
                .expandAnimation(true)
                .enableLinkify(true)
                .textClickable(false, false).build();
        builder.addShowMoreLess(binding.discriptionTxt,item.getProduct().getDescription(),false);
        binding.discriptionTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(builder.getContentExpandStatus())
                    builder.addShowMoreLess(binding.discriptionTxt,item.getProduct().getDescription(),false);
                else
                    builder.addShowMoreLess(binding.discriptionTxt,item.getProduct().getDescription(),true);
            }
        });



        binding.priceTxt.setText(Constants.productShowingCurrency +""+Functions.INSTANCE.getProductPrice(item));
        binding.ratingTxt.setText(item.getProduct().getTotalRatingsObject().getTotalRatings());
        binding.soldTxt.setText(""+item.getProduct().getSold());


        if(item.getProductFavouriteObject().getFavourite().equals("1")) {
            binding.favBtn.setImageDrawable(getDrawable(R.drawable.ic_fav_fill));
        }
        else {
            binding.favBtn.setImageDrawable(getDrawable(R.drawable.ic_fav));
        }

        if(item.getUser()!=null) {
            binding.profileimage.setController(Functions.frescoImageLoad(item.getUser().getProfilePic(),R.drawable.ic_user_icon,binding.profileimage,false));
            binding.username.setText(getString(R.string.dot_shop, item.getUser().username));
        }

        binding.storeLay.setVisibility(View.VISIBLE);

    }

    ArrayList<SliderModel> image_list=new ArrayList<>();
    public void setSliderAdapter() {

        image_list.clear();
        for (int i=0;i<item.getProductImage().size();i++){
            SliderModel model=new SliderModel();
            model.id=item.getProductImage().get(i).getId();
            model.setImage(item.getProductImage().get(i).getImage());
            image_list.add(model);
        }

        binding.pageIndicatorView.setCount(image_list.size());
        binding.pageIndicatorView.setSelection(0);
        binding.viewPager.setAdapter(new SlidingAdapter(this, image_list, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
            }
        }));

        binding.pageIndicatorView.setViewPager(binding.viewPager);

        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }


        });

    }



    ProductVariationAdapter productVariationAdapter;
    ArrayList<ProductAttribute> variationList;
    ArrayList<ProductAttributeVariation> selectedAttributes =new ArrayList<>();
    ProductAttribute selectedAttributeProduct;
    public void setDataToVariationList(){
        variationList= item.getProductAttribute();

        if(variationList.size()>0) {
            binding.recyclerViewVariations.setVisibility(View.VISIBLE);
            productVariationAdapter = new ProductVariationAdapter(this, selectedAttributes, variationList, new ProductVariationAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int parent_pos, int postion) {


                    ProductAttribute productAttribute= variationList.get(parent_pos);
                    selectedItem.getProductAttribute().get(parent_pos).getProductAttributeVariation().clear();
                    selectedItem.getProductAttribute().get(parent_pos).getProductAttributeVariation().add(productAttribute.getProductAttributeVariation().get(postion));


                    for(int j=0;j<productAttribute.getProductAttributeVariation().size();j++) {
                        if (selectedAttributes.contains(productAttribute.getProductAttributeVariation().get(j)))
                            selectedAttributes.remove(productAttribute.getProductAttributeVariation().get(j));
                    }

                    selectedAttributes.add(productAttribute.getProductAttributeVariation().get(postion));

                    for (int i = 0; i< variationList.size(); i++){
                        if(variationList.get(i).getProductAttributeVariation().size()== selectedAttributes.size() && variationList.get(i).getProductAttributeVariation().containsAll(selectedAttributes)){
                            selectedAttributeProduct = variationList.get(i);
                            break;
                        }
                    }

                    productVariationAdapter.notifyDataSetChanged();

                }
            });
            binding.recyclerViewVariations.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerViewVariations.setHasFixedSize(false);
            binding.recyclerViewVariations.setAdapter(productVariationAdapter);
        }

    }


    public void checkValidation(){


        if(selectedItem.getProductAttribute()!=null && selectedItem.getProductAttribute().size()>0) {
            for (int i=0;i<selectedItem.getProductAttribute().size();i++){
                ProductAttribute productAttribute = selectedItem.getProductAttribute().get(i);
                if(productAttribute.getProductAttributeVariation().size()==0){
                    Toast.makeText(this, "Please select "+productAttribute.getName(), Toast.LENGTH_SHORT).show();
                    return;
                }


            }
        }

        String storeUserID=Functions.getSettingsPreference(this).getString(Variables.cartProductStoreId,"");
        if(!TextUtils.isEmpty(storeUserID) && !storeUserID.equalsIgnoreCase(selectedItem.getProduct().getUser_id())){
            Dialogs.showAlert(ShopItemDetailA.this,"Alert","You can't order the products of different seller at same time");
            return;
        }

        addToCart();

    }

    public  void addToCart() {

        if (Functions.checkLoginUser(this)) {
            rootref.keepSynced(true);
            rootref.child("Cart")
                    .child(Functions.getSharedPreference(this).getString(Variables.U_ID, ""))
                    .child(selectedItem.getProduct().getId()).setValue(selectedItem).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            startActivity(new Intent(getApplicationContext(), YourCartA.class));
                        }
                    });
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.backBtn:
                callBackToShop();
                break;

            case R.id.cartBtn:
                if (Functions.checkLoginUser(this)) {
                    startActivity(new Intent(ShopItemDetailA.this, YourCartA.class));
                }
                break;


            case R.id.chatBtn:
                if(Functions.checkLoginUser(this)) {
                    openChatF();
                }
                break;

            case R.id.favBtn:
                if(Functions.checkLoginUser(this)) {
                    callApiFavProduct();
                }
                break;

            case R.id.menuBtn:
                showShopBottomSheet();

                break;

        }
    }

    private void openChatF() {
        Intent intent=new Intent(this, ChatActivity.class);
        intent.putExtra("user_id", item.getProduct().getUser_id());
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    private void callApiDeleteProduct() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("product_id",  item.getProduct().getId());

        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(this,false,false);
        VolleyRequest.JsonPostRequest(this, ApiLinks.deleteProduct, parameters,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.cancelLoader();
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");


                    if (code.equals("200")) {
                        callBackToShop();
                        isDelete = true;
                    }

                } catch (JSONException e) {
                    Log.d(Constants.tag,"Exception: "+e);
                }

            }
        });

    }

    public  void showShopBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        ShopBottomsheetMenuBinding bindingSheet = ShopBottomsheetMenuBinding.inflate(LayoutInflater.from(getApplicationContext()));
        bottomSheetDialog.setContentView(bindingSheet.getRoot());

        if (item.getUser().id.equals(Functions.getSharedPreference(this).getString(Variables.U_ID,""))){
            bindingSheet.deleteBtn.setVisibility(View.VISIBLE);
            bindingSheet.editBtn.setVisibility(View.VISIBLE);
        }else {
            bindingSheet.deleteBtn.setVisibility(View.GONE);
            bindingSheet.editBtn.setVisibility(View.GONE);
        }
        bindingSheet.reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopItemDetailA.this, ReportTypeActivity.class);
                intent.putExtra("id", item.getProduct().getId());
                intent.putExtra("type", "product");
                intent.putExtra("isFrom", false);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                bottomSheetDialog.dismiss();
            }
        });

        bindingSheet.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                Functions.showAlert(ShopItemDetailA.this, "Delete Product", "Are you really want to delete?", "Delete", "Cancel", new Callback() {
                    @Override
                    public void onResponce(String s) {
                        if (s.equalsIgnoreCase(getString(R.string.yes))) {
                            callApiDeleteProduct();
                        }
                    }
                });
            }
        });
        bindingSheet.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopItemDetailA.this, AddDetailsA.class);
                intent.putExtra("from","edit");
                intent.putExtra("productModel",item);
                shopItemResultLauncher.launch(intent);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }
    boolean isUpdate = false;
    boolean isDelete = false;
    private final ActivityResultLauncher<Intent> shopItemResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        isUpdate = data.getBooleanExtra("isUpdate", false);
                        if (isUpdate) {
                           callApiProductDetails();
                        }
                    }
                }
            }
    );

    @Override
    public void onBackPressed() {
        callBackToShop();
    }

    private void callBackToShop(){
        if (isUpdate || isDelete){
            Intent intent = new Intent();
            intent.putExtra("productId",item.getProduct().getId());
            intent.putExtra("update",true);
            setResult(RESULT_OK,intent);
        }
        finish();
    }

}
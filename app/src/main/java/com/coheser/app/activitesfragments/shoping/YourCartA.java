package com.coheser.app.activitesfragments.shoping;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.coheser.app.BuildConfig;
import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.activitesfragments.location.AddAddressActivity;
import com.coheser.app.activitesfragments.location.DeliveryAddress;
import com.coheser.app.activitesfragments.payment.PaymentFragment;
import com.coheser.app.activitesfragments.shoping.adapter.YourCartAdapter;
import com.coheser.app.activitesfragments.shoping.models.Product;
import com.coheser.app.activitesfragments.shoping.models.ProductModel;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.databinding.ActivityYourCartBinding;
import com.coheser.app.interfaces.AdapterClickListener;
import com.coheser.app.interfaces.FragmentCallBack;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.paperdb.Paper;

public class YourCartA extends AppCompatActivity implements View.OnClickListener {
    ActivityYourCartBinding binding;
    YourCartAdapter adapter;
    ArrayList<ProductModel> datalist = new ArrayList<>();
    DatabaseReference myRef;

    String userId;
    int subTotal = 0, totalPrice = 0, shippingFee = 0, discountPrice = 0, totalCoins = 0;
    int discountPercentage = 0;
    String addressId, couponId = "0", paymentMethodId = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityYourCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        myRef = FirebaseDatabase.getInstance().getReference();
        userId = Functions.getSharedPreference(this).getString(Variables.U_ID, "");

        binding.recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recyclerview.setHasFixedSize(true);
        adapter = new YourCartAdapter(this, datalist, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                ProductModel model = (ProductModel) object;
                switch (view.getId()) {
                    case R.id.delete_product_btn:
                        datalist.remove(pos);
                        adapter.notifyDataSetChanged();
                        myRef.child("Cart").child(userId).child(model.getProduct().getId()).removeValue();
                        calculateTotalSum();
                        break;

                    case R.id.rl_plus:
                        ProductModel productModel = datalist.get(pos);
                        productModel.getProduct().setCount(productModel.getProduct().getCount() + 1);
                        datalist.set(pos, productModel);
                        adapter.notifyDataSetChanged();
                        calculateTotalSum();
                        break;

                    case R.id.rl_minus:
                        ProductModel productModel1 = datalist.get(pos);
                        if (productModel1.getProduct().getCount() > 1) {
                            productModel1.getProduct().setCount(productModel1.getProduct().getCount() - 1);
                            datalist.set(pos, productModel1);
                            adapter.notifyDataSetChanged();
                            calculateTotalSum();
                        }
                        break;

                }

            }
        });
        binding.recyclerview.setAdapter(adapter);
        binding.tvAddressChange.setOnClickListener(this);
        binding.btnAddCoupon.setOnClickListener(this);
        binding.paymentlayout.setOnClickListener(this);
        binding.btnContinuCheckout.setOnClickListener(this);
        binding.deleteCartBtn.setOnClickListener(this);
        binding.backBtn.setOnClickListener(this);

        if (Constants.IsProductPriceInCoin) {
            binding.paymentlayout.setVisibility(View.GONE);
        }

        getCartData();
    }

    DatabaseReference query;
    ValueEventListener valueEventListener;

    ///get the all the data from the cart
    public void getCartData() {
        myRef.keepSynced(true);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                datalist.clear();
                if (dataSnapshot.exists()) {
                    Functions.cancelLoader();
                    Functions.printLog(Constants.tag, dataSnapshot.toString());
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Functions.printLog(Constants.tag, snapshot.toString());
                        ProductModel item = snapshot.getValue(ProductModel.class);
                        datalist.add(item);
                    }
                    adapter.notifyDataSetChanged();

                    calculateTotalSum();

                } else {
                    binding.nodataLayout.getRoot().setVisibility(View.VISIBLE);
                    Functions.cancelLoader();
                }

                setCartCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                datalist.clear();
                binding.nodataLayout.getRoot().setVisibility(View.VISIBLE);
                Functions.cancelLoader();
                setCartCount();
            }
        };
        query = myRef.child("Cart").child(userId);
        query.addValueEventListener(valueEventListener);


    }


    void setCartCount() {
        SharedPreferences.Editor editor = Functions.getSettingsPreference(YourCartA.this).edit();
        editor.putInt(Variables.cartCount, datalist.size());

        if (!datalist.isEmpty())
            editor.putString(Variables.cartProductStoreId, datalist.get(0).getProduct().getUser_id());
        else
            editor.putString(Variables.cartProductStoreId, "");

        editor.commit();
    }

    public void calculateTotalSum() {
        int subtotal = 0;
        for (int i = 0; i < datalist.size(); i++) {
            Product product = datalist.get(i).getProduct();
            subtotal = subtotal + (Functions.changeValueToInt(Functions.INSTANCE.getProductPrice(datalist.get(i))) * product.getCount());

        }

        this.subTotal = subtotal;

        discountPrice = ((this.subTotal * discountPercentage) / 100);

        totalPrice = (subTotal + shippingFee - discountPrice);

        binding.subTotalPrice.setText(Constants.productShowingCurrency + subTotal);
        binding.shippingFee.setText(Constants.productShowingCurrency + shippingFee);
        binding.discountTxt.setText("" + discountPercentage);
        binding.totalPriceTxt.setText(Constants.productShowingCurrency + totalPrice);


        String amount = Functions.getSettingsPreference(YourCartA.this).getString(Variables.CoinWorth, "0");
        double coinsWorth = Double.parseDouble(amount);

        totalCoins = (int) (totalPrice / coinsWorth);
        binding.totalBtnTxt.setText(Constants.productSellingCurrency + totalCoins);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.backBtn:
                finish();
                break;

            case R.id.tv_address_change:
                Functions.hideSoftKeyboard(this);
                if (Functions.checkLoginUser(YourCartA.this)) {
                    Intent intent = new Intent(YourCartA.this, AddAddressActivity.class);
                    intent.putExtra("showCurrentLocation", false);
                    try {
                        resultCallback.launch(intent);
                    } catch (Exception e) {
                        startActivity(intent);
                    }
                }

                break;

            case R.id.btn_add_coupon:
                Functions.hideSoftKeyboard(this);
                if (Functions.checkLoginUser(this)) {
                    callVerifyCouponApi();
                }
                break;


            case R.id.paymentlayout:
                if (Functions.checkLoginUser(this)) {
                    openPaymentScreen();
                }
                break;

            case R.id.btn_continu_checkout:
                if (Functions.checkLoginUser(YourCartA.this)) {
                    if (checkValidations()) {
                        callPlaceOrder();
                    }
                }
                break;

            case R.id.delete_cart_btn:
                cartDelete();
                break;

        }

    }

    public void openPaymentScreen() {
        PaymentFragment fragment = new PaymentFragment(new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                if (bundle != null) {
                    setPayment();
                    calculateTotalSum();
                }
            }

        }, true);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(
                R.anim.in_from_right,
                R.anim.out_to_left,
                R.anim.in_from_left,
                R.anim.out_to_right
        );
        fragmentTransaction.replace(android.R.id.content, fragment)
                .addToBackStack(null)
                .commit();
    }


    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        setAddress();
                        calculateTotalSum();
                    }
                }
            });

    public void setAddress() {
        DeliveryAddress deliveryAddress = Paper.book().read(Variables.AdressModel);
        if (deliveryAddress != null) {

            addressId = deliveryAddress.id;

            binding.tvUserAddress.setVisibility(View.VISIBLE);
            binding.tvAddressChange.setText(R.string.change_address);
            binding.tvUserAddress.setText(Functions.INSTANCE.getAddressString(deliveryAddress));
            shippingFee = 0;
            calculateTotalSum();

        }
    }

    public void setPayment() {
        paymentMethodId = Functions.getSharedPreference(this)
                .getString(Variables.payment_id, "0");
        String card4 = Functions.getSharedPreference(this)
                .getString(Variables.last_4, "");

        binding.tvDeliveryMethod.setText(card4);
    }


    public void cartDelete() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setMessage("Do you want to delete your cart")
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    myRef.child("Cart").child(userId).setValue(null);
                    binding.nodataLayout.getRoot().setVisibility(View.VISIBLE);
                    clearCartData();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void clearCartData() {
        binding.tvUserAddress.setVisibility(View.GONE);
        binding.tvUserAddress.setText(null);
        binding.tvAddressChange.setText(R.string.add_address);
        binding.etCouponCart.setText(null);
        binding.etCouponCart.setHint(getString(R.string.add_a_discount_code));
        myRef.child("Cart").child(userId).removeValue();
        binding.nodataLayout.getRoot().setVisibility(View.VISIBLE);
    }

    private void callVerifyCouponApi() {

        if (binding.couponTextinput.getEditText().getText().toString().isEmpty()) {
            binding.couponTextinput.setError(" ");
        } else {
            String coupon = binding.couponTextinput.getEditText().getText().toString().trim();
            binding.couponTextinput.setError(null);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("user_id", userId);
                jsonObject.put("coupon_code", coupon);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Functions.showLoader(this, false, false);

            VolleyRequest.JsonPostRequest(this, ApiLinks.verifyCoupon, jsonObject, Functions.getHeaders(this), new Callback() {
                @Override
                public void onResponce(String res) {
                    if (res != null) {
                        try {
                            JSONObject jsonResponse = new JSONObject(res);
                            int codeId = Integer.parseInt(jsonResponse.optString("code"));
                            Functions.cancelLoader();
                            if (codeId == 200) {
                                JSONObject json = new JSONObject(jsonResponse.toString());
                                JSONObject msgObj = json.getJSONObject("msg");
                                JSONObject json1 = new JSONObject(msgObj.toString());
                                JSONObject couponList = json1.getJSONObject("Coupon");
                                couponId = couponList.getString("id");
                                discountPercentage = Integer.parseInt(couponList.getString("discount"));
                                calculateTotalSum();
                            } else if (codeId == 201) {
                                binding.couponTextinput.setError("Invalid Coupon");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        }
    }

    boolean checkValidations() {
        if (TextUtils.isEmpty(addressId)) {
            Functions.showToast(this, getString(R.string.please_select_the_delivery_address));
            return false;
        } else {
            return true;
        }
    }

    private void callPlaceOrder() {


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", userId);
            jsonObject.put("store_user_id", datalist.get(0).getProduct().getUser_id());
            jsonObject.put("delivery_address_id", addressId);

            if (Constants.IsProductPriceInCoin) {
                jsonObject.put("cod", "0");
            } else {
                jsonObject.put("cod", "1");
            }

            jsonObject.put("instruction", "");
            jsonObject.put("coupon_id", couponId);
            jsonObject.put("device", "android");
            jsonObject.put("version", BuildConfig.VERSION_NAME);
            jsonObject.put("delivery_fee", shippingFee);
            jsonObject.put("delivery", "1");
            jsonObject.put("total", totalCoins);
            jsonObject.put("discount", discountPrice);

            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < datalist.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("product_id", datalist.get(i).getProduct().getId());
                object.put("product_quantity", datalist.get(i).getProduct().getCount());
                object.put("product_attribute_variation_id", datalist.get(i).getProductAttribute());
                jsonArray.put(object);
            }
            jsonObject.put("products", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Functions.showLoader(this, false, false);


        VolleyRequest.JsonPostRequest(this, ApiLinks.placeOrder, jsonObject, Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String res) {
                Functions.cancelLoader();
                if (res != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(res);
                        int codeId = Integer.parseInt(jsonResponse.optString("code"));
                        Functions.cancelLoader();
                        if (codeId == 200) {
                            Functions.showToast(YourCartA.this, getString(R.string.order_place_successfully));
                            clearCartData();
                        } else if (codeId == 201) {
                            Functions.showToast(YourCartA.this, jsonResponse.optString("msg"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (valueEventListener != null && query != null) {
            query.removeEventListener(valueEventListener);
        }
        super.onDestroy();
    }


}
package com.coheser.app.activitesfragments.payment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewFragment;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.databinding.FragmentPaymentBinding;
import com.coheser.app.interfaces.AdapterClickListener;
import com.coheser.app.interfaces.FragmentCallBack;
import com.coheser.app.models.Card;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;

import com.google.gson.Gson;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class PaymentFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    FragmentPaymentBinding binding;
    String userId;
    PaymentMethodsAdapter adapter;
    ArrayList<Card> dataList = new ArrayList<>();
    boolean isViewCreated = false;
    FragmentCallBack fragmentCallBack;
    boolean aBoolean = false;
    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {

                        binding.addPaymentLayout.setVisibility(View.GONE);
                        binding.paymentLayout.setVisibility(View.GONE);
                        binding.editCard.setVisibility(View.VISIBLE);
                        adapter.enableEdit(false);
                        methodCallForPayment();

                    }
                }
            });

    public PaymentFragment(FragmentCallBack fragmentCallBack, boolean aBoolean) {
        this.fragmentCallBack = fragmentCallBack;
        this.aBoolean = aBoolean;
    }


    public PaymentFragment() {

    }

    private void methodOpenWebView() {
        Link link = new Link(getString(R.string.learn_more)).setTextColor(Color.parseColor("#00b14f")).setUnderlined(false);
        LinkBuilder.on(binding.setUpPaymentText).addLink(link).build();
        link.setOnClickListener(new Link.OnClickListener() {
            @Override
            public void onClick(@NotNull String s) {
                openWebView(getString(R.string.learn_more), Constants.privacy_policy);
            }
        });

    }

    private void openWebView(String urlTitle, String sliderUrl) {
        Functions.hideSoftKeyboard(getActivity());

        Intent intent2 = new Intent(getActivity(), WebViewFragment.class);
        intent2.putExtra("url", sliderUrl);
        intent2.putExtra("title", urlTitle);
        startActivity(intent2);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPaymentBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        isViewCreated = true;
        userId = Functions.getSharedPreference(getActivity()).getString(Variables.U_ID, "");
        methodInitView();
        methodInitClickListener();
        methodOpenWebView();
        methodSetPaymentMethodsAdapter();
        binding.editCard.setVisibility(View.VISIBLE);
        methodCallForPayment();
        return view;
    }

    private void methodInitView() {
        binding.addPaymentLayout.setVisibility(View.GONE);
        binding.paymentLayout.setVisibility(View.GONE);
    }

    private void methodSetPaymentMethodsAdapter() {
        Functions.printLog(Constants.tag, "paymentMethodsModelArrayList : " + dataList.size());
        adapter = new PaymentMethodsAdapter(getActivity(), dataList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int postion, Object model) {
                Card card = (Card) model;
                int id = view.getId();
                if (id == R.id.rledit) {
                    Bundle args1 = new Bundle();
                    args1.putString("id", card.getPaymentMethodId());
                    DeleteFragmantDialog payWithBottomSheetFragment = new DeleteFragmantDialog(new FragmentCallBack() {
                        @Override
                        public void onResponce(Bundle bundle) {
                            binding.swiperefreshlayout.setRefreshing(true);
                            methodCallForPayment();
                            adapter.enableEdit(true);
                        }

                    });
                    payWithBottomSheetFragment.setArguments(args1);
                    payWithBottomSheetFragment.show(getActivity().getSupportFragmentManager(), "payWithBottomSheetFragment");

                } else if (id == R.id.mainLayout) {

                    Functions.getSharedPreference(getContext()).edit().putString(Variables.last_4, card.getCard()).apply();
                    Functions.getSharedPreference(getContext()).edit().putString(Variables.payment_id, card.getPaymentMethodId()).apply();
                    Functions.getSharedPreference(getContext()).edit().putString(Variables.cardExpireDate, card.getExpMonth()+"/"+ card.getExpYear()).apply();
                    getActivity().getSupportFragmentManager().popBackStackImmediate();

                }

            }
        });


        binding.paymentMethodsRc.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        binding.paymentMethodsRc.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }


    @Override
    public void onDetach() {
        if (fragmentCallBack != null) {
            Bundle args2 = new Bundle();
            fragmentCallBack.onResponce(args2);
        }
        super.onDetach();

    }

    private void methodCallForPayment() {
        JSONObject params = new JSONObject();

        try {
            params.put("user_id", userId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (dataList.isEmpty() && !binding.swiperefreshlayout.isRefreshing()) {
            binding.shimmerLayoutFrame.shimmerViewContainer.setVisibility(View.VISIBLE);
            binding.shimmerLayoutFrame.shimmerViewContainer.startShimmer();
        }


        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showCards, params, Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {

                binding.swiperefreshlayout.setRefreshing(false);
                binding.shimmerLayoutFrame.shimmerViewContainer.setVisibility(View.GONE);
                binding.shimmerLayoutFrame.shimmerViewContainer.stopShimmer();

                if (resp != null) {

                    try {
                        JSONObject respobj = new JSONObject(resp);
                        if (respobj.getString("code").equals("200")) {

                            JSONArray msgarray = respobj.getJSONArray("msg");
                            dataList.clear();
                            for (int i = 0; i < msgarray.length(); i++) {
                                JSONObject obj = msgarray.getJSONObject(i);
                                JSONObject card = obj.optJSONObject("Card");
                                Card card1=new Gson().fromJson(card.toString(),Card.class);
                                dataList.add(card1);
                            }
                            adapter.notifyDataSetChanged();

                        } else {
                            dataList.clear();
                        }

                    } catch (Exception e) {
                        Functions.printLog(Constants.tag, "Exception: " + e);
                    }
                    finally {

                        if(dataList.isEmpty()){
                            showAddPaymentLayout();
                        }else {
                            binding.addPaymentLayout.setVisibility(View.GONE);
                            binding.paymentLayout.setVisibility(View.VISIBLE);

                        }
                    }
                }

            }


        });


    }

    private void showAddPaymentLayout(){
        binding.editDone.setVisibility(View.GONE);
        binding.editCard.setVisibility(View.GONE);
        binding.addPaymentLayout.setVisibility(View.VISIBLE);
        binding.paymentLayout.setVisibility(View.GONE);
    }



    private void methodInitClickListener() {
        binding.addPaymentMethodBtn.setOnClickListener(this);
        binding.addPaymentLayout.setOnClickListener(this);
        binding.addPaymentBtn.setOnClickListener(this);
        binding.backBtn.setOnClickListener(this);
        binding.editCard.setOnClickListener(this);
        binding.editDone.setOnClickListener(this);
        binding.swiperefreshlayout.setOnRefreshListener(this);
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.addPaymentMethodBtn || id == R.id.add_payment_btn) {

            Intent intent = new Intent(getActivity(), AddCreditCardActivity.class);
            resultCallback.launch(intent);

        } else if (id == R.id.editCard) {
            binding.editDone.setVisibility(View.VISIBLE);
            binding.editCard.setVisibility(View.GONE);
            adapter.enableEdit(true);
        }

        else if (id == R.id.editDone) {
            binding.editDone.setVisibility(View.GONE);
            binding.editCard.setVisibility(View.VISIBLE);
            adapter.enableEdit(false);
        }

        else if (id == R.id.backBtn) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onRefresh() {
        binding.editCard.setVisibility(View.VISIBLE);
        binding.editDone.setVisibility(View.GONE);
        adapter.enableEdit(false);
        methodCallForPayment();
    }


}
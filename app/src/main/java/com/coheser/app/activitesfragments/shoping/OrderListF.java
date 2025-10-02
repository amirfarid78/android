package com.coheser.app.activitesfragments.shoping;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.coheser.app.interfaces.AdapterClickListener;
import com.volley.plus.VPackages.VolleyRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.activitesfragments.shoping.adapter.OrderHistoryAdapter;
import com.coheser.app.activitesfragments.shoping.models.OrderHistoryModel;
import com.coheser.app.apiclasses.ApiLinks;
import com.coheser.app.databinding.FragmentOrderListBinding;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;

public class OrderListF extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    String userId;
    ArrayList<OrderHistoryModel> datalist = new ArrayList<>();
    Handler handler;

    String type;
    int page=0;

    FragmentOrderListBinding binding;

    public static OrderListF newInstance(String type) {
        OrderListF fragment = new OrderListF();
        Bundle args = new Bundle();
        args.putString("type",type);
        fragment.setArguments(args);
        return fragment;
    }

    public OrderListF() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_order_list, container, false);

        userId = Functions.getSharedPreference(requireContext()).getString(Variables.U_ID,"");

        Bundle bundle=getArguments();
        if(bundle!=null) {
            type=bundle.getString("type");
        }

        writeRecycler();

        fetchOrderHistoryApi();


        return binding.getRoot();
    }


    private void fetchOrderHistoryApi() {
        if (datalist.isEmpty() && !binding.refreshLayout.isRefreshing()) {
            binding.progressbar.setVisibility(View.VISIBLE);
        } else {
            binding.progressbar.setVisibility(View.GONE);
        }
        JSONObject params = new JSONObject();
        try {
            params.put("user_id", userId);
            params.put("type", type);
            params.put("starting_point", ""+page);

        } catch (JSONException e) {
            binding.progressbar.setVisibility(View.GONE);
            e.printStackTrace();
        }


        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showUserOrders, params,Functions.getHeaders(requireContext()), new com.volley.plus.interfaces.Callback() {
            @Override
            public void onResponce(String resp) {
                binding.progressbar.setVisibility(View.GONE);
                binding.refreshLayout.setRefreshing(false);
                if (resp != null) {
                    try {
                        JSONObject response = new JSONObject(resp);
                        int code = response.optInt("code");
                        if (code == 200) {
                            ArrayList<OrderHistoryModel> tempList=new ArrayList<>();
                            JSONArray msgObj = response.optJSONArray("msg");
                            for (int i=0;i<msgObj.length();i++){
                                JSONObject order=msgObj.getJSONObject(i);
                                OrderHistoryModel item = new Gson().fromJson(String.valueOf(order), OrderHistoryModel.class);
                                tempList.add(item);
                            }


                            if(page==0){
                                datalist.clear();
                            }

                            datalist.addAll(tempList);
                            adapter.notifyDataSetChanged();
                            Functions.printLog(Constants.tag,"Size:"+datalist.size());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(datalist.isEmpty()){
                            binding.nodataLayout.getRoot().setVisibility(View.VISIBLE);
                        }
                        else {
                            binding.nodataLayout.getRoot().setVisibility(View.GONE);
                        }
                    }
                }

            }
        });


    }

    OrderHistoryAdapter adapter;
    private void writeRecycler() {

         LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
         binding.recylerview.setLayoutManager(layoutManager);
         adapter = new OrderHistoryAdapter(getActivity(), datalist, new AdapterClickListener() {
             @Override
             public void onItemClick(View view, int pos, Object object) {
                 OrderHistoryModel model = (OrderHistoryModel) object;
                 if (view.getId() == R.id.order_detail_main_d) {
                     OrderHistory_F orderHistoryF = new OrderHistory_F();
                     FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                     Bundle args = new Bundle();
                     args.putParcelable("data", model);
                     orderHistoryF.setArguments(args);
                     transaction.addToBackStack(null);
                     transaction.replace(R.id.HistoryA, orderHistoryF).commit();
                 }
             }
         });
         binding.recylerview.setAdapter(adapter);
    }

    @Override
    public void setMenuVisibility(boolean isVisibleToUser) {
        super.setMenuVisibility(isVisibleToUser);
        if (binding != null && isVisibleToUser) {
            handler = new Handler();
            new Handler().postDelayed(() -> fetchOrderHistoryApi(), 500);
        }
    }

    @Override
    public void onRefresh() {
        fetchOrderHistoryApi();
    }

}

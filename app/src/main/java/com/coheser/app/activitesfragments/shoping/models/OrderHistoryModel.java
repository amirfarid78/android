package com.coheser.app.activitesfragments.shoping.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import com.coheser.app.activitesfragments.location.DeliveryAddress;
import com.coheser.app.models.UserModel;

public class OrderHistoryModel implements Parcelable {
    @SerializedName("Order")
    public Order order;

    @SerializedName("DeliveryAddress")
    public DeliveryAddress deliveryAddress;

    @SerializedName("Product")
    public ProductHistoryModel product;

    @SerializedName("User")
    public UserModel user;

    protected OrderHistoryModel(Parcel in) {
        order = in.readParcelable(Order.class.getClassLoader());
        deliveryAddress = in.readParcelable(DeliveryAddress.class.getClassLoader());
        product = in.readParcelable(ProductHistoryModel.class.getClassLoader());
        user = in.readParcelable(UserModel.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(order, flags);
        dest.writeParcelable(deliveryAddress, flags);
        dest.writeParcelable(product, flags);
        dest.writeParcelable(user, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OrderHistoryModel> CREATOR = new Creator<OrderHistoryModel>() {
        @Override
        public OrderHistoryModel createFromParcel(Parcel in) {
            return new OrderHistoryModel(in);
        }

        @Override
        public OrderHistoryModel[] newArray(int size) {
            return new OrderHistoryModel[size];
        }
    };
}

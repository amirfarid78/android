package com.coheser.app.activitesfragments.shoping.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class OrderProduct implements Parcelable {
    public String id;
    public String product_id;
    public String order_id;
    public String product_title;
    public String product_price;
    public String product_quantity;
    public String product_image;
    public String product_attritube_combination_id;

    @SerializedName("ProductRating")
    public ProductRating productRating;

    protected OrderProduct(Parcel in) {
        id = in.readString();
        product_id = in.readString();
        order_id = in.readString();
        product_title = in.readString();
        product_price = in.readString();
        product_quantity = in.readString();
        product_image = in.readString();
        product_attritube_combination_id = in.readString();
        productRating = in.readParcelable(ProductRating.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(product_id);
        dest.writeString(order_id);
        dest.writeString(product_title);
        dest.writeString(product_price);
        dest.writeString(product_quantity);
        dest.writeString(product_image);
        dest.writeString(product_attritube_combination_id);
        dest.writeParcelable(productRating, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OrderProduct> CREATOR = new Creator<OrderProduct>() {
        @Override
        public OrderProduct createFromParcel(Parcel in) {
            return new OrderProduct(in);
        }

        @Override
        public OrderProduct[] newArray(int size) {
            return new OrderProduct[size];
        }
    };
}

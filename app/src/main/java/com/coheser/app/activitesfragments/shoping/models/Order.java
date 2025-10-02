package com.coheser.app.activitesfragments.shoping.models;

import android.os.Parcel;
import android.os.Parcelable;


public class Order implements Parcelable {
    public String id;
    public String store_user_id;
    public String user_id;
    public String delivery;
    public String delivery_fee;
    public String discount;
    public String total;
    public String status;
    public String device;
    public String version;
    public String created;

    protected Order(Parcel in) {
        id = in.readString();
        store_user_id = in.readString();
        user_id = in.readString();
        delivery = in.readString();
        delivery_fee = in.readString();
        discount = in.readString();
        total = in.readString();
        status = in.readString();
        device = in.readString();
        version = in.readString();
        created = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(store_user_id);
        dest.writeString(user_id);
        dest.writeString(delivery);
        dest.writeString(delivery_fee);
        dest.writeString(discount);
        dest.writeString(total);
        dest.writeString(status);
        dest.writeString(device);
        dest.writeString(version);
        dest.writeString(created);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };
}


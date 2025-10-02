package com.coheser.app.activitesfragments.shoping.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ProductRating implements Parcelable {

    public String id;
    public String star;
    public String comment;
    public String created;
    public String user_id;
    public String product_id;
    public String order_id;
    public String language_id;

    protected ProductRating(Parcel in) {
        id = in.readString();
        star = in.readString();
        comment = in.readString();
        created = in.readString();
        user_id = in.readString();
        product_id = in.readString();
        order_id = in.readString();
        language_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(star);
        dest.writeString(comment);
        dest.writeString(created);
        dest.writeString(user_id);
        dest.writeString(product_id);
        dest.writeString(order_id);
        dest.writeString(language_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProductRating> CREATOR = new Creator<ProductRating>() {
        @Override
        public ProductRating createFromParcel(Parcel in) {
            return new ProductRating(in);
        }

        @Override
        public ProductRating[] newArray(int size) {
            return new ProductRating[size];
        }
    };
}

package com.coheser.app.activitesfragments.shoping.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ProductFavourite implements Parcelable {
    private String favourite;


    // Getter Methods

    protected ProductFavourite(Parcel in) {
        favourite = in.readString();
    }

    public static final Creator<ProductFavourite> CREATOR = new Creator<ProductFavourite>() {
        @Override
        public ProductFavourite createFromParcel(Parcel in) {
            return new ProductFavourite(in);
        }

        @Override
        public ProductFavourite[] newArray(int size) {
            return new ProductFavourite[size];
        }
    };

    public String getFavourite() {
        return favourite;
    }

    // Setter Methods

    public void setFavourite(String favourite) {
        this.favourite = favourite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(favourite);
    }
}
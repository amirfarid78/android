package com.coheser.app.activitesfragments.shoping.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ProductImage implements Parcelable {
    private String id;
    private String product_id;
    private String image;
    private String thum;
    private String created;

    public ProductImage() {
    }

    public ProductImage(String id, String product_id, String image, String thum, String created) {
        this.id = id;
        this.product_id = product_id;
        this.image = image;
        this.thum = thum;
        this.created = created;
    }

    public ProductImage(ProductImage productImage) {
        this.id = productImage.id;
        this.product_id = productImage.product_id;
        this.image = productImage.image;
        this.thum = productImage.thum;
        this.created = productImage.created;
    }

    protected ProductImage(Parcel in) {
        id = in.readString();
        product_id = in.readString();
        image = in.readString();
        thum = in.readString();
        created = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(product_id);
        dest.writeString(image);
        dest.writeString(thum);
        dest.writeString(created);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProductImage> CREATOR = new Creator<ProductImage>() {
        @Override
        public ProductImage createFromParcel(Parcel in) {
            return new ProductImage(in);
        }

        @Override
        public ProductImage[] newArray(int size) {
            return new ProductImage[size];
        }
    };

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThum() {
        return thum;
    }

    public void setThum(String thum) {
        this.thum = thum;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
}

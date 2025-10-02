package com.coheser.app.activitesfragments.shoping.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ProductAttribute implements Parcelable {
    String id;
    String product_id;
    String name;
    String created;
    @SerializedName("ProductAttributeVariation")
    ArrayList<ProductAttributeVariation> productAttributeVariation=new ArrayList<>();

    public ProductAttribute(){}

    public ProductAttribute(String id, String product_id, String name, String created, ArrayList<ProductAttributeVariation> productAttributeVariation) {
        this.id = id;
        this.product_id = product_id;
        this.name = name;
        this.created = created;
        this.productAttributeVariation = productAttributeVariation;
    }

    public ProductAttribute(ProductAttribute productAttribute) {
        this.id = productAttribute.id;
        this.product_id = productAttribute.product_id;
        this.name = productAttribute.name;
        this.created = productAttribute.created;

        for(ProductAttributeVariation productAttributeVariation:productAttribute.getProductAttributeVariation()){
            this.productAttributeVariation.add(new ProductAttributeVariation(productAttributeVariation));
        }
    }


    protected ProductAttribute(Parcel in) {
        id = in.readString();
        product_id = in.readString();
        name = in.readString();
        created = in.readString();
    }

    public static final Creator<ProductAttribute> CREATOR = new Creator<ProductAttribute>() {
        @Override
        public ProductAttribute createFromParcel(Parcel in) {
            return new ProductAttribute(in);
        }

        @Override
        public ProductAttribute[] newArray(int size) {
            return new ProductAttribute[size];
        }
    };

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public ArrayList<ProductAttributeVariation> getProductAttributeVariation() {
        return productAttributeVariation;
    }

    public void setProductAttributeVariation(ArrayList<ProductAttributeVariation> productAttributeVariation) {
        this.productAttributeVariation = productAttributeVariation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(product_id);
        dest.writeString(name);
        dest.writeString(created);
    }
}

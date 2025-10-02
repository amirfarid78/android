package com.coheser.app.activitesfragments.shoping.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.coheser.app.models.UserModel;

import java.util.ArrayList;

public class ProductModel implements Parcelable {
    @SerializedName("Product")
    private Product product;

    @SerializedName("ProductImage")
    private ArrayList<ProductImage> productImage = new ArrayList<>();

    @SerializedName("ProductAttribute")
    private ArrayList<ProductAttribute> productAttribute = new ArrayList<>();

    @SerializedName("User")
    private UserModel user;

    @SerializedName("ProductFavourite")
    private ProductFavourite productFavourite;
    @SerializedName("Category")
    private CategoryModel category;

    private Boolean isSelect = false;
    public int count = 0;

    public ProductModel() {
    }

    public ProductModel(Product product, ArrayList<ProductImage> productImage, ArrayList<ProductAttribute> productAttribute) {
        this.product = product;
        this.productImage = productImage;
        this.productAttribute = productAttribute;
    }

    protected ProductModel(Parcel in) {
        product = in.readParcelable(Product.class.getClassLoader());
        productImage = in.createTypedArrayList(ProductImage.CREATOR);
        productAttribute = in.createTypedArrayList(ProductAttribute.CREATOR);
        user = in.readParcelable(UserModel.class.getClassLoader());
        productFavourite = in.readParcelable(ProductFavourite.class.getClassLoader());
        category = in.readParcelable(CategoryModel.class.getClassLoader());
        isSelect = in.readByte() != 0;
        count = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(product, flags);
        dest.writeTypedList(productImage);
        dest.writeTypedList(productAttribute);
        dest.writeParcelable(user, flags);
        dest.writeParcelable(productFavourite, flags);
        dest.writeParcelable(category, flags);
        dest.writeByte((byte) (isSelect ? 1 : 0));
        dest.writeInt(count);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProductModel> CREATOR = new Creator<ProductModel>() {
        @Override
        public ProductModel createFromParcel(Parcel in) {
            return new ProductModel(in);
        }

        @Override
        public ProductModel[] newArray(int size) {
            return new ProductModel[size];
        }
    };

    public void clone(ProductModel productModel) {
        this.product = new Product(productModel.product);

        for (ProductImage productImage1 : productModel.getProductImage()) {
            this.productImage.add(new ProductImage(productImage1));
        }
        for (ProductAttribute attribute : productModel.getProductAttribute()) {
            this.productAttribute.add(new ProductAttribute(attribute));
        }

        this.productFavourite = productModel.productFavourite;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ArrayList<ProductImage> getProductImage() {
        return productImage;
    }

    public void setProductImage(ArrayList<ProductImage> productImage) {
        this.productImage = productImage;
    }

    public ArrayList<ProductAttribute> getProductAttribute() {
        return productAttribute;
    }

    public void setProductAttribute(ArrayList<ProductAttribute> productAttribute) {
        this.productAttribute = productAttribute;
    }

    public ProductFavourite getProductFavouriteObject() {
        return productFavourite;
    }

    public void setProductFavouriteObject(ProductFavourite productFavouriteObject) {
        this.productFavourite = productFavouriteObject;
    }

    public Boolean getSelect() {
        return isSelect;
    }

    public void setSelect(Boolean select) {
        isSelect = select;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public CategoryModel getCategory() {
        return category;
    }

    public void setCategory(CategoryModel category) {
        this.category = category;
    }
}
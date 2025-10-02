package com.coheser.app.activitesfragments.shoping.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Product implements Parcelable {
   String id;
   String category_id;
   String user_id;
   String title;
   String taggedName;
   String description;
   String size;
   String price;
   String sale_price;
   String promote;
   String view;
   String condition;
   String delivery_method;
   String meetup_location_lat;
   String meetup_location_long;
   String meetup_location_string;
   String updated;
   String created;
   int count =1;
   float sold;

   @SerializedName("Shipping")
   Shipping ShippingObject;

   @SerializedName("TotalRatings")
   TotalRatings TotalRatingsObject;

   public Product (){
   }

   public Product(Product product) {
      this.id = product.id;
      this.category_id = product.category_id;
      this.user_id = product.user_id;
      this.title = product.title;
      this.taggedName=product.taggedName;
      this.description = product.description;
      this.size = product.size;
      this.price = product.price;
      this.sale_price = product.sale_price;
      this.condition = product.condition;
      this.delivery_method = product.delivery_method;
      this.meetup_location_lat = product.meetup_location_lat;
      this.meetup_location_long = product.meetup_location_long;
      this.meetup_location_string = product.meetup_location_string;
      this.promote = product.promote;
      this.view = product.view;
      this.updated = product.updated;
      this.created = product.created;
      this.count = product.count;
      this.ShippingObject=product.getShippingObject();
      this.sold =product.sold;
      this.TotalRatingsObject = product.TotalRatingsObject;
   }

   protected Product(Parcel in) {
      id = in.readString();
      category_id = in.readString();
      user_id = in.readString();
      title = in.readString();
      taggedName = in.readString();
      description = in.readString();
      size = in.readString();
      price = in.readString();
      sale_price = in.readString();
      promote = in.readString();
      view = in.readString();
      condition = in.readString();
      delivery_method = in.readString();
      meetup_location_lat = in.readString();
      meetup_location_long = in.readString();
      meetup_location_string = in.readString();
      updated = in.readString();
      created = in.readString();
      count = in.readInt();
      sold = in.readFloat();
   }

   public static final Creator<Product> CREATOR = new Creator<Product>() {
      @Override
      public Product createFromParcel(Parcel in) {
         return new Product(in);
      }

      @Override
      public Product[] newArray(int size) {
         return new Product[size];
      }
   };

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getcategory_id() {
      return category_id;
   }

   public void setcategory_id(String category_id) {
      this.category_id = category_id;
   }

   public String getUser_id() {
      return user_id;
   }

   public void setUser_id(String user_id) {
      this.user_id = user_id;
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getTaggedName() {
      return taggedName;
   }

   public void setTaggedName(String taggedName) {
      this.taggedName = taggedName;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getSize() {
      return size;
   }

   public void setSize(String size) {
      this.size = size;
   }

   public String getPrice() {
      return price;
   }

   public void setPrice(String price) {
      this.price = price;
   }

   public String getSale_price() {
      return sale_price;
   }

   public void setSale_price(String sale_price) {
      this.sale_price = sale_price;
   }

   public String getPromote() {
      return promote;
   }

   public void setPromote(String promote) {
      this.promote = promote;
   }

   public String getView() {
      return view;
   }

   public void setView(String view) {
      this.view = view;
   }


   public String getCondition() {
      return condition;
   }

   public void setCondition(String condition) {
      this.condition = condition;
   }

   public String getDelivery_method() {
      return delivery_method;
   }

   public void setDelivery_method(String delivery_method) {
      this.delivery_method = delivery_method;
   }

   public String getMeetup_location_lat() {
      return meetup_location_lat;
   }

   public void setMeetup_location_lat(String meetup_location_lat) {
      this.meetup_location_lat = meetup_location_lat;
   }

   public String getMeetup_location_long() {
      return meetup_location_long;
   }

   public void setMeetup_location_long(String meetup_location_long) {
      this.meetup_location_long = meetup_location_long;
   }

   public String getMeetup_location_string() {
      return meetup_location_string;
   }

   public void setMeetup_location_string(String meetup_location_string) {
      this.meetup_location_string = meetup_location_string;
   }

   public String getUpdated() {
      return updated;
   }

   public void setUpdated(String updated) {
      this.updated = updated;
   }

   public String getCreated() {
      return created;
   }

   public void setCreated(String created) {
      this.created = created;
   }

   public int getCount() {
      return count;
   }

   public void setCount(int count) {
      this.count = count;
   }

   public Shipping getShippingObject() {
      return ShippingObject;
   }

   public void setShippingObject(Shipping shippingObject) {
      ShippingObject = shippingObject;
   }

   public float getSold() {
      return sold;
   }

   public void setSold(float sold) {
      this.sold = sold;
   }

   public TotalRatings getTotalRatingsObject() {
      return TotalRatingsObject;
   }

   public void setTotalRatingsObject(TotalRatings totalRatingsObject) {
      TotalRatingsObject = totalRatingsObject;
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(@NonNull Parcel dest, int flags) {
      dest.writeString(id);
      dest.writeString(category_id);
      dest.writeString(user_id);
      dest.writeString(title);
      dest.writeString(taggedName);
      dest.writeString(description);
      dest.writeString(size);
      dest.writeString(price);
      dest.writeString(sale_price);
      dest.writeString(promote);
      dest.writeString(view);
      dest.writeString(condition);
      dest.writeString(delivery_method);
      dest.writeString(meetup_location_lat);
      dest.writeString(meetup_location_long);
      dest.writeString(meetup_location_string);
      dest.writeString(updated);
      dest.writeString(created);
      dest.writeInt(count);
      dest.writeFloat(sold);
   }
}

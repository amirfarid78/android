package com.coheser.app.activitesfragments.shoping.models;

import com.google.gson.annotations.SerializedName;
import com.coheser.app.activitesfragments.location.DeliveryAddress;

import java.io.Serializable;

public class Shipping implements Serializable {

    @SerializedName("DeliveryAddress")
    DeliveryAddress DeliveryAddressObject;

    @SerializedName("Country")
    Country CountryObject;


    // Getter Methods

    public DeliveryAddress getDeliveryAddress() {
        return DeliveryAddressObject;
    }

    public Country getCountry() {
        return CountryObject;
    }

    // Setter Methods

    public void setDeliveryAddress(DeliveryAddress DeliveryAddressObject) {
        this.DeliveryAddressObject = DeliveryAddressObject;
    }

    public void setCountry(Country CountryObject) {
        this.CountryObject = CountryObject;
    }
}
package com.coheser.app.activitesfragments.shoping.models;

public class ShippingRate {
    private String id;
    private String country_id;
    private String weight_min;
    private String weight_max;
    private String quantity_min;
    private String quantity_max;
    private String order_price;
    private String shipping_fee;


    // Getter Methods

    public String getId() {
        return id;
    }

    public String getCountry_id() {
        return country_id;
    }

    public String getWeight_min() {
        return weight_min;
    }

    public String getWeight_max() {
        return weight_max;
    }

    public String getQuantity_min() {
        return quantity_min;
    }

    public String getQuantity_max() {
        return quantity_max;
    }

    public String getOrder_price() {
        return order_price;
    }

    public String getShipping_fee() {
        return shipping_fee;
    }

    // Setter Methods

    public void setId(String id) {
        this.id = id;
    }

    public void setCountry_id(String country_id) {
        this.country_id = country_id;
    }

    public void setWeight_min(String weight_min) {
        this.weight_min = weight_min;
    }

    public void setWeight_max(String weight_max) {
        this.weight_max = weight_max;
    }

    public void setQuantity_min(String quantity_min) {
        this.quantity_min = quantity_min;
    }

    public void setQuantity_max(String quantity_max) {
        this.quantity_max = quantity_max;
    }

    public void setOrder_price(String order_price) {
        this.order_price = order_price;
    }

    public void setShipping_fee(String shipping_fee) {
        this.shipping_fee = shipping_fee;
    }
}

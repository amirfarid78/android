package com.coheser.app.activitesfragments.shoping.models;

import java.io.Serializable;

public class ProductAttributeVariation implements Serializable ,Cloneable{
     String id;
     String product_attribute_id;
     String value;
     String price;
     String created;

    public ProductAttributeVariation (){
    }

    public ProductAttributeVariation(String id, String product_attribute_id, String value, String price, String created) {
        this.id = id;
        this.product_attribute_id = product_attribute_id;
        this.value = value;
        this.price = price;
        this.created = created;
    }

    public ProductAttributeVariation(ProductAttributeVariation productAttributeVariation) {
        this.id = productAttributeVariation.id;
        this.product_attribute_id = productAttributeVariation.product_attribute_id;
        this.value = productAttributeVariation.value;
        this.price = productAttributeVariation.price;
        this.created = productAttributeVariation.created;
    }

    @Override
    public ProductAttributeVariation clone() throws CloneNotSupportedException {
        return (ProductAttributeVariation) super.clone();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProduct_attribute_id() {
        return product_attribute_id;
    }

    public void setProduct_attribute_id(String product_attribute_id) {
        this.product_attribute_id = product_attribute_id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
}

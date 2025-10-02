package com.coheser.app.activitesfragments.shoping.models;

import com.google.gson.annotations.SerializedName;

public class Country {
    private String id;
    private String name;
    private String iso3;
    private String short_name;
    private String phonecode;
    private String capital;
    private String currency;
    private String region;
    private String subregion;
    private String emoji;
    private String emojiU;
    private String created_at;
    private String updated_at;
    private boolean flag;
    private String wikiDataId;
    private String active;
    @SerializedName("ShippingRate")
    ShippingRate ShippingRateObject;


    // Getter Methods

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIso3() {
        return iso3;
    }

    public String getShort_name() {
        return short_name;
    }

    public String getPhonecode() {
        return phonecode;
    }

    public String getCapital() {
        return capital;
    }

    public String getCurrency() {
        return currency;
    }



    public String getRegion() {
        return region;
    }

    public String getSubregion() {
        return subregion;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getEmojiU() {
        return emojiU;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public boolean getFlag() {
        return flag;
    }

    public String getWikiDataId() {
        return wikiDataId;
    }

    public String getActive() {
        return active;
    }

    public ShippingRate getShippingRate() {
        return ShippingRateObject;
    }

    // Setter Methods

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIso3(String iso3) {
        this.iso3 = iso3;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public void setPhonecode(String phonecode) {
        this.phonecode = phonecode;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }



    public void setRegion(String region) {
        this.region = region;
    }

    public void setSubregion(String subregion) {
        this.subregion = subregion;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public void setEmojiU(String emojiU) {
        this.emojiU = emojiU;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public void setWikiDataId(String wikiDataId) {
        this.wikiDataId = wikiDataId;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public void setShippingRate(ShippingRate ShippingRateObject) {
        this.ShippingRateObject = ShippingRateObject;
    }
}
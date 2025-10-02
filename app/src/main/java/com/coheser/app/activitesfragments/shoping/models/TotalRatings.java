package com.coheser.app.activitesfragments.shoping.models;

import java.io.Serializable;

public class TotalRatings implements Serializable {
    private String avg;
    private String totalRatings;


    public String getAvg() {
        return avg;
    }

    public String getTotalRatings() {
        return totalRatings;
    }


    public void setAvg(String avg) {
        this.avg = avg;
    }

    public void setTotalRatings(String totalRatings) {
        this.totalRatings = totalRatings;
    }


}
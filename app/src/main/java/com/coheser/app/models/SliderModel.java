package com.coheser.app.models;

import com.coheser.app.Constants;
import com.coheser.app.simpleclasses.Variables;


public class SliderModel {

    public String id;
    private String url, image;

    public SliderModel() {
    }

    public String getUrl() {
        if (url == null || url.trim().isEmpty()) {
            return "";
        }
//        if (!url.contains(Variables.http)) {
//            return Constants.BASE_URL + url;
//        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImage() {
        if (image == null || image.trim().isEmpty()){
            return "";
        }
        if (!image.contains(Variables.http)) {
            return Constants.BASE_URL + image;
        }
        return image;
    }


    public void setImage(String image) {
        this.image = image;
    }
}
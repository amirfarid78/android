package com.coheser.app.models;


import com.coheser.app.Constants;
import com.coheser.app.simpleclasses.Variables;

import java.io.Serializable;

/**
 * Created by qboxus on 2/22/2019.
 */


public class SoundsModel implements Serializable {

    public String id, name, description, section, duration, created, favourite;
    private String audio;
    private String thum;

    public SoundsModel() {
    }

    public String getAudio() {
        if (!audio.contains(Variables.http)) {
            audio = Constants.BASE_URL + audio;
        }
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getThum() {
        if (!thum.contains(Variables.http)) {
            thum = Constants.BASE_URL + thum;
        }
        return thum;
    }

    public void setThum(String thum) {
        this.thum = thum;
    }
}

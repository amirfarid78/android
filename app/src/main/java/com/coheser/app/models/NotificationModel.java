package com.coheser.app.models;

import com.coheser.app.Constants;
import com.coheser.app.simpleclasses.Variables;

import java.io.Serializable;

/**
 * Created by qboxus on 2/25/2019.
 */

public class NotificationModel implements Serializable {


    public String  effected_fb_id, type;
    public String video_id, created, status, live_streaming_id;

    public String id, string,order_id;
    private String video, thum, gif;

    public UserModel senderModel;

    public NotificationModel() {
    }

    public String getVideo() {
        if (!video.contains(Variables.http)) {
            video = Constants.BASE_URL + video;
        }
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
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

    public String getGif() {
        if (!gif.contains(Variables.http)) {
            gif = Constants.BASE_URL + gif;
        }
        return gif;
    }

    public void setGif(String gif) {
        this.gif = gif;
    }

}

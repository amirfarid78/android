package com.coheser.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.coheser.app.Constants;
import com.coheser.app.simpleclasses.Variables;

public class StoryVideoModel implements Parcelable {
    public static final Creator<StoryVideoModel> CREATOR = new Creator<StoryVideoModel>() {
        @Override
        public StoryVideoModel createFromParcel(Parcel in) {
            return new StoryVideoModel(in);
        }

        @Override
        public StoryVideoModel[] newArray(int size) {
            return new StoryVideoModel[size];
        }
    };
    private String id;
    private String user_id;
    private String description;
    private String video;
    private String thum;
    private String gif;
    private String view;
    private String section;
    private String sound_id;
    private String privacy_type;
    private String allow_comments;
    private String allow_duet;
    private String block;
    private String duet_video_id;
    private String old_video_id;
    private String duration;
    private String promote;
    private String pin_comment_id;
    private String pin;
    private String repost_user_id;
    private String repost_video_id;
    private String quality_check;
    private String aws_job_id;
    private String aws_label;
    private String story;
    private String created;

    public StoryVideoModel() {
    }

    protected StoryVideoModel(Parcel in) {
        id = in.readString();
        user_id = in.readString();
        description = in.readString();
        video = in.readString();
        thum = in.readString();
        gif = in.readString();
        view = in.readString();
        section = in.readString();
        sound_id = in.readString();
        privacy_type = in.readString();
        allow_comments = in.readString();
        allow_duet = in.readString();
        block = in.readString();
        duet_video_id = in.readString();
        old_video_id = in.readString();
        duration = in.readString();
        promote = in.readString();
        pin_comment_id = in.readString();
        pin = in.readString();
        repost_user_id = in.readString();
        repost_video_id = in.readString();
        quality_check = in.readString();
        aws_job_id = in.readString();
        aws_label = in.readString();
        story = in.readString();
        created = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(user_id);
        dest.writeString(description);
        dest.writeString(video);
        dest.writeString(thum);
        dest.writeString(gif);
        dest.writeString(view);
        dest.writeString(section);
        dest.writeString(sound_id);
        dest.writeString(privacy_type);
        dest.writeString(allow_comments);
        dest.writeString(allow_duet);
        dest.writeString(block);
        dest.writeString(duet_video_id);
        dest.writeString(old_video_id);
        dest.writeString(duration);
        dest.writeString(promote);
        dest.writeString(pin_comment_id);
        dest.writeString(pin);
        dest.writeString(repost_user_id);
        dest.writeString(repost_video_id);
        dest.writeString(quality_check);
        dest.writeString(aws_job_id);
        dest.writeString(aws_label);
        dest.writeString(story);
        dest.writeString(created);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSound_id() {
        return sound_id;
    }

    public void setSound_id(String sound_id) {
        this.sound_id = sound_id;
    }

    public String getPrivacy_type() {
        return privacy_type;
    }

    public void setPrivacy_type(String privacy_type) {
        this.privacy_type = privacy_type;
    }

    public String getAllow_comments() {
        return allow_comments;
    }

    public void setAllow_comments(String allow_comments) {
        this.allow_comments = allow_comments;
    }

    public String getAllow_duet() {
        return allow_duet;
    }

    public void setAllow_duet(String allow_duet) {
        this.allow_duet = allow_duet;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getDuet_video_id() {
        return duet_video_id;
    }

    public void setDuet_video_id(String duet_video_id) {
        this.duet_video_id = duet_video_id;
    }

    public String getOld_video_id() {
        return old_video_id;
    }

    public void setOld_video_id(String old_video_id) {
        this.old_video_id = old_video_id;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPromote() {
        return promote;
    }

    public void setPromote(String promote) {
        this.promote = promote;
    }

    public String getPin_comment_id() {
        return pin_comment_id;
    }

    public void setPin_comment_id(String pin_comment_id) {
        this.pin_comment_id = pin_comment_id;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getRepost_user_id() {
        return repost_user_id;
    }

    public void setRepost_user_id(String repost_user_id) {
        this.repost_user_id = repost_user_id;
    }

    public String getRepost_video_id() {
        return repost_video_id;
    }

    public void setRepost_video_id(String repost_video_id) {
        this.repost_video_id = repost_video_id;
    }

    public String getQuality_check() {
        return quality_check;
    }

    public void setQuality_check(String quality_check) {
        this.quality_check = quality_check;
    }

    public String getAws_job_id() {
        return aws_job_id;
    }

    public void setAws_job_id(String aws_job_id) {
        this.aws_job_id = aws_job_id;
    }

    public String getAws_label() {
        return aws_label;
    }

    public void setAws_label(String aws_label) {
        this.aws_label = aws_label;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
}

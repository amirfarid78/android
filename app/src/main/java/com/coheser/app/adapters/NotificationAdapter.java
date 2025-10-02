package com.coheser.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.coheser.app.Constants;
import com.coheser.app.R;
import com.coheser.app.models.NotificationModel;
import com.coheser.app.simpleclasses.DateOprations;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.TicTicApp;
import com.coheser.app.simpleclasses.Variables;

import java.util.ArrayList;

/**
 * Created by qboxus on 3/20/2018.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.CustomViewHolder> {
    public Context context;
    public OnItemClickListener listener;
    ArrayList<NotificationModel> datalist;

    public NotificationAdapter(Context context, ArrayList<NotificationModel> arrayList, OnItemClickListener listener) {
        this.context = context;
        datalist = arrayList;
        this.listener = listener;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notification, viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        final NotificationModel item = datalist.get(i);
        holder.username.setText(item.senderModel.username);
        holder.userImage.setController(Functions.frescoImageLoad(item.senderModel.getProfilePic(), holder.userImage, false));


        String date = DateOprations.changeDateLatterFormat("yyyy-MM-dd HH:mm:ssZZ", context, item.created + "+0000");


        if (Functions.getSharedPreference(holder.itemView.getContext())
                .getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE).equals("en")) {

            SpannableString messageSpanble = new SpannableString(item.string);
            SpannableString spanDate = new SpannableString(date);

            spanDate.setSpan(new ForegroundColorSpan(context.getColor(R.color.graycolor)), 0, date.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanDate.setSpan(new RelativeSizeSpan(0.9f), 0, date.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            CharSequence finalText = TextUtils.concat(messageSpanble, " ", spanDate);
            Log.d(Constants.tag, finalText.toString());

            holder.message.setText(finalText);
        } else {
            String message = item.string;

            String messageVideoLikeEn = item.senderModel.username + " " + Variables.liked_your_video_en;
            String messageVideoLikeAr = item.senderModel.username + " " + Variables.liked_your_video_ar;
            message = message.replace(messageVideoLikeEn, messageVideoLikeAr);


            String messageVideoPostEn = item.senderModel.username + " " + Variables.has_posted_a_video_en;
            String messageVideoPostAr = item.senderModel.username + " " + Variables.has_posted_a_video_ar;
            message = message.replace(messageVideoPostEn, messageVideoPostAr);


            String messageEn = item.senderModel.username + " " + Variables.started_following_you_en;
            String messageAr = item.senderModel.username + " " + Variables.started_following_you_ar;
            message = message.replace(messageEn, messageAr);


            String messageFollowEn = item.senderModel.username + " " + Variables.started_following_you_en;
            String messageFollowAr = item.senderModel.username + " " + Variables.started_following_you_ar;
            message = message.replace(messageFollowEn, messageFollowAr);


            String messageLiveEn = item.senderModel.username + " " + Variables.is_live_now_en;
            String messageLiveAr = item.senderModel.username + " " + Variables.is_live_now_ar;
            message = message.replace(messageLiveEn, messageLiveAr);


            String messageTagEn = item.senderModel.username + " " + Variables.mentioned_you_in_a_comment_en;
            String messageTagAr = item.senderModel.username + " " + Variables.mentioned_you_in_a_comment_ar;
            message = message.replace(messageTagEn, messageTagAr);


            String messageRepliedEn = item.senderModel.username + " " + Variables.replied_to_your_comment_en;
            String messageRepliedAr = item.senderModel.username + " " + Variables.replied_to_your_comment_ar;
            message = message.replace(messageRepliedEn, messageRepliedAr);

            String messageCommentEn = item.senderModel.username + " " + Variables.commented_en;
            String messageCommentAr = item.senderModel.username + " " + Variables.commented_ar;
            message = message.replace(messageCommentEn, messageCommentAr);

            SpannableString messageSpanble = new SpannableString(message);
            SpannableString spanDate = new SpannableString(date);

            spanDate.setSpan(new ForegroundColorSpan(Color.RED), 0, date.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanDate.setSpan(new RelativeSizeSpan(1.5f), 0, date.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            CharSequence finalText = TextUtils.concat(messageSpanble, " ", spanDate);
            Log.d(Constants.tag, finalText.toString());
            holder.message.setText(finalText);

        }

        if (item.type.equalsIgnoreCase("video_comment") || item.type.equals("video_updates")|| item.type.equalsIgnoreCase("comment_like")) {
            holder.video_thumb.setController(Functions.frescoImageLoad(item.getThum(), holder.video_thumb, false));
            holder.rightView.setVisibility(View.VISIBLE);
            holder.watchBtn.setVisibility(View.VISIBLE);
            holder.followBtn.setVisibility(View.GONE);
            holder.btnDeleteRequest.setVisibility(View.GONE);
            holder.btnAcceptRequest.setVisibility(View.GONE);
        } else if (item.type.equalsIgnoreCase("video_like") || item.type.equals("video_updates")) {
            holder.video_thumb.setController(Functions.frescoImageLoad(item.getThum(), holder.video_thumb, false));
            holder.rightView.setVisibility(View.VISIBLE);
            holder.watchBtn.setVisibility(View.VISIBLE);
            holder.followBtn.setVisibility(View.GONE);
            holder.btnDeleteRequest.setVisibility(View.GONE);
            holder.btnAcceptRequest.setVisibility(View.GONE);
        } else if (item.type.equalsIgnoreCase("single") || item.type.equalsIgnoreCase("multiple")) {

            if (item.status.equalsIgnoreCase("0")) {
                if (TicTicApp.allLiveStreaming.containsKey(item.live_streaming_id)) {
                    holder.rightView.setVisibility(View.VISIBLE);
                    holder.watchBtn.setVisibility(View.GONE);
                    holder.followBtn.setVisibility(View.GONE);
                    holder.btnDeleteRequest.setVisibility(View.VISIBLE);
                    holder.btnAcceptRequest.setVisibility(View.VISIBLE);
                } else {
                    holder.rightView.setVisibility(View.GONE);
                }
            } else {
                holder.rightView.setVisibility(View.GONE);
            }
        } else if (item.type.equalsIgnoreCase("following")) {
            holder.rightView.setVisibility(View.VISIBLE);
            holder.watchBtn.setVisibility(View.GONE);

            holder.btnDeleteRequest.setVisibility(View.GONE);
            holder.btnAcceptRequest.setVisibility(View.GONE);

            if (item.senderModel.id.equals(item.effected_fb_id)) {
                holder.followBtn.setVisibility(View.GONE);
                holder.rightView.setVisibility(View.GONE);
            } else {
                holder.followBtn.setVisibility(View.VISIBLE);
                holder.followBtn.setText(item.senderModel.button);

                if (item.senderModel.button != null &&
                        (item.senderModel.button.equalsIgnoreCase("follow") || item.senderModel.button.equalsIgnoreCase("follow back"))) {
                    holder.followBtn.setVisibility(View.VISIBLE);
                    holder.followBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.d_round_colord_6));
                    holder.followBtn.setTextColor(ContextCompat.getColor(context, R.color.whiteColor));

                } else if (item.senderModel.button != null &&
                        (item.senderModel.button.equalsIgnoreCase("following") || item.senderModel.button.equalsIgnoreCase("friends"))) {
                    if (item.senderModel.button.equalsIgnoreCase("friends")){
                        holder.followBtn.setText("Message");
                        holder.followBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.d_round_gray_background_2));
                        holder.followBtn.setTextColor(ContextCompat.getColor(context, R.color.black));
                    }
                    holder.followBtn.setVisibility(View.VISIBLE);
                    holder.rightView.setVisibility(View.VISIBLE);

                } else if (item.senderModel.button != null && item.senderModel.button.equalsIgnoreCase("0")) {
                    holder.followBtn.setVisibility(View.GONE);
                    holder.rightView.setVisibility(View.GONE);
                }
            }

        } else {
            holder.rightView.setVisibility(View.GONE);
        }

        holder.tvTime.setText(date);


        holder.bind(i, datalist.get(i), listener);

    }

    public interface OnItemClickListener {
        void onItemClick(View view, int postion, NotificationModel item);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView userImage, video_thumb;
        RelativeLayout rightView;

        CardView watchBtn;
        TextView username, message, followBtn, btnAcceptRequest, btnDeleteRequest, tvTime;

        public CustomViewHolder(View view) {
            super(view);
            rightView = view.findViewById(R.id.rightView);
            userImage = view.findViewById(R.id.user_image);
            username = view.findViewById(R.id.username);
            message = view.findViewById(R.id.message);
            watchBtn = view.findViewById(R.id.watch_btn);
            btnAcceptRequest = view.findViewById(R.id.btnAcceptRequest);
            btnDeleteRequest = view.findViewById(R.id.btnDeleteRequest);
            followBtn = view.findViewById(R.id.follow_btn);
            video_thumb = view.findViewById(R.id.video_thumb);
            tvTime = view.findViewById(R.id.tvTime);

        }

        public void bind(final int pos, final NotificationModel item, final OnItemClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);
            });

            watchBtn.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);
            });

            btnAcceptRequest.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);
            });

            btnDeleteRequest.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);
            });


            followBtn.setOnClickListener(v -> {
                listener.onItemClick(v, pos, item);
            });

        }


    }


}
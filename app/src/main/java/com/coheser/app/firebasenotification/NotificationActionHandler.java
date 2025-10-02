package com.coheser.app.firebasenotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.coheser.app.Constants;
import com.coheser.app.activitesfragments.WatchVideosActivity;
import com.coheser.app.activitesfragments.WebviewActivity;
import com.coheser.app.activitesfragments.chat.ChatActivity;
import com.coheser.app.activitesfragments.livestreaming.activities.LiveUsersActivity;
import com.coheser.app.activitesfragments.profile.ProfileActivity;
import com.coheser.app.mainmenu.MainMenuActivity;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;

public class NotificationActionHandler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            
            String receiver_id = intent.getStringExtra("receiver_id");
            String sender_id = intent.getStringExtra("sender_id");
            String user_id = intent.getStringExtra("user_id");
            String video_id = intent.getStringExtra("video_id");
            String image = intent.getStringExtra("image");
            String title = intent.getStringExtra("title");
            String order_id = intent.getStringExtra("order_id");
            String tracking_link = intent.getStringExtra("tracking_link");
            String type = intent.getStringExtra("type");




            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.cancel(intent.getIntExtra("notification_id", 0));

            if (Functions.getSharedPreference(context).getString(Variables.U_ID, "").equalsIgnoreCase(receiver_id)) {


                if (type.equals("live")) {
                    Intent goingIntent = new Intent(context, LiveUsersActivity.class);
                    goingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(goingIntent);
                }

                else if (type.equals("follow")) {
                    if (Functions.checkProfileOpenValidation(sender_id)) {
                        Intent goingIntent = new Intent(context, ProfileActivity.class);
                        goingIntent.putExtra("user_id", sender_id);
                        goingIntent.putExtra("user_name", title.replace(" started following you", ""));
                        goingIntent.putExtra("user_pic", image);
                        goingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(goingIntent);
                    }
                }

                else if (type.equals("video_new_post")) {
                    Intent goingIntent = new Intent(context, WatchVideosActivity.class);
                    goingIntent.putExtra("video_id", video_id);
                    goingIntent.putExtra("position", 0);
                    goingIntent.putExtra("pageCount", 0);
                    goingIntent.putExtra("userId", receiver_id);
                    goingIntent.putExtra("whereFrom", "IdVideo");
                    goingIntent.putExtra("video_comment", false);
                    goingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(goingIntent);
                }

                else if (type.equals("message")) {
                    Intent goingIntent = new Intent(context, ChatActivity.class);
                    goingIntent.putExtra("user_id", user_id);
                    goingIntent.putExtra("user_name", title);
                    goingIntent.putExtra("user_pic", image);
                    goingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(goingIntent);
                }

                else if (type.equals("comment") || type.equals("comment_like")) {
                    Intent goingIntent = new Intent(context, WatchVideosActivity.class);
                    goingIntent.putExtra("video_id", video_id);
                    goingIntent.putExtra("position", 0);
                    goingIntent.putExtra("pageCount", 0);
                    goingIntent.putExtra("userId", receiver_id);
                    goingIntent.putExtra("whereFrom", "IdVideo");
                    goingIntent.putExtra("video_comment", true);
                    goingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(goingIntent);
                }

                else if (type.equals("video_like")) {
                    Intent goingIntent = new Intent(context, WatchVideosActivity.class);
                    goingIntent.putExtra("video_id", video_id);
                    goingIntent.putExtra("position", 0);
                    goingIntent.putExtra("pageCount", 0);
                    goingIntent.putExtra("userId", receiver_id);
                    goingIntent.putExtra("whereFrom", "IdVideo");
                    goingIntent.putExtra("video_comment", false);
                    goingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(goingIntent);
                }

                else if(type.equals("order_update")){

                    Intent goingIntent = new Intent(context, WebviewActivity.class);
                    goingIntent.putExtra("url", tracking_link);
                    goingIntent.putExtra("title", order_id);
                    goingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(goingIntent);

                }

                else {
                    Intent goingIntent = new Intent(context, MainMenuActivity.class);
                    goingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(goingIntent);
                }

            }


        } catch (Exception e) {
            Log.d(Constants.tag, "Exception: Notification Handler: " + e);
        }
    }


}

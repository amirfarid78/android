package com.coheser.app.simpleclasses;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.coheser.app.R;

public class TimerDialog extends AlertDialog {
    private TextView timerTextView;
    private CountDownTimer countDownTimer;
    private TimerCallback timerCallback;

    public TimerDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.item_timer_dialog);
        timerTextView = findViewById(R.id.timerTextView);


        // Create a countdown timer for 3 seconds
        countDownTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the textview with the remaining seconds
                timerTextView.setText(String.valueOf((millisUntilFinished + 1000) / 1000));
            }

            @Override
            public void onFinish() {
                // Call the callback if it's not null
                if (timerCallback != null) {
                    timerCallback.onTimerFinished();
                }
                dismiss();
            }
        };


        // Start the countdown timer
        countDownTimer.start();

    }

    public void setTimerCallback(TimerCallback callback) {
        this.timerCallback = callback;
    }

    public interface TimerCallback {
        void onTimerFinished();
    }
}
package com.coheser.app.simpleclasses;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RadioGroup;

public class ToggleableRadioButton extends androidx.appcompat.widget.AppCompatRadioButton {


public ToggleableRadioButton(Context context) {
        super(context);
        }

public ToggleableRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        }

public ToggleableRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        }

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public ToggleableRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context);
        }

@Override
public void toggle() {
        if(isChecked()) {
        if(getParent() instanceof RadioGroup) {
        ((RadioGroup)getParent()).clearCheck();
        }
        } else {
        setChecked(true);
        }
        }
        }
package com.coheser.app.interfaces;

import android.content.pm.ResolveInfo;

public interface ShareIntentCallback {
    void onResponse(ResolveInfo resolveInfo);
}

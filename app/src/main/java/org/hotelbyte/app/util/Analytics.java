package org.hotelbyte.app.util;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by lexfaraday
 * Helper class to log analytic events
 */
public final class Analytics {
    public static void logEvent(FirebaseAnalytics mFirebaseAnalytics, String event, String id, String name, String contentType) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
        mFirebaseAnalytics.logEvent(event, bundle);
    }
}

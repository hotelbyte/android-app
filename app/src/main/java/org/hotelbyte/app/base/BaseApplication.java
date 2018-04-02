package org.hotelbyte.app.base;

import android.support.multidex.MultiDexApplication;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;


public class BaseApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

    }
}

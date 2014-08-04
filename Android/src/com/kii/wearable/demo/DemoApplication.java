package com.kii.wearable.demo;

import android.app.Application;

import com.kii.cloud.analytics.KiiAnalytics;
import com.kii.cloud.storage.Kii;

/**
 * Created by tian on 14-6-24.
 */
public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Kii.initialize(Constants.APP_ID, Constants.APP_KEY, Kii.Site.CN);
        KiiAnalytics.initialize(this, Constants.APP_ID, Constants.APP_KEY, KiiAnalytics.Site.CN);
    }

}

package com.sendbird.main;


import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.sendbird.android.SendBird;
import com.sendbird.fcm.MyFirebaseMessagingService;
import com.sendbird.utils.PreferenceUtils;
import com.sendbird.utils.PushUtils;

public class BaseApplication extends MultiDexApplication {

//    public static final String APP_ID = "9DA1B1F4-0BE6-4DA8-82C5-2E81DAB56F23"; // US-1 Demo
//    private static final String APP_ID = "58BB27E6-E4EA-4448-83F5-8E03A873AD6A"; // US-1 Demo
    public static final String APP_ID = "A1D36AE4-1FFF-4472-B2DA-AA237598ECB1";
    public static final String MASTER_TOKEN = "d7e6ec94571a0866270703273be79dad19b2713e";
    public static final String VERSION = "3.0.40";

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);

        PreferenceUtils.init(getApplicationContext());

        SendBird.init(APP_ID, getApplicationContext());
    }
}

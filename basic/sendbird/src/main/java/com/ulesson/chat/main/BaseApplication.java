package com.ulesson.chat.main;


import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.sendbird.android.SendBird;
import com.ulesson.chat.utils.PreferenceUtils;

public class BaseApplication extends MultiDexApplication {

    //    public static final String APP_ID = "9DA1B1F4-0BE6-4DA8-82C5-2E81DAB56F23"; // US-1 Demo
//    private static final String APP_ID = "58BB27E6-E4EA-4448-83F5-8E03A873AD6A"; // US-1 Demo
    public static final String APP_ID = "74FBB89C-C9B6-4E15-99E6-1AF8E67CEFFC";
    public static final String MASTER_TOKEN = "2b6e9d6693e2e402ee84f5306206dc0b638bb6dc";
    public static final String VERSION = "3.0.40";
    public static final String PACKAGE_NAME = "com.ulesson.debug";

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);

        PreferenceUtils.init(getApplicationContext(), MASTER_TOKEN, PACKAGE_NAME, APP_ID);

        SendBird.init(APP_ID, getApplicationContext());
    }
}

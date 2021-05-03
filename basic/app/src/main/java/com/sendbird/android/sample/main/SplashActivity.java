package com.sendbird.android.sample.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.groupchannel.GroupChannelActivity;
import com.sendbird.android.sample.utils.PreferenceUtils;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getIntent() != null && (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            getIntent().removeExtra("groupChannelUrl");
        }

        String userId = PreferenceUtils.getUserId();
        if (ConnectionManager.isLogin() && !TextUtils.isEmpty(userId)) {
//            ConnectionManager.login(userId, new SendBird.ConnectHandler() {
//                @Override
//                public void onConnected(User user, SendBirdException e) {
////                    startActivity(getNextIntent());
//                    getNextIntent();
//                }
//            });
        } else {
           getNextIntent();

//            finish();
        }
    }

    private void getNextIntent() {
        Intent intent = new Intent(SplashActivity.this, GroupChannelActivity.class);
        if (getIntent().hasExtra("groupChannelUrl")) {
            intent.putExtra("groupChannelUrl", getIntent().getStringExtra("groupChannelUrl"));
        }
        startActivity(intent);
    }
}

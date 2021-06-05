package com.ulesson.chat.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.ulesson.chat.groupchannel.GroupChannelActivity;
import com.ulesson.chat.utils.PreferenceUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null && (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) {
            getIntent().removeExtra("groupChannelUrl");
        }

        String userId = PreferenceUtils.getUserId();
        if (ConnectionManager.isLogin() && !TextUtils.isEmpty(userId)) {

        } else {
            getNextIntent();

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

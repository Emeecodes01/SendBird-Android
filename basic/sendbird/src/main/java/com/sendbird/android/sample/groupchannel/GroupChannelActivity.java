package com.sendbird.android.sample.groupchannel;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;

import com.sendbird.android.sample.R;
import com.sendbird.android.sample.main.sendBird.Chat;
import com.sendbird.android.sample.main.sendBird.User;
import com.sendbird.android.sample.main.sendBird.UserData;
import com.sendbird.android.sample.network.createUser.ConnectUserRequest;

import kotlin.Unit;


public class GroupChannelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_channel);

        UserData hostUserData = new UserData("1827", "Taiwo Adebayo", "e5e5464ced91b325eee18fe842bffa43384a270a");
        UserData otherUserData = new UserData("1347", "Tamilore Oyola", "b1a2a46618d338605ef5589fc17a9b2042fd06df");
        ConnectUserRequest connectUserData = new ConnectUserRequest("1827", "Taiwo Adebayo", "https://ulesson-staging.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png",
                true);

//        new Chat().showChatList(this, R.id.container_group_channel, hostUserData);

        new User().connectUser(connectUserData, "6890058c524ad5198c661fee70aa8dd4c324d69b", (userResponse) -> {
            new Chat().showChatList(this, R.id.container_group_channel, new UserData(userResponse.getUser_id(), userResponse.getNickname(), userResponse.getAccess_token()));

//            new Chat().createChat(this, otherUserData, hostUserData);
            return Unit.INSTANCE;
        }, (errorData) -> {
            Log.d("okh", errorData.getMessage() + "message");
            return Unit.INSTANCE;
        }, (updateAccessToken) -> {
            Log.d("okh", updateAccessToken + "update token");
            return Unit.INSTANCE;
        });

        String channelUrl = getIntent().getStringExtra("groupChannelUrl");
        if (channelUrl != null) {
            // If started from notification
            Fragment fragment = GroupChatFragment.newInstance(channelUrl);
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.container_group_channel, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    interface onBackPressedListener {
        boolean onBack();
    }

    private onBackPressedListener mOnBackPressedListener;

    public void setOnBackPressedListener(onBackPressedListener listener) {
        mOnBackPressedListener = listener;
    }

    @Override
    public void onBackPressed() {
        if (mOnBackPressedListener != null && mOnBackPressedListener.onBack()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

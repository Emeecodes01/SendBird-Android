package com.sendbird.android.sample.groupchannel;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.main.ConnectionManager;
import com.sendbird.android.sample.main.sendBird.Chat;
import com.sendbird.android.sample.main.sendBird.Connect;
import com.sendbird.android.sample.main.sendBird.UserData;
import com.sendbird.android.sample.network.createUser.CreateUserRequest;

import kotlin.Unit;


public class GroupChannelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_channel);

//        new SendBirdChat().createChat( this, new SendBirdChat.UserData("taiwo", "taiwo", "7a6b647f303fa9663609c4c56296ed9ad152a70f"), new SendBirdChat.UserData("448178", "448178", ""));
//        new SendBirdChat().showChatList(this, new SendBirdChat.UserData("taiwo", "taiwo", "7a6b647f303fa9663609c4c56296ed9ad152a70f"));

//        CreateUserRequest user = new CreateUserRequest("newuser", "New User", "https://pbs.twimg.com/profile_images/1388521890294161415/SVbNtY_T_400x400.jpg", true);
//
//        new User().createUser(user,  (CreateUserRequest ) -> {
//
//            String accessToken = CreateUserRequest.getAccess_token();
//
//            Log.d("okh", accessToken);
//
//            return Unit.INSTANCE;
//        });

//        new Chat().showChatList(this, new Chat.UserData("newuser", "newuser","1bb9a80704d7f7f3574f8597711834572777f326"));

//        new Chat().createChat(this, new Chat.UserData("newuser", "newuser", "1bb9a80704d7f7f3574f8597711834572777f326"), new Chat.UserData("448178", "448178", "f3238e3bef7e7a627076c7eff8b7c0f1df826328"));

//        UserData userData = new UserData("newuser", "newuser", "1bb9a80704d7f7f3574f8597711834572777f326");

//        new Connect().logout();

//        new Connect().login(userData, new SendBird.ConnectHandler() {
//            @Override
//            public void onConnected(User user, SendBirdException e) {
//
//                Log.d("okh", user.getUserId());
//            }
//        });

        UserData hostUserData = new UserData("1827", "Taiwo Adebayo", "567053530d8d9daec7d59379f6760e60bb2a2155");
//        UserData otherUserData = new UserData("1829", "Ayodeji Okikiolu", "ad8008a525b64f1f505c8fe62773e223a70840bc");

//        new Chat().createChat(this, hostUserData, otherUserData);
        new Chat().showChatList(this, R.id.container_group_channel, hostUserData);

        String channelUrl = getIntent().getStringExtra("groupChannelUrl");
        if (channelUrl != null) {
            // If started from notification
            Fragment fragment = GroupChatFragment.newInstance(channelUrl, false);
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

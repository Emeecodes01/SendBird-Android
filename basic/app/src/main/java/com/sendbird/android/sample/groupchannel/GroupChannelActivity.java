package com.sendbird.android.sample.groupchannel;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;

import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.main.SendBirdChat;


public class GroupChannelActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_channel);

//        new SendBirdChat().createChat( this, new SendBirdChat.UserData("taiwo", "taiwo", "7a6b647f303fa9663609c4c56296ed9ad152a70f"), new SendBirdChat.UserData("448178", "448178", ""));
        new SendBirdChat().showChatList(this, new SendBirdChat.UserData("taiwo", "taiwo", "7a6b647f303fa9663609c4c56296ed9ad152a70f"));
//
//        new SendBirdChat().start("448178", "448178", new SendBird.ConnectHandler() {
//           @Override
//           public void onConnected(User user, SendBirdException e) {
//
//               Fragment fragment = GroupChannelListFragment.newInstance();
//
//               FragmentManager manager = getSupportFragmentManager();
//               manager.popBackStack();
//
//               manager.beginTransaction()
//                       .replace(R.id.container_group_channel, fragment)
//                       .commit();
//
//           }
//       });


        String channelUrl = getIntent().getStringExtra("groupChannelUrl");
        if(channelUrl != null) {
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

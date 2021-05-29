package com.sendbird.groupchannel;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sendbird.R;
import com.sendbird.android.Member;
import com.sendbird.main.sendBird.Chat;
import com.sendbird.main.sendBird.ChatActions;
import com.sendbird.main.sendBird.TutorActions;
import com.sendbird.main.sendBird.User;
import com.sendbird.main.model.UserData;
import com.sendbird.network.userModel.ConnectUserRequest;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;


public class GroupChannelActivity extends AppCompatActivity {

    private onBackPressedListener mOnBackPressedListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_channel);

        ConnectUserRequest connectUserRequest = new ConnectUserRequest("1827", "Taiwo Adebayo", "https://ulesson-staging.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png", true);
//        ConnectUserRequest tutorUserData = new ConnectUserRequest("1347", "Tamilore Oyola", "https://ulesson-staging.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png",
//                true);

        UserData hostUserData = new UserData("1827", "Taiwo Adebayo", "44edac0c469c513c7fe56676b3df93445e0c06");
        UserData tutorUserData = new UserData("7", "Wapnen Gowok", "");

        HashMap<String, Object> questionMap =  new HashMap<String, Object>();
        questionMap.put("questionId", "123");
        questionMap.put("subject", "11");
        questionMap.put("tutorId", "12");
        questionMap.put("questionText", "pp What is computer");
        questionMap.put("questionUrl", "https://ulesson-staging.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png");
        questionMap.put("subjectName", "Maths");
        questionMap.put("subjectAvatar", "https://ulesson-staging.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png");

        new User().connectUser(connectUserRequest, "44edac0c469c513c7fe56676b3df93445e0c06", (userResponse) -> {

            Log.d("okh", userResponse.getAccess_token()+" new token");
            new Chat().createChat(this, hostUserData, tutorUserData, questionMap, (channelUrl) -> {
                Log.d("okh", channelUrl + "channelUrl");
                return Unit.INSTANCE;
            }, (updatedToken) -> {
                Log.d("okh", updatedToken + "updatedToken");
                return Unit.INSTANCE;
            });

            return Unit.INSTANCE;
        }, (errorData) -> {
            Log.d("okh", errorData.getMessage() + "message");

            return Unit.INSTANCE;
        }, (updateAccessToken) -> {

            return Unit.INSTANCE;
        });

        String channelUrl = getIntent().getStringExtra("groupChannelUrl");
        if (channelUrl != null) {
            // If started from notification
            Fragment fragment = GroupChatFragment.newInstance(channelUrl, "", new TutorActions() {
                @Override
                public void showTutorProfile(@NotNull List<? extends Member> members) {

                }

                @Override
                public void showTutorRating(Map<String, Object> questionMap) {
                }

            });

            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.container_group_channel, fragment)
                    .addToBackStack(null)
                    .commit();
        }
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

    interface onBackPressedListener {
        boolean onBack();
    }

}

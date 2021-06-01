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
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.main.sendBird.Chat;
import com.sendbird.main.sendBird.ChatActions;
import com.sendbird.main.sendBird.TutorActions;
import com.sendbird.main.sendBird.User;
import com.sendbird.main.model.UserData;
import com.sendbird.network.userModel.ConnectUserRequest;
import com.sendbird.utils.PreferenceUtils;
import com.sendbird.utils.PushUtils;

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

//        ConnectUserRequest connectUserRequest = new ConnectUserRequest("Tutor-22", "Taiwo", "https://ulesson-staging.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png", true);
        ConnectUserRequest connectUserRequest = new ConnectUserRequest("Tutor-30", "Emmanuel Ozibo", "https://ulesson-staging.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png",
                true);

//        UserData hostUserData = new UserData("Tutor-30", "Emmanuel Ozibo", "8f67d4764b9c868f66c22ce03cfd8e67577e14ef");
        UserData hostUserData = new UserData("Tutor-22", "Taiwo", "5259b789f8bbbbc6e9a7c9068fcfdfaf72f2b1e7");
        UserData tutorUserData = new UserData("Tutor-30", "Emmanuel Ozibo", "67f4a4ae94be607654b854eeb07fd21400d7e947");

        HashMap<String, Object> questionMap =  new HashMap<String, Object>();
        questionMap.put("questionId", "123");
        questionMap.put("subject", "11");
        questionMap.put("tutorId", "12");
        questionMap.put("questionText", "hey");
        questionMap.put("questionUrl", "https://ulesson-staging.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png");
        questionMap.put("subjectName", "Maths");
        questionMap.put("subjectAvatar", "https://ulesson-staging.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png");

        new User().connectUser(connectUserRequest, "5259b789f8bbbbc6e9a7c9068fcfdfaf72f2b1e7", (userResponse) -> {

            PushUtils.registerPushTokenForCurrentUser(new SendBird.RegisterPushTokenWithStatusHandler() {
                @Override
                public void onRegistered(SendBird.PushTokenRegistrationStatus pushTokenRegistrationStatus, SendBirdException e) {
                    if (e != null) {

                        return;
                    }

                    PreferenceUtils.setNotifications(true);
                    PreferenceUtils.setNotificationsShowPreviews(true);
                }
            });

            Log.d("okh", userResponse.getAccess_token()+" new token");
            Log.d("okh",   "channe started");
            new Chat().createChat(this, tutorUserData, hostUserData, questionMap, (channelUrl) -> {
//            new Chat().createChat(this, hostUserData, tutorUserData, questionMap, (channelUrl) -> {
                Log.d("okh", channelUrl + "channelUrl");
                return Unit.INSTANCE;
            }, () -> {

                Log.d("okh", "channe started");
                return Unit.INSTANCE;
            }, new TutorActions() {
                @Override
                public void showTutorProfile(@NotNull List<? extends Member> members) {

                }

                @Override
                public void showTutorRating(@NotNull Map<String, Object> questionMap) {

                }
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
            Fragment fragment = GroupChatFragment.newInstance(channelUrl, false, new TutorActions() {
                @Override
                public void showTutorProfile(@NotNull List<? extends Member> members) {

                }

                @Override
                public void showTutorRating(Map<String, Object> questionMap) {
                }

            }, () -> {

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

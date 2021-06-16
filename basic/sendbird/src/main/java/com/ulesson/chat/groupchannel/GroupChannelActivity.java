package com.ulesson.chat.groupchannel;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.sendbird.android.Member;
import com.ulesson.chat.R;
import com.ulesson.chat.main.model.Question;
import com.ulesson.chat.main.model.UserData;
import com.ulesson.chat.main.sendBird.Chat;
import com.ulesson.chat.main.sendBird.ChatActions;
import com.ulesson.chat.main.sendBird.TutorActions;
import com.ulesson.chat.main.sendBird.User;
import com.ulesson.chat.network.userModel.ConnectUserRequest;
import com.ulesson.chat.utils.PushUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

        ConnectUserRequest connectUserRequest = new ConnectUserRequest("Tutor-22", "Taiwo", "https://ulesson-staging.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png", true);
//        ConnectUserRequest connectUserRequest = new ConnectUserRequest("Tutor-30", "Emmanuel Ozibo", "https://ulesson-staging.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png",
//                true);

//        UserData hostUserData = new UserData("Tutor-30", "Emmanuel Ozibo", "8f67d4764b9c868f66c22ce03cfd8e67577e14ef");
        UserData hostUserData = new UserData("Tutor-22", "Taiwo", "5259b789f8bbbbc6e9a7c9068fcfdfaf72f2b1e7");
        UserData tutorUserData = new UserData("Tutor-30", "Emmanuel Ozibo", "67f4a4ae94be607654b854eeb07fd21400d7e947");

        HashMap<String, Object> questionMap = new HashMap<String, Object>();
        questionMap.put("questionId", "123");
        questionMap.put("subject", "11");
        questionMap.put("tutorId", "12");
        questionMap.put("questionText", "hey");
        questionMap.put("questionUrl", "https://ulesson-staging.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png");
        questionMap.put("subjectName", "Maths");
        questionMap.put("subjectAvatar", "https://ulesson-staging.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png");

        new User().connectUser(connectUserRequest, "5259b789f8bbbbc6e9a7c9068fcfdfaf72f2b1e7", (userResponse) -> {

            PushUtils.registerPushTokenForCurrentUser((pushTokenRegistrationStatus, e) -> {
                if (e != null) {
                    return;
                }

            });

//            new Chat().createChat(this, tutorUserData, hostUserData, questionMap, (channelUrl) -> {

//            new Chat().createChat(this, hostUserData, tutorUserData, true, questionMap, (channelUrl) -> {
//
//                return Unit.INSTANCE;
//            }, () -> Unit.INSTANCE, (question) -> Unit.INSTANCE, new TutorActions() {
//                @Override
//                public void showTutorProfile(@NotNull List<? extends Member> members) {
//
//                }
//
//                @Override
//                public void showTutorRating(@NotNull Map<String, Object> questionMap) {
//
//                }
//            });

            List<Question> questionList = new ArrayList<>();
            questionList.add(new Question(1,
                    "https://ulesson-uat.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png",
                    "What is Mathematics", "What's good",
                    "https://ulesson-uat.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png",
                    "20210609"));
            questionList.add(new Question(2,
                    "https://ulesson-uat.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png",
                    "What is Chemistry", "Another question",
                    "https://ulesson-uat.s3.eu-west-2.amazonaws.com/learners/avatars/defaults/thumb/missing.png",
                    "20210609"));

            new Chat().setPendingQuestions(new Gson().toJson(questionList));

            new Chat().showChatList(this, R.id.container_group_channel, hostUserData, new TutorActions() {

                @Override
                public void showTutorProfile(@NotNull List<? extends Member> members) {

                }

                @Override
                public void showTutorRating(@NotNull Map<String, Object> questionMap) {

                }
            }, new ChatActions() {
                @Override
                public void getPendingQuestions() {
                    questionList.clear();
                    new Chat().setPendingQuestions(new Gson().toJson(questionList));
                }

                @Override
                public void chatReceived() {

                }

                @Override
                public void showDummyChat(@NotNull Question question) {
                    Log.d("okh", question.toString() + " question");
                }
            });

            return Unit.INSTANCE;
        }, (errorData) -> Unit.INSTANCE, (updateAccessToken) -> Unit.INSTANCE);

        String channelUrl = getIntent().getStringExtra("groupChannelUrl");
        if (channelUrl != null) {
            // If started from notification
            Fragment fragment = GroupChatFragment.newInstance(channelUrl, false, false, new TutorActions() {
                @Override
                public void showTutorProfile(@NotNull List<? extends Member> members) {

                }

                @Override
                public void showTutorRating(Map<String, Object> questionMap) {
                }

            }, new ChatActions() {
                @Override
                public void getPendingQuestions() {

                }

                @Override
                public void chatReceived() {

                }

                @Override
                public void showDummyChat(@NotNull Question question) {

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
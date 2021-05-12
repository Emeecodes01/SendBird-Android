package com.sendbird.groupchannel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.sendbird.R;
import com.sendbird.utils.GenericDialog;
import com.sendbird.utils.TimerUtils;

import kotlin.Unit;

import static com.sendbird.utils.TextUtils.THEME_MATH;

public class DummyChatFragment extends Fragment {

    GenericDialog joinChatDialog =
            new GenericDialog().newInstance(THEME_MATH);
    private Toolbar dummy_toolbar;
    private ConstraintLayout layout_group_chat_root;
    private TextView dummy_message_text;
    private ProgressBar progressBar;
    private EditText edittext_group_chat_message;
    private ImageButton button_voice;
    private Button button_record_voice;

    public static DummyChatFragment newInstance(@NonNull String channelUrl) {
        DummyChatFragment fragment = new DummyChatFragment();

        Bundle args = new Bundle();
        args.putString(GroupChannelListFragment.EXTRA_GROUP_CHANNEL_URL, channelUrl);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get question text and uri

        new TimerUtils().timer(5, (l) -> Unit.INSTANCE, () -> {
            joinChat();
            return Unit.INSTANCE;
        });

        checkTutorStatus(0);

    }

    private void joinChat() {
        joinChatDialog.show(getActivity().getSupportFragmentManager(), "");
        progressBar.setVisibility(View.GONE);
    }

    private void checkTutorStatus(int tutorStatus) {

        if (tutorStatus == 0) {

            joinChatDialog.setTitle(getString(R.string.tutor_is_ready))
                    .setMessage(getString(R.string.join_chat));

            joinChatDialog.setPositiveButton(R.string.join, R.drawable.bg_btn_complete, () -> {
                joinChatDialog.dismiss();
                return Unit.INSTANCE;
            });

        } else if (tutorStatus == 1) {

            joinChatDialog.setTitle(getString(R.string.no_tutor))
                    .setMessage(getString(R.string.retry_for_tutor));


        } else if (tutorStatus == 2) {

            joinChatDialog.setTitle(getString(R.string.connection_error))
                    .setMessage(getString(R.string.error_connecting));

        } else {
            Toast.makeText(requireContext(), "Something went wrong, please retry", Toast.LENGTH_LONG).show();
        }

        if (tutorStatus == 1 || tutorStatus == 2) {

            joinChatDialog.setPositiveButton(R.string.retry, R.drawable.ic_continue_enabled, () -> {
                joinChatDialog.dismiss();
                return Unit.INSTANCE;
            });

            joinChatDialog.setNegativeButton(R.string.back, null, () -> {
                joinChatDialog.dismiss();
                return Unit.INSTANCE;
            });

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dummy_group_chat, container, false);

        dummy_toolbar = rootView.findViewById(R.id.dummy_toolbar);
        edittext_group_chat_message = rootView.findViewById(R.id.edittext_group_chat_message);
        button_voice = rootView.findViewById(R.id.button_voice);
        button_record_voice = rootView.findViewById(R.id.button_record_voice);
        layout_group_chat_root = rootView.findViewById(R.id.layout_group_chat_root);
        dummy_message_text = rootView.findViewById(R.id.dummy_message_text);
        progressBar = rootView.findViewById(R.id.progressBar);

        dummy_message_text.setText(R.string.sample_question);

        dummy_toolbar.setNavigationOnClickListener(view -> {

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }

        });

        layout_group_chat_root.setOnClickListener(view -> joinChatDialog.dismiss());

        edittext_group_chat_message.setEnabled(false);
        button_record_voice.setEnabled(false);
        button_voice.setEnabled(false);

        return rootView;
    }

}

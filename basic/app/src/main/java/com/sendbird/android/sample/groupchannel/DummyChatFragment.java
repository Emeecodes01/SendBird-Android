package com.sendbird.android.sample.groupchannel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.utils.GenericDialog;
import static com.sendbird.android.sample.utils.TextUtils.THEME_MATH;


public class DummyChatFragment extends Fragment {

//    private GenericDialog joinChatDialog =
//            new GenericDialog().newInstance(THEME_MATH)
//                    .setTitle(getString(R.string.tutor_is_ready))
//                    .setMessage(getString(R.string.join_chat));

    private Toolbar dummy_toolbar;
    private TextView dummy_message_text;

    /**
     * To create an instance of this fragment, a Channel URL should be required.
     */
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

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dummy_group_chat, container, false);

        dummy_toolbar = rootView.findViewById(R.id.dummy_toolbar);
        dummy_message_text = rootView.findViewById(R.id.dummy_message_text);

        dummy_message_text.setText(R.string.sample_question);

        dummy_toolbar.setNavigationOnClickListener(view -> {
            getActivity().getSupportFragmentManager().popBackStack();
        });

        return rootView;
    }

}

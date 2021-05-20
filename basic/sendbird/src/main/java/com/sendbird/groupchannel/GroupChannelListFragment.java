package com.sendbird.groupchannel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sendbird.R;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.main.BaseFragment;
import com.sendbird.main.ConnectionManager;
import com.sendbird.main.sendBird.Chat;
import com.sendbird.main.sendBird.TutorActions;
import com.sendbird.main.model.UserData;

import java.util.List;

public class GroupChannelListFragment extends BaseFragment {

    public static final String EXTRA_GROUP_CHANNEL_URL = "GROUP_CHANNEL_URL";
    public static final String IS_ACTIVE = "IS_ACTIVE";
    public static final String HOST_USER_DATA = "HOST_USER_DATA";

    private static final int CHANNEL_LIST_LIMIT = 15;
    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHANNEL_LIST";
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_LIST";

    private RecyclerView mRecyclerView;
    private Button seeAllBtn;
    private CardView noChatCardView;
    private LinearLayoutManager mLayoutManager;
    private GroupChannelListAdapter mChannelListAdapter;
    private GroupChannelListQuery mChannelListQuery;
    private SwipeRefreshLayout mSwipeRefresh;
    private UserData hostUserData;
    public static TutorActions tutorActionsChannel;

    public static GroupChannelListFragment newInstance(@NonNull Boolean isActive, UserData hostUserData, TutorActions dothis) {
        GroupChannelListFragment fragment = new GroupChannelListFragment();
        Bundle args = new Bundle();
        tutorActionsChannel = dothis;
        args.putParcelable(GroupChannelListFragment.HOST_USER_DATA, hostUserData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            hostUserData = getArguments().getParcelable(GroupChannelListFragment.HOST_USER_DATA);
        }

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_group_channel_list, container, false);

        setRetainInstance(true);

        mRecyclerView = rootView.findViewById(R.id.recycler_group_channel_list);
        seeAllBtn = rootView.findViewById(R.id.seeAllBtn);
        noChatCardView = rootView.findViewById(R.id.nochatCardView);
        mSwipeRefresh = rootView.findViewById(R.id.swipe_layout_group_channel_list);

        mSwipeRefresh.setOnRefreshListener(() -> {
            mSwipeRefresh.setRefreshing(true);
            refresh();
        });

        seeAllBtn.setOnClickListener(view -> new Chat().showAllChat(getActivity(), android.R.id.content, hostUserData));

        mChannelListAdapter = new GroupChannelListAdapter(getActivity());

        setUpRecyclerView();
        setUpChannelListAdapter();

        refresh();

        return rootView;
    }

    @Override
    public void onResume() {

        ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID, reconnect -> refresh());

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
            }

            @Override
            public void onChannelChanged(BaseChannel channel) {
                mChannelListAdapter.clearMap();
                mChannelListAdapter.updateOrInsert(channel);
            }

            @Override
            public void onTypingStatusUpdated(GroupChannel channel) {
                mChannelListAdapter.notifyDataSetChanged();
            }
        });

        super.onResume();
    }

    @Override
    public void onPause() {
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        super.onPause();
    }

    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mChannelListAdapter);

        // If user scrolls to bottom of the list, loads more channels.
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (mLayoutManager.findLastVisibleItemPosition() == mChannelListAdapter.getItemCount() - 1) {
                    loadNextChannelList();
                }
            }
        });
    }

    private void setUpChannelListAdapter() {
        mChannelListAdapter.setOnItemClickListener(channel -> enterGroupChannel(channel));
    }

    void enterGroupChannel(GroupChannel channel) {
        final String channelUrl = channel.getUrl();

        enterGroupChannel(channelUrl);
    }

    void enterGroupChannel(String channelUrl) {

        GroupChatFragment fragment = GroupChatFragment.newInstance(channelUrl, new TutorActions() {

            @Override
            public void showTutorRating() {
                tutorActionsChannel.showTutorRating();
            }

            @Override
            public void showTutorProfile(List<? extends Member> members) {
                tutorActionsChannel.showTutorProfile(members);
            }
        });

        if (getActivity() != null && !fragment.isAdded()) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fragment)
                    .addToBackStack(fragment.getTag())
                    .commit();
        }

    }

    private void refresh() {
        refreshChannelList();
    }

    private void refreshChannelList() {
        mChannelListQuery = GroupChannel.createMyGroupChannelListQuery();
        mChannelListQuery.setLimit(GroupChannelListFragment.CHANNEL_LIST_LIMIT);

        mChannelListQuery.next((list, e) -> {
            if (e != null) {
                // Error!
                e.printStackTrace();
//                    return;
            }

            if (list != null) {

                mChannelListAdapter.clearMap();
                mChannelListAdapter.setGroupChannelList(list);

                if (list.isEmpty()) {
                    mRecyclerView.setVisibility(View.GONE);
                    noChatCardView.setVisibility(View.VISIBLE);
                } else {
                    noChatCardView.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            }

        });

        if (mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
    }

    private void loadNextChannelList() {

        try {
            if (mChannelListQuery != null) {
                mChannelListQuery.next((list, e) -> {
                    if (e != null) {
                        // Error!
                        e.printStackTrace();
                        return;
                    }

                    for (GroupChannel channel : list) {
                        mChannelListAdapter.addLast(channel);
                    }
                });
            }
        } catch (Exception e) {

        }

    }
}

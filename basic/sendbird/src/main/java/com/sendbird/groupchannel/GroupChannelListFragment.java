package com.sendbird.groupchannel;

import android.os.Bundle;
import android.util.Log;
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
import com.sendbird.android.SendBirdException;
import com.sendbird.main.BaseFragment;
import com.sendbird.main.ConnectionManager;
import com.sendbird.main.sendBird.Chat;
import com.sendbird.main.sendBird.TutorActions;
import com.sendbird.main.model.UserData;
import com.sendbird.syncmanager.ChannelCollection;
import com.sendbird.syncmanager.ChannelEventAction;
import com.sendbird.syncmanager.handler.ChannelCollectionHandler;
import com.sendbird.syncmanager.handler.CompletionHandler;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class GroupChannelListFragment extends BaseFragment {

    public static final String EXTRA_GROUP_CHANNEL_URL = "GROUP_CHANNEL_URL";
    public static final String HOST_USER_DATA = "HOST_USER_DATA";

    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHANNEL_LIST";
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_LIST";

    private RecyclerView mRecyclerView;
    private CardView noChatCard;
    private LinearLayoutManager mLayoutManager;
    private GroupChannelListAdapter mChannelListAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    private UserData hostUserData;
    public static TutorActions tutorActionsChannel;
    private ChannelCollection mChannelCollection;

    public static GroupChannelListFragment newInstance(UserData hostUserData, TutorActions doThis) {
        GroupChannelListFragment fragment = new GroupChannelListFragment();
        Bundle args = new Bundle();
        tutorActionsChannel = doThis;
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
        noChatCard = rootView.findViewById(R.id.nochatCardView);
        Button seeAllBtn = rootView.findViewById(R.id.seeAllBtn);
        mSwipeRefresh = rootView.findViewById(R.id.swipe_layout_group_channel_list);

        mSwipeRefresh.setOnRefreshListener(() -> {
            mSwipeRefresh.setRefreshing(true);
            refresh();
        });

        seeAllBtn.setOnClickListener(view -> new Chat().showAllChat(getActivity(), android.R.id.content, hostUserData));

        mChannelListAdapter = new GroupChannelListAdapter(getActivity());

        setUpRecyclerView();

        setUpChannelListAdapter();

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

    @Override
    public void onDestroy() {
        if (mChannelCollection != null) {
            mChannelCollection.setCollectionHandler(null);
            mChannelCollection.remove();
        }
        super.onDestroy();
    }

    private void setUpRecyclerView() {
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mChannelListAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (mLayoutManager.findLastVisibleItemPosition() == mChannelListAdapter.getItemCount() - 1) {
                        if (mChannelCollection != null) {
                            mChannelCollection.fetch(new CompletionHandler() {
                                @Override
                                public void onCompleted(SendBirdException e) {
                                    if (mSwipeRefresh.isRefreshing()) {
                                        mSwipeRefresh.setRefreshing(false);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

    }

    private void setUpChannelListAdapter() {
        mChannelListAdapter.setOnItemClickListener(this::enterGroupChannel);
    }

    void enterGroupChannel(GroupChannel channel) {
        final String channelUrl = channel.getUrl();

        enterGroupChannel(channelUrl);
    }

    void enterGroupChannel(String channelUrl) {

        GroupChatFragment fragment = GroupChatFragment.newInstance(channelUrl, "", new TutorActions() {

            @Override
            public void showTutorRating(@NotNull Map<String, Object> questionMap) {
                tutorActionsChannel.showTutorRating(questionMap);
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
        if (mChannelCollection != null) {
            mChannelCollection.remove();
        }

        mChannelListAdapter.clearMap();
        mChannelListAdapter.clearChannelList();
        GroupChannelListQuery query = GroupChannel.createMyGroupChannelListQuery();
        mChannelCollection = new ChannelCollection(query);
        mChannelCollection.setCollectionHandler(mChannelCollectionHandler);
        mChannelCollection.fetch(e -> {
            if (mSwipeRefresh.isRefreshing()) {
                mSwipeRefresh.setRefreshing(false);
            }

        });

    }

    ChannelCollectionHandler mChannelCollectionHandler = new ChannelCollectionHandler() {
        @Override
        public void onChannelEvent(final ChannelCollection channelCollection, final List<GroupChannel> list, final ChannelEventAction channelEventAction) {
            if (getActivity() == null) {
                return;
            }

            getActivity().runOnUiThread(() -> {
                if (mSwipeRefresh.isRefreshing()) {
                    mSwipeRefresh.setRefreshing(false);
                }

                if (list.isEmpty()){
                    noChatCard.setVisibility(View.VISIBLE);
                }

                switch (channelEventAction) {
                    case INSERT:
                        mChannelListAdapter.clearMap();
                        mChannelListAdapter.insertChannels(list, channelCollection.getQuery().getOrder());
                        break;

                    case UPDATE:
                        mChannelListAdapter.clearMap();
                        mChannelListAdapter.updateChannels(list);
                        break;

                    case MOVE:
                        mChannelListAdapter.clearMap();
                        mChannelListAdapter.moveChannels(list, channelCollection.getQuery().getOrder());
                        break;

                    case REMOVE:
                        mChannelListAdapter.clearMap();
                        mChannelListAdapter.removeChannels(list);
                        break;

                    case CLEAR:
                        mChannelListAdapter.clearMap();
                        mChannelListAdapter.clearChannelList();
                        break;
                }
            });
        }
    };

}

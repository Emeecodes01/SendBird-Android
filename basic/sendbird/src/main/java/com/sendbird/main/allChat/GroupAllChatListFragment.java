package com.sendbird.main.allChat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sendbird.R;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.groupchannel.GroupChatFragment;
import com.sendbird.main.ConnectionManager;

import java.util.ArrayList;
import java.util.List;

public class GroupAllChatListFragment extends Fragment {

    public static final String IS_ACTIVE = "IS_ACTIVE";
    private static final int CHANNEL_LIST_LIMIT = 15;
    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHANNEL_LIST";
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_LIST";

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private GroupAllChatListAdapter mChannelListAdapter;
    private GroupChannelListQuery mChannelListQuery;
    private SwipeRefreshLayout mSwipeRefresh;
    private Boolean isActive;

    public static GroupAllChatListFragment newInstance(@NonNull Boolean isActive) {
        GroupAllChatListFragment fragment = new GroupAllChatListFragment();

        Bundle args = new Bundle();
        args.putBoolean(GroupAllChatListFragment.IS_ACTIVE, isActive);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_all_chat_channel_list, container, false);

        setRetainInstance(true);

        mRecyclerView = rootView.findViewById(R.id.recycler_group_all_chat_list);
        mSwipeRefresh = rootView.findViewById(R.id.swipe_layout_group_channel_list);

        mSwipeRefresh.setOnRefreshListener(() -> {
            mSwipeRefresh.setRefreshing(true);
            refresh();
        });

        if (getArguments() != null) {
            isActive = getArguments().getBoolean(GroupAllChatListFragment.IS_ACTIVE, true);
        }

        mChannelListAdapter = new GroupAllChatListAdapter(getActivity());

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
        GroupChatFragment fragment = GroupChatFragment.newInstance(channelUrl);

        if (getActivity() != null) {
            requireActivity().getSupportFragmentManager().beginTransaction()
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
        mChannelListQuery.setLimit(GroupAllChatListFragment.CHANNEL_LIST_LIMIT);

        mChannelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {

                List<GroupChannel> allChannelList = list;

                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    allChannelList = mChannelListAdapter.load();
//                    return;
                } else {
                    allChannelList = list;
                }

                mChannelListAdapter.clearMap();
                mChannelListAdapter.setAllGroupChannelList(allChannelList);

                List<GroupChannel> isActiveChannel = new ArrayList<>();
                List<GroupChannel> isPastChannel = new ArrayList<>();

                for (int i = 0; i < allChannelList.size(); i++) {

                    if (allChannelList.get(i).getData().equalsIgnoreCase("active")) {
                        isActiveChannel.add(allChannelList.get(i));
                    } else {
                        isPastChannel.add(allChannelList.get(i));
                    }

                    if (isActive) {
                        mChannelListAdapter.setGroupChannelList(isActiveChannel);
                    } else {
                        mChannelListAdapter.setGroupChannelList(isPastChannel);
                    }

                }

                if (allChannelList.isEmpty()) {
                    mRecyclerView.setVisibility(View.GONE);
                } else {
                    mRecyclerView.setVisibility(View.VISIBLE);

                }
            }
        });

        if (mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
    }

    private void loadNextChannelList() {
        mChannelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }

                List<GroupChannel> isActiveChannel = new ArrayList<>();
                List<GroupChannel> isPastChannel = new ArrayList<>();

                for (int i = 0; i < list.size(); i++) {

                    if (list.get(i).getData().equalsIgnoreCase("active")) {
                        isActiveChannel.add(list.get(i));
                    } else {
                        isPastChannel.add(list.get(i));
                    }

                    if (isActive) {
                        mChannelListAdapter.addLast(isActiveChannel);
                    } else {
                        mChannelListAdapter.addLast(isPastChannel);
                    }

                }
            }
        });
    }

}

package com.sendbird.android.sample.groupchannel;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.sample.R;
import com.sendbird.android.sample.main.ConnectionManager;
import com.sendbird.android.sample.main.sendBird.Chat;
import com.sendbird.android.sample.main.sendBird.UserData;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class GroupChannelListFragment extends Fragment {

    public static final String EXTRA_GROUP_CHANNEL_URL = "GROUP_CHANNEL_URL";
    public static final String IS_ACTIVE = "IS_ACTIVE";
    private static final int INTENT_REQUEST_NEW_GROUP_CHANNEL = 302;

    private static final int CHANNEL_LIST_LIMIT = 15;
    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHANNEL_LIST";
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_LIST";

    private RecyclerView mRecyclerView;
    private Button seeAllBtn;
    private CardView nochatCardView;
    private LinearLayoutManager mLayoutManager;
    private GroupChannelListAdapter mChannelListAdapter;
    private GroupChannelListQuery mChannelListQuery;
    private SwipeRefreshLayout mSwipeRefresh;
    private Boolean isActive;

    public static GroupChannelListFragment newInstance(@NonNull Boolean isActive) {
        GroupChannelListFragment fragment = new GroupChannelListFragment();

        Bundle args = new Bundle();
        args.putBoolean(GroupChannelListFragment.IS_ACTIVE, isActive);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_group_channel_list, container, false);

        setRetainInstance(true);

        mRecyclerView = rootView.findViewById(R.id.recycler_group_channel_list);
        seeAllBtn = rootView.findViewById(R.id.seeAllBtn);
        nochatCardView = rootView.findViewById(R.id.nochatCardView);
        mSwipeRefresh = rootView.findViewById(R.id.swipe_layout_group_channel_list);

        mSwipeRefresh.setOnRefreshListener(() -> {
            mSwipeRefresh.setRefreshing(true);
            refresh();
        });

        UserData hostUserData = new UserData("1827", "Taiwo Adebayo", "567053530d8d9daec7d59379f6760e60bb2a2155");

        seeAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Chat().showAllChat(getActivity(), android.R.id.content, hostUserData);
            }
        });

        if (getArguments() != null) {
            isActive = getArguments().getBoolean(GroupChannelListFragment.IS_ACTIVE, true);
        }

        mChannelListAdapter = new GroupChannelListAdapter(getActivity());
        mChannelListAdapter.load();

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
        mChannelListAdapter.save();

//        ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID);
//        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_REQUEST_NEW_GROUP_CHANNEL) {
            if (resultCode == RESULT_OK) {
                // Channel successfully created
                // Enter the newly created channel.
                String newChannelUrl = data.getStringExtra(CreateGroupChannelActivity.EXTRA_NEW_CHANNEL_URL);
                if (newChannelUrl != null) {
                    enterGroupChannel(newChannelUrl);
                }
            } else {
                Log.d("GrChLIST", "resultCode not STATUS_OK");
            }
        }
    }

    // Sets up recycler view
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

    // Sets up channel list adapter
    private void setUpChannelListAdapter() {
        mChannelListAdapter.setOnItemClickListener(channel -> enterGroupChannel(channel));

        mChannelListAdapter.setOnItemLongClickListener(channel -> showChannelOptionsDialog(channel));
    }

    /**
     * Displays a dialog listing channel-specific options.
     */
    private void showChannelOptionsDialog(final GroupChannel channel) {
        String[] options;
        final boolean pushCurrentlyEnabled = channel.isPushEnabled();

        options = pushCurrentlyEnabled
                ? new String[]{"Leave channel", "Turn push notifications OFF"}
                : new String[]{"Leave channel", "Turn push notifications ON"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Channel options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Show a dialog to confirm that the user wants to leave the channel.
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("Leave channel " + channel.getName() + "?")
                                    .setPositiveButton("Leave", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            leaveChannel(channel);
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .create().show();
                        } else if (which == 1) {
                            setChannelPushPreferences(channel, !pushCurrentlyEnabled);
                        }
                    }
                });
        builder.create().show();
    }

    /**
     * Turns push notifications on or off for a selected channel.
     *
     * @param channel The channel for which push preferences should be changed.
     * @param on      Whether to set push notifications on or off.
     */
    private void setChannelPushPreferences(final GroupChannel channel, final boolean on) {
        // Change push preferences.
        channel.setPushPreference(on, new GroupChannel.GroupChannelSetPushPreferenceHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                String toast = on
                        ? "Push notifications have been turned ON"
                        : "Push notifications have been turned OFF";

                Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    /**
     * Enters a Group Channel. Upon entering, a GroupChatFragment will be inflated
     * to display messages within the channel.
     *
     * @param channel The Group Channel to enter.
     */
    void enterGroupChannel(GroupChannel channel) {
        final String channelUrl = channel.getUrl();

        enterGroupChannel(channelUrl);
    }

    /**
     * Enters a Group Channel with a URL.
     *
     * @param channelUrl The URL of the channel to enter.
     */
    void enterGroupChannel(String channelUrl) {
        GroupChatFragment fragment = GroupChatFragment.newInstance(channelUrl, false);
        if (getActivity() !=null && !fragment.isAdded()){
            getActivity().getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fragment)
                    .addToBackStack(fragment.getTag())
                    .commit();
        }

    }

    private void refresh() {
        refreshChannelList(CHANNEL_LIST_LIMIT);
    }

    /**
     * Creates a new query to get the list of the user's Group Channels,
     * then replaces the existing dataset.
     *
     * @param numChannels The number of channels to load.
     */
    private void refreshChannelList(int numChannels) {
        mChannelListQuery = GroupChannel.createMyGroupChannelListQuery();
        mChannelListQuery.setLimit(numChannels);

        mChannelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }

                mChannelListAdapter.clearMap();
                mChannelListAdapter.setGroupChannelList(list);

//                list.clear();

                if (list.isEmpty()) {
                    mRecyclerView.setVisibility(View.GONE);
                    nochatCardView.setVisibility(View.VISIBLE);
                } else {
                    nochatCardView.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);

                }
            }
        });

        if (mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
    }

    /**
     * Loads the next channels from the current query instance.
     */
    private void loadNextChannelList() {
        mChannelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null) {
                    // Error!
                    e.printStackTrace();
                    return;
                }

                for (GroupChannel channel : list) {
                    mChannelListAdapter.addLast(channel);
                }
            }
        });
    }

    /**
     * Leaves a Group Channel.
     *
     * @param channel The channel to leave.
     */
    private void leaveChannel(final GroupChannel channel) {
        channel.leave(new GroupChannel.GroupChannelLeaveHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    // Error!
                    return;
                }

                // Re-query message list
                refresh();
            }
        });
    }
}

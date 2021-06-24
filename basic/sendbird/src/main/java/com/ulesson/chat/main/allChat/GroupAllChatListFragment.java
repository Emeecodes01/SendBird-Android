package com.ulesson.chat.main.allChat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.syncmanager.ChannelCollection;
import com.sendbird.syncmanager.ChannelEventAction;
import com.sendbird.syncmanager.handler.ChannelCollectionHandler;
import com.ulesson.chat.R;
import com.ulesson.chat.groupchannel.GroupChatFragment;
import com.ulesson.chat.main.model.Question;
import com.ulesson.chat.main.sendBird.ChatActions;
import com.ulesson.chat.main.sendBird.Connect;
import com.ulesson.chat.main.sendBird.TutorActions;
import com.ulesson.chat.utils.CustomFontTextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import kotlin.Unit;

public class GroupAllChatListFragment extends Fragment {

    public static final String CHAT_TYPE = "CHAT_TYPE";
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_LIST";
    public static TutorActions tutorActionsChannel;
    public static ChatActions chatActionsChannel;
    private GroupAllChatListAdapter mChannelListAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefresh;
    private CardView noChatCard;
    private boolean groupChannelEmpty = true;
    ChannelCollectionHandler mChannelCollectionHandler = new ChannelCollectionHandler() {
        @Override
        public void onChannelEvent(final ChannelCollection channelCollection, final List<GroupChannel> list, final ChannelEventAction channelEventAction) {
            if (getActivity() != null) {

                groupChannelEmpty = false;

                getActivity().runOnUiThread(() -> {
                    if (mSwipeRefresh.isRefreshing()) {
                        mSwipeRefresh.setRefreshing(false);
                    }

                    switch (channelEventAction) {
                        case INSERT:
                            mChannelListAdapter.clearMap();
                            if (getArguments() != null) {
                                String chatType = getArguments().getString(GroupAllChatListFragment.CHAT_TYPE);
                                List<GroupChannel> groupChannelList = mChannelListAdapter.insertChannels(list, channelCollection.getQuery().getOrder(), chatType);
                                groupChannelEmpty = groupChannelList.isEmpty();
                                setUpChatView(groupChannelEmpty);
                            }
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
        }
    };
    private ChannelCollection mChannelCollection;
    static GroupAllChatListFragment fragment;

    public static GroupAllChatListFragment newInstance(@NonNull ChatType chatType, TutorActions tutorActions, ChatActions chatActions) {
        fragment = new GroupAllChatListFragment();
        tutorActionsChannel = tutorActions;
        chatActionsChannel = chatActions;
        Bundle args = new Bundle();
        args.putString(GroupAllChatListFragment.CHAT_TYPE, chatType.name());
        fragment.setArguments(args);

        return fragment;
    }

    public void updateQuestion() {
        if (mChannelListAdapter != null) {
            mChannelListAdapter.notifyDataSetChanged();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_all_chat_channel_list, container, false);

        mRecyclerView = rootView.findViewById(R.id.recycler_group_all_chat_list);
        noChatCard = rootView.findViewById(R.id.nochatCardView);
        CustomFontTextView noChatTxt = rootView.findViewById(R.id.noChatTxt);
        CustomFontTextView noChatDetailsTxt = rootView.findViewById(R.id.noChatDetailsTxt);
        mSwipeRefresh = rootView.findViewById(R.id.swipe_layout_group_channel_list);

        if (getArguments() != null) {
            String chatType = getArguments().getString(GroupAllChatListFragment.CHAT_TYPE);
            noChatTxt.setText(getString(R.string.chat_status_message, chatType));
            noChatDetailsTxt.setText(getString(R.string.chat_status_detail, chatType.toLowerCase()));
        }

        mSwipeRefresh.setOnRefreshListener(this::refresh);

        mChannelListAdapter = new GroupAllChatListAdapter(requireContext());

        setUpRecyclerView();

        setUpChannelListAdapter();

        refresh();

        return rootView;
    }

    @Override
    public void onResume() {

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
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (mLayoutManager.findLastVisibleItemPosition() == mChannelListAdapter.getItemCount() - 1) {
                        if (mChannelCollection != null) {
                            mChannelCollection.fetch(e -> {
                                if (mSwipeRefresh.isRefreshing()) {
                                    mSwipeRefresh.setRefreshing(false);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void setUpChannelListAdapter() {
        mChannelListAdapter.setOnItemClickListener(this::enterGroupChannel, this::enterDummyChat);
    }

    void enterGroupChannel(GroupChannel channel) {
        final String channelUrl = channel.getUrl();
        enterGroupChannel(channelUrl);
    }

    void enterDummyChat(Question question) {
        chatActionsChannel.showDummyChat(question);
    }

    void enterGroupChannel(String channelUrl) {
        GroupChatFragment fragment = GroupChatFragment.newInstance(channelUrl, false, false, new TutorActions() {
            @Override
            public void showTutorRating(@NotNull Map<String, Object> questionMap) {
                tutorActionsChannel.showTutorRating(questionMap);
            }

            @Override
            public void showTutorProfile(@NotNull List<? extends Member> members) {
                tutorActionsChannel.showTutorProfile(members);
            }
        }, new ChatActions() {
            @Override
            public void getPendingQuestions() {
                chatActionsChannel.getPendingQuestions();
            }

            @Override
            public void chatReceived() {

            }

            @Override
            public void showDummyChat(@NotNull Question question) {
                chatActionsChannel.showDummyChat(question);
            }
        });

        if (getActivity() != null) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fragment)
                    .addToBackStack(fragment.getTag())
                    .commit();
        }

    }

    private void refresh() {

        try {

            if (mChannelCollection != null) {
                mChannelCollection.remove();
            }

            mChannelListAdapter.clearMap();
            mChannelListAdapter.clearChannelList();
            GroupChannelListQuery query = GroupChannel.createMyGroupChannelListQuery();
            mChannelCollection = new ChannelCollection(query);
            mChannelCollection.setCollectionHandler(mChannelCollectionHandler);
            mChannelCollection.fetch(e -> {

                if (getArguments() != null) {
                    String chatType = getArguments().getString(GroupAllChatListFragment.CHAT_TYPE);
                    List<GroupChannel> groupChannelList = mChannelListAdapter.insertChannels(null, mChannelCollection.getQuery().getOrder(), chatType);
                    groupChannelEmpty = groupChannelList.isEmpty();
                }

                setUpChatView(groupChannelEmpty);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (mSwipeRefresh.isRefreshing()) {
                            mSwipeRefresh.setRefreshing(false);
                        }
                        mChannelListAdapter.notifyDataSetChanged();
                    });
                }
                mChannelListAdapter.notifyDataSetChanged();
            });

            if (getArguments() != null) {
                String chatType = getArguments().getString(GroupAllChatListFragment.CHAT_TYPE);
                if (chatType.equalsIgnoreCase(GroupAllChatListFragment.ChatType.Pending.name())) {
                    chatActionsChannel.getPendingQuestions();
                }
            }

        } catch (Exception e) {

            if (getContext() != null) {
                new Connect().refreshChannel(() -> {
                    refresh();
                    return Unit.INSTANCE;
                }, () -> Unit.INSTANCE);

                Toast.makeText(getContext(), "Please refresh app to show your chats", Toast.LENGTH_LONG).show();
            }
        }

    }

    void setUpChatView(Boolean groupChannelEmpty) {
        if (groupChannelEmpty) {
            mRecyclerView.setVisibility(View.GONE);
            noChatCard.setVisibility(View.VISIBLE);
        } else {
            noChatCard.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mRecyclerView.smoothScrollToPosition(0);
        }
    }

    enum ChatType {
        Pending,
        Active,
        Past,
    }

}

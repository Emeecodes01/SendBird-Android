package com.ulesson.chat.main.allChat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.UserMessage;
import com.ulesson.chat.R;
import com.ulesson.chat.main.SyncManagerUtils;
import com.ulesson.chat.main.model.Question;
import com.ulesson.chat.utils.DateUtils;
import com.ulesson.chat.utils.PreferenceUtils;
import com.ulesson.chat.utils.StringUtils;
import com.ulesson.chat.utils.TypingIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class GroupAllChatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ConcurrentHashMap<SimpleTarget<Bitmap>, Integer> mSimpleTargetIndexMap;
    private final ConcurrentHashMap<SimpleTarget<Bitmap>, GroupChannel> mSimpleTargetGroupChannelMap;
    private final ConcurrentHashMap<String, Integer> mChannelImageNumMap;
    private final ConcurrentHashMap<String, ImageView> mChannelImageViewMap;
    private final ConcurrentHashMap<String, SparseArray<Bitmap>> mChannelBitmapMap;
    private final Context mContext;
    private final List<GroupChannel> mChannelList;
    private final List<Question> questionList;
    private final List<GroupChannel> isActiveChannel;
    private final List<GroupChannel> isPendingChannel;
    private final List<GroupChannel> isPastChannel;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;
    private OnQuestionClickListener onQuestionClickListener;
    private OnQuestionLongClickListener onQuestionLongClickListener;
    private String chatType = "pending";


    private static final int VIEW_TYPE_GROUP_CHANNELS = 1;
    private static final int VIEW_TYPE_PENDING_QUESTION = 2;

    GroupAllChatListAdapter(Context context) {
        mContext = context;

        mSimpleTargetIndexMap = new ConcurrentHashMap<>();
        mSimpleTargetGroupChannelMap = new ConcurrentHashMap<>();
        mChannelImageNumMap = new ConcurrentHashMap<>();
        mChannelImageViewMap = new ConcurrentHashMap<>();
        mChannelBitmapMap = new ConcurrentHashMap<>();
        mChannelList = new ArrayList<>();
        questionList = new ArrayList<>();
        isPendingChannel = new ArrayList<>();
        isActiveChannel = new ArrayList<>();
        isPastChannel = new ArrayList<>();
    }

    void clearChannelList() {
        mChannelList.clear();
        isPastChannel.clear();
        isPendingChannel.clear();
        isActiveChannel.clear();
        isPastChannel.clear();
        notifyDataSetChanged();
    }

    void clearMap() {
        mSimpleTargetIndexMap.clear();
        mSimpleTargetGroupChannelMap.clear();
        mChannelImageNumMap.clear();
        mChannelImageViewMap.clear();
        mChannelBitmapMap.clear();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {

            case VIEW_TYPE_GROUP_CHANNELS:
                View groupChannelView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_all_chat_channel, parent, false);
                return new ChannelHolder(groupChannelView);

            case VIEW_TYPE_PENDING_QUESTION:
                View pendingQuestionView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_all_chat_channel, parent, false);
                return new QuestionHolder(pendingQuestionView);

            default:
                return null;

        }

    }

    @Override
    public int getItemViewType(int position) {

        if (chatType.equalsIgnoreCase(GroupAllChatListFragment.ChatType.Pending.name())) {
            return VIEW_TYPE_PENDING_QUESTION;
        } else {
            return VIEW_TYPE_GROUP_CHANNELS;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_GROUP_CHANNELS:
                ((ChannelHolder) holder).bind(mContext, position, mChannelList.get(position), mItemClickListener, mItemLongClickListener);
                break;
            case VIEW_TYPE_PENDING_QUESTION:
                ((QuestionHolder) holder).bind(position, PreferenceUtils.getPendingQuestions().get(position), onQuestionClickListener, onQuestionLongClickListener);
        }
    }

    @Override
    public int getItemCount() {

        if (chatType.equalsIgnoreCase(GroupAllChatListFragment.ChatType.Pending.name())) {
            return isPendingChannel.size();
        } else if (chatType.equalsIgnoreCase(GroupAllChatListFragment.ChatType.Active.name())) {
            return isActiveChannel.size();
        } else {
            return isPastChannel.size();
        }
    }

    List<GroupChannel> insertChannels(List<GroupChannel> channels, GroupChannelListQuery.Order order, String chatType) {

        this.chatType = chatType;

        for (GroupChannel newChannel : channels) {
            int activeIndex = SyncManagerUtils.findIndexOfChannel(isActiveChannel, newChannel, order);
            int pastIndex = SyncManagerUtils.findIndexOfChannel(isPastChannel, newChannel, order);

            if (new StringUtils().isActive(newChannel.getData())) {
                isActiveChannel.add(activeIndex, newChannel);
            } else {
                isPastChannel.add(pastIndex, newChannel);
            }
        }

        if (chatType.equalsIgnoreCase(GroupAllChatListFragment.ChatType.Pending.name())) {
            //set pending questions list
            List<Question> pendingQuestions = PreferenceUtils.getPendingQuestions();

            for (Question question : pendingQuestions) {
                isPendingChannel.add(null);
                questionList.add(question);
            }
            notifyDataSetChanged();
            return isPendingChannel;
        } else if (chatType.equalsIgnoreCase(GroupAllChatListFragment.ChatType.Active.name())) {
            mChannelList.addAll(isActiveChannel);
            notifyDataSetChanged();
            return isActiveChannel;
        } else {
            mChannelList.addAll(isPastChannel);
            notifyDataSetChanged();
            return isPastChannel;
        }
    }

    void updateChannels(List<GroupChannel> channels) {
        for (GroupChannel updatedChannel : channels) {
            int index = SyncManagerUtils.getIndexOfChannel(mChannelList, updatedChannel);
            if (index != -1) {
                mChannelList.set(index, updatedChannel);
                notifyItemChanged(index);
            }
        }
    }

    void moveChannels(List<GroupChannel> channels, GroupChannelListQuery.Order order) {
        for (GroupChannel movedChannel : channels) {
            int fromIndex = SyncManagerUtils.getIndexOfChannel(mChannelList, movedChannel);
            int toIndex = SyncManagerUtils.findIndexOfChannel(mChannelList, movedChannel, order);
            if (fromIndex != -1) {
                mChannelList.remove(fromIndex);
                mChannelList.add(toIndex, movedChannel);
                notifyItemMoved(fromIndex, toIndex);
                notifyItemChanged(toIndex);
            }
        }
    }

    void removeChannels(List<GroupChannel> channels) {
        for (GroupChannel removedChannel : channels) {
            int index = SyncManagerUtils.getIndexOfChannel(mChannelList, removedChannel);
            if (index != -1) {
                mChannelList.remove(index);
                notifyItemRemoved(index);
            }
        }
    }

    void addLast(List<GroupChannel> channel) {
        mChannelList.addAll(channel);
        notifyItemInserted(mChannelList.size() - 1);
    }

    void updateOrInsert(BaseChannel channel) {
        if (!(channel instanceof GroupChannel)) {
            return;
        }

        GroupChannel groupChannel = (GroupChannel) channel;

        for (int i = 0; i < mChannelList.size(); i++) {
            if (mChannelList.get(i).getUrl().equals(groupChannel.getUrl())) {
                mChannelList.remove(mChannelList.get(i));
                mChannelList.add(0, groupChannel);
                notifyDataSetChanged();
                Log.v(GroupAllChatListAdapter.class.getSimpleName(), "Channel replaced.");
                return;
            }
        }

        mChannelList.add(0, groupChannel);
        notifyDataSetChanged();
    }

    void setOnItemClickListener(OnItemClickListener listener, OnQuestionClickListener questionListener) {
        mItemClickListener = listener;
        onQuestionClickListener = questionListener;
    }

    interface OnItemClickListener {
        void onItemClick(GroupChannel channel);
    }

    interface OnItemLongClickListener {
        void onItemLongClick(GroupChannel channel);
    }

    interface OnQuestionClickListener {
        void onItemClick(Question question);
    }

    interface OnQuestionLongClickListener {
        void onItemLongClick(Question question);
    }

    /**
     * A ViewHolder that contains UI to display information about a GroupChannel.
     */
    private class ChannelHolder extends RecyclerView.ViewHolder {

        //        memberCountText, topicText
        TextView subjectText, lastMessageText, unreadCountText, dateText;
        LinearLayout typingIndicatorContainer;
        ImageView subjectIcon;

        ChannelHolder(View itemView) {
            super(itemView);

            subjectText = itemView.findViewById(R.id.text_group_channel_list_subject);
            lastMessageText = itemView.findViewById(R.id.text_group_channel_list_message);
            unreadCountText = itemView.findViewById(R.id.text_group_channel_list_unread_count);
            dateText = itemView.findViewById(R.id.text_group_channel_list_date);
            subjectIcon = itemView.findViewById(R.id.subjectIcon);

            typingIndicatorContainer = itemView.findViewById(R.id.container_group_channel_list_typing_indicator);
        }

        void bind(final Context context, int position, final GroupChannel channel,
                  @Nullable final OnItemClickListener clickListener,
                  @Nullable final OnItemLongClickListener longClickListener) {

            int unreadCount = channel.getUnreadMessageCount();
            // If there are no unread messages, hide the unread count badge.
            if (unreadCount == 0) {
                unreadCountText.setVisibility(View.INVISIBLE);
            } else {
                unreadCountText.setVisibility(View.VISIBLE);
                unreadCountText.setText(String.valueOf(channel.getUnreadMessageCount()));
            }

            Map<String, Object> questionMap = StringUtils.toMutableMap(channel.getData());
            String subjectName = questionMap.get("subjectName") + "";
            subjectText.setText(subjectName);
            Glide.with(mContext).load(questionMap.get("subjectAvatar"))
                    .into(subjectIcon);

            if (!new StringUtils().isActive(channel.getData())) {
                subjectIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.fade), PorterDuff.Mode.MULTIPLY);
            }

            BaseMessage lastMessage = channel.getLastMessage();
            if (lastMessage != null) {
                dateText.setVisibility(View.VISIBLE);
                lastMessageText.setVisibility(View.VISIBLE);

                // Display information about the most recently sent message in the channel.
                dateText.setText(String.valueOf(DateUtils.formatDateTime(lastMessage.getCreatedAt())));

                // Bind last message text according to the type of message. Specifically, if
                // the last message is a File Message, there must be special formatting.
                if (lastMessage instanceof UserMessage) {
                    lastMessageText.setText(lastMessage.getMessage());
                } else if (lastMessage instanceof AdminMessage) {
                    lastMessageText.setText(lastMessage.getMessage());
                } else {
                    String lastMessageString = String.format(
                            context.getString(R.string.group_channel_list_file_message_text),
                            lastMessage.getSender().getNickname());
                    lastMessageText.setText(lastMessageString);
                }
            } else {
                dateText.setVisibility(View.INVISIBLE);
                lastMessageText.setVisibility(View.INVISIBLE);
            }

            ArrayList<ImageView> indicatorImages = new ArrayList<>();
            indicatorImages.add(typingIndicatorContainer.findViewById(R.id.typing_indicator_dot_1));
            indicatorImages.add(typingIndicatorContainer.findViewById(R.id.typing_indicator_dot_2));
            indicatorImages.add(typingIndicatorContainer.findViewById(R.id.typing_indicator_dot_3));

            TypingIndicator indicator = new TypingIndicator(indicatorImages, 600);
            indicator.animate();

            if (channel.isTyping()) {
                typingIndicatorContainer.setVisibility(View.VISIBLE);
                lastMessageText.setText(("typing..."));
            } else {
                // Display typing indicator only when someone is typing
                typingIndicatorContainer.setVisibility(View.GONE);
            }

            // Set an OnClickListener to this item.
            if (clickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onItemClick(channel);
                    }
                });
            }

            // Set an OnLongClickListener to this item.
            if (longClickListener != null) {
                itemView.setOnLongClickListener(v -> {
                    longClickListener.onItemLongClick(channel);
                    return true;
                });
            }
        }

    }

    private class QuestionHolder extends RecyclerView.ViewHolder {

        TextView subjectText, lastMessageText, unreadCountText, dateText;
        LinearLayout typingIndicatorContainer;
        ImageView subjectIcon;

        QuestionHolder(View itemView) {
            super(itemView);

            subjectText = itemView.findViewById(R.id.text_group_channel_list_subject);
            lastMessageText = itemView.findViewById(R.id.text_group_channel_list_message);
            unreadCountText = itemView.findViewById(R.id.text_group_channel_list_unread_count);
            dateText = itemView.findViewById(R.id.text_group_channel_list_date);
            subjectIcon = itemView.findViewById(R.id.subjectIcon);

            typingIndicatorContainer = itemView.findViewById(R.id.container_group_channel_list_typing_indicator);
        }

        void bind(int position, final Question question,
                  @Nullable final OnQuestionClickListener questionClickListener,
                  @Nullable final OnQuestionLongClickListener questionLongClickListener) {

            subjectText.setText(question.getSubjectName());
            Glide.with(mContext).load(question.getQuestionUrl())
                    .into(subjectIcon);

            unreadCountText.setVisibility(View.GONE);
            typingIndicatorContainer.setVisibility(View.GONE);

            if (question.getQuestionText() != null) {
                dateText.setVisibility(View.VISIBLE);
                lastMessageText.setVisibility(View.VISIBLE);

                // Display information about the most recently sent message in the channel.
                dateText.setText(question.getDate());
                lastMessageText.setText(question.getQuestionText());

            } else {
                dateText.setVisibility(View.INVISIBLE);
                lastMessageText.setVisibility(View.INVISIBLE);
            }

            // Set an OnClickListener to this item.
            if (questionClickListener != null) {
                itemView.setOnClickListener(v -> questionClickListener.onItemClick(question));
            }

            // Set an OnLongClickListener to this item.
            if (questionLongClickListener != null) {
                itemView.setOnLongClickListener(v -> {
                    questionLongClickListener.onItemLongClick(question);
                    return true;
                });
            }
        }

    }
}

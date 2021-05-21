package com.sendbird.main.allChat;

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
import com.sendbird.R;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.UserMessage;
import com.sendbird.main.SyncManagerUtils;
import com.sendbird.utils.DateUtils;
import com.sendbird.utils.StringUtils;
import com.sendbird.utils.TypingIndicator;

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
    private List<GroupChannel> mChannelList;
    private List<GroupChannel> mAllChannelList;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    GroupAllChatListAdapter(Context context) {
        mContext = context;

        mSimpleTargetIndexMap = new ConcurrentHashMap<>();
        mSimpleTargetGroupChannelMap = new ConcurrentHashMap<>();
        mChannelImageNumMap = new ConcurrentHashMap<>();
        mChannelImageViewMap = new ConcurrentHashMap<>();
        mChannelBitmapMap = new ConcurrentHashMap<>();

        mChannelList = new ArrayList<>();
        mAllChannelList = new ArrayList<>();
    }

    void clearChannelList() {
        mChannelList.clear();
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_all_chat_channel, parent, false);
        return new ChannelHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ChannelHolder) holder).bind(mContext, position, mChannelList.get(position), mItemClickListener, mItemLongClickListener);
    }

    @Override
    public int getItemCount() {
        return mChannelList.size();
    }

    void insertChannels(List<GroupChannel> channels, GroupChannelListQuery.Order order, Boolean isActive) {

        List<GroupChannel> isActiveChannel = new ArrayList<>();
        List<GroupChannel> isPastChannel = new ArrayList<>();

        for (GroupChannel newChannel : channels) {
            int index = SyncManagerUtils.findIndexOfChannel(mChannelList, newChannel, order);

            if (new StringUtils().isActive(newChannel.getData())) {
                isActiveChannel.add(index, newChannel);
            } else {
                isPastChannel.add(index, newChannel);
            }

        }

        if (isActive) {
            mChannelList.addAll(isActiveChannel);
        } else {
            mChannelList.addAll(isPastChannel);
        }

        notifyDataSetChanged();
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

    void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    interface OnItemClickListener {
        void onItemClick(GroupChannel channel);
    }

    interface OnItemLongClickListener {
        void onItemLongClick(GroupChannel channel);
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

            subjectText = (TextView) itemView.findViewById(R.id.text_group_channel_list_subject);
            lastMessageText = (TextView) itemView.findViewById(R.id.text_group_channel_list_message);
            unreadCountText = (TextView) itemView.findViewById(R.id.text_group_channel_list_unread_count);
            dateText = (TextView) itemView.findViewById(R.id.text_group_channel_list_date);
            subjectIcon = (ImageView) itemView.findViewById(R.id.subjectIcon);

            typingIndicatorContainer = (LinearLayout) itemView.findViewById(R.id.container_group_channel_list_typing_indicator);
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
            subjectText.setText(questionMap.get("subjectName").toString());
            Glide.with(mContext).load(questionMap.get("subjectAvatar")).into(subjectIcon);

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
                    lastMessageText.setText(((UserMessage) lastMessage).getMessage());
                } else if (lastMessage instanceof AdminMessage) {
                    lastMessageText.setText(((AdminMessage) lastMessage).getMessage());
                } else {
                    String lastMessageString = String.format(
                            context.getString(R.string.group_channel_list_file_message_text),
                            ((FileMessage) lastMessage).getSender().getNickname());
                    lastMessageText.setText(lastMessageString);
                }
            } else {
                dateText.setVisibility(View.INVISIBLE);
                lastMessageText.setVisibility(View.INVISIBLE);
            }

            /*
             * Set up the typing indicator.
             * A typing indicator is basically just three dots contained within the layout
             * that animates. The animation is implemented in the {@link TypingIndicator#animate() class}
             */
            ArrayList<ImageView> indicatorImages = new ArrayList<>();
            indicatorImages.add((ImageView) typingIndicatorContainer.findViewById(R.id.typing_indicator_dot_1));
            indicatorImages.add((ImageView) typingIndicatorContainer.findViewById(R.id.typing_indicator_dot_2));
            indicatorImages.add((ImageView) typingIndicatorContainer.findViewById(R.id.typing_indicator_dot_3));

            TypingIndicator indicator = new TypingIndicator(indicatorImages, 600);
            indicator.animate();

            // debug
//            typingIndicatorContainer.setVisibility(View.VISIBLE);
//            lastMessageText.setText(("Someone is typing"));

            // If someone in the channel is typing, display the typing indicator.
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
}

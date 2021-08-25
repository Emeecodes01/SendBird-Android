package com.ulesson.chat.groupchannel;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.UserMessage;
import com.ulesson.chat.R;
import com.ulesson.chat.main.SyncManagerUtils;
import com.ulesson.chat.main.sendBird.ChatActions;
import com.ulesson.chat.utils.ChatType;
import com.ulesson.chat.utils.DateUtils;
import com.ulesson.chat.utils.ImageUtils;
import com.ulesson.chat.utils.StringUtils;
import com.ulesson.chat.utils.TypingIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class GroupChannelListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ConcurrentHashMap<SimpleTarget<Bitmap>, Integer> mSimpleTargetIndexMap;
    private final ConcurrentHashMap<SimpleTarget<Bitmap>, GroupChannel> mSimpleTargetGroupChannelMap;
    private final ConcurrentHashMap<String, Integer> mChannelImageNumMap;
    private final ConcurrentHashMap<String, ImageView> mChannelImageViewMap;
    private final ConcurrentHashMap<String, SparseArray<Bitmap>> mChannelBitmapMap;
    private final Context mContext;
    private final List<GroupChannel> mChannelList;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    GroupChannelListAdapter(Context context) {
        mContext = context;

        mSimpleTargetIndexMap = new ConcurrentHashMap<>();
        mSimpleTargetGroupChannelMap = new ConcurrentHashMap<>();
        mChannelImageNumMap = new ConcurrentHashMap<>();
        mChannelImageViewMap = new ConcurrentHashMap<>();
        mChannelBitmapMap = new ConcurrentHashMap<>();
        mChannelList = new ArrayList<>();
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
                .inflate(R.layout.list_item_group_channel, parent, false);
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

    void insertChannels(List<GroupChannel> channels, GroupChannelListQuery.Order order, ChatActions chatActions) {

        for (GroupChannel newChannel : channels) {
            newChannel.refresh(e -> {});
            int index = SyncManagerUtils.findIndexOfChannel(mChannelList, newChannel, order);
            mChannelList.add(index, newChannel);
        }

        notifyDataSetChanged();

        chatActions.chatReceived();

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
                return;
            }
        }

        mChannelList.add(0, groupChannel);
        notifyDataSetChanged();
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
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
            subjectText.setText((String) questionMap.get("subjectName"));

            String subjectThemeKey = (String) questionMap.get("subjectThemeKey");
            HashMap<String, ImageUtils.Theme> subjectThemeMap = ImageUtils.getThemeMap();
            ImageUtils.Theme theme = subjectThemeMap.get(subjectThemeKey);

            if (new StringUtils().chatType(channel.getData()) != ChatType.Past) {

                int pastIcon = R.drawable.ic_maths_grey_fill;
                if (theme != null) {
                    pastIcon = theme.pastIcon;
                }

                subjectIcon.setImageResource(pastIcon);
            } else {

                int activeIcon = R.drawable.ic_maths_fill;
                if (theme != null) {
                    activeIcon = theme.pastIcon;
                }

                subjectIcon.setImageResource(activeIcon);
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
                typingIndicatorContainer.setVisibility(View.GONE);
            }

            if (clickListener != null) {
                itemView.setOnClickListener(v -> clickListener.onItemClick(channel));
            }

            if (longClickListener != null) {
                itemView.setOnLongClickListener(v -> {
                    longClickListener.onItemLongClick(channel);
                    return true;
                });
            }
        }

    }
}

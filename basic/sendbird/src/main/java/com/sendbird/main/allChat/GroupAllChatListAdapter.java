package com.sendbird.main.allChat;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
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
import com.sendbird.R;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.UserMessage;
import com.sendbird.utils.DateUtils;
import com.sendbird.utils.FileUtils;
import com.sendbird.utils.PreferenceUtils;
import com.sendbird.utils.TextUtils;
import com.sendbird.utils.TypingIndicator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    void clearMap() {
        mSimpleTargetIndexMap.clear();
        mSimpleTargetGroupChannelMap.clear();
        mChannelImageNumMap.clear();
        mChannelImageViewMap.clear();
        mChannelBitmapMap.clear();
    }

    public List<GroupChannel> load() {

        try {
            File appDir = new File(mContext.getCacheDir(), SendBird.getApplicationId());
            appDir.mkdirs();

            File dataFile = new File(appDir, TextUtils.generateMD5(PreferenceUtils.getUserId() + "channel_list") + ".data");

            String content = FileUtils.loadFromFile(dataFile);
            String[] dataArray = content.split("\n");

            // Reset channel list, then add cached data.
            mAllChannelList.clear();
            for (String s : dataArray) {
                mAllChannelList.add((GroupChannel) BaseChannel.buildFromSerializedData(Base64.decode(s, Base64.DEFAULT | Base64.NO_WRAP)));
            }

            notifyDataSetChanged();
        } catch (Exception e) {
            // Nothing to load.
        }

        return mAllChannelList;
    }

    public void save() {
        try {
            StringBuilder sb = new StringBuilder();

            // Save the data into file.
            File appDir = new File(mContext.getCacheDir(), SendBird.getApplicationId());
            appDir.mkdirs();

            File hashFile = new File(appDir, TextUtils.generateMD5(PreferenceUtils.getUserId() + "channel_list") + ".hash");
            File dataFile = new File(appDir, TextUtils.generateMD5(PreferenceUtils.getUserId() + "channel_list") + ".data");

            if (mAllChannelList != null && mAllChannelList.size() > 0) {
                // Convert current data into string.
                GroupChannel channel = null;
                for (int i = 0; i < Math.min(mAllChannelList.size(), 100); i++) {
                    channel = mAllChannelList.get(i);
                    sb.append("\n");
                    sb.append(Base64.encodeToString(channel.serialize(), Base64.DEFAULT | Base64.NO_WRAP));
                }
                // Remove first newline.
                sb.delete(0, 1);

                String data = sb.toString();
                String md5 = TextUtils.generateMD5(data);

                try {
                    String content = FileUtils.loadFromFile(hashFile);
                    // If data has not been changed, do not save.
                    if (md5.equals(content)) {
                        return;
                    }
                } catch (IOException e) {
                    // File not found. Save the data.
                }

                FileUtils.saveToFile(dataFile, data);
                FileUtils.saveToFile(hashFile, md5);
            } else {
                FileUtils.deleteFile(dataFile);
                FileUtils.deleteFile(hashFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    void setGroupChannelList(List<GroupChannel> channelList) {
        mChannelList = channelList;
        notifyDataSetChanged();
    }

    void setAllGroupChannelList(List<GroupChannel> channelList) {
        mAllChannelList = channelList;
    }

    void addLast(List<GroupChannel> channel) {
        mChannelList.addAll(channel);
        notifyItemInserted(mChannelList.size() - 1);
    }

    // If the channel is not in the list yet, adds it.
    // If it is, finds the channel in current list, and replaces it.
    // Moves the updated channel to the front of the list.
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
        TextView lastMessageText, unreadCountText, dateText;
        LinearLayout typingIndicatorContainer;

        ChannelHolder(View itemView) {
            super(itemView);

            lastMessageText = (TextView) itemView.findViewById(R.id.text_group_channel_list_message);
            unreadCountText = (TextView) itemView.findViewById(R.id.text_group_channel_list_unread_count);
            dateText = (TextView) itemView.findViewById(R.id.text_group_channel_list_date);

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
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        longClickListener.onItemLongClick(channel);

                        // return true if the callback consumed the long click
                        return true;
                    }
                });
            }
        }

    }
}

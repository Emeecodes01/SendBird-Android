package com.sendbird.android.sample.main.chat

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.*
import com.sendbird.android.sample.R

import com.sendbird.android.sample.utils.*
import com.sendbird.android.sample.utils.SyncManagerUtils.findIndexOfMessage
import com.sendbird.android.sample.utils.SyncManagerUtils.getIndexOfMessage
import com.sendbird.android.sample.widget.MessageStatusView
import org.json.JSONException
import java.util.*
import java.util.concurrent.TimeUnit

class ChatDiffUtil : DiffUtil.ItemCallback<BaseMessage>() {
    override fun areItemsTheSame(oldItem: BaseMessage, newItem: BaseMessage): Boolean {
        return oldItem.requestId == newItem.requestId
    }

    override fun areContentsTheSame(oldItem: BaseMessage, newItem: BaseMessage): Boolean {
        return isItemTheSame(oldItem, newItem)
    }

    private fun isItemTheSame(oldItem: BaseMessage, newItem: BaseMessage): Boolean {
        return when(oldItem) {
            is FileMessage -> {
                (newItem as FileMessage)
                if (oldItem.type.startsWith("image")
                    || newItem.type.startsWith("image")) {
                    return (oldItem.thumbnails[0].url == newItem.thumbnails[0].url)
                            && (oldItem.sendingStatus == newItem.sendingStatus)
                } else if (oldItem.type.startsWith("video/3gpp") || newItem.type.startsWith("video/3gpp")) {
                    return oldItem.url == newItem.url &&
                            oldItem.sendingStatus == newItem.sendingStatus
                }
                return false
            }
            is UserMessage -> {
                oldItem.message == newItem.message
                        && oldItem.sendingStatus == newItem.sendingStatus
            }
            else -> oldItem.toString() == newItem.toString()
        }
    }

}


internal class GroupChatAdapter(private var mContext: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mChannel: GroupChannel? = null

    private var mMessageList: MutableList<BaseMessage> = mutableListOf()

    private var mItemClickListener: OnItemClickListener? = null
    private var mItemLongClickListener: OnItemLongClickListener? = null
    private var timerProgressListener: Progress? = null
    private val mTempFileMessageUriTable = Hashtable<String, Uri>()
    private var mFailedMessageList: MutableList<BaseMessage> = mutableListOf()
    private var mResendingMessageSet: MutableSet<String> = mutableSetOf()
    private var currentPlayingPosition: Int = -1
    private var isPlaying: Boolean = false

    @get:Synchronized
    @set:Synchronized
    private var isMessageListLoading = false

    internal interface OnItemLongClickListener {
        fun onUserMessageItemLongClick(message: UserMessage?, position: Int)
        fun onFileMessageItemLongClick(message: FileMessage?)
        fun onAdminMessageItemLongClick(message: AdminMessage?)
    }

    internal interface OnItemClickListener {
        fun onUserMessageItemClick(message: UserMessage?)
        fun onFileMessageItemClick(message: FileMessage?)
    }

    internal interface Progress {
        fun onProgressChange(position: Int)
    }

    fun setContext(context: Context) {
        mContext = context
    }

    /**
     * Inflates the correct layout according to the View Type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER_MESSAGE_ME -> {
                val myUserMsgView = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.list_item_group_chat_user_me,
                        parent,
                        false
                    )
                MyUserMessageHolder(myUserMsgView)
            }
            VIEW_TYPE_USER_MESSAGE_OTHER -> {
                val otherUserMsgView = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.list_item_group_chat_user_other,
                        parent,
                        false
                    )
                OtherUserMessageHolder(otherUserMsgView)
            }
            VIEW_TYPE_ADMIN_MESSAGE -> {
                val adminMsgView = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.list_item_group_chat_admin,
                        parent,
                        false
                    )
                AdminMessageHolder(
                    adminMsgView
                )
            }
            VIEW_TYPE_FILE_MESSAGE_ME -> {
                val myFileMsgView = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.list_item_group_chat_file_me,
                        parent,
                        false
                    )
                MyFileMessageHolder(myFileMsgView)
            }
            VIEW_TYPE_FILE_MESSAGE_OTHER -> {
                val otherFileMsgView = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.list_item_group_chat_file_other,
                        parent,
                        false
                    )
                OtherFileMessageHolder(otherFileMsgView)
            }
            VIEW_TYPE_FILE_MESSAGE_IMAGE_ME -> {
                val myImageFileMsgView = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.list_item_group_chat_file_image_me,
                        parent,
                        false
                    )
                MyImageFileMessageHolder(myImageFileMsgView)
            }
            VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER -> {
                val otherImageFileMsgView = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.list_item_group_chat_file_image_other,
                        parent,
                        false
                    )
                OtherImageFileMessageHolder(otherImageFileMsgView)
            }
            VIEW_TYPE_FILE_MESSAGE_VIDEO_ME -> {
                val myVideoFileMsgView = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.list_item_group_chat_file_video_me,
                        parent,
                        false
                    )
                MyVideoFileMessageHolder(myVideoFileMsgView)
            }
            VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER -> {
                val otherVideoFileMsgView = LayoutInflater.from(parent.context)
                    .inflate(
                        R.layout.list_item_group_chat_file_video_other,
                        parent,
                        false
                    )
                OtherVideoFileMessageHolder(otherVideoFileMsgView)
            }

            VIEW_TYPE_FILE_MESSAGE_AUDIO_ME -> {
                val meFileMsgView = LayoutInflater.from(parent.context).inflate(
                    R.layout.list_item_group_chat_file_audio_me,
                    parent, false
                )

                MeAudioFileMessageViewHolder(meFileMsgView)
            }

            VIEW_TYPE_FILE_MESSAGE_AUDIO_OTHER -> {
                val otherAudioFileMsgView = LayoutInflater.from(parent.context).inflate(
                    R.layout.list_item_group_chat_file_audio_other,
                    parent, false
                )
                OtherAudioFileMessageViewHolder(otherAudioFileMsgView)
            }

            else -> throw Exception("Class not found")
        }
    }

    /**
     * Binds variables in the BaseMessage to UI components in the ViewHolder.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = mMessageList[position]
        var isContinuous = false
        var isNewDay = false
        var isTempMessage = false
        var tempFileMessageUri: Uri? = null

        // If there is at least one item preceding the current one, check the previous message.
        if (position < mMessageList.size - 1) {
            val prevMessage = mMessageList[position + 1]

            // If the date of the previous message is different, display the date before the message,
            // and also set isContinuous to false to show information such as the sender's nickname
            // and profile image.
            if (!DateUtils.hasSameDate(message.createdAt, prevMessage.createdAt)) {
                isNewDay = true
                isContinuous = false
            } else {
                isContinuous = isContinuous(message, prevMessage)
            }
        } else if (position == mMessageList.size - 1) {
            isNewDay = true
        }
        isTempMessage = isTempMessage(message)
        tempFileMessageUri = getTempFileMessageUri(message)
        when (holder!!.itemViewType) {
            VIEW_TYPE_USER_MESSAGE_ME -> (holder as MyUserMessageHolder?)!!.bind(
                mContext,
                message as UserMessage,
                mChannel,
                isContinuous,
                isNewDay,
                mItemClickListener,
                mItemLongClickListener,
                position
            )
            VIEW_TYPE_USER_MESSAGE_OTHER -> (holder as OtherUserMessageHolder?)!!.bind(
                mContext,
                message as UserMessage,
                mChannel,
                isNewDay,
                isContinuous,
                mItemClickListener,
                mItemLongClickListener,
                position
            )
            VIEW_TYPE_ADMIN_MESSAGE -> (holder as AdminMessageHolder?)!!.bind(
                mContext,
                message as AdminMessage,
                mChannel,
                isNewDay
            )
            VIEW_TYPE_FILE_MESSAGE_ME -> (holder as MyFileMessageHolder?)!!.bind(
                mContext,
                message as FileMessage,
                mChannel,
                isNewDay,
                mItemClickListener
            )
            VIEW_TYPE_FILE_MESSAGE_OTHER -> (holder as OtherFileMessageHolder?)!!.bind(
                mContext,
                message as FileMessage,
                mChannel,
                isNewDay,
                isContinuous,
                mItemClickListener
            )
            VIEW_TYPE_FILE_MESSAGE_IMAGE_ME -> (holder as MyImageFileMessageHolder?)!!.bind(
                mContext,
                message as FileMessage,
                mChannel,
                isNewDay,
                isTempMessage,
                tempFileMessageUri,
                mItemClickListener
            )
            VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER -> (holder as OtherImageFileMessageHolder?)!!.bind(
                mContext,
                message as FileMessage,
                mChannel,
                isNewDay,
                isContinuous,
                mItemClickListener
            )
            VIEW_TYPE_FILE_MESSAGE_VIDEO_ME -> (holder as MyVideoFileMessageHolder?)!!.bind(
                mContext,
                message as FileMessage,
                mChannel,
                isNewDay,
                isTempMessage,
                tempFileMessageUri,
                mItemClickListener
            )
            VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER -> (holder as OtherVideoFileMessageHolder?)!!.bind(
                mContext,
                message as FileMessage,
                mChannel,
                isNewDay,
                isContinuous,
                mItemClickListener
            )

            VIEW_TYPE_FILE_MESSAGE_AUDIO_OTHER ->
                (holder as OtherAudioFileMessageViewHolder).bind(
                    mContext,
                    message as FileMessage,
                    mChannel,
                    isNewDay,
                    mItemClickListener
                )

            VIEW_TYPE_FILE_MESSAGE_AUDIO_ME -> {
                //create the player and player runnable
                (holder as MeAudioFileMessageViewHolder).bind(
                    mContext,
                    message as FileMessage,
                    mChannel,
                    isNewDay,
                    isTempMessage,
                    tempFileMessageUri,
                    isContinuous,
                    mItemClickListener
                )
            }


            else -> {
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder.itemViewType == GroupChatAdapter.VIEW_TYPE_FILE_MESSAGE_AUDIO_ME) {
            (holder as MeAudioFileMessageViewHolder).cleanUp()
        }
        if (holder.itemViewType == GroupChatAdapter.VIEW_TYPE_FILE_MESSAGE_AUDIO_OTHER) {
            (holder as OtherAudioFileMessageViewHolder).cleanUp()
        }
    }

    /**
     * Declares the View Type according to the type of message and the sender.
     */
    override fun getItemViewType(position: Int): Int {
        val message = mMessageList[position]
        if (message is UserMessage) {
            val userMessage = message
            // If the sender is current user
            if (userMessage.sender != null && userMessage.sender.userId != null) {
                return if (userMessage.sender.userId == PreferenceUtils.getUserId()) {
                    VIEW_TYPE_USER_MESSAGE_ME
                } else {
                    VIEW_TYPE_USER_MESSAGE_OTHER
                }
            }
        } else if (message is FileMessage) {
            val fileMessage = message
            return if (fileMessage.type.toLowerCase().startsWith("image")) {
                // If the sender is current user
                if (fileMessage.sender.userId == PreferenceUtils.getUserId()) {
                    VIEW_TYPE_FILE_MESSAGE_IMAGE_ME
                } else {
                    VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER
                }
            } else if (fileMessage.type.toLowerCase().startsWith("audio") ||
                    fileMessage.type.toLowerCase().startsWith("video/3gpp")) {
                //NOTE: THIS IS ACTUALLY AN AUDIO FILE
                return if (fileMessage.getSender().userId == PreferenceUtils.getUserId()) {
                    VIEW_TYPE_FILE_MESSAGE_AUDIO_ME
                } else {
                    VIEW_TYPE_FILE_MESSAGE_AUDIO_OTHER
                }
            } else if (fileMessage.type.toLowerCase().startsWith("video")) {
                if (fileMessage.sender.userId == PreferenceUtils.getUserId()) {
                    VIEW_TYPE_FILE_MESSAGE_VIDEO_ME
                } else {
                    VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER
                }
            } else {
                if (fileMessage.sender.userId == PreferenceUtils.getUserId()) {
                    VIEW_TYPE_FILE_MESSAGE_ME
                } else {
                    VIEW_TYPE_FILE_MESSAGE_OTHER
                }
            }
        } else if (message is AdminMessage) {
            return VIEW_TYPE_ADMIN_MESSAGE
        }
        return VIEW_TYPE_USER_MESSAGE_ME
    }

    override fun getItemCount(): Int {
        return mMessageList.size
    }

    fun setChannel(channel: GroupChannel?) {
        mChannel = channel
    }

    fun isTempMessage(message: BaseMessage?): Boolean {
        return message!!.messageId == 0L
    }

    private fun getRequestId(message: BaseMessage): String? {
        if (message is UserMessage) {
            return message.requestId
        } else if (message is FileMessage) {
            return message.requestId
        }
        return ""
    }

    fun isFailedMessage(message: BaseMessage?): Boolean {
        if (message == null) {
            return false;
        }

        return mFailedMessageList.contains(message);
    }

    fun isResendingMessage(message: BaseMessage?): Boolean {
        return if (message == null) {
            false
        } else mResendingMessageSet.contains(getRequestId(message))
    }

    fun getTempFileMessageUri(message: BaseMessage?): Uri? {
        if (!isTempMessage(message)) {
            return null
        }
        return if (message !is FileMessage) {
            null
        } else mTempFileMessageUriTable[message.requestId]
    }

    fun markMessageFailed(message: BaseMessage) {
        var msg: BaseMessage
        for (i in mMessageList.indices.reversed()) {
            msg = mMessageList[i]
            if (msg.requestId == message.requestId) {
                mMessageList[i] = message
                notifyDataSetChanged()
                //submitList(mMessageList)
                return
            }
        }
    }

    fun removeFailedMessage(message: BaseMessage) {
        mTempFileMessageUriTable.remove((message as FileMessage).requestId)
        mMessageList.remove(message)
        notifyDataSetChanged()
        //submitList(mMessageList)
    }

    fun markMessageSent(message: BaseMessage?) {
        var msg: BaseMessage
        for (i in mMessageList.indices.reversed()) {
            msg = mMessageList[i]
            if (message is UserMessage && msg is UserMessage) {
                if (msg.requestId == message.requestId) {
                    mMessageList[i] = message
                    //submitList(mMessageList)
                     notifyDataSetChanged()
                    //notifyItemChanged(i)
                    return
                }
            } else if (message is FileMessage && msg is FileMessage) {
                if (msg.requestId == message.requestId) {
                    mTempFileMessageUriTable.remove(message.requestId)
                    mMessageList[i] = message
                    //notifyItemChanged(i)
                    notifyDataSetChanged()
                    //submitList(mMessageList)
                    return
                }
            }
        }
    }

    fun addTempFileMessageInfo(message: FileMessage, uri: Uri) {
        mTempFileMessageUriTable[message.requestId] = uri
    }

    fun insertSucceededMessages(messages: List<BaseMessage?>) {
        //mMessageList = messages.sortedBy { it?.createdAt }.filterNotNull()
        for (message in messages) {
            val index = findIndexOfMessage(
                mMessageList,
                message!!
            )
            mMessageList.add(index, message)
            notifyItemChanged(index)
        }
    }

    fun updateSucceededMessages(messages: List<BaseMessage?>) {
        for (message in messages) {
            val index = getIndexOfMessage(
                mMessageList,
                message!!
            )
            if (index != -1) {
                mMessageList[index] = message
                notifyItemChanged(index)
            }
        }
        //mMessageList = messages.sortedBy { it?.createdAt }.filterNotNull()
    }

    fun removeSucceededMessages(messages: List<BaseMessage?>) {
        for (message in messages) {
            val index = getIndexOfMessage(
                mMessageList,
                message!!
            )
            if (index != -1) {
                mMessageList.removeAt(index)
                //notifyItemChanged(index)
            }
        }
        //submitList(mMessageList)
        notifyDataSetChanged()
        // mMessageList = messages.sortedBy { it?.createdAt }.filterNotNull()
    }

    //this correspond to onDataSetChange
    fun resubmitList() {
        //submitList(mMessageList)
    }


    fun insertFailedMessages(messages: List<BaseMessage?>) {
        synchronized(mFailedMessageList) {
            for (message in messages) {
                val requestId = getRequestId(message!!)
                if (requestId!!.isEmpty()) {
                    continue
                }

                mResendingMessageSet.add(requestId)
                mFailedMessageList.add(message)
            }
            mFailedMessageList.sortWith(Comparator { m1, m2 ->
                val x = m1.createdAt
                val y = m2.createdAt
                if (x < y) 1 else if (x == y) 0 else -1
            })
        }
        updateFailedMessage()
    }

    fun updateFailedMessages(messages: List<BaseMessage?>) {
        synchronized(mFailedMessageList) {
            for (message in messages) {
                val requestId = getRequestId(message!!)
                if (requestId!!.isEmpty()) {
                    continue
                }
                mResendingMessageSet.remove(requestId)
            }
        }
        updateFailedMessage()
    }

    fun removeFailedMessages(messages: List<BaseMessage?>) {
        synchronized(mFailedMessageList) {
            for (message in messages) {
                val requestId = getRequestId(message!!)
                mResendingMessageSet.remove(requestId)
                mFailedMessageList.remove(message)
            }
        }
        updateFailedMessage()
    }

    fun updateFailedMessage() {
        val tempList = mMessageList
        mMessageList.clear()
        mMessageList = tempList
        notifyDataSetChanged()
        //submitList(mMessageList)
    }

    fun failedMessageListContains(message: BaseMessage?): Boolean {
        if (mFailedMessageList.isEmpty()) {
            return false
        }
        for (failedMessage in mFailedMessageList) {
            if (message is UserMessage && failedMessage is UserMessage) {
                if (message.requestId.equals(failedMessage.requestId)) {
                    return true
                }
            } else if (message is FileMessage && failedMessage is FileMessage) {
                if (message.requestId.equals(failedMessage.requestId)) {
                    return true
                }
            }
        }
        return false
    }


    fun clear() {
        mMessageList.clear()
        mFailedMessageList.clear()
        //submitList(mMessageList)
        notifyDataSetChanged()
    }

    /**
     * Notifies that the user has read all (previously unread) messages in the channel.
     * Typically, this would be called immediately after the user enters the chat and loads
     * its most recent messages.
     */
    fun markAllMessagesAsRead() {
        if (mChannel != null) {
            mChannel!!.markAsRead()
        }
        notifyDataSetChanged()
        //submitList(mMessageList)
    }

    fun getLastReadPosition(lastRead: Long): Int {
        for (i in mMessageList.indices) {
            if (mMessageList[i].createdAt == lastRead) {
                return i + mFailedMessageList.size
            }
        }
        return 0
    }

    fun setItemLongClickListener(listener: OnItemLongClickListener?) {
        mItemLongClickListener = null
    }

    fun setItemClickListener(listener: OnItemClickListener?) {
        mItemClickListener = listener
    }

    fun setProgressListener(listener: Progress?) {
        timerProgressListener = listener
    }

    /**
     * Checks if the current message was sent by the same person that sent the preceding message.
     *
     *
     * This is done so that redundant UI, such as sender nickname and profile picture,
     * does not have to displayed when not necessary.
     */
    private fun isContinuous(currentMsg: BaseMessage?, precedingMsg: BaseMessage?): Boolean {
        // null check
        if (currentMsg == null || precedingMsg == null) {
            return false
        }
        if (currentMsg is AdminMessage && precedingMsg is AdminMessage) {
            return true
        }
        var currentUser: User? = null
        var precedingUser: User? = null
        if (currentMsg is UserMessage) {
            currentUser = currentMsg.sender
        } else if (currentMsg is FileMessage) {
            currentUser = currentMsg.sender
        }
        if (precedingMsg is UserMessage) {
            precedingUser = precedingMsg.sender
        } else if (precedingMsg is FileMessage) {
            precedingUser = precedingMsg.sender
        }

        // If admin message or
        return (!(currentUser == null || precedingUser == null)
                && currentUser.userId == precedingUser.userId)
    }

    private inner class AdminMessageHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView
        private val dateText: TextView
        fun bind(
            context: Context?,
            message: AdminMessage,
            channel: GroupChannel?,
            isNewDay: Boolean
        ) {
            messageText.text = message.message
            if (isNewDay) {
                dateText.visibility = View.VISIBLE
                dateText.text = DateUtils.formatDate(message.createdAt)
            } else {
                dateText.visibility = View.GONE
            }
        }

        init {
            messageText = itemView.findViewById<View>(R.id.text_group_chat_message) as TextView
            dateText = itemView.findViewById<View>(R.id.text_group_chat_date) as TextView
        }
    }

    private inner class MyUserMessageHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var messageText: TextView
        var editedText: TextView
        var timeText: TextView
        var dateText: TextView
        var urlPreviewContainer: ViewGroup
        var urlPreviewSiteNameText: TextView
        var urlPreviewTitleText: TextView
        var urlPreviewDescriptionText: TextView
        var urlPreviewMainImageView: ImageView
        var padding: View
        var messageStatusView: MessageStatusView
        fun bind(
            context: Context?,
            message: UserMessage,
            channel: GroupChannel?,
            isContinuous: Boolean,
            isNewDay: Boolean,
            clickListener: OnItemClickListener?,
            longClickListener: OnItemLongClickListener?,
            position: Int
        ) {
            messageText.text = message.message
            timeText.text = DateUtils.formatTime(message.createdAt)
            if (message.updatedAt > 0) {
                editedText.visibility = View.VISIBLE
            } else {
                editedText.visibility = View.GONE
            }

            // If continuous from previous message, remove extra padding.
//            if (isContinuous) {
//                padding.setVisibility(View.GONE);
//            } else {
//                padding.setVisibility(View.VISIBLE);
//            }

            // If the message is sent on a different date than the previous one, display the date.
            if (isNewDay) {
                dateText.visibility = View.VISIBLE
                dateText.text = DateUtils.formatDate(message.createdAt)
            } else {
                dateText.visibility = View.GONE
            }
            urlPreviewContainer.visibility = View.GONE
            if (message.customType == URL_PREVIEW_CUSTOM_TYPE) {
                try {
                    urlPreviewContainer.visibility = View.VISIBLE
                    val previewInfo = UrlPreviewInfo(message.data)
                    urlPreviewSiteNameText.text = "@" + previewInfo.siteName
                    urlPreviewTitleText.text = previewInfo.title
                    urlPreviewDescriptionText.text = previewInfo.description
                    ImageUtils.displayRoundCornerImageFromUrl(
                        context,
                        previewInfo.imageUrl,
                        urlPreviewMainImageView
                    )
                } catch (e: JSONException) {
                    urlPreviewContainer.visibility = View.GONE
                    e.printStackTrace()
                }
            }
            if (clickListener != null) {
                itemView.setOnClickListener { clickListener.onUserMessageItemClick(message) }
            }
            if (longClickListener != null) {
                itemView.setOnLongClickListener {
                    longClickListener.onUserMessageItemLongClick(message, position)
                    true
                }
            }
            messageStatusView.drawMessageStatus(channel, message)
        }

        init {
            messageText = itemView.findViewById<View>(R.id.text_group_chat_message) as TextView
            editedText = itemView.findViewById<View>(R.id.text_group_chat_edited) as TextView
            timeText = itemView.findViewById<View>(R.id.text_group_chat_time) as TextView
            dateText = itemView.findViewById<View>(R.id.text_group_chat_date) as TextView
            messageStatusView = itemView.findViewById(R.id.message_status_group_chat)
            urlPreviewContainer =
                itemView.findViewById<View>(R.id.url_preview_container) as ViewGroup
            urlPreviewSiteNameText =
                itemView.findViewById<View>(R.id.text_url_preview_site_name) as TextView
            urlPreviewTitleText =
                itemView.findViewById<View>(R.id.text_url_preview_title) as TextView
            urlPreviewDescriptionText =
                itemView.findViewById<View>(R.id.text_url_preview_description) as TextView
            urlPreviewMainImageView =
                itemView.findViewById<View>(R.id.image_url_preview_main) as ImageView

            // Dynamic padding that can be hidden or shown based on whether the message is continuous.
            padding = itemView.findViewById(R.id.view_group_chat_padding)
        }
    }

    private inner class OtherUserMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageText: TextView
        var editedText: TextView
        var nicknameText: TextView
        var timeText: TextView
        var dateText: TextView
        var profileImage: ImageView
        var urlPreviewContainer: ViewGroup
        var urlPreviewSiteNameText: TextView
        var urlPreviewTitleText: TextView
        var urlPreviewDescriptionText: TextView
        var urlPreviewMainImageView: ImageView
        fun bind(
            context: Context?,
            message: UserMessage,
            channel: GroupChannel?,
            isNewDay: Boolean,
            isContinuous: Boolean,
            clickListener: OnItemClickListener?,
            longClickListener: OnItemLongClickListener?,
            position: Int
        ) {
            // Show the date if the message was sent on a different date than the previous message.
            if (isNewDay) {
                dateText.visibility = View.VISIBLE
                dateText.text = DateUtils.formatDate(message.createdAt)
            } else {
                dateText.visibility = View.GONE
            }

            // Hide profile image and nickname if the previous message was also sent by current sender.
            if (isContinuous) {
//                profileImage.setVisibility(View.INVISIBLE);
                nicknameText.visibility = View.GONE
            } else {
//                profileImage.setVisibility(View.VISIBLE);
//                ImageUtils.displayRoundCornerImageFromUrl(
//                    context,
//                    message.sender.profileUrl,
//                    profileImage
//                )

//                nicknameText.setVisibility(View.VISIBLE);
                nicknameText.text = message.sender.nickname

            }
            messageText.text = message.message
            timeText.text = DateUtils.formatTime(message.createdAt)
            if (message.updatedAt > 0) {
                editedText.visibility = View.VISIBLE
            } else {
                editedText.visibility = View.GONE
            }
            urlPreviewContainer.visibility = View.GONE


            if (message.data.isNotEmpty()) {
                urlPreviewMainImageView.visibility = View.VISIBLE
                ImageUtils.displayRoundCornerImageFromUrl(
                    context,
                    message.data,
                    urlPreviewMainImageView
                )
            } else {
                urlPreviewMainImageView.visibility = View.GONE
            }

            /*if (message.customType == URL_PREVIEW_CUSTOM_TYPE) {
                try {
                    urlPreviewContainer.visibility = View.VISIBLE
                    val previewInfo = UrlPreviewInfo(message.data)
                    urlPreviewSiteNameText.text = "@" + previewInfo.siteName
                    urlPreviewTitleText.text = previewInfo.title
                    urlPreviewDescriptionText.text = previewInfo.description
                    ImageUtils.displayRoundCornerImageFromUrl(
                        context,
                        previewInfo.imageUrl,
                        urlPreviewMainImageView
                    )
                } catch (e: JSONException) {
                    urlPreviewContainer.visibility = View.GONE
                    e.printStackTrace()
                }
            }*/
            if (clickListener != null) {
                itemView.setOnClickListener { clickListener.onUserMessageItemClick(message) }
            }
            if (longClickListener != null) {
                itemView.setOnLongClickListener {
                    longClickListener.onUserMessageItemLongClick(message, position)
                    true
                }
            }
        }

        init {
            messageText = itemView.findViewById<View>(R.id.text_group_chat_message) as TextView
            editedText = itemView.findViewById<View>(R.id.text_group_chat_edited) as TextView
            timeText = itemView.findViewById<View>(R.id.text_group_chat_time) as TextView
            nicknameText = itemView.findViewById<View>(R.id.text_group_chat_nickname) as TextView
            profileImage = itemView.findViewById<View>(R.id.image_group_chat_profile) as ImageView
            dateText = itemView.findViewById<View>(R.id.text_group_chat_date) as TextView
            urlPreviewContainer =
                itemView.findViewById<View>(R.id.url_preview_container) as ViewGroup
            urlPreviewSiteNameText =
                itemView.findViewById<View>(R.id.text_url_preview_site_name) as TextView
            urlPreviewTitleText =
                itemView.findViewById<View>(R.id.text_url_preview_title) as TextView
            urlPreviewDescriptionText =
                itemView.findViewById<View>(R.id.text_url_preview_description) as TextView
            urlPreviewMainImageView =
                itemView.findViewById<View>(R.id.image_url_preview_main) as ImageView
        }
    }

    private inner class MyFileMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fileNameText: TextView
        var timeText: TextView
        var dateText: TextView
        var messageStatusView: MessageStatusView
        fun bind(
            context: Context?,
            message: FileMessage,
            channel: GroupChannel?,
            isNewDay: Boolean,
            listener: OnItemClickListener?
        ) {
            fileNameText.text = message.name
            timeText.text = DateUtils.formatTime(message.createdAt)

            // Show the date if the message was sent on a different date than the previous message.
            if (isNewDay) {
                dateText.visibility = View.VISIBLE
                dateText.text = DateUtils.formatDate(message.createdAt)
            } else {
                dateText.visibility = View.GONE
            }
            if (listener != null) {
                itemView.setOnClickListener { listener.onFileMessageItemClick(message) }
            }
            messageStatusView.drawMessageStatus(channel, message)
        }

        init {
            timeText = itemView.findViewById<View>(R.id.text_group_chat_time) as TextView
            fileNameText = itemView.findViewById<View>(R.id.text_group_chat_file_name) as TextView
            dateText = itemView.findViewById<View>(R.id.text_group_chat_date) as TextView
            messageStatusView = itemView.findViewById(R.id.message_status_group_chat)
        }
    }

    private inner class OtherFileMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nicknameText: TextView
        var timeText: TextView
        var fileNameText: TextView
        var fileSizeText: TextView? = null
        var dateText: TextView
        var profileImage: ImageView
        fun bind(
            context: Context?,
            message: FileMessage,
            channel: GroupChannel?,
            isNewDay: Boolean,
            isContinuous: Boolean,
            listener: OnItemClickListener?
        ) {
            fileNameText.text = message.name
            timeText.text = DateUtils.formatTime(message.createdAt)
            //            fileSizeText.setText(String.valueOf(message.getSize()));

            // Show the date if the message was sent on a different date than the previous message.
            if (isNewDay) {
                dateText.visibility = View.VISIBLE
                dateText.text = DateUtils.formatDate(message.createdAt)
            } else {
                dateText.visibility = View.GONE
            }

            // Hide profile image and nickname if the previous message was also sent by current sender.
            if (isContinuous) {
//                profileImage.setVisibility(View.INVISIBLE);
                nicknameText.visibility = View.GONE
            } else {
//                profileImage.setVisibility(View.VISIBLE);
                ImageUtils.displayRoundCornerImageFromUrl(
                    context,
                    message.sender.profileUrl,
                    profileImage
                )

//                nicknameText.setVisibility(View.VISIBLE);
                nicknameText.text = message.sender.nickname
            }
            if (listener != null) {
                itemView.setOnClickListener { listener.onFileMessageItemClick(message) }
            }
        }

        init {
            nicknameText = itemView.findViewById<View>(R.id.text_group_chat_nickname) as TextView
            timeText = itemView.findViewById<View>(R.id.text_group_chat_time) as TextView
            fileNameText = itemView.findViewById<View>(R.id.text_group_chat_file_name) as TextView
            //            fileSizeText = (TextView) itemView.findViewById(R.id.text_group_chat_file_size);
            profileImage = itemView.findViewById<View>(R.id.image_group_chat_profile) as ImageView
            dateText = itemView.findViewById<View>(R.id.text_group_chat_date) as TextView
        }
    }

    /**
     * A ViewHolder for file messages that are images.
     * Displays only the image thumbnail.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private inner class MyImageFileMessageHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var timeText: TextView
        var dateText: TextView
        var fileThumbnailImage: ImageView
        var messageStatusView: MessageStatusView
        fun bind(
            context: Context?,
            message: FileMessage,
            channel: GroupChannel?,
            isNewDay: Boolean,
            isTempMessage: Boolean,
            tempFileMessageUri: Uri?,
            listener: OnItemClickListener?
        ) {
            timeText.text = DateUtils.formatTime(message.createdAt)

            // Show the date if the message was sent on a different date than the previous message.
            if (isNewDay) {
                dateText.visibility = View.VISIBLE
                dateText.text = DateUtils.formatDate(message.createdAt)
            } else {
                dateText.visibility = View.GONE
            }
            if (isTempMessage && tempFileMessageUri != null) {
                ImageUtils.displayRoundCornerImageFromUrl(
                    context,
                    tempFileMessageUri.toString(),
                    fileThumbnailImage
                )
            } else {
                // Get thumbnails from FileMessage
                val thumbnails = message.thumbnails as ArrayList<FileMessage.Thumbnail>

                // If thumbnails exist, get smallest (first) thumbnail and display it in the message
                if (thumbnails.size > 0) {
                    if (message.type.toLowerCase().contains("gif")) {
                        ImageUtils.displayGifImageFromUrl(
                            context,
                            message.url,
                            fileThumbnailImage,
                            thumbnails[0].url
                        )
                    } else {
                        ImageUtils.displayRoundCornerImageFromUrl(
                            context,
                            thumbnails[0].url,
                            fileThumbnailImage
                        )
                    }
                } else {
                    if (message.type.toLowerCase().contains("gif")) {
                        ImageUtils.displayGifImageFromUrl(
                            context,
                            message.url,
                            fileThumbnailImage,
                            null as String?
                        )
                    } else {
                        ImageUtils.displayRoundCornerImageFromUrl(
                            context,
                            message.url,
                            fileThumbnailImage
                        )
                    }
                }
            }
            if (listener != null) {
                itemView.setOnClickListener { listener.onFileMessageItemClick(message) }
            }
            messageStatusView.drawMessageStatus(channel, message)
        }

        init {
            timeText = itemView.findViewById<View>(R.id.text_group_chat_time) as TextView
            fileThumbnailImage =
                itemView.findViewById<View>(R.id.image_group_chat_file_thumbnail) as ImageView
            fileThumbnailImage.clipToOutline = true
            dateText = itemView.findViewById<View>(R.id.text_group_chat_date) as TextView
            messageStatusView = itemView.findViewById(R.id.message_status_group_chat)
        }
    }

    private inner class OtherImageFileMessageHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var timeText: TextView
        var nicknameText: TextView
        var dateText: TextView
        var profileImage: ImageView
        var fileThumbnailImage: ImageView
        fun bind(
            context: Context?,
            message: FileMessage,
            channel: GroupChannel?,
            isNewDay: Boolean,
            isContinuous: Boolean,
            listener: OnItemClickListener?
        ) {
            timeText.text = DateUtils.formatTime(message.createdAt)

            // Show the date if the message was sent on a different date than the previous message.
            if (isNewDay) {
                dateText.visibility = View.VISIBLE
                dateText.text = DateUtils.formatDate(message.createdAt)
            } else {
                dateText.visibility = View.GONE
            }

            // Hide profile image and nickname if the previous message was also sent by current sender.
            if (isContinuous) {
//                profileImage.setVisibility(View.INVISIBLE);
                nicknameText.visibility = View.GONE
            } else {
//                profileImage.setVisibility(View.VISIBLE);
                ImageUtils.displayRoundCornerImageFromUrl(
                    context,
                    message.sender.profileUrl,
                    profileImage
                )

//                nicknameText.setVisibility(View.VISIBLE);
                nicknameText.text = message.sender.nickname
            }

            // Get thumbnails from FileMessage
            val thumbnails = message.thumbnails as ArrayList<FileMessage.Thumbnail>

            // If thumbnails exist, get smallest (first) thumbnail and display it in the message
            if (thumbnails.size > 0) {
                if (message.type.toLowerCase().contains("gif")) {
                    ImageUtils.displayGifImageFromUrl(
                        context,
                        message.url,
                        fileThumbnailImage,
                        thumbnails[0].url
                    )
                } else {
                    ImageUtils.displayRoundCornerImageFromUrl(
                        context,
                        thumbnails[0].url,
                        fileThumbnailImage
                    )
                }
            } else {
                if (message.type.toLowerCase().contains("gif")) {
                    ImageUtils.displayGifImageFromUrl(
                        context,
                        message.url,
                        fileThumbnailImage,
                        null as String?
                    )
                } else {
                    ImageUtils.displayRoundCornerImageFromUrl(
                        context,
                        message.url,
                        fileThumbnailImage
                    )
                }
            }
            if (listener != null) {
                itemView.setOnClickListener { listener.onFileMessageItemClick(message) }
            }
        }

        init {
            timeText = itemView.findViewById<View>(R.id.text_group_chat_time) as TextView
            nicknameText = itemView.findViewById<View>(R.id.text_group_chat_nickname) as TextView
            fileThumbnailImage =
                itemView.findViewById<View>(R.id.image_group_chat_file_thumbnail) as ImageView
            profileImage = itemView.findViewById<View>(R.id.image_group_chat_profile) as ImageView
            dateText = itemView.findViewById<View>(R.id.text_group_chat_date) as TextView
        }
    }

    /**
     * A ViewHolder for file messages that are videos.
     * Displays only the video thumbnail.
     */
    private inner class MyVideoFileMessageHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var timeText: TextView
        var dateText: TextView
        var fileThumbnailImage: ImageView
        var messageStatusView: MessageStatusView
        fun bind(
            context: Context?,
            message: FileMessage,
            channel: GroupChannel?,
            isNewDay: Boolean,
            isTempMessage: Boolean,
            tempFileMessageUri: Uri?,
            listener: OnItemClickListener?
        ) {
            timeText.text = DateUtils.formatTime(message.createdAt)

            // Show the date if the message was sent on a different date than the previous message.
            if (isNewDay) {
                dateText.visibility = View.VISIBLE
                dateText.text = DateUtils.formatDate(message.createdAt)
            } else {
                dateText.visibility = View.GONE
            }
            if (isTempMessage && tempFileMessageUri != null) {
                ImageUtils.displayRoundCornerImageFromUrl(
                    context,
                    tempFileMessageUri.toString(),
                    fileThumbnailImage
                )
            } else {
                // Get thumbnails from FileMessage
                val thumbnails = message.thumbnails as ArrayList<FileMessage.Thumbnail>

                // If thumbnails exist, get smallest (first) thumbnail and display it in the message
                if (thumbnails.size > 0) {
                    ImageUtils.displayRoundCornerImageFromUrl(
                        context,
                        thumbnails[0].url,
                        fileThumbnailImage
                    )
                }
            }
            if (listener != null) {
                itemView.setOnClickListener { listener.onFileMessageItemClick(message) }
            }
            messageStatusView.drawMessageStatus(channel, message)
        }

        init {
            timeText = itemView.findViewById<View>(R.id.text_group_chat_time) as TextView
            fileThumbnailImage =
                itemView.findViewById<View>(R.id.image_group_chat_file_thumbnail) as ImageView
            dateText = itemView.findViewById<View>(R.id.text_group_chat_date) as TextView
            messageStatusView = itemView.findViewById(R.id.message_status_group_chat)
        }
    }

    private inner class OtherVideoFileMessageHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var timeText: TextView
        var nicknameText: TextView
        var dateText: TextView
        var profileImage: ImageView
        var fileThumbnailImage: ImageView
        fun bind(
            context: Context?,
            message: FileMessage,
            channel: GroupChannel?,
            isNewDay: Boolean,
            isContinuous: Boolean,
            listener: OnItemClickListener?
        ) {
            timeText.text = DateUtils.formatTime(message.createdAt)

            // Show the date if the message was sent on a different date than the previous message.
            if (isNewDay) {
                dateText.visibility = View.VISIBLE
                dateText.text = DateUtils.formatDate(message.createdAt)
            } else {
                dateText.visibility = View.GONE
            }

            // Hide profile image and nickname if the previous message was also sent by current sender.
            if (isContinuous) {
                profileImage.visibility = View.INVISIBLE
                nicknameText.visibility = View.GONE
            } else {
//                profileImage.setVisibility(View.VISIBLE);
                ImageUtils.displayRoundCornerImageFromUrl(
                    context,
                    message.sender.profileUrl,
                    profileImage
                )

//                nicknameText.setVisibility(View.VISIBLE);
                nicknameText.text = message.sender.nickname
            }

            // Get thumbnails from FileMessage
            val thumbnails = message.thumbnails as ArrayList<FileMessage.Thumbnail>

            // If thumbnails exist, get smallest (first) thumbnail and display it in the message
            if (thumbnails.size > 0) {
                ImageUtils.displayRoundCornerImageFromUrl(
                    context,
                    thumbnails[0].url,
                    fileThumbnailImage
                )
            }
            if (listener != null) {
                itemView.setOnClickListener { listener.onFileMessageItemClick(message) }
            }
        }

        init {
            timeText = itemView.findViewById<View>(R.id.text_group_chat_time) as TextView
            nicknameText = itemView.findViewById<View>(R.id.text_group_chat_nickname) as TextView
            fileThumbnailImage =
                itemView.findViewById<View>(R.id.image_group_chat_file_thumbnail) as ImageView
            profileImage = itemView.findViewById<View>(R.id.image_group_chat_profile) as ImageView
            dateText = itemView.findViewById<View>(R.id.text_group_chat_date) as TextView
        }
    }


    private class OtherAudioFileMessageViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var btnPlayPause: ImageView? = null
        var tvDuration: TextView? = null
        var seekBar: SeekBar? = null
        var progressBar: ProgressBar? = null
        //var messageStatusView: MessageStatusView? = null
        var isPlaying = false
        var player: MediaPlayer? = null
        private val format = "%02d:%02d"

        private val mSeekbarUpdateHandler = Handler()
        private val mUpdateSeekbar: Runnable = object : Runnable {
            override fun run() {
                val duration = player!!.duration.toDouble()
                val pos = player!!.currentPosition.toDouble()
                updateDurationTxt(pos.toInt())
                val progressPercent = (pos / duration * 100.0).toInt()
                seekBar?.progress = progressPercent
                mSeekbarUpdateHandler.postDelayed(this, 50)
            }
        }

        fun bind(
            context: Context?,
            message: FileMessage,
            channel: GroupChannel?,
            isNewDay: Boolean,
            listener: OnItemClickListener?
        ) {
            //messageStatusView?.drawMessageStatus(channel, message)
            player = MediaPlayer()
            try {
                player!!.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                player!!.setDataSource(message.url)
                player!!.prepareAsync()
                showLoaderProgress()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        player!!.pause()
                        val playerPosition = (player!!.duration * (progress / 100.0)).toInt()
                        updateDurationTxt(playerPosition)
                        player!!.seekTo(playerPosition)
                        btnPlayPause?.setImageResource(R.drawable.ic_play)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
            player!!.setOnBufferingUpdateListener { mp, percent ->
                seekBar?.secondaryProgress = percent
            }
            player!!.setOnCompletionListener {
                mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar)
                btnPlayPause?.setImageResource(R.drawable.ic_play)
                seekBar?.progress = 100
            }


            player!!.setOnPreparedListener {
                hideLoaderProgress()
                updateDurationTxt(player!!.duration)
                seekBar?.progress = 0
                btnPlayPause?.setImageResource(R.drawable.ic_play)
            }

            btnPlayPause?.setOnClickListener {
                if (!player!!.isPlaying) {
//                    player?.prepareAsync()
//                    showLoaderProgress()
                    player!!.start()
                    mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0)
                    btnPlayPause?.setImageResource(R.drawable.ic_pause_btn)
                } else {
                    player!!.pause()
                    btnPlayPause?.setImageResource(R.drawable.ic_play)
                    mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar)
                }
            }
        }

        private fun updateDurationTxt(playerPosition: Int) {
            val hms = String.format(
                format,
                TimeUnit.MILLISECONDS.toMinutes(playerPosition.toLong()) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(playerPosition.toLong())
                ),
                TimeUnit.MILLISECONDS.toSeconds(
                    playerPosition.toLong()
                ) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(playerPosition.toLong())
                )
            )
            tvDuration?.text = hms
        }

        private fun showLoaderProgress() {
            progressBar?.visibility = View.VISIBLE
            tvDuration?.visibility = View.INVISIBLE
        }

        private fun hideLoaderProgress() {
            progressBar?.visibility = View.INVISIBLE
            tvDuration?.visibility = View.VISIBLE
        }


        fun cleanUp() {
            player!!.release()
            player = null
            mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar)
        }

        init {
            btnPlayPause = itemView.findViewById(R.id.mv_play_pause)
            tvDuration = itemView.findViewById(R.id.tv_duration)
            seekBar = itemView.findViewById(R.id.seekBar)
            //messageStatusView = itemView.findViewById(R.id.message_status_group_chat)
            progressBar = itemView.findViewById(R.id.player_loader)
        }
    }

    private class MeAudioFileMessageViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var btnPlayPause: ImageView? = null
        var tvDuration: TextView? = null
        var seekBar: SeekBar? = null
        var messageStatusView: MessageStatusView? = null
        var progressBar: ProgressBar? = null
        var isPlaying = false
        var progress: Progress? = null
        var player: MediaPlayer? = null
        private val format = "%02d:%02d"
        private val mSeekbarUpdateHandler = Handler()
        private val mUpdateSeekbar: Runnable = object : Runnable {
            override fun run() {
                val duration = player!!.duration.toDouble()
                val pos = player!!.currentPosition.toDouble()
                updateDurationTxt(pos.toInt())
                val progressPercent = (pos / duration * 100.0).toInt()
                seekBar?.progress = progressPercent
                mSeekbarUpdateHandler.postDelayed(this, 50)
            }
        }

        fun bind(
            context: Context?, message: FileMessage, channel: GroupChannel?,
            isNewDay: Boolean, isTempMessage: Boolean, tempFileMessageUri: Uri?,
            isContinuous: Boolean,
            listener: OnItemClickListener?
        ) {
            this.progress = progress
            messageStatusView?.drawMessageStatus(channel, message)
            player = MediaPlayer()

            try {
                if (isTempMessage && tempFileMessageUri != null) {
                    player?.setDataSource(context, tempFileMessageUri)
                    player?.prepareAsync()
                    updateDurationTxt(player!!.duration)
                } else {
                    player?.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    player?.setDataSource(message.url)
                    player?.prepareAsync()
                    showLoaderProgress()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        player!!.pause()
                        btnPlayPause?.setImageResource(R.drawable.ic_play)
                        val playerPosition = (player!!.duration * (progress / 100.0)).toInt()
                        updateDurationTxt(playerPosition)
                        player!!.seekTo(playerPosition)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })

            player!!.setOnBufferingUpdateListener { mp, percent ->
                seekBar?.secondaryProgress = percent
            }


            player!!.setOnCompletionListener {
                mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar)
                btnPlayPause?.setImageResource(R.drawable.ic_play)
                seekBar?.progress = 100
            }

            player!!.setOnPreparedListener {
                //reset
                hideLoaderProgress()
                updateDurationTxt(player!!.duration)
                seekBar?.progress = 0
                btnPlayPause?.setImageResource(R.drawable.ic_play)
            }

            btnPlayPause?.setOnClickListener {
                if (!player!!.isPlaying) {
                    player!!.start()
                    btnPlayPause?.setImageResource(R.drawable.ic_pause_btn)
                    mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0)
                } else {
                    player!!.pause()
                    btnPlayPause?.setImageResource(R.drawable.ic_play)
                    mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar)
                }

            }
        }

        private fun showLoaderProgress() {
            progressBar?.visibility = View.VISIBLE
            tvDuration?.visibility = View.INVISIBLE
        }

        private fun hideLoaderProgress() {
            progressBar?.visibility = View.INVISIBLE
            tvDuration?.visibility = View.VISIBLE
        }

        private fun updateDurationTxt(playerPosition: Int) {
            val hms = String.format(
                format,
                TimeUnit.MILLISECONDS.toMinutes(playerPosition.toLong()) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(playerPosition.toLong())
                ),
                TimeUnit.MILLISECONDS.toSeconds(
                    playerPosition.toLong()
                ) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(playerPosition.toLong())
                )
            )
            tvDuration?.text = hms
        }

        fun cleanUp() {
            player!!.release()
            player = null
            mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar)
        }

        init {
            btnPlayPause = itemView.findViewById(R.id.mv_play_pause)
            tvDuration = itemView.findViewById(R.id.tv_duration)
            seekBar = itemView.findViewById(R.id.seekBar)
            messageStatusView = itemView.findViewById(R.id.message_status_group_chat)
            progressBar = itemView.findViewById(R.id.player_loader)
        }
    }

    companion object {
        const val URL_PREVIEW_CUSTOM_TYPE = "url_preview"
        private const val VIEW_TYPE_USER_MESSAGE_ME = 10
        private const val VIEW_TYPE_USER_MESSAGE_OTHER = 11
        private const val VIEW_TYPE_FILE_MESSAGE_ME = 20
        private const val VIEW_TYPE_FILE_MESSAGE_OTHER = 21
        private const val VIEW_TYPE_FILE_MESSAGE_IMAGE_ME = 22
        private const val VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER = 23
        private const val VIEW_TYPE_FILE_MESSAGE_VIDEO_ME = 24
        private const val VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER = 25
        private const val VIEW_TYPE_FILE_MESSAGE_AUDIO_ME = 26
        private const val VIEW_TYPE_FILE_MESSAGE_AUDIO_OTHER = 27
        private const val VIEW_TYPE_ADMIN_MESSAGE = 30
    }

    init {
        mMessageList = ArrayList()
    }
}
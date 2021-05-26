package com.sendbird.android.sample.main.chat

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.android.*
import com.sendbird.android.sample.R

import com.sendbird.android.sample.utils.*
import kotlin.jvm.Synchronized
import com.sendbird.android.sample.widget.MessageStatusView
import org.json.JSONException
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.HashSet
import com.sendbird.android.sample.utils.SyncManagerUtils
import com.sendbird.android.sample.utils.SyncManagerUtils.findIndexOfMessage
import com.sendbird.android.sample.utils.SyncManagerUtils.getIndexOfMessage


internal class GroupChatAdapter(private var mContext: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mChannel: GroupChannel? = null
    private var mMessageList: MutableList<BaseMessage> = mutableListOf()
    private var mItemClickListener: OnItemClickListener? = null
    private var mItemLongClickListener: OnItemLongClickListener? = null
    private val mTempFileMessageUriTable = Hashtable<String, Uri>()
    private var mFailedMessageList: MutableList<BaseMessage> = mutableListOf()
    private var mResendingMessageSet: MutableSet<String> = mutableSetOf()

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

    fun setContext(context: Context) {
        mContext = context
    }

//    fun load(channelUrl: String) {
//        try {
//            val appDir = File(mContext.cacheDir, SendBird.getApplicationId())
//            appDir.mkdirs()
//            val dataFile = File(
//                appDir,
//                TextUtils.generateMD5(PreferenceUtils.getUserId() + channelUrl) + ".data"
//            )
//            val content = FileUtils.loadFromFile(dataFile)
//            val dataArray = content.split("\n".toRegex()).toTypedArray()
//            mChannel = GroupChannel.buildFromSerializedData(
//                Base64.decode(
//                    dataArray[0], Base64.DEFAULT or Base64.NO_WRAP
//                )
//            ) as GroupChannel
//
//            // Reset message list, then add cached messages.
//            mMessageList.clear()
//            for (i in 1 until dataArray.size) {
//                mMessageList.add(
//                    BaseMessage.buildFromSerializedData(
//                        Base64.decode(
//                            dataArray[i], Base64.DEFAULT or Base64.NO_WRAP
//                        )
//                    )
//                )
//            }
//            notifyDataSetChanged()
//        } catch (e: Exception) {
//            // Nothing to load.
//        }
//    }

//    fun save() {
//        try {
//            val sb = StringBuilder()
//            if (mChannel != null) {
//                // Convert current data into string.
//                sb.append(
//                    Base64.encodeToString(
//                        mChannel!!.serialize(),
//                        Base64.DEFAULT or Base64.NO_WRAP
//                    )
//                )
//                var message: BaseMessage? = null
//                for (i in 0 until Math.min(mMessageList.size, 100)) {
//                    message = mMessageList[i]
//                    if (!isTempMessage(message)) {
//                        sb.append("\n")
//                        sb.append(
//                            Base64.encodeToString(
//                                message.serialize(),
//                                Base64.DEFAULT or Base64.NO_WRAP
//                            )
//                        )
//                    }
//                }
//                val data = sb.toString()
//                val md5 = TextUtils.generateMD5(data)
//
//                // Save the data into file.
//                val appDir = File(mContext.cacheDir, SendBird.getApplicationId())
//                appDir.mkdirs()
//                val hashFile = File(
//                    appDir,
//                    TextUtils.generateMD5(PreferenceUtils.getUserId() + mChannel!!.url) + ".hash"
//                )
//                val dataFile = File(
//                    appDir,
//                    TextUtils.generateMD5(PreferenceUtils.getUserId() + mChannel!!.url) + ".data"
//                )
//                try {
//                    val content = FileUtils.loadFromFile(hashFile)
//                    // If data has not been changed, do not save.
//                    if (md5 == content) {
//                        return
//                    }
//                } catch (e: IOException) {
//                    // File not found. Save the data.
//                }
//                FileUtils.saveToFile(dataFile, data)
//                FileUtils.saveToFile(hashFile, md5)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

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
            else -> {
            }
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
                return
            }
        }
    }

    fun removeFailedMessage(message: BaseMessage) {
        mTempFileMessageUriTable.remove((message as FileMessage).requestId)
        mMessageList.remove(message)
        notifyDataSetChanged()
    }

    fun markMessageSent(message: BaseMessage?) {
        var msg: BaseMessage
        for (i in mMessageList.indices.reversed()) {
            msg = mMessageList[i]
            if (message is UserMessage && msg is UserMessage) {
                if (msg.requestId == message.requestId) {
                    mMessageList[i] = message
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
                    return
                }
            }
        }
    }

    fun addTempFileMessageInfo(message: FileMessage, uri: Uri) {
        mTempFileMessageUriTable[message.requestId] = uri
    }

    fun insertSucceededMessages(messages: List<BaseMessage?>) {
        for (message in messages) {
            val index = findIndexOfMessage(
                mMessageList,
                message!!
            )
            mMessageList.add(index, message)
            //notifyItemChanged(index)
        }
        notifyItemChanged(itemCount-1)
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
        notifyDataSetChanged()
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
        notifyDataSetChanged()
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
        notifyDataSetChanged()
    }

    fun removeFailedMessages(messages: List<BaseMessage?>) {
        synchronized(mFailedMessageList) {
            for (message in messages) {
                val requestId = getRequestId(message!!)
                mResendingMessageSet.remove(requestId)
                mFailedMessageList.remove(message)
            }
        }
        notifyDataSetChanged()
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
    }

    fun getLastReadPosition(lastRead: Long): Int {
//        for (i in mMessageList.indices) {
//            if (mMessageList[i].createdAt == lastRead) {
//                return i + mFailedMessageList.size
//            }
//        }
        return 0
    }

    /**
     * Load old message list.
     *
     * @param limit
     * @param handler
     */
//    fun loadPreviousMessages(limit: Int, handler: BaseChannel.GetMessagesHandler?) {
//        if (mChannel == null) {
//            return
//        }
//        if (isMessageListLoading) {
//            return
//        }
//        var oldestMessageCreatedAt = Long.MAX_VALUE
//        if (mMessageList.size > 0) {
//            oldestMessageCreatedAt = mMessageList[mMessageList.size - 1].createdAt
//        }
//        isMessageListLoading = true
//        mChannel!!.getPreviousMessagesByTimestamp(
//            oldestMessageCreatedAt,
//            false,
//            limit,
//            true,
//            BaseChannel.MessageTypeFilter.ALL,
//            null,
//            object : BaseChannel.GetMessagesHandler {
//                override fun onResult(list: List<BaseMessage>, e: SendBirdException?) {
//                    handler?.onResult(list, e)
//                    isMessageListLoading = false
//                    if (e != null) {
//                        e.printStackTrace()
//                        return
//                    }
//                    for (message in list) {
//                        mMessageList.add(message)
//                    }
//                    notifyDataSetChanged()
//                }
//            })
//    }
//
//    /**
//     * Replaces current message list with new list.
//     * Should be used only on initial load or refresh.
//     */
//    fun loadLatestMessages(limit: Int, handler: BaseChannel.GetMessagesHandler?) {
//        if (mChannel == null) {
//            return
//        }
//        if (isMessageListLoading) {
//            return
//        }
//        isMessageListLoading = true
//        mChannel!!.getPreviousMessagesByTimestamp(
//            Long.MAX_VALUE,
//            true,
//            limit,
//            true,
//            BaseChannel.MessageTypeFilter.ALL,
//            null,
//            object : BaseChannel.GetMessagesHandler {
//                override fun onResult(list: MutableList<BaseMessage>, e: SendBirdException?) {
//                    handler?.onResult(list, e)
//                    isMessageListLoading = false
//                    if (e != null) {
//                        e.printStackTrace()
//                        return
//                    }
//                    if (list.size <= 0) {
//                        return
//                    }
//                    for (message in mMessageList) {
//                        if (isTempMessage(message) || isFailedMessage(message)) {
//                            list.add(0, message)
//                        }
//                    }
//                    mMessageList.clear()
//                    for (message in list) {
//                        mMessageList.add(message)
//                    }
//                    notifyDataSetChanged()
//                }
//            })
//    }

    fun setItemLongClickListener(listener: OnItemLongClickListener?) {
        mItemLongClickListener = null
    }

    fun setItemClickListener(listener: OnItemClickListener?) {
        mItemClickListener = listener
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
        private const val VIEW_TYPE_ADMIN_MESSAGE = 30
    }

    init {
        mMessageList = ArrayList()
    }
}
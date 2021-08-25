package com.sendbird.android.sample.main.chat

//import com.sendbird.android.sample.utils.GenericDialog.newInstance
//import com.sendbird.android.sample.utils.GenericDialog.setTitle
//import com.sendbird.android.sample.utils.GenericDialog.setMessage
//import com.sendbird.android.sample.utils.GenericDialog.setUploadFile
//import com.sendbird.android.sample.utils.BaseBottomSheetDialog.show

import android.Manifest
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
//import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.sendbird.android.*
import com.sendbird.android.sample.R
import com.sendbird.android.sample.groupchannel.GroupChannelListFragment
import com.sendbird.android.sample.main.scheduler.ScheduleManager
import com.sendbird.android.sample.main.scheduler.SessionStoreManager
import com.sendbird.android.sample.main.service.EndChatService
import com.sendbird.android.sample.main.worker.WorkRequestManager
import com.sendbird.android.sample.network.NetworkRequest
import com.sendbird.android.sample.utils.*
import com.sendbird.android.sample.utils.WebUtils.UrlPreviewAsyncTask
import com.sendbird.syncmanager.FailedMessageEventActionReason
import com.sendbird.syncmanager.MessageCollection
import com.sendbird.syncmanager.MessageEventAction
import com.sendbird.syncmanager.MessageFilter
import com.sendbird.syncmanager.handler.CompletionHandler
import com.sendbird.syncmanager.handler.MessageCollectionCreateHandler
import com.sendbird.syncmanager.handler.MessageCollectionHandler
import kotlinx.android.synthetic.main.fragment_group_chat.*
import org.json.JSONException
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class GroupChatFragment : Fragment() {
    private lateinit var smoothScroller: LinearSmoothScroller
    private var countDownTimer: CountDownTimer? = null
    private val groupChatFragmentArgs: GroupChatFragmentArgs by navArgs()
    private var mIMM: InputMethodManager? = null
    private var mRootLayout: ConstraintLayout? = null
    private var mRecyclerView: RecyclerView? = null
    private var mChatAdapter: GroupChatAdapter? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private var mMessageEditText: EditText? = null
    private var mRecordVoiceButton: ImageView? = null
    private var mProfileImage: ImageView? = null
    private var mUploadFileButton: ImageButton? = null
    private var toolbar_group_channel: Toolbar? = null
    private var timerFrame: FrameLayout? = null
    private var timerTv: TextView? = null
    private var mCurrentEventText: TextView? = null
    private var mUserName: TextView? = null
    private var endChatTv: TextView? = null
    private var mChannel: GroupChannel? = null
    private val mChannelUrl: String by lazy { groupChatFragmentArgs.channelUrl }
    private val mPrevMessageListQuery: PreviousMessageListQuery? = null
    private var mIsTyping = false
    private var mCurrentState = STATE_NORMAL
    private var mEditingMessage: BaseMessage? = null
    private var mChatBox: ConstraintLayout? = null
    private val viewOnly: Boolean by lazy { !groupChatFragmentArgs.isActive }
    var groupChatEventListener: GroupChatClickListener? = null
    private var recordVoice: Boolean = true
    private val FORMAT = "%02d:%02d"
    private var mLastRead: Long = 0
    private var mMessageCollection: MessageCollection? = null
    private var progressBar3: ProgressBar? = null
    private var isRecording = false
    private val recordPermission = arrayOf(Manifest.permission.RECORD_AUDIO)
    private val RECORD_AUDIO_REQUEST_CODE = 1000
    private var mMediaRecorder: MediaRecorder? = null
    private var recordGroup: Group? = null
    private var textGroup: Group? = null
    private var icVoice: ImageView? = null
    private var icVoice1: ImageView? = null
    private var icVoice2: ImageView? = null
    private var cancelRecordTv: TextView? = null
    private var newFile: File? = null
    private var timerStarted: Boolean = false

    val mMessageFilter = MessageFilter(BaseChannel.MessageTypeFilter.ALL, null, null)

    private val uploadFileDialog = GenericDialog().newInstance(TextUtils.THEME_MATH)
        .setTitle("Upload a file")
        .setMessage(R.string.empty)

    private val endChatSession = GenericDialog().newInstance(TextUtils.THEME_MATH)
        .setTitle("End chat session?")
        .setMessage("Are you sure you want to end the chat session?")


    private val endChatSessionTimeOut = GenericDialog().newInstance(TextUtils.THEME_MATH)
        .setTitle("Time Up!")
        .setMessage("Ooops! time is up")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        smoothScroller = object : LinearSmoothScroller(requireContext()) {
            override fun getVerticalSnapPreference(): Int {
                return LinearSmoothScroller.SNAP_TO_START
            }
        }


        mIMM =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        Log.d(LOG_TAG, mChannelUrl)
        mLastRead = PreferenceUtils.getLastRead(mChannelUrl);
        mChatAdapter = GroupChatAdapter(requireContext())
        setUpChatListAdapter()

        // Load messages from cache.
        //mChatAdapter!!.load(mChannelUrl!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_group_chat, container, false)

        retainInstance = true
        mRootLayout = rootView.findViewById(R.id.layout_group_chat_root)
        mRecyclerView = rootView.findViewById(R.id.recycler_group_chat)
        mUserName = rootView.findViewById(R.id.userName)
        endChatTv = rootView.findViewById(R.id.endChatTv)
        toolbar_group_channel = rootView.findViewById(R.id.toolbar_group_channel)
        mCurrentEventText = rootView.findViewById(R.id.text_group_chat_current_event)
        mMessageEditText = rootView.findViewById(R.id.edittext_group_chat_message)
        mChatBox = rootView.findViewById(R.id.layout_group_chat_chatbox)
        mRecordVoiceButton = rootView.findViewById(R.id.button_record_voice)
        mUploadFileButton = rootView.findViewById(R.id.button_group_chat_upload)
        timerFrame = rootView.findViewById(R.id.end_timer)
        timerTv = rootView.findViewById(R.id.end_timer_tv)
        mProfileImage = rootView.findViewById(R.id.profile_image)
        progressBar3 = rootView.findViewById(R.id.progressBar3)
        textGroup = rootView.findViewById(R.id.text_group)
        recordGroup = rootView.findViewById(R.id.record_group)
        cancelRecordTv = rootView.findViewById(R.id.cancel_record_txt)
        mUploadFileButton = rootView.findViewById(R.id.button_group_chat_upload)
        icVoice = rootView.findViewById(R.id.ic_voice)
        icVoice1 = rootView.findViewById(R.id.ic_voice1)
        icVoice2 = rootView.findViewById(R.id.ic_voice2)

        toolbar_group_channel?.setNavigationOnClickListener(View.OnClickListener { view: View? ->
            goBack()
        })

        cancelRecordTv?.setOnClickListener {
            recordVoice()
            newFile?.delete()
            mRecordVoiceButton?.setImageResource(R.drawable.ic_mic)
        }

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    goBack()
                }
            })


        //todo remove this shit
//        PreferenceUtils.saveUlessonApiToken("405|Ceyu7796mFfn1rL3VgT6k75iGgvLw7xQ83FWoHPZ")
//        PreferenceUtils.saveDeviceId("jhghyjhggjujghj")

        if (viewOnly) {
            mChatBox?.visibility = View.GONE
            timerFrame?.visibility = View.GONE
            endChatTv?.visibility = View.GONE
        }


        mMessageEditText?.setOnEditorActionListener(TextView.OnEditorActionListener { textView: TextView?, actionId: Int, keyEvent: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendTextMessage()
            }
            false
        })

        mUploadFileButton?.setOnClickListener(View.OnClickListener { v: View? -> pickMedia() })
        mIsTyping = false
        mMessageEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!mIsTyping) {
                    setTypingStatus(true)
                }
                if (s.length == 0) {
                    setTypingStatus(false)
                }

                val buttonIcon = if (s.isNotEmpty()) {
                    recordVoice = false
                    R.drawable.ic_send_btn
                } else {
                    recordVoice = true
                    R.drawable.ic_mic
                }
                mRecordVoiceButton?.setImageResource(buttonIcon)
            }

            override fun afterTextChanged(s: Editable) {}
        })

        mRecordVoiceButton?.setOnClickListener {
            if (recordVoice) {
                if (!isRecording) {
                    recordVoice()
                } else {
                    voiceView(false)
                    isRecording = false
                    animateVoice(false)

                    //stop recording
                    try {
                        mMediaRecorder?.stop()
                        mMediaRecorder?.release()
                        mMediaRecorder = null
                        sendRecordedFileToSendBird()
                        mRecordVoiceButton?.setImageResource(R.drawable.ic_mic)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        //FirebaseCrashlytics.getInstance().recordException(e)
                    }
                }
            } else {
                sendTextMessage()
            }
        }

        endChatTv?.setOnClickListener {
            setUpEndChatClickListeners()
        }

        setHasOptionsMenu(true)

        createMessageCollection(mChannelUrl)

        return rootView
    }


    private fun sendRecordedFileToSendBird() {
        val uri = FileProvider.getUriForFile(
            requireContext(), requireContext().packageName + ".theprovider",
            newFile!!
        )
        sendFileWithThumbnail(uri)
    }


    private fun goBack() {
        when (groupChatFragmentArgs.entryPoint) {
            DASHBOARD -> {
                //show navigate to dashboard
                try {
                    findNavController().navigate(Uri.parse(groupChatFragmentArgs.deeplinkUrl))
                } catch (e: Exception) {
                    e.printStackTrace()
                    activity?.finish()
                    //FirebaseCrashlytics.getInstance().recordException(e)
                }

            }

            CHAT_LIST -> {
                try {
                    findNavController().navigate(Uri.parse(groupChatFragmentArgs.deeplinkUrl))
                } catch (e: Exception) {
                    e.printStackTrace()
                    activity?.finish()
                    //FirebaseCrashlytics.getInstance().recordException(e)
                }
            }

            else -> {
                findNavController().popBackStack()
            }
        }
    }

    private fun setUpEndChatClickListeners() {
        endChatSession.setPositiveButton(
            R.string.yes,
            R.drawable.bg_chat_btn
        ) {
            endChatSession.dismiss()
            endChat {
                showSessionEndFragment()
            }
        }.show(requireFragmentManager(), "")

        endChatSession.setNegativeButton(R.string.no, null) {
            endChatSession.dismiss()
        }
    }

    override fun onStop() {
        super.onStop()
        countDownTimer?.cancel()
        mRecyclerView?.adapter = null
    }


    private fun endChat(completed: () -> Unit) {
        //yyyy-MM-dd HH:mm:ss
        val dateMillis = System.currentTimeMillis()
        val date = Date(dateMillis)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val dateString = simpleDateFormat.format(date)

        progressBar3?.visibility = View.VISIBLE
        disableChatViews()


        mChannel?.let { channel ->
            var questionId = (channel.data.toMutableMap()["questionId"] ?: "").toInt()

            NetworkRequest().endChat(questionId, dateString, channel.url,
                success = {
                    progressBar3?.visibility = View.GONE
                    val questionDetailsMap = channel.data.toMutableMap()

                    val temp = questionDetailsMap["active"] ?: ""

                    val strData = if (temp.isBooleanString()) {
                        //v1
                        questionDetailsMap["active"] = false.toString()
                        questionDetailsMap.toString()
                    } else {
                        //v2
                        questionDetailsMap["active"] = "past"
                        val gson = Gson()
                        gson.toJson(questionDetailsMap)
                    }

//                    val strData = if (temp is String) {
//                        questionDetailsMap.toString()
//                    } else {
//                        val gson = Gson()
//                        gson.toJson(questionDetailsMap)
//                    }


                    channel.updateChannel(channel.name, channel.coverUrl, strData) { _, e ->
                        if (e != null) {
                            e.printStackTrace()
                            return@updateChannel
                        }
                        completed.invoke()
                    }
                },

                error = {
                    Log.e(EndChatService::class.java.simpleName, it)
                    progressBar3?.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "An error occurred while trying to end chat...",
                        Toast.LENGTH_SHORT
                    ).show()

                    WorkRequestManager.enQueueWork(requireContext(), questionId, channel.url, 0)

                    completed.invoke()
                })
        }

    }


    private fun disableChatViews() {
        mChatBox?.visibility = View.GONE
        timerFrame?.visibility = View.GONE
        endChatTv?.visibility = View.GONE
    }


    private fun showSessionEndFragment() {
        countDownTimer?.cancel()

        try {
            val direction =
                GroupChatFragmentDirections
                    .actionNavGraphToChatEndSessionFragment(groupChatFragmentArgs.deeplinkUrl)

            findNavController().navigate(direction)

        } catch (e: Exception) {
            e.printStackTrace()
            //FirebaseCrashlytics.getInstance().recordException(e)
            activity?.finish()
        }
    }

    private fun countTime(minute: Long, timerMillis: Long) {
        if (minute != 0L) {
            timer(minute, timerMillis)
        } else {
            activity?.finish()
        }

        timerFrame!!.visibility = View.VISIBLE
        if (minute == 5L && !viewOnly) {
            timerFrame!!.visibility = View.VISIBLE
        }
    }

    private fun timer(minute: Long, millis: Long) {
        if (countDownTimer != null) {
            countDownTimer?.cancel()
        }


        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                val mins = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                if (mins <= 4 && !viewOnly) {
                    timerFrame!!.visibility = View.VISIBLE
                }

                val timeStr = "" + String.format(
                    FORMAT,
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                    )
                )

                Log.i("TIME: ", timeStr)

                timerTv!!.text = timeStr
            }

            override fun onFinish() {
                timerTv!!.text = "00:00"
                endChatSessionTimeOut()
            }
        }.start()
    }


    private fun endChatSessionTimeOut() {

        endChat {
            activity?.supportFragmentManager?.let {
                endChatSessionTimeOut.setPositiveButton(R.string.close, R.drawable.bg_chat_btn) {
                    endChatSessionTimeOut.dismiss()
                    showSessionEndFragment()
                }.show(it, "s")
            }
        }
    }


    private fun sendTextMessage() {
        val userInput = mMessageEditText!!.text.toString()
        if (mCurrentState == STATE_EDIT) {
            if (userInput.isNotEmpty()) {
                if (mEditingMessage != null) {
                    editMessage(mEditingMessage!!, userInput)
                }
            }
            setState(STATE_NORMAL, null, -1)
        } else {
            if (userInput.isNotEmpty()) {
                sendUserMessage(userInput)
                mMessageEditText!!.setText("")
            }
        }
    }


    private fun setUpUIChannelElements() {
        mChannel?.let { channel ->
            val questionDetailsMap = channel.data.toMutableMap()

            try {
                mUserName?.text = (questionDetailsMap["studentName"] as String)
                    .split(" ")[0]

                Glide.with(this).load(questionDetailsMap["studentAvatar"])
                    .error(R.drawable.profile_thumbnail)
                    .placeholder(R.drawable.profile_thumbnail)
                    .into(mProfileImage!!)
            } catch (e: Exception) {
                e.printStackTrace()
                //FirebaseCrashlytics.getInstance().recordException(e)
            }

        }
    }


    /**
     * This method controls the chat timing
     */
    private fun extractAndStartTimer() {
        mChannel?.let { channel ->

            val map = channel.data.toMutableMap()
            val questionId = (map["questionId"] ?: "").toInt()

            val shMgr = ScheduleManager.getInstance(SessionStoreManager(requireContext()))
                .apply {
                    this.questionId = questionId
                    channelUrl = mChannelUrl
                }

            val startTime = try {
                map["startTime"].toString()
            } catch (e: Exception) {
                e.printStackTrace()
                //FirebaseCrashlytics.getInstance().recordException(e)
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(System.currentTimeMillis()))
            }


            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .parse(startTime)

            val dateNow = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(System.currentTimeMillis())

            val nowMillis = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .parse(dateNow)

            val elapse = nowMillis.time - date.time
            val elapseMins = TimeUnit.MILLISECONDS.toMinutes(elapse)
            val chatDuration = (map["chatDuration"] ?: "").toInt()
            val chatDurationMillis = TimeUnit.MINUTES.toMillis(chatDuration.toLong())


            val countDown = chatDurationMillis - abs(elapse)

            val countDownMillis = /*TimeUnit.MINUTES.toMillis(countDown)*/ countDown

            //use this value for the timer
            val timerMillis = TimeUnit.MINUTES.toMillis(chatDuration.toLong()) - abs(elapse)
            countTime(countDown, timerMillis)

            if (countDownMillis > 0) {
                shMgr.scheduleEndChat(countDownMillis)
            }

        }
    }

    private fun startChatTimer() {
        mChannel?.let { channel ->
            if (!viewOnly) {
                extractAndStartTimer()
            }
        }
    }


    override fun onResume() {
        super.onResume()

        if (mChannel != null) {
            startChatTimer()
        } else {

            GroupChannel.getChannel(groupChatFragmentArgs.channelUrl) { channel: GroupChannel?, error: SendBirdException? ->
                if (error != null) {
                    error.printStackTrace()
                    return@getChannel
                }

                mChannel = channel

                startChatTimer()
            }
        }

        fetchInitialMessages()
        //ConnectionManager.addConnectionManagementHandler(CONNECTION_HANDLER_ID) { refresh() }
        mChatAdapter!!.setContext(requireContext()) // Glide bug fix (java.lang.IllegalArgumentException: You cannot start a load for a destroyed activity)

        setUpRecyclerView()
        // Gets channel from URL user requested
        Log.d(LOG_TAG, mChannelUrl)


        SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, object : SendBird.ConnectionHandler {
            override fun onReconnectStarted() {
                //Toast.makeText(requireContext(), "-- RECONNECTED --", Toast.LENGTH_SHORT).show()
            }

            override fun onReconnectSucceeded() {
                if (mMessageCollection != null) {
                    if (mLayoutManager!!.findFirstVisibleItemPosition() <= 0) {
                        mMessageCollection!!.fetchAllNextMessages { hasMore, e -> }
                    }
                    if (mLayoutManager!!.findLastVisibleItemPosition() == mChatAdapter!!.itemCount - 1) {
                        mMessageCollection!!.fetchSucceededMessages(
                            MessageCollection.Direction.PREVIOUS
                        ) { hasMore, e -> }
                    }
                }
            }

            override fun onReconnectFailed() {
                //Toast.makeText(requireContext(), "-- FAILED --", Toast.LENGTH_SHORT).show()
            }
        })

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, object : SendBird.ChannelHandler() {
            override fun onMessageReceived(baseChannel: BaseChannel, baseMessage: BaseMessage) {
//                if (baseChannel.url == mChannelUrl) {
//                    mChatAdapter!!.markAllMessagesAsRead()
//                    // Add new message to view
//                    mChatAdapter!!.addFirst(baseMessage)
//                }
            }

            override fun onMessageDeleted(baseChannel: BaseChannel, msgId: Long) {
                super.onMessageDeleted(baseChannel, msgId)
//                if (baseChannel.url == mChannelUrl) {
//                    mChatAdapter!!.delete(msgId)
//                }
            }

            /* override fun onMessageUpdated(channel: BaseChannel, message: BaseMessage) {
                 super.onMessageUpdated(channel, message)
                 if (channel.url == mChannelUrl) {
                     mChatAdapter!!.update(message)
                 }
             }*/

            override fun onReadReceiptUpdated(channel: GroupChannel) {
                if (channel.url == mChannelUrl) {
                    mChatAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onTypingStatusUpdated(channel: GroupChannel) {
                if (channel.url == mChannelUrl) {
                    val typingUsers = channel.typingMembers
                    displayTyping(typingUsers)
                }
            }

            override fun onDeliveryReceiptUpdated(channel: GroupChannel) {
                if (channel.url == mChannelUrl) {
                    mChatAdapter!!.notifyDataSetChanged()
                }
            }
        })
    }

    override fun onPause() {
        setTypingStatus(false)
        //ConnectionManager.removeConnectionManagementHandler(CONNECTION_HANDLER_ID)
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID)
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID)
        super.onPause()
    }

    override fun onDestroy() {
        // Save messages to cache.
        if (mMessageCollection != null) {
            mMessageCollection?.setCollectionHandler(null);
            mMessageCollection?.remove();
        }

        //Disconnect from sendbird

        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_CHANNEL_URL, mChannelUrl)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Set this as true to restore background connection management.
        SendBird.setAutoBackgroundDetection(true)
        if (requestCode == MediaUtils.MEDIA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            sendFileWithThumbnail(data!!.data)
            uploadFileDialog.dismiss()
        }
        if (requestCode == INTENT_REQUEST_CHOOSE_MEDIA && resultCode == Activity.RESULT_OK) {
            // If user has successfully chosen the image, show a dialog to confirm upload.
            if (data == null) {
                Log.d(LOG_TAG, "data is null!")
                return
            }
            sendFileWithThumbnail(data.data)
        }
    }

    private fun setUpRecyclerView() {
        if (activity != null) {
            mLayoutManager = LinearLayoutManager(requireContext())
            mLayoutManager!!.reverseLayout = true
            mLayoutManager?.stackFromEnd = true
            mRecyclerView!!.layoutManager = mLayoutManager
            mRecyclerView!!.setItemViewCacheSize(30)
            mRecyclerView!!.adapter = mChatAdapter

            mRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        if (mLayoutManager!!.findFirstVisibleItemPosition() == 0) {
                            mMessageCollection?.fetchSucceededMessages(
                                MessageCollection.Direction.NEXT,
                                null
                            )
                        }
                        if (mLayoutManager!!.findLastVisibleItemPosition() == mChatAdapter!!.itemCount - 1) {
                            mMessageCollection?.fetchSucceededMessages(
                                MessageCollection.Direction.PREVIOUS,
                                null
                            )
                        }
                    }
                }
            })
        }
    }

    private fun setUpChatListAdapter() {
        mChatAdapter!!.setItemClickListener(object : GroupChatAdapter.OnItemClickListener {
            override fun onUserMessageItemClick(message: UserMessage?) {
                // Restore failed message and remove the failed message from list.
                message?.let {
                    if (mChatAdapter!!.isFailedMessage(message)) {
                        retryFailedMessage(message)
                        return
                    }

                }


                // Message is sending. Do nothing on click event.
                if (mChatAdapter!!.isTempMessage(message)) {
                    return
                }


                if (message!!.data.isNotEmpty()) {
                    val i = Intent(activity, PhotoViewerActivity::class.java)
                    i.putExtra("url", message.data)
                    i.putExtra("type", "network")
                    startActivity(i)
                    return
                }


                if (message?.customType == GroupChatAdapter.URL_PREVIEW_CUSTOM_TYPE) {
                    try {
                        val info = UrlPreviewInfo(message.data)
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(info.url))
                        startActivity(browserIntent)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        //FirebaseCrashlytics.getInstance().recordException(e)
                    }
                }
            }

            override fun onFileMessageItemClick(message: FileMessage?) {
                // Load media chooser and remove the failed message from list.
                message?.let {
                    if (mChatAdapter!!.isFailedMessage(message)) {
                        retryFailedMessage(message)
                        return
                    }
                }


                // Message is sending. Do nothing on click event.
                if (mChatAdapter!!.isTempMessage(message)) {
                    return
                }
                onFileMessageClicked(message!!)
            }
        })
        mChatAdapter!!.setItemLongClickListener(object : GroupChatAdapter.OnItemLongClickListener {
            override fun onUserMessageItemLongClick(message: UserMessage?, position: Int) {
                if (message?.sender?.userId == PreferenceUtils.getUserId()) {
                    showMessageOptionsDialog(message!!, position)
                }
            }

            override fun onFileMessageItemLongClick(message: FileMessage?) {}
            override fun onAdminMessageItemLongClick(message: AdminMessage?) {}
        })

        //mChatAdapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        mChatAdapter?.setProgressListener(object : GroupChatAdapter.Progress {
            override fun onProgressChange(position: Int) {
                mRecyclerView?.scrollToPosition(position)
            }

        })
    }

    private fun showMessageOptionsDialog(message: BaseMessage, position: Int) {
        val options = arrayOf("Edit message", "Delete message")
        val builder = AlertDialog.Builder(
            requireContext()
        )
        builder.setItems(options) { dialog, which ->
            if (which == 0) {
                setState(STATE_EDIT, message, position)
            } else if (which == 1) {
                deleteMessage(message)
            }
        }
        builder.create().show()
    }

    private fun setState(state: Int, editingMessage: BaseMessage?, position: Int) {
        when (state) {
            STATE_NORMAL -> {
                mCurrentState = STATE_NORMAL
                mEditingMessage = null
                mUploadFileButton!!.visibility = View.VISIBLE
                mMessageEditText!!.setText("")
            }
            STATE_EDIT -> {
                mCurrentState = STATE_EDIT
                mEditingMessage = editingMessage
                mUploadFileButton!!.visibility = View.GONE
                var messageString = (editingMessage as UserMessage?)!!.message
                if (messageString == null) {
                    messageString = ""
                }
                mMessageEditText!!.setText(messageString)
                if (messageString.isNotEmpty()) {
                    mMessageEditText!!.setSelection(0, messageString.length)
                }
                mMessageEditText!!.requestFocus()
                mMessageEditText!!.postDelayed({
                    mIMM!!.showSoftInput(mMessageEditText, 0)
                    mRecyclerView!!.postDelayed({ mRecyclerView!!.scrollToPosition(position) }, 500)
                }, 100)
            }
        }
    }

    private fun fetchInitialMessages() {
        if (mMessageCollection == null) {
            return
        }

        //Fetch succeeded past messages
        mMessageCollection!!.fetchSucceededMessages(
            MessageCollection.Direction.PREVIOUS
        ) { hasMore, e ->

        }

        mMessageCollection!!.fetchSucceededMessages(
            MessageCollection.Direction.NEXT
        ) { hasMore, e ->

        }

        mMessageCollection!!.fetchFailedMessages(object : CompletionHandler {
            override fun onCompleted(e: SendBirdException?) {
                if (activity == null) {
                    return
                }
                activity!!.runOnUiThread {
                    mChatAdapter!!.markAllMessagesAsRead()
                    smoothScroller.targetPosition = 0

                    mLayoutManager?.startSmoothScroll(smoothScroller)
                }
            }
        })
    }

    private fun createMessageCollection(channelUrl: String) {
        GroupChannel.getChannel(channelUrl, object : GroupChannel.GroupChannelGetHandler {
            override fun onResult(groupChannel: GroupChannel?, e: SendBirdException?) {

                if (groupChannel == null) {
                    Toast.makeText(
                        requireContext(),
                        "An error occurred, try again",
                        Toast.LENGTH_SHORT
                    ).show()

                    goBack()
                    return
                }


                if (e != null) {
                    Toast.makeText(
                        requireContext(),
                        "An error occurred, try again \n ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                    goBack()
                    return

                    /*MessageCollection.create(channelUrl, mMessageFilter, Long.MAX_VALUE,
                        MessageCollectionCreateHandler { messageCollection: MessageCollection?, e: SendBirdException? ->
                            if (e == null) {
                                if (mMessageCollection != null) {
                                    mMessageCollection?.remove()
                                }
                                mMessageCollection = messageCollection
                                mMessageCollection?.setCollectionHandler(mMessageCollectionHandler)
                                mChannel = mMessageCollection?.channel
                                mChatAdapter!!.setChannel(mChannel)
                                if (activity == null) {
                                    return@MessageCollectionCreateHandler
                                }
                                activity!!.runOnUiThread {
                                    mChatAdapter?.clear()
                                    setUpUIChannelElements()
                                }
                                fetchInitialMessages()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to get message",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Handler().postDelayed(Runnable { goBack() }, 1000)
                            }
                        })*/
                } /*else {

                }*/

                if (mMessageCollection != null) {
                    mMessageCollection?.remove()
                }
                mMessageCollection = MessageCollection(groupChannel, mMessageFilter, mLastRead)
                mMessageCollection?.setCollectionHandler(mMessageCollectionHandler)
                mChannel = groupChannel
                setUpUIChannelElements()
                mChatAdapter!!.setChannel(mChannel)
                mChatAdapter?.clear()
                updateActionBarTitle()
                fetchInitialMessages()

            }
        })
    }

    private fun updateLastSeenTimestamp(messages: List<BaseMessage?>) {
        var lastSeenTimestamp = if (mLastRead == Long.MAX_VALUE) 0 else mLastRead
        for (message in messages) {
            if (lastSeenTimestamp < message?.createdAt ?: 0) {
                lastSeenTimestamp = message?.createdAt ?: 0
            }
        }
        if (lastSeenTimestamp > mLastRead) {
            PreferenceUtils.setLastRead(mChannelUrl, lastSeenTimestamp)
            mLastRead = lastSeenTimestamp
        }
    }


    private val mMessageCollectionHandler: MessageCollectionHandler =
        object : MessageCollectionHandler() {
            override fun onMessageEvent(
                collection: MessageCollection?,
                messages: List<BaseMessage?>?,
                action: MessageEventAction?
            ) {
            }

            override fun onSucceededMessageEvent(
                collection: MessageCollection?,
                messages: List<BaseMessage?>,
                action: MessageEventAction
            ) {
                Log.d(
                    "SyncManager",
                    "onSucceededMessageEvent: size = " + messages.size + ", action = " + action
                )
                if (activity == null) {
                    return
                }
                activity!!.runOnUiThread {
                    when (action) {
                        MessageEventAction.INSERT -> {
                            mChatAdapter?.insertSucceededMessages(messages)
                            mChatAdapter!!.markAllMessagesAsRead()
                            val lastReadPos = mChatAdapter!!.itemCount
                            smoothScroller.targetPosition = 0
                            //mChatAdapter!!.getLastReadPosition(mLastRead)
                            mLayoutManager?.startSmoothScroll(smoothScroller)
                        }
                        MessageEventAction.REMOVE -> mChatAdapter?.removeSucceededMessages(messages)
                        MessageEventAction.UPDATE -> mChatAdapter?.updateSucceededMessages(messages)
                        MessageEventAction.CLEAR -> mChatAdapter?.clear()
                    }
                }
                updateLastSeenTimestamp(messages)
            }

            override fun onPendingMessageEvent(
                collection: MessageCollection?,
                messages: List<BaseMessage>,
                action: MessageEventAction
            ) {
                Log.d(
                    "SyncManager",
                    "onPendingMessageEvent: size = " + messages.size + ", action = " + action
                )
                if (activity == null) {
                    return
                }
                activity!!.runOnUiThread {
                    when (action) {
                        MessageEventAction.INSERT -> {
                            val pendingMessages: MutableList<BaseMessage> =
                                ArrayList()
                            for (message in messages) {
                                if (!mChatAdapter!!.failedMessageListContains(message)) {
                                    pendingMessages.add(message)
                                }
                            }
                            mChatAdapter?.insertSucceededMessages(pendingMessages)
                            smoothScroller.targetPosition = 0
                            //mChatAdapter!!.getLastReadPosition(mLastRead);
                            mLayoutManager?.startSmoothScroll(smoothScroller);
                        }
                        MessageEventAction.REMOVE -> mChatAdapter?.removeSucceededMessages(messages)
                    }
                }
            }

            override fun onFailedMessageEvent(
                collection: MessageCollection?,
                messages: List<BaseMessage?>,
                action: MessageEventAction?,
                reason: FailedMessageEventActionReason?
            ) {

                Log.d(
                    "SyncManager",
                    "onFailedMessageEvent: size = " + messages.size + ", action = " + action
                )
                if (activity == null) {
                    return
                }
                activity!!.runOnUiThread {
                    when (action) {
                        MessageEventAction.INSERT -> mChatAdapter?.insertFailedMessages(messages)
                        MessageEventAction.REMOVE -> mChatAdapter?.removeFailedMessages(messages)
                        MessageEventAction.UPDATE -> if (reason == FailedMessageEventActionReason.UPDATE_RESEND_FAILED) {
                            mChatAdapter?.updateFailedMessages(messages)
                        }
                    }
                }

            }

            override fun onNewMessage(collection: MessageCollection?, message: BaseMessage) {
//                Log.d("SyncManager", "onNewMessage: message = $message")
//                //show when the scroll position is bottom ONLY.
//                if (mLayoutManager!!.findFirstVisibleItemPosition() != 0) {
//                    if (message is UserMessage) {
//                        if (!message.sender.userId.equals(PreferenceUtils.getUserId())) {
////                            mNewMessageText.setText("New Message = " + message.sender.nickname.toString() + " : " + message.message)
////                            mNewMessageText.setVisibility(View.VISIBLE)
//                        }
//                    } else if (message is FileMessage) {
//                        if (!message.sender.userId.equals(PreferenceUtils.getUserId())) {
////                            mNewMessageText.setText("New Message = " + message.sender.nickname.toString() + "Send a File")
////                            mNewMessageText.setVisibility(View.VISIBLE)
//                        }
//                    }
//                }
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mCurrentState == STATE_EDIT) {
                    setState(STATE_NORMAL, null, -1)
                    mIMM!!.hideSoftInputFromWindow(mMessageEditText!!.windowToken, 0)
                }
                activity?.supportFragmentManager?.popBackStack()
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(this, callback)
    }

    private fun retryFailedMessage(message: BaseMessage) {
        AlertDialog.Builder(requireContext())
            .setMessage("Retry?")
            .setPositiveButton(R.string.resend_message) { dialog, which ->
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (message is UserMessage) {
                        val userInput = message.message
                        sendUserMessage(userInput)
                    } else if (message is FileMessage) {
                        val uri = mChatAdapter!!.getTempFileMessageUri(message)
                        sendFileWithThumbnail(uri!!)
                    }
                    mChatAdapter!!.removeFailedMessage(message)
                }
            }
            .setNegativeButton(R.string.delete_message) { dialog, which ->
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    mChatAdapter!!.removeFailedMessage(message)
                }
            }.show()
    }

    /**
     * Display which users are typing.
     * If more than two users are currently typing, this will state that "multiple users" are typing.
     *
     * @param typingUsers The list of currently typing users.
     */
    private fun displayTyping(typingUsers: List<Member>) {
        if (typingUsers.size > 0) {
//            mCurrentEventLayout.setVisibility(View.VISIBLE);
            val string: String
            string = if (typingUsers.size == 1) {
                String.format(
                    getString(R.string.user_typing),
                    typingUsers[0].nickname
                )
            } else if (typingUsers.size == 2) {
                String.format(
                    getString(R.string.two_users_typing),
                    typingUsers[0].nickname,
                    typingUsers[1].nickname
                )
            } else {
                getString(R.string.users_typing)
            }
            mCurrentEventText!!.text = string
        } else {
//            mCurrentEventLayout.setVisibility(View.GONE);
        }
    }

    private fun animateVoice(animate: Boolean) {
        if (requireContext() != null) {
            val set = AnimatorInflater.loadAnimator(
                requireContext(),
                R.animator.fade_animator
            ) as AnimatorSet
            if (animate) {
                set.setTarget(icVoice)
                set.setTarget(icVoice1)
                set.setTarget(icVoice2)
                set.start()
            } else {
                set.cancel()
            }
        }
    }


    private fun recordVoice() {
        //request recording permission
        //also request the external storage directory
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestAudioRecordPermission()
            return
        }

        //We need this to set the file output type
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestStoragePermissions()
            return
        }


        if (isRecording) {
            voiceView(false)
            isRecording = false
            animateVoice(false)
            //stop recording
            try {
                mMediaRecorder?.stop()
            } catch (e: Exception) {
                e.printStackTrace()
//                FirebaseCrashlytics.getInstance()
//                    .recordException(e)
            }
        } else {
            initVoiceRecorder()
            voiceView(true)
            isRecording = true
            animateVoice(true)
            //start recording
            mMediaRecorder?.start()
            chronometer.stop()
            chronometer.base = SystemClock.elapsedRealtime()
            chronometer.start()
            chronometer.onChronometerTickListener =
                Chronometer.OnChronometerTickListener { chronometer: Chronometer ->
                    chronometer.text = chronometer.text.toString() + ""
                }
        }
    }


    private fun voiceView(show: Boolean) {
        if (show) {
            textGroup?.visibility = View.INVISIBLE
            recordGroup?.visibility = View.VISIBLE
            mRecordVoiceButton?.setImageResource(R.drawable.ic_send_btn)
//            icVoice?.setImageResource(R.drawable.ic_voice)
//            icVoice1?.setImageResource(R.drawable.ic_voice1)
        } else {
            recordGroup?.visibility = View.GONE
            textGroup?.visibility = View.VISIBLE
            mRecordVoiceButton?.setImageResource(R.drawable.ic_mic)
//            icVoice1?.setImageResource(R.drawable.ic_voice)
//            icVoice1?.visibility = View.VISIBLE
        }
    }


    private fun initVoiceRecorder() {
        val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault())
        val date = Date()
        date.time = System.currentTimeMillis()
        val dateStr = simpleDateFormat.format(date)
        val fileName = mChannel!!.name.replace(
            " ",
            "_"
        ) + dateStr + ".mp3"


        val dir = File(requireContext().externalCacheDir!!.absolutePath + "/voice_chats")
        if (!dir.exists()) {
            dir.mkdir()
        }

        newFile = File(dir, fileName)
        mMediaRecorder = MediaRecorder()
        mMediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mMediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mMediaRecorder?.setOutputFile(newFile!!.absolutePath)
        mMediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)


        try {
            mMediaRecorder?.prepare()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            //FirebaseCrashlytics.getInstance().recordException(e)
        }
    }


    private fun requestAudioRecordPermission() {
        if (activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.RECORD_AUDIO
                )
            } == true
        ) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(
                mRootLayout!!, "Micro phone access permissions are required to record audio.",
                Snackbar.LENGTH_LONG
            )
                .setAction(
                    "Okay"
                ) { view: View? ->
                    requestPermissions(
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        RECORD_AUDIO_REQUEST_CODE
                    )
                }
                .show()
        } else {
            // Permission has not been granted yet. Request it directly.
            requestPermissions(
                recordPermission,
                RECORD_AUDIO_REQUEST_CODE
            )
        }
    }


    private fun pickMedia() {
        uploadFileDialog.setUploadFile(true, {
            val intent = Intent(requireContext(), MediaUtils::class.java)
            intent.putExtra(MediaUtils.useCamera, true)
            startActivityForResult(intent, MediaUtils.MEDIA_REQUEST_CODE)
            Unit
        }) {
            val intent = Intent(requireContext(), MediaUtils::class.java)
            intent.putExtra(MediaUtils.useCamera, false)
            startActivityForResult(intent, MediaUtils.MEDIA_REQUEST_CODE)
            Unit
        }.show(requireFragmentManager(), "")
    }

    private fun requestStoragePermissions() {
        if (activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } == true
        ) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(
                mRootLayout!!, "Storage access permissions are required to upload/download files.",
                Snackbar.LENGTH_LONG
            ).setAction("Okay") {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_WRITE_EXTERNAL_STORAGE
                )
            }
                .show()
        } else {
            // Permission has not been granted yet. Request it directly.
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_WRITE_EXTERNAL_STORAGE
            )
        }
    }

    private fun onFileMessageClicked(message: FileMessage) {
        val type = message.type.toLowerCase()
        if (type.startsWith("image")) {
            val i = Intent(activity, PhotoViewerActivity::class.java)
            i.putExtra("url", message.url)
            i.putExtra("type", message.type)
            startActivity(i)
        } else if (type.startsWith("video")) {
            val intent = Intent(activity, MediaPlayerActivity::class.java)
            intent.putExtra("url", message.url)
            startActivity(intent)
        } else {
            showDownloadConfirmDialog(message)
        }
    }

    private fun showDownloadConfirmDialog(message: FileMessage) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // If storage permissions are not granted, request permissions at run-time,
            // as per < API 23 guidelines.
            requestStoragePermissions()
        } else {
            AlertDialog.Builder(requireContext())
                .setMessage("Download file?")
                .setPositiveButton(R.string.download) { dialog, which ->
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        FileUtils.downloadFile(activity, message.url, message.name)
                    }
                }
                .setNegativeButton(R.string.cancel, null).show()
        }
    }

    private fun updateActionBarTitle() {
//        var title: String? = ""
//        if (mChannel != null) {
//            title = TextUtils.getGroupChannelTitle(mChannel)
//        }
//        mUserName!!.text = title
    }

    private fun sendUserMessageWithUrl(text: String, url: String) {
        if (mChannel == null) {
            return
        }
        object : UrlPreviewAsyncTask() {
            override fun onPostExecute(info: UrlPreviewInfo) {
                if (mChannel == null) {
                    return
                }
                var tempUserMessage: UserMessage? = null
                val handler = BaseChannel.SendUserMessageHandler { userMessage, e ->
                    if (e != null) {
                        // Error!
                        Log.e(LOG_TAG, e.toString())
                        if (activity != null) {
                            Toast.makeText(
                                activity,
                                "Send failed with error " + e.code + ": " + e.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                        mChatAdapter!!.markMessageFailed(userMessage)
                        return@SendUserMessageHandler
                    }

                    // Update a sent message to RecyclerView
                    mChatAdapter!!.markMessageSent(userMessage)
                }
                tempUserMessage = try {
                    // Sending a message with URL preview information and custom type.
                    val jsonString = info.toJsonString()
                    mChannel!!.sendUserMessage(
                        text,
                        jsonString,
                        GroupChatAdapter.URL_PREVIEW_CUSTOM_TYPE,
                        handler
                    )
                } catch (e: Exception) {
                    //FirebaseCrashlytics.getInstance().recordException(e)
                    // Sending a message without URL preview information.
                    mChannel!!.sendUserMessage(text, handler)

                }


                // Display a user message to RecyclerView
                // mChatAdapter!!.addFirst(tempUserMessage!!)
            }
        }.execute(url)
    }

    private fun sendUserMessage(text: String) {
        if (mChannel == null) {
            return
        }
//        val urls = WebUtils.extractUrls(text)
//        if (urls.size > 0) {
//            sendUserMessageWithUrl(text, urls[0])
//            return
//        }

        val pendingMessage: UserMessage =
            mChannel!!.sendUserMessage(text, object : BaseChannel.SendUserMessageHandler {
                override fun onSent(userMessage: UserMessage?, e: SendBirdException?) {
                    if (mMessageCollection != null) {
                        mMessageCollection?.handleSendMessageResponse(userMessage, e)
                        mMessageCollection?.fetchAllNextMessages(null)
                    }
                    if (e != null) {
                        // Error!
                        Log.e(LOG_TAG, e.toString())
                        Toast.makeText(
                            activity,
                            e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                }
            })

        if (mMessageCollection != null) {
            mMessageCollection!!.appendMessage(pendingMessage)
        }

        // Display a user message to RecyclerView
//        smoothScroller.targetPosition = mChatAdapter!!.getLastReadPosition(mLastRead)
//        mLayoutManager?.startSmoothScroll(smoothScroller)
    }

    /**
     * Notify other users whether the current user is typing.
     *
     * @param typing Whether the user is currently typing.
     */
    private fun setTypingStatus(typing: Boolean) {
        if (mChannel == null) {
            return
        }
        if (typing) {
            mIsTyping = true
            mChannel!!.startTyping()
        } else {
            mIsTyping = false
            mChannel!!.endTyping()
        }
    }

    /**
     * Sends a File Message containing an image file.
     * Also requests thumbnails to be generated in specified sizes.
     *
     * @param uri The URI of the image, which in this case is received through an Intent request.
     */
    private fun sendFileWithThumbnail(uri: Uri) {
        if (mChannel == null) {
            return
        }

        // Specify two dimensions of thumbnails to generate
        val thumbnailSizes: MutableList<FileMessage.ThumbnailSize> = ArrayList()
        thumbnailSizes.add(FileMessage.ThumbnailSize(240, 240))
        thumbnailSizes.add(FileMessage.ThumbnailSize(320, 320))
        val info = FileUtils.getFileInfo(activity, uri)
        if (info == null || info.isEmpty) {
            Toast.makeText(activity, "Extracting file information failed.", Toast.LENGTH_LONG)
                .show()
            return
        }
        val name: String?
        name = if (info.containsKey("name")) {
            info["name"] as String?
        } else {
            "Sendbird File"
        }
        val path = info["path"] as String?
        val file = File(path)
        val mime = info["mime"] as String?
        val size = info["size"] as Int
        if (path == null || path == "") {
            Toast.makeText(activity, "File must be located in local storage.", Toast.LENGTH_LONG)
                .show()
        } else {
            val fileMessageHandler = BaseChannel.SendFileMessageHandler { fileMessage, e ->
                mMessageCollection?.handleSendMessageResponse(fileMessage, e);
                mMessageCollection?.fetchAllNextMessages(null);

                if (e != null) {
                    Log.d("MyTag", "onSent: " + getActivity());
                    if (getActivity() != null) {
                        Toast.makeText(activity, "" + e.code + ":" + e.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }

//                if (e != null) {
//                    if (activity != null) {
//                        Toast.makeText(activity, "" + e.code + ":" + e.message, Toast.LENGTH_SHORT)
//                            .show()
//                    }
//                    mChatAdapter!!.markMessageFailed(fileMessage)
//                    return@SendFileMessageHandler
//                }


                //mChatAdapter!!.markMessageSent(fileMessage)
            }

            // Send image with thumbnails in the specified dimensions
            val tempFileMessage = mChannel!!.sendFileMessage(
                file,
                name,
                mime,
                size,
                "",
                null,
                thumbnailSizes,
                fileMessageHandler
            )
            mChatAdapter!!.addTempFileMessageInfo(tempFileMessage, uri)
            if (mMessageCollection != null) {
                mMessageCollection?.appendMessage(tempFileMessage);
            }
            //mChatAdapter!!.addFirst(tempFileMessage)
        }
    }

    private fun editMessage(message: BaseMessage, editedMessage: String) {
        if (mChannel == null) {
            return
        }
        mChannel!!.updateUserMessage(
            message.messageId,
            editedMessage,
            null,
            null,
            BaseChannel.UpdateUserMessageHandler { userMessage, e ->
                if (e != null) {
                    // Error!
                    Toast.makeText(
                        activity,
                        "Error " + e.code + ": " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@UpdateUserMessageHandler
                }
                //mChatAdapter!!.loadLatestMessages(CHANNEL_LIST_LIMIT) { list, e -> mChatAdapter!!.markAllMessagesAsRead() }
            })
    }

    /**
     * Deletes a message within the channel.
     * Note that users can only delete messages sent by oneself.
     *
     * @param message The message to delete.
     */
    private fun deleteMessage(message: BaseMessage) {
        if (mChannel == null) {
            return
        }
        mChannel!!.deleteMessage(message, BaseChannel.DeleteMessageHandler { e ->
            if (e != null) {
                // Error!
                Toast.makeText(activity, "Error " + e.code + ": " + e.message, Toast.LENGTH_SHORT)
                    .show()
                return@DeleteMessageHandler
            }
            // mChatAdapter!!.loadLatestMessages(CHANNEL_LIST_LIMIT) { list, e -> mChatAdapter!!.markAllMessagesAsRead() }
        })
    }

    interface GroupChatClickListener {
        fun onBackToTimelineClicked()
        fun onChatSessionEnd()
    }

    companion object {
        private val LOG_TAG = GroupChatFragment::class.java.simpleName
        private const val CHANNEL_LIST_LIMIT = 30
        const val CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHAT"
        private const val CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_CHAT"
        private const val STATE_NORMAL = 0
        private const val STATE_EDIT = 1
        private const val STATE_CHANNEL_URL = "STATE_CHANNEL_URL"
        private const val INTENT_REQUEST_CHOOSE_MEDIA = 301
        private const val PERMISSION_WRITE_EXTERNAL_STORAGE = 13
        const val EXTRA_CHANNEL_URL = "EXTRA_CHANNEL_URL"
        const val IS_VIEW_ONLY = "view_only"
        const val DASHBOARD = "dashboard"
        const val CHAT_LIST = "chatList"
        const val CHAT_SESSION_DURATION = 10L

        /**
         * To create an instance of this fragment, a Channel URL should be required.
         */
        @JvmStatic
        fun newInstance(channelUrl: String, isViewOnly: Boolean): GroupChatFragment {
            val fragment = GroupChatFragment()
            val args = Bundle()
            args.putString(GroupChannelListFragment.EXTRA_GROUP_CHANNEL_URL, channelUrl)
            args.putBoolean(IS_VIEW_ONLY, isViewOnly)
            fragment.arguments = args
            return fragment
        }

    }
}
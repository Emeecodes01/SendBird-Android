package com.sendbird.groupchannel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.sendbird.R;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;
import com.sendbird.main.sendBird.Chat;
import com.sendbird.syncmanager.FailedMessageEventActionReason;
import com.sendbird.syncmanager.MessageCollection;
import com.sendbird.syncmanager.MessageEventAction;
import com.sendbird.syncmanager.MessageFilter;
import com.sendbird.syncmanager.handler.FetchCompletionHandler;
import com.sendbird.syncmanager.handler.MessageCollectionHandler;
import com.sendbird.utils.FileUtils;
import com.sendbird.utils.GenericDialog;
import com.sendbird.utils.MediaPlayerActivity;
import com.sendbird.utils.MediaUtils;
import com.sendbird.utils.PhotoViewerActivity;
import com.sendbird.utils.PreferenceUtils;
import com.sendbird.utils.TextUtils;
import com.sendbird.utils.TimerUtils;
import com.sendbird.utils.UrlPreviewInfo;
import com.sendbird.utils.WebUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import kotlin.Unit;

import static com.sendbird.utils.MediaUtils.MEDIA_REQUEST_CODE;
import static com.sendbird.utils.MediaUtils.useCamera;
import static com.sendbird.utils.TextUtils.THEME_MATH;

public class GroupChatFragment extends Fragment {

    public static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHAT";
    private static final String LOG_TAG = GroupChatFragment.class.getSimpleName();
    private static final int CHANNEL_LIST_LIMIT = 30;
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_CHAT";
    private static final int STATE_NORMAL = 0;
    private static final int STATE_EDIT = 1;
    private static final String STATE_CHANNEL_URL = "STATE_CHANNEL_URL";
    private static final int INTENT_REQUEST_CHOOSE_MEDIA = 301;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 13;
    final MessageFilter mMessageFilter = new MessageFilter(BaseChannel.MessageTypeFilter.ALL, null, null);
    private final GenericDialog uploadFileDialog =
            new GenericDialog().newInstance(THEME_MATH)
                    .setTitle("Upload a file")
                    .setMessage(R.string.empty);
    private InputMethodManager mIMM;
    private ConstraintLayout mRootLayout;
    private RecyclerView mRecyclerView;
    private GroupChatAdapter mChatAdapter;
    private LinearLayoutManager mLayoutManager;
    private EditText mMessageEditText;
    private Button mRecordVoiceButton;
    private Button button_voice;
    private ImageButton mUploadFileButton;
    private Toolbar toolbar_group_channel;
    private TextView mCurrentEventText;
    private TextView mUserName;
    private TextView countdownTxt;
    private GroupChannel mChannel;
    private String mChannelUrl;
    private boolean mIsTyping;
    private int mCurrentState = STATE_NORMAL;
    private BaseMessage mEditingMessage = null;
    private MessageCollection mMessageCollection;
    private long mLastRead;
    private final MessageCollectionHandler mMessageCollectionHandler = new MessageCollectionHandler() {
        @Override
        public void onMessageEvent(MessageCollection collection, final List<BaseMessage> messages, final MessageEventAction action) {
        }

        @Override
        public void onSucceededMessageEvent(MessageCollection collection, final List<BaseMessage> messages, final MessageEventAction action) {

            if (getActivity() == null) {
                return;
            }

            getActivity().runOnUiThread(() -> {
                switch (action) {
                    case INSERT:
                        mChatAdapter.insertSucceededMessages(messages);
                        mChatAdapter.markAllMessagesAsRead();
                        break;

                    case REMOVE:
                        mChatAdapter.removeSucceededMessages(messages);
                        break;

                    case UPDATE:
                        mChatAdapter.updateSucceededMessages(messages);
                        break;

                    case CLEAR:
                        mChatAdapter.clear();
                        break;
                }
            });

            updateLastSeenTimestamp(messages);

        }

        @Override
        public void onPendingMessageEvent(MessageCollection collection, final List<BaseMessage> messages, final MessageEventAction action) {
            if (getActivity() == null) {
                return;
            }

            getActivity().runOnUiThread(() -> {
                switch (action) {
                    case INSERT:
                        List<BaseMessage> pendingMessages = new ArrayList<>();
                        for (BaseMessage message : messages) {
                            if (!mChatAdapter.failedMessageListContains(message)) {
                                pendingMessages.add(message);
                            }
                        }
                        mChatAdapter.insertSucceededMessages(pendingMessages);
                        break;

                    case REMOVE:
                        mChatAdapter.removeSucceededMessages(messages);
                        break;
                }
            });
        }

        @Override
        public void onFailedMessageEvent(MessageCollection collection, final List<BaseMessage> messages, final MessageEventAction action, final FailedMessageEventActionReason reason) {
            Log.d("SyncManager", "onFailedMessageEvent: size = " + messages.size() + ", action = " + action);
            if (getActivity() == null) {
                return;
            }

            getActivity().runOnUiThread(() -> {
                switch (action) {
                    case INSERT:
                        mChatAdapter.insertFailedMessages(messages);
                        break;

                    case REMOVE:
                        mChatAdapter.removeFailedMessages(messages);
                        break;
                    case UPDATE:
                        if (reason == FailedMessageEventActionReason.UPDATE_RESEND_FAILED) {
                            mChatAdapter.updateFailedMessages(messages);
                        }
                        break;
                }
            });
        }

        @Override
        public void onNewMessage(MessageCollection collection, BaseMessage message) {
        }
    };

    public static GroupChatFragment newInstance(@NonNull String channelUrl) {
        GroupChatFragment fragment = new GroupChatFragment();

        Bundle args = new Bundle();
        args.putString(GroupChannelListFragment.EXTRA_GROUP_CHANNEL_URL, channelUrl);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIMM = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (savedInstanceState != null) {
            // Get channel URL from saved state.
            mChannelUrl = savedInstanceState.getString(STATE_CHANNEL_URL);
        } else {
            // Get channel URL from GroupChannelListFragment.
            mChannelUrl = getArguments().getString(GroupChannelListFragment.EXTRA_GROUP_CHANNEL_URL);
        }

        mLastRead = PreferenceUtils.getLastRead(mChannelUrl);
        mChatAdapter = new GroupChatAdapter(getActivity());

        setUpChatListAdapter();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_chat, container, false);

        setRetainInstance(true);

        initializeViews(rootView);

        setUpRecyclerView();

        createMessageCollection(mChannelUrl);

        onBack();

        handleTimer();

        sendMessage();

        mUploadFileButton.setOnClickListener(v -> pickMedia());
        mIsTyping = false;

        onTyping();

        return rootView;
    }

    private void onTyping() {
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mIsTyping) {
                    setTypingStatus(true);
                }

                if (s.length() == 0) {
                    setTypingStatus(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void onBack() {
        toolbar_group_channel.setNavigationOnClickListener(view -> {

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }

        });
    }

    private void initializeViews(View rootView) {
        mRootLayout = rootView.findViewById(R.id.layout_group_chat_root);
        mRecyclerView = rootView.findViewById(R.id.recycler_group_chat);
        mUserName = rootView.findViewById(R.id.userName);
        countdownTxt = rootView.findViewById(R.id.countdownTxt);
        toolbar_group_channel = rootView.findViewById(R.id.toolbar_group_channel);
        mCurrentEventText = rootView.findViewById(R.id.text_group_chat_current_event);
        mMessageEditText = rootView.findViewById(R.id.edittext_group_chat_message);
        mRecordVoiceButton = rootView.findViewById(R.id.button_record_voice);
        button_voice = rootView.findViewById(R.id.button_voice);
        mUploadFileButton = rootView.findViewById(R.id.button_group_chat_upload);
    }

    private void sendMessage() {
        mMessageEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {

            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendTextMessage();
            }

            return false;
        });
    }

    private void handleTimer() {
        new TimerUtils().getTime(mChannelUrl, (countDownTime) -> {

            int countDownMinutes = countDownTime / 60;
            int countDownSeconds = countDownTime - (60 * countDownMinutes);

            countTime(countDownMinutes, countDownSeconds);

            return Unit.INSTANCE;
        }, () -> {

            countdownTxt.setVisibility(View.GONE);

            mMessageEditText.setEnabled(false);
            mUploadFileButton.setEnabled(false);
            button_voice.setEnabled(false);

            return Unit.INSTANCE;
        });
    }

    @SuppressLint("SetTextI18n")
    private void countTime(long minute, long seconds) {

        if (minute >= 0) {

            new TimerUtils().timer(seconds, (l) -> {

                countdownTxt.setText(String.format(Locale.US, "%02d", minute) + ":" + String.format(Locale.US, "%02d", l));

                return Unit.INSTANCE;
            }, () -> {

                countTime(minute - 1, 59);
                return Unit.INSTANCE;
            });

        } else {
            if (getActivity() != null) {
                new Chat().updateGroupChat(mChannelUrl, (groupChannel, e) -> {
                });
                getActivity().getSupportFragmentManager().popBackStack();
            }

        }

    }

    private void sendTextMessage() {
        String userInput = mMessageEditText.getText().toString();
        if (mCurrentState == STATE_EDIT) {
            if (userInput.length() > 0) {
                if (mEditingMessage != null) {
                    editMessage(mEditingMessage, userInput);
                }
            }
            setState(STATE_NORMAL, null, -1);
        } else {
            if (userInput.length() > 0) {
                sendUserMessage(userInput);
                mMessageEditText.setText("");
            }
        }
    }

    private void fetchInitialMessages() {
        if (mMessageCollection == null) {
            return;
        }

        mMessageCollection.fetchSucceededMessages(MessageCollection.Direction.PREVIOUS, (hasMore, e) -> mMessageCollection.fetchSucceededMessages(MessageCollection.Direction.NEXT, new FetchCompletionHandler() {
            @Override
            public void onCompleted(boolean hasMore, SendBirdException e) {
                mMessageCollection.fetchFailedMessages(e1 -> {
                    if (getActivity() == null) {
                        return;
                    }

                    getActivity().runOnUiThread(() -> {
                        mChatAdapter.markAllMessagesAsRead();
                        mLayoutManager.scrollToPositionWithOffset(mChatAdapter.getLastReadPosition(mLastRead), mRecyclerView.getHeight() / 2);
                    });
                });
            }
        }));
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null) {
            mChatAdapter.setContext(getActivity());
        }

        SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, new SendBird.ConnectionHandler() {
            @Override
            public void onReconnectStarted() {
            }

            @Override
            public void onReconnectSucceeded() {

                if (mMessageCollection != null) {
                    if (mLayoutManager.findFirstVisibleItemPosition() <= 0) {
                        mMessageCollection.fetchAllNextMessages((hasMore, e) -> {
                        });
                    }

                    if (mLayoutManager.findLastVisibleItemPosition() == mChatAdapter.getItemCount() - 1) {
                        mMessageCollection.fetchSucceededMessages(MessageCollection.Direction.PREVIOUS, (hasMore, e) -> {
                        });
                    }
                }
            }

            @Override
            public void onReconnectFailed() {
            }
        });

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
            }

            @Override
            public void onMessageDeleted(BaseChannel baseChannel, long msgId) {
                super.onMessageDeleted(baseChannel, msgId);
                if (baseChannel.getUrl().equals(mChannelUrl)) {
                    mChatAdapter.delete(msgId);
                }
            }

            @Override
            public void onMessageUpdated(BaseChannel channel, BaseMessage message) {
                super.onMessageUpdated(channel, message);
                if (channel.getUrl().equals(mChannelUrl)) {
                    mChatAdapter.update(message);
                }
            }

            @Override
            public void onReadReceiptUpdated(GroupChannel channel) {
                if (channel.getUrl().equals(mChannelUrl)) {
                    mChatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onTypingStatusUpdated(GroupChannel channel) {
                if (channel.getUrl().equals(mChannelUrl)) {
                    List<Member> typingUsers = channel.getTypingMembers();
                    displayTyping(typingUsers);
                }
            }

            @Override
            public void onDeliveryReceiptUpdated(GroupChannel channel) {
                if (channel.getUrl().equals(mChannelUrl)) {
                    mChatAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onPause() {
        setTypingStatus(false);

        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        // Save messages to cache.
        if (mMessageCollection != null) {
            mMessageCollection.setCollectionHandler(null);
            mMessageCollection.remove();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_CHANNEL_URL, mChannelUrl);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Set this as true to restore background connection management.
        SendBird.setAutoBackgroundDetection(true);

        if (requestCode == MEDIA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            sendFileWithThumbnail(data.getData());
            uploadFileDialog.dismiss();
        }

        if (requestCode == INTENT_REQUEST_CHOOSE_MEDIA && resultCode == Activity.RESULT_OK) {
            // If user has successfully chosen the image, show a dialog to confirm upload.
            if (data == null) {
                Log.d(LOG_TAG, "data is null!");
                return;
            }

            sendFileWithThumbnail(data.getData());
        }
    }

    private void setUpRecyclerView() {

        if (getActivity() != null) {

            mLayoutManager = new LinearLayoutManager(getActivity());
            mLayoutManager.setReverseLayout(true);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mChatAdapter);
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        if (mLayoutManager.findFirstVisibleItemPosition() == 0) {
                            mMessageCollection.fetchSucceededMessages(MessageCollection.Direction.NEXT, null);
                        }

                        if (mLayoutManager.findLastVisibleItemPosition() == mChatAdapter.getItemCount() - 1) {
                            mMessageCollection.fetchSucceededMessages(MessageCollection.Direction.PREVIOUS, null);
                        }

                    }
                }
            });
        }

    }

    private void setUpChatListAdapter() {
        mChatAdapter.setItemClickListener(new GroupChatAdapter.OnItemClickListener() {
            @Override
            public void onUserMessageItemClick(UserMessage message) {
                // Restore failed message and remove the failed message from list.
                if (mChatAdapter.isFailedMessage(message) && !mChatAdapter.isResendingMessage(message)) {
                    retryFailedMessage(message);
                    return;
                }

                // Message is sending. Do nothing on click event.
                if (mChatAdapter.isTempMessage(message)) {
                    return;
                }

                if (message.getCustomType().equals(GroupChatAdapter.URL_PREVIEW_CUSTOM_TYPE)) {
                    try {
                        UrlPreviewInfo info = new UrlPreviewInfo(message.getData());
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(info.getUrl()));
                        startActivity(browserIntent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFileMessageItemClick(FileMessage message) {
                // Load media chooser and remove the failed message from list.
                if (mChatAdapter.isFailedMessage(message)) {
                    retryFailedMessage(message);
                    return;
                }

                // Message is sending. Do nothing on click event.
                if (mChatAdapter.isTempMessage(message)) {
                    return;
                }

                onFileMessageClicked(message);
            }
        });

        mChatAdapter.setItemLongClickListener(new GroupChatAdapter.OnItemLongClickListener() {
            @Override
            public void onUserMessageItemLongClick(UserMessage message, int position) {
                if (message.getSender().getUserId().equals(PreferenceUtils.getUserId())) {
                    showMessageOptionsDialog(message, position);
                }
            }

            @Override
            public void onFileMessageItemLongClick(FileMessage message) {
            }

            @Override
            public void onAdminMessageItemLongClick(AdminMessage message) {
            }
        });
    }

    private void showMessageOptionsDialog(final BaseMessage message, final int position) {
        String[] options = new String[]{"Edit message", "Delete message"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                setState(STATE_EDIT, message, position);
            } else if (which == 1) {
                deleteMessage(message);
            }
        });
        builder.create().show();
    }

    private void setState(int state, BaseMessage editingMessage, final int position) {
        switch (state) {
            case STATE_NORMAL:
                mCurrentState = STATE_NORMAL;
                mEditingMessage = null;

                mUploadFileButton.setVisibility(View.VISIBLE);
                mMessageEditText.setText("");
                break;

            case STATE_EDIT:
                mCurrentState = STATE_EDIT;
                mEditingMessage = editingMessage;

                mUploadFileButton.setVisibility(View.GONE);
                String messageString = ((UserMessage) editingMessage).getMessage();
                if (messageString == null) {
                    messageString = "";
                }
                mMessageEditText.setText(messageString);
                if (messageString.length() > 0) {
                    mMessageEditText.setSelection(0, messageString.length());
                }

                mMessageEditText.requestFocus();
                mMessageEditText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIMM.showSoftInput(mMessageEditText, 0);

                        mRecyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mRecyclerView.scrollToPosition(position);
                            }
                        }, 500);
                    }
                }, 100);
                break;
        }
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {

            @Override
            public void handleOnBackPressed() {
                if (mCurrentState == STATE_EDIT) {
                    setState(STATE_NORMAL, null, -1);
                    mIMM.hideSoftInputFromWindow(mMessageEditText.getWindowToken(), 0);
                }

                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }

            }

        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

    }

    private void createMessageCollection(final String channelUrl) {
        GroupChannel.getChannel(channelUrl, (groupChannel, e) -> {
            if (e != null) {
                MessageCollection.create(channelUrl, mMessageFilter, mLastRead, (messageCollection, e1) -> {
                    if (e1 == null) {
                        if (mMessageCollection != null) {
                            mMessageCollection.remove();
                        }

                        mMessageCollection = messageCollection;
                        mMessageCollection.setCollectionHandler(mMessageCollectionHandler);

                        mChannel = mMessageCollection.getChannel();
                        mChatAdapter.setChannel(mChannel);

                        if (getActivity() == null) {
                            return;
                        }

                        getActivity().runOnUiThread(() -> {
                            mChatAdapter.clear();
                            updateActionBarTitle();
                        });

                        fetchInitialMessages();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.get_channel_failed), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                if (mMessageCollection != null) {
                    mMessageCollection.remove();
                }

                mMessageCollection = new MessageCollection(groupChannel, mMessageFilter, mLastRead);
                mMessageCollection.setCollectionHandler(mMessageCollectionHandler);

                mChannel = groupChannel;
                mChatAdapter.setChannel(mChannel);
                mChatAdapter.clear();
                updateActionBarTitle();

                fetchInitialMessages();
            }
        });
    }

    private void retryFailedMessage(final BaseMessage message) {
        new AlertDialog.Builder(getActivity())
                .setMessage("Retry?")
                .setPositiveButton(R.string.resend_message, (dialog, which) -> {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        if (message instanceof UserMessage) {
                            mChannel.resendUserMessage((UserMessage) message, (userMessage, e) -> mMessageCollection.handleSendMessageResponse(userMessage, e));
                        } else if (message instanceof FileMessage) {
                            Uri uri = mChatAdapter.getTempFileMessageUri(message);
                            sendFileWithThumbnail(uri);
                        }
                        mChatAdapter.removeFailedMessage(message);
                    }
                })
                .setNegativeButton(R.string.delete_message, (dialog, which) -> {
                    if (which == DialogInterface.BUTTON_NEGATIVE) {
                        mChatAdapter.removeFailedMessage(message);
                    }
                }).show();
    }

    private void displayTyping(List<Member> typingUsers) {
        if (typingUsers.size() > 0) {
            String nickName;
            if (typingUsers.size() == 1) {
                nickName = String.format(getString(R.string.user_typing), typingUsers.get(0).getNickname());
            } else if (typingUsers.size() == 2) {
                nickName = String.format(getString(R.string.two_users_typing), typingUsers.get(0).getNickname(), typingUsers.get(1).getNickname());
            } else {
                nickName = getString(R.string.users_typing);
            }
            mCurrentEventText.setText(nickName);
        }
    }

    private void pickMedia() {

        if (getActivity() != null) {
            uploadFileDialog.setUploadFile(true, () -> {

                Intent intent = new Intent(requireContext(), MediaUtils.class);
                intent.putExtra(useCamera, true);

                startActivityForResult(intent, MEDIA_REQUEST_CODE);
                return Unit.INSTANCE;
            }, () -> {

                Intent intent = new Intent(requireContext(), MediaUtils.class);
                intent.putExtra(useCamera, false);
                startActivityForResult(intent, MEDIA_REQUEST_CODE);

                return Unit.INSTANCE;
            }).show(getActivity().getSupportFragmentManager(), "");
        }

    }

    private void requestStoragePermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Snackbar.make(mRootLayout, "Storage access permissions are required to upload/download files.",
                    Snackbar.LENGTH_LONG)
                    .setAction("Okay", view -> requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_WRITE_EXTERNAL_STORAGE))
                    .show();
        } else {
            // Permission has not been granted yet. Request it directly.
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void onFileMessageClicked(FileMessage message) {
        String type = message.getType().toLowerCase();
        if (type.startsWith("image")) {
            Intent i = new Intent(getActivity(), PhotoViewerActivity.class);
            i.putExtra("url", message.getUrl());
            i.putExtra("type", message.getType());
            startActivity(i);
        } else if (type.startsWith("video")) {
            Intent intent = new Intent(getActivity(), MediaPlayerActivity.class);
            intent.putExtra("url", message.getUrl());
            startActivity(intent);
        } else {
            showDownloadConfirmDialog(message);
        }
    }

    private void showDownloadConfirmDialog(final FileMessage message) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // If storage permissions are not granted, request permissions at run-time,
            // as per < API 23 guidelines.
            requestStoragePermissions();
        } else {
            new AlertDialog.Builder(getActivity())
                    .setMessage("Download file?")
                    .setPositiveButton(R.string.download, (dialog, which) -> {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            FileUtils.downloadFile(getActivity(), message.getUrl(), message.getName());
                        }
                    })
                    .setNegativeButton(R.string.cancel, null).show();
        }

    }

    private void updateActionBarTitle() {
        String title = "";

        if (mChannel != null) {
            title = TextUtils.getGroupChannelTitle(mChannel);
        }
        mUserName.setText(title);

    }

    @SuppressLint("StaticFieldLeak")
    private void sendUserMessageWithUrl(final String text, String url) {
        if (mChannel == null) {
            return;
        }
        try {
            new WebUtils.UrlPreviewAsyncTask() {

                @Override
                protected void onPostExecute(UrlPreviewInfo info) {
                    if (mChannel == null) {
                        return;
                    }

                    UserMessage tempUserMessage = null;
                    BaseChannel.SendUserMessageHandler handler = (userMessage, e) -> {
                        if (e != null) {
                            // Error!
                            Log.e(LOG_TAG, e.toString());
                            if (getActivity() != null) {
                                Toast.makeText(
                                        getActivity(),
                                        "Send failed with error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT)
                                        .show();
                            }
                            mChatAdapter.markMessageFailed(userMessage);
                            return;
                        }

                        // Update a sent message to RecyclerView
//                        mChatAdapter.markMessageSent(userMessage);
                        mMessageCollection.handleSendMessageResponse(userMessage, e);
                    };

                    try {
                        // Sending a message with URL preview information and custom type.
                        String jsonString = info.toJsonString();
                        tempUserMessage = mChannel.sendUserMessage(text, jsonString, GroupChatAdapter.URL_PREVIEW_CUSTOM_TYPE, handler);
                    } catch (Exception e) {
                        // Sending a message without URL preview information.
                        tempUserMessage = mChannel.sendUserMessage(text, handler);
                    }


                    if (mMessageCollection != null) {
                        mMessageCollection.appendMessage(tempUserMessage);
                    }
                }
            }.execute(url);
        } catch (Exception e) {
        }

    }

    private void sendUserMessage(String text) {
        if (mChannel == null) {
            return;
        }

        List<String> urls = WebUtils.extractUrls(text);
        if (urls.size() > 0) {
            sendUserMessageWithUrl(text, urls.get(0));
            return;
        }

        final UserMessage pendingMessage = mChannel.sendUserMessage(text, (userMessage, e) -> {
            if (mMessageCollection != null) {
                mMessageCollection.handleSendMessageResponse(userMessage, e);
                mMessageCollection.fetchAllNextMessages(null);
            }
        });

        // Display a user message to RecyclerView
        if (mMessageCollection != null) {
            mMessageCollection.appendMessage(pendingMessage);
        }
    }

    private void setTypingStatus(boolean typing) {
        if (mChannel == null) {
            return;
        }

        if (typing) {
            mIsTyping = true;
            mChannel.startTyping();
        } else {
            mIsTyping = false;
            mChannel.endTyping();
        }
    }

    private void sendFileWithThumbnail(Uri uri) {
        if (mChannel == null) {
            return;
        }

        // Specify two dimensions of thumbnails to generate
        List<FileMessage.ThumbnailSize> thumbnailSizes = new ArrayList<>();
        thumbnailSizes.add(new FileMessage.ThumbnailSize(240, 240));
        thumbnailSizes.add(new FileMessage.ThumbnailSize(320, 320));

        Hashtable<String, Object> info = FileUtils.getFileInfo(getActivity(), uri);

        if (info == null || info.isEmpty()) {
            Toast.makeText(getActivity(), "Extracting file information failed.", Toast.LENGTH_LONG).show();
            return;
        }

        final String name;
        if (info.containsKey("name")) {
            name = (String) info.get("name");
        } else {
            name = "Sendbird File";
        }
        final String path = (String) info.get("path");
        final File file = new File(path);
        final String mime = (String) info.get("mime");
        final int size = (Integer) info.get("size");

        if (path == null || path.equals("")) {
            Toast.makeText(getActivity(), "File must be located in local storage.", Toast.LENGTH_LONG).show();
        } else {
            BaseChannel.SendFileMessageHandler fileMessageHandler = (fileMessage, e) -> {
                mMessageCollection.handleSendMessageResponse(fileMessage, e);
                mMessageCollection.fetchAllNextMessages(null);

                if (e != null) {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    mChatAdapter.markMessageFailed(fileMessage);
                    return;
                }

                mChatAdapter.markMessageSent(fileMessage);
            };

            // Send image with thumbnails in the specified dimensions
            FileMessage tempFileMessage = mChannel.sendFileMessage(file, name, mime, size, "", null, thumbnailSizes, fileMessageHandler);

            mChatAdapter.addTempFileMessageInfo(tempFileMessage, uri);

            if (mMessageCollection != null) {
                mMessageCollection.appendMessage(tempFileMessage);
            }
        }
    }

    private void editMessage(final BaseMessage message, String editedMessage) {
        if (mChannel == null) {
            return;
        }

        mChannel.updateUserMessage(message.getMessageId(), editedMessage, null, null, (userMessage, e) -> {
            if (e != null) {
                Toast.makeText(requireContext(), "Error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            mChatAdapter.loadLatestMessages(CHANNEL_LIST_LIMIT, (list, e1) -> mChatAdapter.markAllMessagesAsRead());
        });
    }

    private void deleteMessage(final BaseMessage message) {
        if (mChannel == null) {
            return;
        }

        mChannel.deleteMessage(message, e -> {
            if (e != null) {
                // Error!
                Toast.makeText(requireContext(), "Error " + e.getCode() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            mChatAdapter.loadLatestMessages(CHANNEL_LIST_LIMIT, (list, e1) -> mChatAdapter.markAllMessagesAsRead());
        });
    }

    private void updateLastSeenTimestamp(List<BaseMessage> messages) {
        long lastSeenTimestamp = mLastRead == Long.MAX_VALUE ? 0 : mLastRead;
        for (BaseMessage message : messages) {
            if (lastSeenTimestamp < message.getCreatedAt()) {
                lastSeenTimestamp = message.getCreatedAt();
            }
        }

        if (lastSeenTimestamp > mLastRead) {
            PreferenceUtils.setLastRead(mChannelUrl, lastSeenTimestamp);
            mLastRead = lastSeenTimestamp;
        }
    }

}

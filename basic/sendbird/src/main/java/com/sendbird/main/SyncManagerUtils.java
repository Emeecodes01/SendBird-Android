package com.sendbird.main;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.UserMessage;
import com.sendbird.syncmanager.SendBirdSyncManager;
import com.sendbird.syncmanager.handler.CompletionHandler;

import java.util.List;

public class SyncManagerUtils {
    public static void setup(Context context, String userId, CompletionHandler handler) {
        SendBirdSyncManager.Options options = new SendBirdSyncManager.Options.Builder()
                .setMessageResendPolicy(SendBirdSyncManager.MessageResendPolicy.AUTOMATIC)
                .setAutomaticMessageResendRetryCount(5)
                .build();
        SendBirdSyncManager.setup(context, userId, options, handler);
    }

    public static int findIndexOfMessage(@NonNull List<BaseMessage> messages, @NonNull BaseMessage newMessage) {
        if (messages.size() == 0) {
            return 0;
        }

        if (messages.get(0).getCreatedAt() < newMessage.getCreatedAt()) {
            return 0;
        }

        for (int i = 0; i < messages.size() - 1; i++) {
            BaseMessage m1 = messages.get(i);
            BaseMessage m2 = messages.get(i + 1);

            if (m1.getCreatedAt() > newMessage.getCreatedAt() && newMessage.getCreatedAt() > m2.getCreatedAt()) {
                return i + 1;
            }
        }

        return messages.size();
    }

    public static int getIndexOfMessage(@NonNull List<BaseMessage> messages, @NonNull BaseMessage targetMessage) {
        for (int i = 0; i < messages.size(); i++) {
            long msgId1 = messages.get(i).getMessageId();
            long msgId2 = targetMessage.getMessageId();

            if (msgId1 == msgId2) {
                if (msgId1 == 0) {
                    if (getRequestId(messages.get(i)).equals(getRequestId(targetMessage))) {
                        return i;
                    }
                } else {
                    return i;
                }
            }
        }

        return -1;
    }

    private static String getRequestId(BaseMessage message) {
        if (message instanceof AdminMessage) {
            return "";
        }

        if (message instanceof UserMessage) {
            return ((UserMessage) message).getRequestId();
        }

        if (message instanceof FileMessage) {
            return ((FileMessage) message).getRequestId();
        }

        return "";
    }

}

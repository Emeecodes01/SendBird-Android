package com.ulesson.chat.main;

import com.sendbird.android.SendBird;
import com.sendbird.syncmanager.SendBirdSyncManager;
import com.ulesson.chat.utils.PreferenceUtils;
import com.ulesson.chat.utils.PushUtils;

public class ConnectionManager {

    public static boolean isLogin() {
        return PreferenceUtils.getConnected();
    }

    public static void login(String userId, String accessToken, final SendBird.ConnectHandler handler) {

        SendBird.connect(userId, accessToken, (user, e) -> {

            if (handler != null) {
                handler.onConnected(user, e);
            }

            PushUtils.refreshPushTokenForCurrentUser();
        });
    }

    public static void logout(final SendBird.DisconnectHandler handler) {
        SendBird.disconnect(() -> {

            try {
                SendBirdSyncManager.getInstance().pauseSync();
                SendBirdSyncManager.getInstance().clearCache();
            } catch (Exception ignored) {
            }

            PreferenceUtils.setConnected(false);
            if (handler != null) {
                handler.onDisconnected();
            }
        });
    }

    public static void addConnectionManagementHandler(String handlerId, final ConnectionManagementHandler handler) {

        SendBird.addConnectionHandler(handlerId, new SendBird.ConnectionHandler() {
            @Override
            public void onReconnectStarted() {
            }

            @Override
            public void onReconnectSucceeded() {
                if (handler != null) {
                    handler.onConnected(true);
                }
            }

            @Override
            public void onReconnectFailed() {
            }
        });

        if (SendBird.getConnectionState() == SendBird.ConnectionState.OPEN) {
            if (handler != null) {
                handler.onConnected(false);
            }
        } else if (SendBird.getConnectionState() == SendBird.ConnectionState.CLOSED) { // push notification or system kill
            String userId = PreferenceUtils.getUserId();
            String accessToken = PreferenceUtils.getAccessToken();
            SendBird.connect(userId, accessToken, (user, e) -> {
                PreferenceUtils.setConnected(e == null);
                handler.onConnected(e == null);
            });
        }
    }

    public static void removeConnectionManagementHandler(String handlerId) {
        SendBird.removeConnectionHandler(handlerId);
    }

    public interface ConnectionManagementHandler {
        void onConnected(boolean reconnect);
    }
}

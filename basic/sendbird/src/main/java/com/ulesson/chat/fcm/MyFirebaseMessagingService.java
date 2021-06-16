package com.ulesson.chat.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sendbird.android.SendBird;
import com.ulesson.chat.R;
import com.ulesson.chat.groupchannel.GroupChannelActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicReference;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "okh";
    private static final AtomicReference<String> pushToken = new AtomicReference<>();

    public static void sendNotification(Context context, String messageBody, String channelUrl, String senderName) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final String CHANNEL_ID = "CHANNEL_ID";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "CHANNEL_NAME", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        Intent intent = new Intent(context, GroupChannelActivity.class);
        intent.putExtra("GROUP_CHANNEL_URL", channelUrl);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.img_notification)
                .setColor(Color.parseColor("#107FDC"))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_ulesson))
                .setContentTitle(senderName)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        notificationBuilder.setContentText(messageBody);

        notificationManager.notify(0, notificationBuilder.build());
    }

    public static void getPushToken(ITokenResult listner) {
        String token = pushToken.get();
        if (!TextUtils.isEmpty(token)) {
            listner.onPushTokenReceived(token);
            return;
        }

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "getInstanceId failed", task.getException());
                    return;
                }

                // Get new Instance ID token
                InstanceIdResult result = task.getResult();
                if (result != null) {
                    String token = result.getToken();
                    pushToken.set(token);
                    listner.onPushTokenReceived(token);
                }
            }
        });
    }

    @Override
    public void onNewToken(String token) {
        Log.i(TAG, "onNewToken(" + token + ")");

        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(final String token) {
        SendBird.registerPushTokenForCurrentUser(token, (pushTokenRegistrationStatus, e) -> {
            if (e != null) {
                Toast.makeText(MyFirebaseMessagingService.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            pushToken.set(token);
        });
    }
    // [END receive_message]

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "Message From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        String channelUrl = null;
        try {
            if (remoteMessage.getData().get("sendbird") != null) {
                JSONObject sendBird = new JSONObject(remoteMessage.getData().get("sendbird"));
                JSONObject channel = (JSONObject) sendBird.get("channel");
                JSONObject sender = (JSONObject) sendBird.get("sender");
                channelUrl = (String) channel.get("channel_url");
                String senderName = (String) sender.get("name");
                String messageContent = remoteMessage.getData().get("message");
                if (messageContent != null) {
                    SendBird.markAsDelivered(channelUrl);
                    String[] messageArray = messageContent.split(":");
                    if (messageArray.length > 1)
                        sendNotification(this, messageArray[1], channelUrl, senderName);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public interface ITokenResult {
        void onPushTokenReceived(String pushToken);
    }
}
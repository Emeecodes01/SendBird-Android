package com.sendbird.android.sample.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtils {

    private static final String PREFERENCE_KEY_USER_ID = "userId";
    private static final String PREFERENCE_KEY_NICKNAME = "nickname";
    private static final String PREFERENCE_KEY_CONNECTED = "connected";

    private static final String PREFERENCE_KEY_NOTIFICATIONS = "notifications";
    private static final String PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS = "notificationsShowPreviews";
    private static final String PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB = "notificationsDoNotDisturb";
    private static final String PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_FROM = "notificationsDoNotDisturbFrom";
    private static final String PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_TO = "notificationsDoNotDisturbTo";
    private static final String PREFERENCE_KEY_GROUP_CHANNEL_DISTINCT = "channelDistinct";
    private static final String SENDBIRD_APP_ID = "sendbird-app-id";
    private static final String SENDBIRD_API_TOKEN = "sendbird-api-token";
    private static final String SENDBIRD_BASE_URL = "sendbird-base-url";
    private static final String ULESSON_API_TOKEN = "ulesson-api-token";
    private static final String DEVICE_TOKEN = "android-device-token";
    private static final String VERSION = "app-version";
    private static final String PREFERENCE_KEY_GROUP_CHANNEL_LAST_READ = "last_read";

    private static Context mAppContext;

    // Prevent instantiation
    private PreferenceUtils() {
    }

    public static void init(Context appContext) {
        mAppContext = appContext;
    }

    private static SharedPreferences getSharedPreferences() {
        return mAppContext.getSharedPreferences("sendbird", Context.MODE_PRIVATE);
    }

    public static void setUserId(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_USER_ID, userId).apply();
    }

    public static void saveSendBirdId(String appId) {
        SharedPreferences.Editor spe = getSharedPreferences().edit();
        spe.putString(SENDBIRD_APP_ID, appId).apply();
    }

    public static String getSendbirdAppId() {
        return getSharedPreferences().getString(SENDBIRD_APP_ID, "");
    }

    public static void saveSendbirdAPIToken(String token) {
        SharedPreferences.Editor spe = getSharedPreferences().edit();
        spe.putString(SENDBIRD_API_TOKEN, token).apply();
    }

    public static String getSendbirdApiToken() {
        return getSharedPreferences().getString(SENDBIRD_API_TOKEN, "");
    }

    public static String getUlessonApiToken() {
        return getSharedPreferences().getString(ULESSON_API_TOKEN, "");
    }


    public static String getDeviceId() {
        return getSharedPreferences().getString(DEVICE_TOKEN, "");
    }


    public static void saveUlessonApiToken(String apiToken) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(ULESSON_API_TOKEN, apiToken).apply();
    }

    public static void saveDeviceId(String deviceId) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(DEVICE_TOKEN, deviceId).apply();
    }

    public static String getUserId() {
        return getSharedPreferences().getString(PREFERENCE_KEY_USER_ID, "");
    }

    public static void setNickname(String nickname) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_NICKNAME, nickname).apply();
    }

    public static String getNickname() {
        return getSharedPreferences().getString(PREFERENCE_KEY_NICKNAME, "");
    }

//    public static void setCountDown(String time) {
//        SharedPreferences.Editor editor = getSharedPreferences().edit();
//        editor.putString(PREFERENCE_KEY_NICKNAME, nickname).apply();
//    }
//
//    public static String getCountDown() {
//        return getSharedPreferences().getString(PREFERENCE_KEY_NICKNAME, "");
//    }

    public static void setConnected(boolean tf) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PREFERENCE_KEY_CONNECTED, tf).apply();
    }

    public static boolean getConnected() {
        return getSharedPreferences().getBoolean(PREFERENCE_KEY_CONNECTED, false);
    }

    public static void clearAll() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.clear().apply();
    }

    public static void setNotifications(boolean notifications) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS, notifications).apply();
    }

    public static boolean getNotifications() {
        return getSharedPreferences().getBoolean(PREFERENCE_KEY_NOTIFICATIONS, true);
    }

    public static void setNotificationsShowPreviews(boolean notificationsShowPreviews) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS, notificationsShowPreviews).apply();
    }

    public static boolean getNotificationsShowPreviews() {
        return getSharedPreferences().getBoolean(PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS, true);
    }

    public static void setNotificationsDoNotDisturb(boolean notificationsDoNotDisturb) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB, notificationsDoNotDisturb).apply();
    }

    public static boolean getNotificationsDoNotDisturb() {
        return getSharedPreferences().getBoolean(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB, false);
    }

    public static void setNotificationsDoNotDisturbFrom(String notificationsDoNotDisturbFrom) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_FROM, notificationsDoNotDisturbFrom).apply();
    }

    public static String getNotificationsDoNotDisturbFrom() {
        return getSharedPreferences().getString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_FROM, "");
    }

    public static void setNotificationsDoNotDisturbTo(String notificationsDoNotDisturbTo) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_TO, notificationsDoNotDisturbTo).apply();
    }

    public static String getNotificationsDoNotDisturbTo() {
        return getSharedPreferences().getString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_TO, "");
    }
    public static void setGroupChannelDistinct(boolean channelDistinct) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PREFERENCE_KEY_GROUP_CHANNEL_DISTINCT, channelDistinct).apply();
    }


    public static void setLastRead(String groupChannelUrl, long ts) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putLong(PREFERENCE_KEY_GROUP_CHANNEL_LAST_READ + groupChannelUrl, ts).apply();
    }

    public static long getLastRead(String groupChannelUrl) {
        return getSharedPreferences().getLong(PREFERENCE_KEY_GROUP_CHANNEL_LAST_READ + groupChannelUrl, Long.MAX_VALUE);
    }

    public static boolean getGroupChannelDistinct() {
        return getSharedPreferences().getBoolean(PREFERENCE_KEY_GROUP_CHANNEL_DISTINCT, true);
    }

    public static void saveBaseUrl(String baseUrl) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(SENDBIRD_BASE_URL, baseUrl).apply();
    }

    public static String getBaseUrl() {
        return getSharedPreferences().getString(SENDBIRD_BASE_URL, null);
    }

    public static void saveVersion(String version) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(VERSION, version).apply();
    }

    public static String getVersion() {
        return getSharedPreferences().getString(VERSION, null);
    }
}
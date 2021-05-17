package com.sendbird.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

public class PreferenceUtils {

    private static final String PREFERENCE_KEY_USER_ID = "userId";
    private static final String PREFERENCE_KEY_ACCESS_TOKEN = "accessToken";
    private static final String PREFERENCE_KEY_NICKNAME = "nickname";
    private static final String PREFERENCE_COUNTDOWN_TIME = "countdown";
    private static final String PREFERENCE_KEY_CONNECTED = "connected";

    private static final String PREFERENCE_KEY_NOTIFICATIONS = "notifications";
    private static final String PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS = "notificationsShowPreviews";
    private static final String PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB = "notificationsDoNotDisturb";
    private static final String PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_FROM = "notificationsDoNotDisturbFrom";
    private static final String PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_TO = "notificationsDoNotDisturbTo";
    private static final String PREFERENCE_KEY_GROUP_CHANNEL_DISTINCT = "channelDistinct";
    private static final String PREFERENCE_KEY_GROUP_CHANNEL_LAST_READ = "last_read";
    private static final Gson gson = new Gson();
    private static Context mAppContext;

    // Prevent instantiation
    private PreferenceUtils() {
    }

    public static void init(Context appContext) {
        mAppContext = appContext;
    }

    public static Context getContext() {
        return mAppContext;
    }

    private static SharedPreferences getSharedPreferences() {
        return mAppContext.getSharedPreferences("sendbird", Context.MODE_PRIVATE);
    }

    public static String getUserId() {
        return getSharedPreferences().getString(PREFERENCE_KEY_USER_ID, "");
    }

    public static void setUserId(String userId) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_USER_ID, userId).apply();
    }

    public static void setLastRead(String groupChannelUrl, long ts) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putLong(PREFERENCE_KEY_GROUP_CHANNEL_LAST_READ + groupChannelUrl, ts).apply();
    }

    public static long getLastRead(String groupChannelUrl) {
        return getSharedPreferences().getLong(PREFERENCE_KEY_GROUP_CHANNEL_LAST_READ + groupChannelUrl, Long.MAX_VALUE);
    }

    public static String getAccessToken() {
        return getSharedPreferences().getString(PREFERENCE_KEY_ACCESS_TOKEN, "");
    }

    public static void setAccessToken(String accessToken) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_ACCESS_TOKEN, accessToken).apply();
    }

    public static String getNickname() {
        return getSharedPreferences().getString(PREFERENCE_KEY_NICKNAME, "");
    }

    public static void setNickname(String nickname) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_NICKNAME, nickname).apply();
    }

    @Nullable
    public static HashMap<String, Integer> getEndTime() {
        Type type = new TypeToken<HashMap<String, Integer>>() {
        }.getType();
        return gson.fromJson(getSharedPreferences().getString(PREFERENCE_COUNTDOWN_TIME, ""), type);
    }

    public static void setEndTime(HashMap<String, Integer> endTimeMap) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        HashMap<String, Integer> endTime = getEndTime();
        if (endTime != null) {
            endTime.putAll(endTimeMap);
        }else{
            endTime = endTimeMap;
        }
        editor.putString(PREFERENCE_COUNTDOWN_TIME, gson.toJson(endTime)).apply();
    }

    public static boolean getConnected() {
        return getSharedPreferences().getBoolean(PREFERENCE_KEY_CONNECTED, false);
    }

    public static void setConnected(boolean tf) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PREFERENCE_KEY_CONNECTED, tf).apply();
    }

    public static void clearAll() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.clear().apply();
    }

    public static boolean getNotifications() {
        return getSharedPreferences().getBoolean(PREFERENCE_KEY_NOTIFICATIONS, true);
    }

    public static void setNotifications(boolean notifications) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS, notifications).apply();
    }

    public static boolean getNotificationsShowPreviews() {
        return getSharedPreferences().getBoolean(PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS, true);
    }

    public static void setNotificationsShowPreviews(boolean notificationsShowPreviews) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS, notificationsShowPreviews).apply();
    }

    public static boolean getNotificationsDoNotDisturb() {
        return getSharedPreferences().getBoolean(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB, false);
    }

    public static void setNotificationsDoNotDisturb(boolean notificationsDoNotDisturb) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB, notificationsDoNotDisturb).apply();
    }

    public static String getNotificationsDoNotDisturbFrom() {
        return getSharedPreferences().getString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_FROM, "");
    }

    public static void setNotificationsDoNotDisturbFrom(String notificationsDoNotDisturbFrom) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_FROM, notificationsDoNotDisturbFrom).apply();
    }

    public static String getNotificationsDoNotDisturbTo() {
        return getSharedPreferences().getString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_TO, "");
    }

    public static void setNotificationsDoNotDisturbTo(String notificationsDoNotDisturbTo) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_TO, notificationsDoNotDisturbTo).apply();
    }

    public static boolean getGroupChannelDistinct() {
        return getSharedPreferences().getBoolean(PREFERENCE_KEY_GROUP_CHANNEL_DISTINCT, true);
    }

    public static void setGroupChannelDistinct(boolean channelDistinct) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(PREFERENCE_KEY_GROUP_CHANNEL_DISTINCT, channelDistinct).apply();
    }
}

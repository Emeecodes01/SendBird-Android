package com.ulesson.chat.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ulesson.chat.main.model.Question;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

public class PreferenceUtils {

    private static final String PREFERENCE_KEY_USER_ID = "userId";
    private static final String PREFERENCE_KEY_ACCESS_TOKEN = "accessToken";
    private static final String PENDING_QUESTIONS = "pendingQuestions";
    private static final String PREFERENCE_KEY_NICKNAME = "nickname";
    private static final String PREFERENCE_COUNTDOWN_TIME = "countdown";
    private static final String PREFERENCE_AUDIO_FILE = "audioFile";
    private static final String PREFERENCE_KEY_CONNECTED = "connected";
    private static final String PREFERENCE_MASTER_TOKEN = "masterToken";
    private static final String PREFERENCE_PACKAGE_NAME = "packageName";
    private static final String PREFERENCE_APP_ID = "appId";

    private static final String PREFERENCE_KEY_NOTIFICATIONS = "notifications";
    private static final String PREFERENCE_KEY_NOTIFICATIONS_SHOW_PREVIEWS = "notificationsShowPreviews";
    private static final String PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB = "notificationsDoNotDisturb";
    private static final String PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_FROM = "notificationsDoNotDisturbFrom";
    private static final String PREFERENCE_KEY_NOTIFICATIONS_DO_NOT_DISTURB_TO = "notificationsDoNotDisturbTo";
    private static final String PREFERENCE_KEY_GROUP_CHANNEL_DISTINCT = "channelDistinct";
    private static final String PREFERENCE_KEY_GROUP_CHANNEL_LAST_READ = "last_read";
    private static final Gson gson = new Gson();
    private static Context mAppContext;
    static String masterToken;

    // Prevent instantiation
    private PreferenceUtils() {
    }

    public static void init(Context appContext, String masterToken, String packageName, String appId) {
        mAppContext = appContext;
        setMasterToken(masterToken);
        setPackageName(packageName);
        setAppId(appId);
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

    public static String getMasterToken() {
        return getSharedPreferences().getString(PREFERENCE_MASTER_TOKEN, "");
    }

    public static void setMasterToken(String masterToken) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_MASTER_TOKEN, masterToken).apply();
    }

    public static String getPackageName() {
        return getSharedPreferences().getString(PREFERENCE_PACKAGE_NAME, "");
    }

    public static void setPackageName(String packageName) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_PACKAGE_NAME, packageName).apply();
    }

    public static String getAppId() {
        return getSharedPreferences().getString(PREFERENCE_APP_ID, "");
    }

    public static void setAppId(String appId) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREFERENCE_APP_ID, appId).apply();
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

    public static List<Question> getPendingQuestions() {
        Type type = new TypeToken<List<Question>>
                () {
        }.getType();
        return gson.fromJson(getSharedPreferences().getString(PENDING_QUESTIONS, ""), type);
    }

    public static void setPendingQuestions(String pendingQuestions) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PENDING_QUESTIONS, pendingQuestions).apply();
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
        } else {
            endTime = endTimeMap;
        }
        editor.putString(PREFERENCE_COUNTDOWN_TIME, gson.toJson(endTime)).apply();
    }


    @Nullable
    public static HashMap<String, String> getAudioFiles() {
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        return gson.fromJson(getSharedPreferences().getString(PREFERENCE_AUDIO_FILE, ""), type);
    }

    public static void setAudioFile(HashMap<String, String> audioMap) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        HashMap<String, String> audioFiles = getAudioFiles();
        if (audioFiles != null) {
            audioFiles.putAll(audioMap);
        } else {
            audioFiles = audioMap;
        }
        editor.putString(PREFERENCE_AUDIO_FILE, gson.toJson(audioFiles)).apply();
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

}

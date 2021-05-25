package com.sendbird.android.sample.utils;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextUtils {

    public static String THEME_MATH = "mathematics_english";

    public static String getGroupChannelTitle(GroupChannel channel) {
        List<Member> members = channel.getMembers();

        if (members.size() < 2 || PreferenceUtils.getUserId().isEmpty()) {
            return "No Members";
        } else if (members.size() == 2) {
            StringBuffer names = new StringBuffer();
            for (Member member : members) {
                if (member.getUserId().equals(PreferenceUtils.getUserId())) {
                    continue;
                }

                names.append(", " + member.getNickname());
            }
            return names.delete(0, 2).toString();
        } else {
            int count = 0;
            StringBuffer names = new StringBuffer();
            for (User member : members) {
                if (member.getUserId().equals(PreferenceUtils.getUserId())) {
                    continue;
                }

                count++;
                names.append(", " + member.getNickname());

                if(count >= 10) {
                    break;
                }
            }
            return names.delete(0, 2).toString();
        }
    }

    /**
     * Calculate MD5
     * @param data
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String generateMD5(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(data.getBytes());
        byte messageDigest[] = digest.digest();

        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < messageDigest.length; i++)
            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

        return hexString.toString();
    }

    public static boolean isEmpty(CharSequence text) {
        return text == null || text.length() == 0;
    }

    public static HashMap<String, Object> toMap(String value) {
        value = value.substring(1, value.length()-1);           //remove curly brackets
        String[] keyValuePairs = value.split(",");              //split the string to creat key-value pairs
        HashMap<String, Object> map = new HashMap<>();

        for(String pair : keyValuePairs) {                   //iterate over the pairs{
            String[] entry = pair.split("=");//split the pairs to get key and value
            try {
                map.put(entry[0].trim(), entry[1].trim());
            }catch (Exception e) {
                e.printStackTrace();
                map.put(entry[0].trim(), "");
            }
        }
        return map;
    }
}

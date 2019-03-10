package com.stfalcon.chatkit.sample.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ToanNMDev on 3/13/2017.
 */

public class SharedPref {

    public static final String KEY_AVATAR = "KEY_AVATAR";
    public static final String KEY_AVATAR_IMAGE = "KEY_AVATAR_IMAGE";
    public static final String KEY_STATUS_COLOR = "KEY_STATUS_COLOR";
    public static final String KEY_CHAT_COLOR = "KEY_CHAT_COLOR";
    public static final String KEY_CHAT_NAME = "KEY_CHAT_NAME";
    public static final String KEY_CHAT_NUMBER_OF_MEMBERS = "KEY_CHAT_NUMBER_OF_MEMBERS";

    private static final String MY_SHARED_PREFERENCE = "com.stfalcon.chatkit.test.MySharedPreference";

    public static void saveInt(Context context, String key, int value) {
        SharedPreferences pref = context.getSharedPreferences(
                MY_SHARED_PREFERENCE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getInt(Context context, String key) {
        return getInt(context, key, 0);
    }

    public static int getInt(Context context, String key, int defaultValue) {
        int ret = 0;
        try {
            SharedPreferences pref = context.getSharedPreferences(
                    MY_SHARED_PREFERENCE, Context.MODE_PRIVATE);

            ret = pref.getInt(key, defaultValue);
        } catch (Exception e) {

        }
        return ret;
    }

    public static void saveString(Context context, String key, String value) {
        SharedPreferences pref = context.getSharedPreferences(
                MY_SHARED_PREFERENCE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(Context context, String key) {
        return getString(context, key, "");
    }

    public static String getString(Context context, String key, String defaultValue) {
        String ret = defaultValue;
        try {
            SharedPreferences pref = context.getSharedPreferences(
                    MY_SHARED_PREFERENCE, Context.MODE_PRIVATE);

            ret = pref.getString(key, defaultValue);
            if (ret.equals(""))
                ret = defaultValue;
        } catch (Exception e) {
        }
        return ret;
    }

}

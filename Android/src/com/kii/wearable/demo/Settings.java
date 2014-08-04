package com.kii.wearable.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

/**
 * Created by tian on 14-6-24.
 */
public class Settings {
    public static final String PREF_NAME = "preferences";
    private static final String USER_ID = "user_id";
    private static final String USER_TOKEN = "user_token";
    static Context gContext = null;

    @SuppressLint("InlinedApi")
    public static SharedPreferences getPrefs(Context context) {
        if (gContext == null && context != null) {
            gContext = context.getApplicationContext();
        }
        int code = Context.MODE_PRIVATE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            code = Context.MODE_MULTI_PROCESS;
        }
        return gContext.getSharedPreferences(PREF_NAME, code);
    }

    public static void setUserToken(Context context, String userToken) {
        getPrefs(context).edit().putString(USER_TOKEN, userToken).commit();
    }

    public static String getUserToken(Context context) {
        return getPrefs(context).getString(USER_TOKEN, "");
    }

    public static void setUserId(Context context, String userId) {
        getPrefs(context).edit().putString(USER_ID, userId).commit();
    }

    public static String getUserId(Context context) {
        return getPrefs(context).getString(USER_ID, "");
    }

    public static boolean isLoggedIn(Context context) {
        return !TextUtils.isEmpty(getUserId(context)) && !TextUtils.isEmpty(getUserToken(context));
    }

}

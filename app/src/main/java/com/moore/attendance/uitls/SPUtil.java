package com.moore.attendance.uitls;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by binbin on 2017/3/6.
 */

public class SPUtil {
    private static String fileName = "attendance";

    public static void saveString(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sp.getString(key, "unDefined");
    }
}

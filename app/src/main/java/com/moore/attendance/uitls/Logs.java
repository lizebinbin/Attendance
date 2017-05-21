package com.moore.attendance.uitls;

import android.util.Log;

import com.moore.attendance.base.Command;

/**
 * 日志管理
 * Created by MooreLi on 2017/3/1.
 */

public class Logs {
    public static void i(String tag, String info) {
        if (Command.isDebug) {
            Log.i(tag, info);
        }
    }

    public static void v(String tag, String info) {
        if (Command.isDebug) {
            Log.v(tag, info);
        }
    }

    public static void d(String tag, String info) {
        if (Command.isDebug) {
            Log.d(tag, info);
        }
    }

    public static void w(String tag, String info) {
        if (Command.isDebug) {
            Log.w(tag, info);
        }
    }

    public static void e(String tag, String info) {
        if (Command.isDebug) {
            Log.e(tag, info);
        }
    }
}

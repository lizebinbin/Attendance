package com.moore.attendance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.moore.attendance.uitls.Logs;

/**
 * 开机自启动广播监听
 * Created by binbin on 2017/3/11.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //暂时屏蔽不做
//        //开启广播
//        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {     // boot
//            Intent intent2 = new Intent(context, MainActivity.class);
//            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent2);
//            Logs.e("BootReceiver","开机广播，自启");
//        }
    }
}

package com.moore.attendance.base;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.moore.attendance.MainActivity;
import com.moore.attendance.udp.SendPackageService;
import com.moore.attendance.uitls.Logs;

import java.util.List;

/**
 * Created by MooreLi on 2017/3/20.
 */

public class WakeUpService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.sendEmptyMessage(1);
        Logs.e("TEST", "考勤：WakeUpService startCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    //循环查询
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                wakeUpService();
                mHandler.sendEmptyMessageDelayed(1, 10000);
            }
        }
    };

    private void wakeUpService() {
        boolean isFind = false;
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        Intent serviceIntent = new Intent();

        List<ActivityManager.RunningServiceInfo> runningServices = mActivityManager.getRunningServices(100);
        if (runningServices != null) {
            for (ActivityManager.RunningServiceInfo runningService : runningServices) {
                if (runningService.process.contains(":WakeUpAttendance")) {
//                    Logs.e("TEST", "考勤: find the service");
                    isFind = true;
                    break;
                }
            }
        }
        if (!isFind) {
//            serviceIntent.setPackage("com.moore.wakeupattendance");
//            serviceIntent.setAction("com.moore.wakeupattendance.WakeUpService");
//            startService(serviceIntent);
            Logs.e("TEST", "考勤: 开始唤醒WakeUpAttendance的Service");
            try {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName cn = new ComponentName("com.moore.wakeupattendance", "com.moore.wakeupattendance.MainActivity");
                intent.setComponent(cn);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Logs.e("TEST", "考勤: 不需要唤醒");
        }

//        Intent sendService = new Intent(this, SendPackageService.class);
//        startService(sendService);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        mHandler.removeMessages(1);
        Logs.e("TEST","考勤：WakeUpService onDestroy");
    }
}

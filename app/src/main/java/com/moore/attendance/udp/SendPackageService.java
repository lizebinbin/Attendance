package com.moore.attendance.udp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.moore.attendance.uitls.Logs;

/**
 * Created by binbin on 2017/3/7.
 */

public class SendPackageService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Logs.e("TEST", "考勤：SendPackageService onCreate");
        registerUdp();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logs.e("TEST", "考勤：SendPackageService startCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.e("TEST", "考勤：SendPackageService onDestroy");
        UdpHandler.getInstance().sendEmptyMessage(UdpHandler.MSG_END);
        stopSelf();
//        mReceiveThread.IsThreadDisable = true;
    }

    private void registerUdp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                ConnectManager.getInstance().connect();
                Looper.loop();
            }
        }).start();
    }

}

package com.moore.attendance.base;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.moore.attendance.jniutils.SerialPort;
import com.moore.attendance.jniutils.SerialPortFinder;
import com.moore.attendance.uitls.CrashHandler;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;

/**
 * Created by MooreLi on 2017/3/1.
 */

public class MyApplication extends Application {

    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mSerialPort = null;

//    String[] allDevices;
//    String[] allDevicesPath;

    private static MyApplication instance;
    public static MyApplication getInstance(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
//        allDevices = mSerialPortFinder.getAllDevices();
//        allDevicesPath = mSerialPortFinder.getAllDevicesPath();
//        if (allDevices != null)
//            Toast.makeText(this, "allDevices:" + Arrays.toString(allDevices), Toast.LENGTH_LONG).show();
//        else
//            Toast.makeText(this, "allDevices:  allDevices == null", Toast.LENGTH_LONG).show();
//        handler.sendEmptyMessageDelayed(1,2000);
        //开启崩溃捕捉
        CrashHandler.getInstance().init(this);
    }

//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (msg.what == 1) {
//                if (allDevicesPath != null)
//                    Toast.makeText(MyApplication.this, "allDevicesPath:" + Arrays.toString(allDevicesPath), Toast.LENGTH_LONG).show();
//                else
//                    Toast.makeText(MyApplication.this, "allDevicesPath:  allDevicesPath == null", Toast.LENGTH_LONG).show();
//            }
//        }
//    };

    private Activity mainActivity;
    public void addMain(Activity activity){
        mainActivity = activity;
    }

    public Activity getMain(){
        if(mainActivity != null)
            return mainActivity;
        return null;
    }

    public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            /* Read serial port parameters */
//            SharedPreferences sp = getSharedPreferences("android_serialport_api.sample_preferences", MODE_PRIVATE);
//            String path = sp.getString("DEVICE", "");
//            int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1"));
//
//			/* Check parameters */
//            if ((path.length() == 0) || (baudrate == -1)) {
////                throw new InvalidParameterException();
//            }
//
//			/* Open the serial port */

            mSerialPort = new SerialPort(new File("/dev/ttyS3"), 9600, 0);

        }
        return mSerialPort;
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }
}

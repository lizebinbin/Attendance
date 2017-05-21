package com.moore.attendance.jniutils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.moore.attendance.base.Command;
import com.moore.attendance.base.MyApplication;
import com.moore.attendance.uitls.Logs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by MooreLi on 2017/3/1.
 */

public abstract class SerialPortActivity extends Activity {
    protected MyApplication mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = MyApplication.getInstance();
        openSerialPortAndStartReceive();
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(this, "destroy colse the serialPort", Toast.LENGTH_SHORT).show();
        closeSerialPort();
        super.onDestroy();
    }

    protected void openSerialPortAndStartReceive() {
        if (mSerialPort == null) {
            try {
                mSerialPort = mApplication.getSerialPort();
                mOutputStream = mSerialPort.getOutputStream();
                mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
                mReadThread = new ReadThread();
                mReadThread.start();
//            Toast.makeText(SerialPortActivity.this,"find the port and start receive data",Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
                DisplayError("You do not have read/write permission to the serial port.");
            } catch (IOException e) {
                DisplayError("The serial port can not be opened for an unknown reason.");
            } catch (InvalidParameterException e) {
                DisplayError("Please configure your serial port first.");
            }
        }
    }

    protected void closeSerialPort() {
        if (mReadThread != null)
            mReadThread.interrupt();
        mApplication.closeSerialPort();
        mSerialPort = null;
    }

    private void DisplayError(String message) {
//        AlertDialog.Builder b = new AlertDialog.Builder(this);
//        b.setTitle("Error");
//        b.setMessage(message);
//        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
////                SerialPortActivity.this.finish();
//            }
//        });
//        b.show();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected abstract void onDataReceived(final byte[] buffer, final int size);

    /**
     * 接收数据线程
     */
    public class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                final byte[] buffer;
                try {
                    if (Command.isSerialPortCardStatus) {
                        buffer = new byte[128];
                        if (mInputStream == null) return;
                        size = mInputStream.read(buffer);
                    } else {
                        byte[] buffer2 = new byte[256];
                        if (mInputStream == null)
                            return;
                        size = mInputStream.read(buffer2);
                        buffer = new byte[size - 1];
                        System.arraycopy(buffer2, 0, buffer, 0, size - 1);
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SerialPortActivity.this,"info:"+Arrays.toString(buffer),Toast.LENGTH_SHORT).show();
                        }
                    });
                    /**
                     * 将读取到的数据存到文件，方便查看
                     */
//                    buffer = new byte[]{55, -90, 43, 65, 23, 65};
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                    File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/attendance/receive");
                    if (!fileDir.exists())
                        fileDir.mkdirs();
                    long timestamp = System.currentTimeMillis();
                    String time = formatter.format(new Date());
                    String fileName = "receive-" + time + "-" + timestamp + ".txt";
                    File file = new File(fileDir, fileName);
                    Logs.e("Serial","fileName:"+fileName);
//                    FileOutputStream fos = new FileOutputStream(file);
//                    String receiveInfo;
//                    if (size > 0) {
//                        receiveInfo = "info  " + Arrays.toString(buffer);
//                    } else {
//                        receiveInfo = "info  size == 0";
//                    }
//                    fos.write(receiveInfo.getBytes());
//                    fos.close();

                    if (size > 0 && !Command.isTakePhoto) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };
}

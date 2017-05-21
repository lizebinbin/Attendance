package com.moore.attendance.udp;

import android.os.Handler;
import android.os.Message;

import com.moore.attendance.MainActivity;
import com.moore.attendance.base.Command;
import com.moore.attendance.base.MyApplication;
import com.moore.attendance.uitls.Logs;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by binbin on 2017/3/8.
 */

public class ConnectManager {
    private final String TAG = "ConnectManager";
    private static DatagramSocket mSocket;
    private static ConnectManager sInstance;
    private int sendNum = 0;

    private SendThread mSendThread;
    private ReceiveThread mReceiveThread;

    private ConnectManager() {
    }

    public static ConnectManager getInstance() {
        if (sInstance == null) {
            synchronized (ConnectManager.class) {
                if (sInstance == null) {
                    sInstance = new ConnectManager();
                }
            }
        }
        return sInstance;
    }

    public void connect() {
        if (mSocket == null || mSocket.isClosed()) {
            try {
                //获取连接
                mSocket = new DatagramSocket();
                Logs.e("Manager", "connect");
                mReceiveThread = new ReceiveThread();
                mReceiveThread.start();

                UdpHandler.getInstance().setIsReConnect(false);
                Message msg = new Message();
                msg.obj = "1001";
                msg.what = UdpHandler.MSG_SEND;
                UdpHandler.getInstance().sendMessage(msg);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    public void disConnect() {
        if (mSocket != null && !mSocket.isClosed()) {
            if (mReceiveThread != null && !mReceiveThread.isInterrupted()) {
                mReceiveThread.interrupt();
            }
            if (mSendThread != null && !mSendThread.isInterrupted()) {
                mSendThread.interrupt();
            }
            mSocket.close();
//            mSocket = null;
        }
        //退出程序
        if (MyApplication.getInstance().getMain() != null) {
            Logs.e("CrashHandler", "finishService");
            MainActivity activity = (MainActivity) MyApplication.getInstance().getMain();
            activity.finishService();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void sendPackageRegister() {
        String content = "(1001," + Command.deviceId + ",register)";
        restartSend(content);
    }

    public void sendPackagePing() {
        if (sendNum > 2) {
            Logs.e("ConnectionManager", "三次没收到1002回复，重新注册连接");
            sendNum = 0;
            UdpHandler.getInstance().setIsRecieveRegister(false);
            UdpHandler.getInstance().setIsReConnect(true);
            ConnectManager.this.disConnect();
            ConnectManager.this.connect();
            return;
        }
        String content = "(1002," + Command.deviceId + ",heartbeat)";
        restartSend(content);
    }

    public void sendPackageOk(String cardNo) {
        String content = "(1003," + Command.deviceId + "," + cardNo + ",ok)";
        restartSend(content);
    }

    public void sendPackageDownload(String url) {
        String content = "(1006," + Command.deviceId + "," + url + ",ok)";
        restartSend(content);
    }

    public void sendPackageDownloadSuccess(String url) {
        String content = "(1007," + Command.deviceId + "," + url + ",ok)";
        restartSend(content);
    }

    private void restartSend(String content) {
        if (mSendThread != null && !mSendThread.isInterrupted()) {
            mSendThread.interrupt();
        }
        mSendThread = null;
        mSendThread = new SendThread(content);
        mSendThread.start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 2) {
                mReceiveThread.interrupt();
                mReceiveThread = null;
                mReceiveThread = new ReceiveThread();
                mReceiveThread.start();
            }
        }
    };

    public class SendThread extends Thread {
        DatagramPacket packet;

        public SendThread(String content) {
            byte[] datas = content.getBytes();
            if (content.contains("heartbeat")) {
                sendNum++;
            }
            InetAddress address = null;
            try {
                address = InetAddress.getByName(Command.udp_address);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            if (address != null)
                packet = new DatagramPacket(datas, datas.length, address, Command.udp_port);
            else {
                if (listener != null)
                    listener.toastInfo("解析UDP通信地址失败！");
            }
        }

        @Override
        public void run() {
            super.run();
            try {
                if (mSocket == null || mSocket.isClosed())
                    return;
                if (packet != null && packet.getAddress() != null) {
                    mSocket.send(packet);
                    Logs.e("ConnectManager", "send success data is:" + new String(packet.getData()));
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            super.run();
            if (mSocket == null || mSocket.isClosed())
                return;
            try {
                byte datas[] = new byte[512];
                InetAddress address = InetAddress.getByName(Command.udp_address);
                DatagramPacket packet = new DatagramPacket(datas, datas.length, address, Command.udp_port);
                mSocket.receive(packet);
                String receiveMsg = new String(packet.getData()).trim();
                //回调在首页显示
                if (listener != null) {
                    listener.sendUdpInfo(receiveMsg);
                }
                Logs.e("ConnectManager", "receive msg data is:" + receiveMsg);
                if (receiveMsg.contains("register")) {
                    //收到1001回复，发送1002
//                    Logs.e("ConnectionManager", "收到1001回复，发送1002");
                    Message msg = new Message();
                    msg.what = UdpHandler.MSG_SEND;
                    UdpHandler.getInstance().setIsRecieveRegister(true);
                    msg.obj = "1002";
                    UdpHandler.getInstance().sendMessage(msg);
                }
                if (receiveMsg.contains("heartbeat")) {
//                    Logs.e("ConnectionManager", "收到心跳包回复，sendNum置0");
                    sendNum = 0;
                }
                if (receiveMsg.contains("photograph")) {
//                    Logs.e("ConnectionManager", "收到开启摄像头指令，打开拍照界面");
                    if (Command.isTakePhoto) {
                        Logs.e(TAG, "正在拍照，不能开启摄像头！");
                        return;
                    }
                    Command.isTakePhoto = true;
                    String[] messages = receiveMsg.split(",");
                    sendPackageOk(messages[2]);
                    if (listener != null) {
                        listener.openCamera(messages[2]);
                    }
                }
                if (receiveMsg.contains("downloadVideo")) {
                    String[] messages = receiveMsg.split(",");
                    sendPackageDownload(messages[2]);
                    if (listener != null) {
                        listener.downloadVideo(messages[2]);
                    }
                }
                mHandler.sendEmptyMessage(2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private UdpSocketListener listener;

    public void setOnOpenCameraListener(UdpSocketListener listener) {
        this.listener = listener;
    }

    public interface UdpSocketListener {
        void openCamera(String cardNo);

        void sendUdpInfo(String udpInfo);

        void downloadVideo(String videoInfo);

        void toastInfo(String info);
    }

}

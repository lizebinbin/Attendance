package com.moore.attendance.udp;

import android.os.Handler;
import android.os.Message;

import com.moore.attendance.uitls.Logs;

/**
 * Created by binbin on 2017/3/8.
 */

public class UdpHandler extends Handler{
    private static UdpHandler sInstance;
    public static final int MSG_SEND = 1;
    public static final int MSG_END = 2;
    private UdpHandler(){}
    public static UdpHandler getInstance(){
        if(sInstance == null){
            synchronized (UdpHandler.class){
                if(sInstance == null){
                    sInstance = new UdpHandler();
                }
            }
        }
        return sInstance;
    }

    private boolean isReceiveRegister = false;
    private boolean isReConnect = false;

    public void setIsRecieveRegister(boolean flag){
        this.isReceiveRegister = flag;
    }

    public void setIsReConnect(boolean flag){
        this.isReConnect = flag;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if(msg.what == MSG_SEND){
            if(isReConnect)
                return;
            String type = (String) msg.obj;
            Message msgSend = new Message();
            msgSend.what = MSG_SEND;
            msgSend.obj = type;

            if("1001".equals(type) && !isReceiveRegister){
                ConnectManager.getInstance().sendPackageRegister();
                this.sendMessageDelayed(msgSend,10000);
            }else if("1002".equals(type) && isReceiveRegister){
                ConnectManager.getInstance().sendPackagePing();
                this.sendMessageDelayed(msgSend,10000);
            }else if("1003".equals(type) && isReceiveRegister){
                //ConnectManager.getInstance().sendPackagePing();
                this.sendMessageDelayed(msgSend,10000);

            }
        }else if(msg.what == MSG_END){
            ConnectManager.getInstance().disConnect();
//            ConnectManager.getInstance().connect();
        }
    }
}

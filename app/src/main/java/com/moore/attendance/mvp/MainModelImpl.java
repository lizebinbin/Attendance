package com.moore.attendance.mvp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.moore.attendance.MainActivity;
import com.moore.attendance.base.Command;
import com.moore.attendance.bean.ParseResponse;
import com.moore.attendance.callback.ModelCallback;
import com.moore.attendance.udp.ConnectManager;
import com.moore.attendance.uitls.FileUtils;
import com.moore.attendance.uitls.Logs;
import com.moore.attendance.uitls.ParseDataUtil;
import com.moore.attendance.uitls.SPUtil;
import com.moore.attendance.upload.UploadUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 业务逻辑层
 * Created by MooreLi on 2017/5/2.
 */

public class MainModelImpl implements MainModel, ModelCallback, ConnectManager.UdpSocketListener {
    private MainActivity mainView;
    private MainPresenter mainPresenter;

    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEEE");
    private TimeChangeHandler mHandler;
    private UploadUtil uploadUtil;

    private List<Bitmap> adImageList;
    private int mCurrentImageIndex;

    public MainModelImpl(MainActivity view, MainPresenter mainPresenter) {
        mainView = view;
        this.mainPresenter = mainPresenter;
        mHandler = new TimeChangeHandler();
        ConnectManager.getInstance().setOnOpenCameraListener(this);
        uploadUtil = new UploadUtil(mainView);
    }

    /**
     * 从sd卡中获取图片
     */
    @Override
    public void getAdImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Bitmap> list = FileUtils.getAdImage();
                if (list != null && list.size() > 0) {
                    loadAdImageCallback(true, list);
                } else {
                    loadAdImageCallback(false, null);
                }
            }
        }).start();
    }

    /**
     * 显示时间
     */
    @Override
    public void getCurrentTime() {
        mHandler.sendEmptyMessage(MESSAGE_CHANGE_TIME);
    }

    /**
     * 读取设置
     */
    @Override
    public void readSetting() {
        String deviceNo = SPUtil.getString(mainView, Command.KEY_DEVICE_NO);
        if (null != deviceNo && !"unDefined".equals(deviceNo))
            Command.deviceId = deviceNo;
        String serviceIp = SPUtil.getString(mainView, Command.serviceIp);
        if (null != serviceIp && !"unDefined".equals(serviceIp))
            Command.serviceIp = serviceIp;
        String servicePort = SPUtil.getString(mainView, Command.KEY_SERVICE_PORT);
        if (null != servicePort && !"unDefined".equals(servicePort))
            Command.servicePort = servicePort;
        String serviceAddress = SPUtil.getString(mainView, Command.KEY_SERVICE_ADDRESS);
        if (null != serviceAddress && !"unDefined".equals(serviceAddress))
            Command.serviceAddress = serviceAddress;
        mainPresenter.showDeviceNum("设备编号:" + Command.deviceId);
        String videoStatus = SPUtil.getString(mainView, Command.KEY_VIDEO_STATUS);
        if (null != videoStatus && "open".equals(videoStatus)) {
            Command.isVideoOpen = true;
        } else {
            Command.isVideoOpen = false;
        }
        String serialPortStatus = SPUtil.getString(mainView, Command.KEY_SERIAL_PORT_STATUS);
        if (null != serialPortStatus && "ICCard".equals(serialPortStatus)) {
            Command.isSerialPortCardStatus = true;
        } else {
            Command.isSerialPortCardStatus = false;
        }
        mainPresenter.videoFunctionControl(Command.isVideoOpen);
    }


    /**
     * 打开相机
     *
     * @param cardNo 卡号
     */
    @Override
    public void openCamera(String cardNo) {
        Command.isTakePhoto = true;
        Message message = Message.obtain();
        message.what = MESSAGE_WHAT_OPEN_CAMERA;
        message.obj = cardNo;
        mHandler.sendMessage(message);
    }

    /**
     * udp通信收到的信息
     *
     * @param udpInfo 收到的消息详情
     */
    @Override
    public void sendUdpInfo(final String udpInfo) {
        Message message = Message.obtain();
        message.what = MESSAGE_WHAT_RECEIVE_DATA;
        message.obj = udpInfo;
        mHandler.sendMessage(message);
    }

    /**
     * 下载视频
     *
     * @param videoInfo 视频地址、信息
     */
    @Override
    public void downloadVideo(String videoInfo) {
        if (videoInfo != null && !"".equals(videoInfo)) {
            Message message = Message.obtain();
            message.what = MESSAGE_WHAT_DOWNLOAD_VIDEO;
            message.obj = videoInfo;
            mHandler.sendMessage(message);
        }
    }

    @Override
    public void toastInfo(String info) {
        Message message = Message.obtain();
        message.what = MESSAGE_WHAT_TOAST_INFO;
        message.obj = info;
        mHandler.sendMessage(message);
    }

    /**
     * 处理刷卡
     *
     * @param info 刷卡信息
     */
    @Override
    public void handleCardInfo(byte[] info) {
        Message message = Message.obtain();
        message.what = MESSAGE_WHAT_CARD_DATA;
        message.obj = info;
        mHandler.sendMessage(message);
    }

    /**
     * 读取图片回调
     *
     * @param isSuccess
     * @param imgList
     */
    @Override
    public void loadAdImageCallback(boolean isSuccess, List<Bitmap> imgList) {
        if (isSuccess) {
            adImageList = imgList;
            mHandler.sendEmptyMessage(MESSAGE_CHANGE_IMAGE);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mainPresenter.showToast("没有找到图片！");
                }
            });
        }
    }

    /**
     * 线程处理
     */
    public class TimeChangeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MESSAGE_CHANGE_TIME) {
                String timeStr = mFormat.format(System.currentTimeMillis());
                mainPresenter.showCurrentTime(timeStr);
                this.sendEmptyMessageDelayed(MESSAGE_CHANGE_TIME, TIME_CHANGE_STEP);
            } else if (msg.what == MESSAGE_CHANGE_IMAGE) {
                if (mCurrentImageIndex < adImageList.size() - 1) {
                    mCurrentImageIndex++;
                } else {
                    mCurrentImageIndex = 0;
                }
                mainPresenter.showAdImage(adImageList.get(mCurrentImageIndex));
                mHandler.sendEmptyMessageDelayed(2, IMAGE_CHANGE_TIME);
            } else if (msg.what == MESSAGE_WHAT_CARD_DATA) {
                if (Command.isTakePhoto) {
                    mainPresenter.showToast("正在拍照，不能刷卡！");
                    return;
                }
                byte[] info = (byte[]) msg.obj;
                ParseResponse response = ParseDataUtil.getCardData(info);
                if (response.getCode() == ParseDataUtil.PARSE_WRONG) {
                    mainPresenter.showToast(response.getData());
                    return;
                }
                String cardNum = response.getData();
                mainPresenter.showToast("卡号为：" + cardNum);
                mainPresenter.openCamera(cardNum);
                mainPresenter.closeSerialPort();
                mainPresenter.openSerialPort();
            } else if (msg.what == MESSAGE_WHAT_RECEIVE_DATA) {
                String receiveInfo = (String) msg.obj;
                mainPresenter.showUdpInfo(receiveInfo);
            } else if (msg.what == MESSAGE_WHAT_OPEN_CAMERA) {
                String cardNum = (String) msg.obj;
                mainPresenter.openCamera(cardNum);
            } else if (msg.what == MESSAGE_WHAT_DOWNLOAD_VIDEO) {
                String videoInfo = (String) msg.obj;
                uploadUtil.downloadFile(videoInfo, FileUtils.getVideoDownloadPath(), new DownloadProgressListener());
            } else if (msg.what == MESSAGE_WHAT_UPDATE_NOTIFICATION) {
                Bundle bundle = msg.getData();
                mainPresenter.updateNotification(bundle.getString("fileUrl"), bundle.getLong("total"), bundle.getLong("current"));
            } else if (msg.what == MESSAGE_WHAT_PLAY_VIDEO) {
                Bundle bundle = msg.getData();
                File file = (File) bundle.getSerializable("file");
                mainPresenter.playVideo(file);
            } else if (msg.what == MESSAGE_WHAT_TOAST_INFO) {
                String info = (String) msg.obj;
                mainPresenter.showToast(info);
            }
        }
    }

    private class DownloadProgressListener implements UploadUtil.OnDownloadListener {

        @Override
        public void onCurrentProgress(String fileUrl, long totalLength, long currentLength) {
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putLong("total", totalLength);
            bundle.putLong("current", currentLength);
            bundle.putString("fileUrl", fileUrl);
            message.setData(bundle);
            message.what = MESSAGE_WHAT_UPDATE_NOTIFICATION;
            mHandler.sendMessage(message);
            Logs.e("model", "fileUrl:" + fileUrl + "  total:" + totalLength + "  current:" + currentLength);
        }

        @Override
        public void onDownloadSuccess(String fileUrl, String fileName) {
            File file = new File(FileUtils.getVideoDownloadPath(), fileName);
            if (file.exists()) {
                String name = fileName.substring(0, fileName.lastIndexOf("."));
                File newFile = new File(FileUtils.getVideoDownloadPath() + name);
                file.renameTo(newFile);
                String temp = name.toLowerCase();
                if (temp.endsWith("mp4") || temp.endsWith("m4v") || temp.endsWith("3gp") || temp.endsWith("3gpp") || temp.endsWith("wmv")) {
                    Message message = Message.obtain();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("file", newFile);
                    message.setData(bundle);
                    message.what = MESSAGE_WHAT_PLAY_VIDEO;
                    mHandler.sendMessage(message);
                }
            }
            ConnectManager.getInstance().sendPackageDownloadSuccess(fileUrl);
        }

        @Override
        public void onDownloadFailed(final String fileUrl) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mainPresenter.showToast(fileUrl + "下载失败！");
                }
            });
        }
    }
}

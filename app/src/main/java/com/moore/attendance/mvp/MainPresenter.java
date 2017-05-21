package com.moore.attendance.mvp;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by MooreLi on 2017/5/2.
 */

public interface MainPresenter {

    void loadAdImage();

    void showTime();

    void readSetting();

    void showAdImage(Bitmap bmp);

    void showCurrentTime(String timeStr);

    void showDeviceNum(String deviceNum);

    void showUdpInfo(String udpInfo);

    void showToast(String message);

    void openCamera(String cardNum);

    void getCardInfo(byte[] info);

    void openSerialPort();

    void closeSerialPort();

    void downloadVideo(String videoInfo);

    void updateNotification(String fileUrl, long total, long current);

    void playVideo(File videoFile);

    void videoFunctionControl(boolean isOPen);
}

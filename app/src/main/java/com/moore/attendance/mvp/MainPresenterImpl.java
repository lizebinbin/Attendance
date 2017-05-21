package com.moore.attendance.mvp;

import android.graphics.Bitmap;

import com.moore.attendance.MainActivity;

import java.io.File;

/**
 * Created by MooreLi on 2017/5/2.
 */

public class MainPresenterImpl implements MainPresenter {
    private MainModel mainModel;
    private MainActivity mainView;

    public MainPresenterImpl(MainActivity view) {
        mainView = view;
        mainModel = new MainModelImpl(mainView, this);
    }

    @Override
    public void loadAdImage() {
        mainModel.getAdImage();
    }

    @Override
    public void showTime() {
        mainModel.getCurrentTime();
    }

    @Override
    public void readSetting() {
        mainModel.readSetting();
    }

    @Override
    public void showAdImage(Bitmap bmp) {
        mainView.showAdImage(bmp);
    }

    @Override
    public void showCurrentTime(String timeStr) {
        mainView.showTime(timeStr);
    }

    @Override
    public void showDeviceNum(String deviceNum) {
        mainView.showDeviceNum(deviceNum);
    }

    @Override
    public void showUdpInfo(String udpInfo) {
        mainView.showUdpInfo(udpInfo);
    }

    @Override
    public void showToast(String message) {
        mainView.showToast(message);
    }

    @Override
    public void openCamera(String cardNum) {
        mainView.showTakePhoto(cardNum);
    }

    @Override
    public void getCardInfo(byte[] info) {
        mainModel.handleCardInfo(info);
    }

    @Override
    public void openSerialPort() {
        mainView.openSerialPortAndStartReceive();
    }

    @Override
    public void closeSerialPort() {
        mainView.closeSerialPort();
    }

    @Override
    public void downloadVideo(String videoInfo) {
        mainModel.downloadVideo(videoInfo);
    }

    @Override
    public void updateNotification(String fileUrl, long total, long current) {
        mainView.updateNotification(fileUrl, total, current);
    }

    @Override
    public void playVideo(File videoFile) {
        mainView.playVideo(videoFile);
    }

    @Override
    public void videoFunctionControl(boolean isOPen) {
        mainView.videoFunctionControl(isOPen);
    }
}

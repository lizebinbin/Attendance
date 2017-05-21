package com.moore.attendance.mvp;

/**
 * Created by MooreLi on 2017/5/2.
 */

public interface MainModel {
    /**
     * 时间轮播
     */
    int MESSAGE_CHANGE_TIME = 1;
    /**
     * 广告图片轮播
     */
    int MESSAGE_CHANGE_IMAGE = 2;
    /**
     * 处理刷卡信息
     */
    int MESSAGE_WHAT_CARD_DATA = 3;
    /**
     * 更新udp通信信息
     */
    int MESSAGE_WHAT_RECEIVE_DATA = 4;
    /**
     * 打开相机
     */
    int MESSAGE_WHAT_OPEN_CAMERA = 5;
    /**
     * 下载视频
     */
    int MESSAGE_WHAT_DOWNLOAD_VIDEO = 6;

    int MESSAGE_WHAT_UPDATE_NOTIFICATION = 7;
    /**
     * 播放视频
     */
    int MESSAGE_WHAT_PLAY_VIDEO = 8;

    /**
     * 吐司信息
     */
    int MESSAGE_WHAT_TOAST_INFO = 9;

    //图片轮播切换时间 3s
    int IMAGE_CHANGE_TIME = 3 * 1000;
    //时间变换频率，1s刷新一次时间
    int TIME_CHANGE_STEP = 1000;

    void getAdImage();

    void getCurrentTime();

    void readSetting();

    void handleCardInfo(byte[] info);

    void downloadVideo(String videoInfo);
}

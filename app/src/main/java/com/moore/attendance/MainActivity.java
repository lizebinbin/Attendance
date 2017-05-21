package com.moore.attendance;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.moore.attendance.base.Command;
import com.moore.attendance.base.WakeUpService;
import com.moore.attendance.jniutils.SerialPortActivity;
import com.moore.attendance.mvp.MainPresenter;
import com.moore.attendance.mvp.MainPresenterImpl;
import com.moore.attendance.udp.SendPackageService;
import com.moore.attendance.udp.UdpHandler;
import com.moore.attendance.uitls.SPUtil;

import java.io.File;

public class MainActivity extends SerialPortActivity implements TakePhotoFragment.OnTakePhotoFinish, SurfaceHolder.Callback {
    private TextView mTvTip;
    private TextView mTvTime, mTvDeviceId, mTvSetting;
    private RelativeLayout mRlVideoLayout;
    //覆盖视频播放区域的图片
    private ImageView mIvImage;
    //视频播放控件
    private SurfaceView mVideoView;
    //首页WebView显示
    private WebView mWvMainShow;
    //二维码显示webView
    private WebView mWvQRCode;
    //拍照区域容器
    private RelativeLayout mPhotoLayout;

    private RemoteViews notificationView;

    private Intent mServiceIntent, mWakeUpService;

    private NotificationManager notificationManager;
    private Notification notification;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransation;
    private TakePhotoFragment mFragment;

    private MediaPlayer mediaPlayer;

    private MainPresenter mainPresenter;

    private int notification_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mApplication.addMain(this);
        //初始化控件
        initViews();
        //初始化webView
        initWebView();
        mainPresenter = new MainPresenterImpl(this);
        //加载图片
        mainPresenter.loadAdImage();
        //更新时间
        mainPresenter.showTime();
        //启动udp通信服务
        mServiceIntent = new Intent(this, SendPackageService.class);
        startService(mServiceIntent);
        //启动相互唤醒服务
        mWakeUpService = new Intent(this, WakeUpService.class);
        startService(mWakeUpService);

        mFragmentManager = getFragmentManager();

//        mainPresenter.downloadVideo("http://www.sharekq.cn/shareKq/abc.mp4");
//        mainPresenter.downloadVideo("http://www.sharekq.cn/shareKq/3pigs.mp4");
//        mainPresenter.downloadVideo("http://avatar.csdn.net/0/B/B/1_true100.jpg");
//        mainPresenter.downloadVideo("http://imgstore.cdn.sogou.com/app/a/100540002/457880.jpg");

        getWindow().setFormat(PixelFormat.TRANSPARENT);
    }

    /**
     * findView啦啦啦啦
     */
    private void initViews() {
        mTvTip = (TextView) findViewById(R.id.Main_tvTip);
        mTvTime = (TextView) findViewById(R.id.Main_tvTime);
        mTvDeviceId = (TextView) findViewById(R.id.Main_tvDeviceId);
        mTvSetting = (TextView) findViewById(R.id.Main_tvSetting);
        mRlVideoLayout = (RelativeLayout) findViewById(R.id.Main_rlVideoView);
        mIvImage = (ImageView) findViewById(R.id.Main_ivImage);
        mVideoView = (SurfaceView) findViewById(R.id.Main_videoView);
        mWvMainShow = (WebView) findViewById(R.id.Main_wvMainShow);
        mWvQRCode = (WebView) findViewById(R.id.Main_wvQRCode);

        mPhotoLayout = (RelativeLayout) findViewById(R.id.Main_rlTakePhotoLayout);
        //测试用,模拟刷卡数据
        mTvTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                byte[] bytes = new byte[]{55, -90, 43, 65, 23, 65};
                byte[] bytes = new byte[]{85, -86, -18, 69, 50, 36};
                onDataReceived(bytes, 20);
//                mainPresenter.playVideo(new File(FileUtils.getVideoDownloadPath(), "/abc.mp4"));
            }
        });
        //设置按钮
        mTvSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goSetting();
//                stopVideo();
            }
        });

//        String videoStatus = SPUtil.getString(this, Command.KEY_VIDEO_STATUS);
//        if (videoStatus.equals("open"))
//            mRlVideoLayout.setVisibility(View.VISIBLE);
//        else
//            mRlVideoLayout.setVisibility(View.GONE);
    }

    /**
     * 设置webView
     */
    private void initWebView() {
        //二维码
        mWvQRCode.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        mWvQRCode.getSettings().setJavaScriptEnabled(true);
        mWvQRCode.loadUrl("http://" + Command.serviceIp + "/" + Command.udp_project + "/qcode.jsp?id=" + Command.deviceId);

        //主页显示webView内容
        mWvMainShow.getSettings().setJavaScriptEnabled(true);
        mWvMainShow.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.loadUrl("javascript:try{myFunction();}catch(e){alert(e)}");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        mWvMainShow.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
            }
        });
        mWvMainShow.loadUrl("http://" + Command.serviceIp + "/" + Command.udp_project + "/mp4.jsp?id=" + Command.deviceId);
    }


    /**
     * 收到刷卡数据
     */
    @Override
    protected void onDataReceived(byte[] buffer, int size) {
        mainPresenter.getCardInfo(buffer);
    }

    /**
     * 打开串口
     */
    @Override
    public void openSerialPortAndStartReceive() {
        super.openSerialPortAndStartReceive();
    }

    /**
     * 关闭串口
     */
    @Override
    public void closeSerialPort() {
        super.closeSerialPort();
    }

    /**
     * 跳转到设置界面
     */
    private void goSetting() {
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }


    public void finishService() {
        stopService(mServiceIntent);
        stopService(mWakeUpService);
        UdpHandler.getInstance().removeMessages(UdpHandler.MSG_SEND);
        UdpHandler.getInstance().setIsRecieveRegister(false);
        Command.isTakePhoto = false;
    }

    @Override
    public void finishTakePhoto() {
        try {
            mFragmentTransation = mFragmentManager.beginTransaction();
            if (mFragment != null) {
                mFragmentTransation.remove(mFragment);
                mFragment = null;
            }
            mFragmentTransation.commitAllowingStateLoss();
            Command.isTakePhoto = false;
            mPhotoLayout.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainPresenter.readSetting();
    }

    @Override
    protected void onPause() {
        if (mWvMainShow != null) {
            mWvMainShow.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishService();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void createNotification() {
        if (notification == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(this);
            builder.setContent(notificationView);

            notification = builder.build();

            notification.icon = R.drawable.ic_launcher;
            notification.tickerText = "开始下载";
//             这里面的参数是通知栏view显示的内容
//            notification.setLatestEventInfo(this, app_name, "下载：0%", pendingIntent);

            // notificationManager.notify(notification_id, notification)

            notificationView = new RemoteViews(getPackageName(), R.layout.view_notification_item);
            notificationView.setTextViewText(R.id.notificationTitle, "正在下载");
            notificationView.setTextViewText(R.id.notificationPercent, "0%");
            notificationView.setProgressBar(R.id.notificationProgress, 100, 0, false);

            notification.contentView = notificationView;
            notificationManager.notify(notification_id, notification);
        }
    }

    /**
     * 显示时间
     *
     * @param timeStr
     */
    public void showTime(String timeStr) {
        if (timeStr != null && !"".equals(timeStr))
            mTvTime.setText(timeStr);
    }

    /**
     * 显示图片
     *
     * @param bitmap
     */
    public void showAdImage(Bitmap bitmap) {
        if (bitmap != null)
            mIvImage.setImageBitmap(bitmap);
    }

    /**
     * 显示设备编号
     *
     * @param deviceNum
     */
    public void showDeviceNum(String deviceNum) {
        if (deviceNum != null && !"".equals(deviceNum))
            mTvDeviceId.setText(deviceNum);
    }

    /**
     * 显示udp通信信息
     *
     * @param udpInfo
     */
    public void showUdpInfo(String udpInfo) {
        if (udpInfo != null && !"".equals(udpInfo)) {
            mTvTip.setText(udpInfo);
        }
    }

    /**
     * 吐司
     *
     * @param message
     */
    public void showToast(String message) {
        if (message != null && !"".equals(message)) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 打开拍照
     *
     * @param cardNum
     */
    public void showTakePhoto(String cardNum) {
        mPhotoLayout.setVisibility(View.VISIBLE);
        mFragment = new TakePhotoFragment();
        mFragment.setOnTakePhotoFinish(this);
        Bundle bundle = new Bundle();
        bundle.putString("cardId", cardNum);
        mFragment.setArguments(bundle);
        mFragmentTransation = mFragmentManager.beginTransaction();
        mFragmentTransation.add(R.id.Main_rlTakePhotoLayout, mFragment);
        mFragmentTransation.commitAllowingStateLoss();
    }

    /**
     * 播放视频
     *
     * @param videoFile
     */
    public void playVideo(final File videoFile) {
        if (videoFile != null && videoFile.exists()) {
            try {
                mVideoView.setVisibility(View.VISIBLE);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                mediaPlayer = new MediaPlayer();
                SurfaceHolder holder = mVideoView.getHolder();
                holder.addCallback(this);
                holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                mediaPlayer.reset();
                mediaPlayer.setDataSource(videoFile.getAbsolutePath());
                mediaPlayer.prepareAsync();
                mediaPlayer.setLooping(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止播放视频
     */
    public void stopVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            mVideoView.setVisibility(View.GONE);
        }
    }

    /**
     * 是否视频播放区域
     * @param isOpen
     */
    public void videoFunctionControl(boolean isOpen){
        if(isOpen){
            mRlVideoLayout.setVisibility(View.VISIBLE);
        }else{
            mRlVideoLayout.setVisibility(View.GONE);
        }
    }

    public void updateNotification(String fileUrl, long totalLength, long currentLength) {
//        if (notification != null) {
//            double current = currentLength * 1.0;
//            double percent = current / totalLength;
//            percent *= 100;
//            String title = fileUrl.substring(fileUrl.lastIndexOf("/"), fileUrl.length());
//            notificationView.setTextViewText(R.id.notificationTitle, "正在下载...  " + title);
//            notificationView.setTextViewText(R.id.notificationPercent, (int) percent + "%");
//            notificationView.setProgressBar(R.id.notificationProgress, 100, (int) percent, false);
//            notificationManager.notify(notification_id, notification);
//        } else {
//            createNotification();
//        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mediaPlayer.setDisplay(mVideoView.getHolder());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {

                }
            });
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    int videoWidth = mediaPlayer.getVideoWidth();
                    int videoHeight = mediaPlayer.getVideoHeight();
                    if (videoWidth != 0 && videoHeight != 0) {
                        mediaPlayer.start();
                        mediaPlayer.setDisplay(mVideoView.getHolder());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopVideo();
    }
}

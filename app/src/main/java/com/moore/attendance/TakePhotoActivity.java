package com.moore.attendance;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.moore.attendance.base.Command;
import com.moore.attendance.bean.CardInfo;
import com.moore.attendance.uitls.FileUtils;
import com.moore.attendance.uitls.Logs;
import com.moore.attendance.upload.UploadCallback;
import com.moore.attendance.upload.UploadUtil;
import com.moore.attendance.widget.CameraView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * Created by MooreLi on 2017/3/1.
 */

public class TakePhotoActivity extends Activity implements CameraView.SavePicCallback, UploadCallback ,CameraView.OnCameraStartListener{
    private CameraView mCameraView;
    private TextView mTvTip;

    private CardInfo mCardInfo;
    private UploadUtil mUpload;

    /**
     * 原本延时4s拍照，现在改为0，即进入界面立即拍照
     */
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        mCameraView = (CameraView) findViewById(R.id.TakePhoto_cameraView);
        mTvTip = (TextView) findViewById(R.id.TakePhoto_tvTip);

        mCardInfo = (CardInfo) getIntent().getSerializableExtra("cardInfo");
        if (mCardInfo == null)
            Toast.makeText(this, "卡号信息获取失败", Toast.LENGTH_SHORT).show();
        mCameraView.setOnCameraListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.startWork();
        mUpload = new UploadUtil(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Command.isTakePhoto = false;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (currentIndex > 0) {
                    if (currentIndex == 4)
                        mTvTip.setText("请准备");
                    else
                        mTvTip.setText(currentIndex + "");
                    currentIndex--;
                    mHandler.sendEmptyMessageDelayed(1, 1000);
                } else {
                    mTvTip.setText("照片保存中…");
                    mCameraView.takePhoto(TakePhotoActivity.this,mCardInfo.getCardId());
                }
            }else if(msg.what == 2){
                TakePhotoActivity.this.finish();
            }
        }
    };

    @Override
    public void saveSuccess(String path) {
        /**
         * 保存成功之后上传数据
         */
        mTvTip.setVisibility(View.VISIBLE);
        mTvTip.setText("拍照成功");
        uploadData(path);
        //1s之后关闭界面
        mHandler.sendEmptyMessageDelayed(2,1000);
    }

    @Override
    public void takePictureFailed() {
        mTvTip.setVisibility(View.VISIBLE);
        mTvTip.setText("拍照失败,请重拍");
        currentIndex = 4;
        mHandler.sendEmptyMessageDelayed(1, 1000);
        mCameraView.startCameraView();
    }

    /**
     * 上传数据
     *
     * @param photoPath
     */
    private void uploadData(String photoPath) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String submitTime = format.format(System.currentTimeMillis());

        HashMap<String, Object> params = new HashMap<String, Object>();
        try {
            File photo = new File(photoPath);
            params.put("myfiles", photo);
            params.put("cardNo", mCardInfo == null ? "unKnow" : mCardInfo.getCardId());
            params.put("deviceNo",Command.deviceId);
            params.put("submitTime", submitTime);
            mUpload.upLoadFile(Command.uploadUrl, params, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void uploadSuccess(String response,String filePath) {
        Logs.e("TakePhoto", "success===response:" + response);
        Looper.prepare();
        Toast.makeText(this, "上传成功！", Toast.LENGTH_SHORT).show();
        //上传成功后删除
        File file = new File(filePath);
        if (file.exists())
            file.delete();
        Looper.loop();
    }

    @Override
    public void uploadFailed(String errorMsg) {
        Logs.e("TakePhoto", "failed===response:" + errorMsg);
        Looper.prepare();
        Toast.makeText(this, "上传失败，请检查网络！"+errorMsg, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

    @Override
    public void onCameraStarted() {
        //相机开启之后拍照
        mHandler.sendEmptyMessage(1);
    }
}

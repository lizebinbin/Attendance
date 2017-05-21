package com.moore.attendance;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.moore.attendance.base.Command;
import com.moore.attendance.uitls.Logs;
import com.moore.attendance.upload.UploadCallback;
import com.moore.attendance.upload.UploadUtil;
import com.moore.attendance.widget.CameraView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * Created by MooreLi on 2017/3/22.
 */

public class TakePhotoFragment extends Fragment implements CameraView.SavePicCallback, UploadCallback, CameraView.OnCameraStartListener {
    private String TAG = "TakePhotoFragment";
    private View contentView;

    private CameraView mCameraView;
    private TextView mTvTip;

    private String mCardId;
    private UploadUtil mUpload;

    /**
     * 原本延时4s拍照，现在改为0，即进入界面立即拍照
     */
    private int currentIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logs.e(TAG,"onCreateView");
        contentView = View.inflate(getActivity(), R.layout.activity_take_photo, null);
        mCameraView = (CameraView) contentView.findViewById(R.id.TakePhoto_cameraView);
        mTvTip = (TextView) contentView.findViewById(R.id.TakePhoto_tvTip);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mCardId = bundle.getString("cardId");
            mCameraView.setOnCameraListener(this);
        }

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Logs.e(TAG,"onResume");
        mCameraView.startWork();
        mUpload = new UploadUtil(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        Logs.e("TakePhoto","onStop");
        mCameraView.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.e("TakePhotoFragment","onDestroy");
        mCameraView.stop();
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
                    mCameraView.takePhoto(TakePhotoFragment.this, mCardId);
                }
            } else if (msg.what == 2) {
//                TakePhotoActivity.this.finish();

                if (onTakePhotoFinish != null)
                    onTakePhotoFinish.finishTakePhoto();
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
        mHandler.sendEmptyMessageDelayed(2, 1500);
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
            params.put("cardNo", TextUtils.isEmpty(mCardId) ? "unKnow" : mCardId);
            params.put("deviceNo", Command.deviceId);
            params.put("submitTime", submitTime);
            mUpload.upLoadFile(Command.uploadUrl, params, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void uploadSuccess(String response, String filePath) {
        Logs.e("TakePhoto", "success===response:" + response);
        Looper.prepare();
//        Toast.makeText(getActivity(), "上传成功！", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(getActivity(), "上传失败，请检查网络！" + errorMsg, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }

    @Override
    public void onCameraStarted() {
        //相机开启之后拍照
        mHandler.sendEmptyMessageDelayed(1,500);
    }

    private OnTakePhotoFinish onTakePhotoFinish;

    public void setOnTakePhotoFinish(OnTakePhotoFinish onTakePhotoFinish) {
        this.onTakePhotoFinish = onTakePhotoFinish;
    }

    public interface OnTakePhotoFinish {
        void finishTakePhoto();
    }
}

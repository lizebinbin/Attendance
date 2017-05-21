package com.moore.attendance;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.moore.attendance.base.Command;
import com.moore.attendance.uitls.Logs;
import com.moore.attendance.uitls.SPUtil;

/**
 * Created by binbin on 2017/3/6.
 */

public class SettingActivity extends Activity {
    private EditText mEtDeviceNo;
    private EditText mEtServiceIp;
    private EditText mEtServicePort;
    private EditText mEtServiceAddress;
    private ImageView mIvBack;
    private TextView mTvSave;

    private RadioGroup mRgVideo, mRgSerialPort;
    private RadioButton mRbVideoOPen, mRbVideoClose, mRbSerialPortCard, mRbSerialPortCode;
    private boolean isVideoOpen = false;
    private boolean isUseCard = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initViews();
        setRadioGroupListener();
        //读取设置，显示在输入框
        mEtDeviceNo.setText(Command.deviceId);
        mEtServiceIp.setText(Command.serviceIp);
        mEtServicePort.setText(Command.servicePort);
        mEtServiceAddress.setText(Command.serviceAddress);
        String videoStatus = SPUtil.getString(this, Command.KEY_VIDEO_STATUS);
        if (videoStatus.equals("open")) {
            mRbVideoOPen.setChecked(true);
            Command.isVideoOpen = true;
        } else {
            mRbVideoClose.setChecked(true);
            Command.isVideoOpen = false;
        }
        String serialPortStatus = SPUtil.getString(this, Command.KEY_SERIAL_PORT_STATUS);
        if (serialPortStatus.equals("ICCard")) {
            mRbSerialPortCard.setChecked(true);
            Command.isSerialPortCardStatus = true;
        } else {
            mRbSerialPortCode.setChecked(true);
            Command.isSerialPortCardStatus = false;
        }
    }

    /**
     * 初始化View
     */
    private void initViews() {
        mEtDeviceNo = (EditText) findViewById(R.id.Setting_etDeviceNo);
        mEtServiceIp = (EditText) findViewById(R.id.Setting_etServiceIp);
        mEtServicePort = (EditText) findViewById(R.id.Setting_etServicePort);
        mEtServiceAddress = (EditText) findViewById(R.id.Setting_etServiceAddress);
        mRgVideo = (RadioGroup) findViewById(R.id.Setting_rgVideo);
        mRgSerialPort = (RadioGroup) findViewById(R.id.Setting_rgSerialPort);
        mRbVideoOPen = (RadioButton) findViewById(R.id.Setting_rbVideoOpen);
        mRbVideoClose = (RadioButton) findViewById(R.id.Setting_rbVideoClose);
        mRbSerialPortCode = (RadioButton) findViewById(R.id.Setting_rbSerialPortCode);
        mRbSerialPortCard = (RadioButton) findViewById(R.id.Setting_rbSerialPortCard);
        mTvSave = (TextView) findViewById(R.id.Setting_tvSave);
        mIvBack = (ImageView) findViewById(R.id.Setting_ivBack);
        mTvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingActivity.this.finish();
            }
        });
    }

    private void setRadioGroupListener() {
        mRgVideo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                if (id == R.id.Setting_rbVideoOpen)
                    isVideoOpen = true;
                else
                    isVideoOpen = false;
                Log.e("TEST","video:"+isVideoOpen);
            }
        });
        mRgSerialPort.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                //i==0,IC卡； i=1，二维码
                if (id == R.id.Setting_rbSerialPortCard)
                    isUseCard = true;
                else
                    isUseCard = false;
                Logs.e("TEST","useCard:"+isUseCard);
            }
        });
    }

    /**
     * 保存设置
     */
    private void saveSettings() {
        String deviceNo = mEtDeviceNo.getText().toString();
        if (TextUtils.isEmpty(deviceNo)) {
            Toast.makeText(this, "请输入设备编号！", Toast.LENGTH_SHORT).show();
            return;
        }
        String serviceIp = mEtServiceIp.getText().toString();
        if (TextUtils.isEmpty(serviceIp)) {
            Toast.makeText(this, "请输入服务器IP地址！", Toast.LENGTH_SHORT).show();
            return;
        }
        String servicePort = mEtServicePort.getText().toString();
        if (TextUtils.isEmpty(servicePort)) {
            Toast.makeText(this, "请输入服务器端口号！", Toast.LENGTH_SHORT).show();
            return;
        }
        String serviceAddress = mEtServiceAddress.getText().toString();
        if (TextUtils.isEmpty(serviceAddress)) {
            Toast.makeText(this, "请输入服务器接口地址！", Toast.LENGTH_SHORT).show();
            return;
        }

        Command.deviceId = deviceNo;
        Command.serviceIp = serviceIp;
        Command.servicePort = servicePort;
        Command.serviceAddress = serviceAddress;

        SPUtil.saveString(this, Command.KEY_DEVICE_NO, deviceNo);
        SPUtil.saveString(this, Command.KEY_SERVICE_IP, serviceIp);
        SPUtil.saveString(this, Command.KEY_SERVICE_PORT, servicePort);
        SPUtil.saveString(this, Command.KEY_SERVICE_ADDRESS, serviceAddress);

        SPUtil.saveString(this, Command.KEY_VIDEO_STATUS, isVideoOpen ? "open" : "close");
        SPUtil.saveString(this, Command.KEY_SERIAL_PORT_STATUS, isUseCard ? "ICCard" : "QRCode");

        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        finish();
    }
}

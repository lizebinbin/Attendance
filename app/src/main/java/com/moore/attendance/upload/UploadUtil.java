package com.moore.attendance.upload;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.moore.attendance.uitls.Logs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by binbin on 2017/3/5.
 */

public class UploadUtil {
    private String TAG = "UploadUtil";
    /**
     * 超时时间 60s
     */
    private final int TIMEOUT_TIME = 60;
    private OkHttpClient mOkHttpClient;
    private Context mContext;

    public UploadUtil(Context context) {
        this.mContext = context;
        init();
        boolean isConnectedInternet = isNetworkConnected();
        if (!isConnectedInternet)
            Toast.makeText(mContext, "当前网络无连接，请检查网络！", Toast.LENGTH_SHORT).show();
    }

    private void init() {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_TIME, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_TIME, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_TIME, TimeUnit.SECONDS)
                .build();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            if (info.isAvailable() && info.isConnected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 上传文件
     *
     * @param actionUrl 接口地址
     * @param paramsMap 参数
     * @param callBack  回调
     */
    public void upLoadFile(String actionUrl, HashMap<String, Object> paramsMap, final UploadCallback callBack) {
        String filePath = null;
        try {
            //补全请求地址
//            String requestUrl = String.format("%s/%s", upload_head, actionUrl);
            MultipartBody.Builder builder = new MultipartBody.Builder();
            //设置类型
            builder.setType(MultipartBody.FORM);
            //追加参数
            for (String key : paramsMap.keySet()) {
                Object object = paramsMap.get(key);
                if (!(object instanceof File)) {
                    Logs.w(TAG, "key:" + key + "  value:" + object.toString());
                    builder.addFormDataPart(key, object.toString());
                } else {
                    File file = (File) object;
                    filePath = file.getAbsolutePath();
                    builder.addFormDataPart(key, file.getName(), RequestBody.create(null, file));
                }
            }
            //创建RequestBody
            RequestBody body = builder.build();
            //创建Request
            final Request request = new Request.Builder().url(actionUrl).post(body).build();
            //单独设置参数 比如读取超时时间
            final Call call = mOkHttpClient.newCall(request);
            final String finalFilePath = filePath;
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call calls, IOException e) {
                    Logs.e(TAG, e.toString());
                    if (callBack != null) {
                        callBack.uploadFailed(e.toString());
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String result = response.body().string();
                        Logs.e(TAG, "response ----->" + result);
                        if (callBack != null) {
                            callBack.uploadSuccess(result, finalFilePath);
                        }
                    } else {
                        if (callBack != null) {
                            callBack.uploadFailed("response not successful");
                        }
                    }
                }
            });
        } catch (Exception e) {
            Logs.e(TAG, e.toString());
        }
    }


    public void downloadFile(final String fileUrl, final String destFileDir, final OnDownloadListener listener) {
        Request request = new Request.Builder().url(fileUrl)
                .build();
        mOkHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Logs.e(TAG, "onFailure  下载失败");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        InputStream is = null;
                        byte[] buf = new byte[2048];
                        int len = 0;
                        FileOutputStream fos = null;
                        try {
                            is = response.body().byteStream();
                            long totalLength = response.body().contentLength();
                            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/"),fileUrl.length());
                            File file = new File(destFileDir, fileName+".temp");
                            if(file.exists()){
                                file.delete();
                                file.mkdir();
                            }
                            fos = new FileOutputStream(file);
                            long currentProgress = 0;
                            while ((len = is.read(buf)) != -1) {
                                fos.write(buf, 0, len);
                                currentProgress += len;
                                Logs.e(TAG, "total:" + totalLength + "  current:" + currentProgress);
                                if (listener != null) {
                                    listener.onCurrentProgress(fileUrl, totalLength, currentProgress);
                                }
                            }
                            fos.flush();
                            if(listener != null){
                                listener.onDownloadSuccess(fileUrl,fileName+".temp");
                            }
                            Logs.e(TAG, "下载成功！");

                        } catch (Exception e) {
                            e.printStackTrace();
                            if(listener != null){
                                listener.onDownloadFailed(fileUrl);
                            }
                            Logs.e(TAG, "下载失败");
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                            if (fos != null) {
                                fos.close();
                            }
                        }
                    }
                });
    }

    public interface OnDownloadListener {
        void onCurrentProgress(String fileUrl, long totalLength, long currentLength);
        void onDownloadSuccess(String fileUrl,String fileName);
        void onDownloadFailed(String fileUrl);
    }
}

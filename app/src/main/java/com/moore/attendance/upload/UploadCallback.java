package com.moore.attendance.upload;

/**
 * Created by binbin on 2017/3/5.
 */

public interface UploadCallback {
    void uploadSuccess(String filePath,String response);

    void uploadFailed(String errorMsg);
}

package com.moore.attendance.callback;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by MooreLi on 2017/5/2.
 */

public interface ModelCallback {
    void loadAdImageCallback(boolean isSuccess, List<Bitmap> imgList);
}

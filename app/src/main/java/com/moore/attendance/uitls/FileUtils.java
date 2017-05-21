package com.moore.attendance.uitls;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MooreLi on 2017/3/2.
 */

public class FileUtils {
    public static boolean mkdir(File file) {
        while (!file.getParentFile().exists()) {
            mkdir(file.getParentFile());
        }
        return file.mkdir();
    }

    public static String getSystemPhotoPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera";
    }

    public static String getPhotoPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/attendance/imageCache";
    }

    public static String getCachePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/attendance/cache";
    }

    public static String getAdImaegPath(){
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/attendance/adImage";
    }

    public static List<Bitmap> getAdImage(){
        File dir = new File(getAdImaegPath());
        if(!dir.exists())
            dir.mkdirs();
        List<Bitmap> bmps = new ArrayList<Bitmap>();
        File files[] = dir.listFiles();
        if(files != null && files.length > 0){
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if(fileName.endsWith(".jpg")||fileName.endsWith(".png")||fileName.endsWith(".jpeg")){
                    Bitmap bmp = BitmapFactory.decodeFile(files[i].getAbsolutePath());
                    bmps.add(bmp);
                }
            }
        }
        return bmps;
    }

    public static String getVideoDownloadPath(){
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/attendance/video");
        if(!dir.exists()){
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }
}

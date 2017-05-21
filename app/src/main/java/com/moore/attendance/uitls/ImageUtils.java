package com.moore.attendance.uitls;

import android.graphics.Bitmap;

import com.moore.attendance.base.Command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by MooreLi on 2017/3/2.
 */

public class ImageUtils {

    //保存图片文件
    public static String saveToFile(String fileFolderStr, boolean isDir, String cardId, Bitmap croppedImage) throws FileNotFoundException, IOException {
        File jpgFile;
        if (isDir) {
            File fileFolder = new File(fileFolderStr);
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间
            String filename = cardId + "_" + format.format(date) + "_" + Command.deviceId + ".jpg";
            if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"finger"的目录
                FileUtils.mkdir(fileFolder);
            }
            jpgFile = new File(fileFolder, filename);
        } else {
            jpgFile = new File(fileFolderStr);
            if (!jpgFile.getParentFile().exists()) { // 如果目录不存在，则创建一个名为"finger"的目录
                FileUtils.mkdir(jpgFile.getParentFile());
            }
        }
        FileOutputStream outputStream = new FileOutputStream(jpgFile); // 文件输出流

        croppedImage.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        IOUtil.closeStream(outputStream);
        return jpgFile.getPath();
    }
}

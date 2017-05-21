package com.moore.attendance.base;

/**
 * Created by MooreLi on 2017/3/1.
 */

public class Command {
    public static final String KEY_DEVICE_NO = "key_deviceNo";
    public static final String KEY_SERVICE_IP = "key_serviceIp";
    public static final String KEY_SERVICE_PORT = "key_servicePort";
    public static final String KEY_SERVICE_ADDRESS = "key_serviceAddress";
    public static final String KEY_VIDEO_STATUS = "key_videoStatus";
    public static final String KEY_SERIAL_PORT_STATUS = "key_serialPortStatus";

    public static boolean isDebug = true;
    public static boolean isTakePhoto = false;

    //视频功能
    public static boolean isVideoOpen = false;
    //串口类型
    public static boolean isSerialPortCardStatus = false;

    /* *******************共享考勤配置Start*****************************/
    public static String udp_project = "shareKq";
    public static String serviceIp = "www.shareKq.cn";
    public static String servicePort = "80";
    public static String serviceAddress = "/shareKq/test/fileUpload.do?dir=yg&uname=jb";
    public static String deviceId = "android_001";
    public static String udp_address = "www.sharekq.cn";
    /* *******************共享考勤配置End*******************************/



    /* *******************内蒙学校配置信息配置Start********************************/
//    public static String udp_project="school";
//    public static String serviceIp = "www.hlbra.org";
//    public static String servicePort = "80";
//    public static String serviceAddress = "/"+udp_project+"/test/fileUpload2.do?dir=yg&uname=jb";
//    public static String deviceId = "android_001";
//    public static String udp_address = "www.hlbra.org";
    /* *******************内蒙学校配置信息配置End**********************************/

    public static int udp_port = 11069;

    public static String uploadUrl = "http://" + serviceIp + ":" + servicePort + serviceAddress;
}

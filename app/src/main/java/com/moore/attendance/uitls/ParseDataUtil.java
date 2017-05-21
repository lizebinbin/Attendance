package com.moore.attendance.uitls;

import com.moore.attendance.base.Command;
import com.moore.attendance.bean.ParseResponse;

import java.util.Arrays;

/**
 * Created by binbin on 2017/3/5.
 */

public class ParseDataUtil {
    public static final int PARSE_SUCCESS = 1;
    public static final int PARSE_WRONG = 2;

    public static ParseResponse getCardData(byte[] bytes) {
        /***************原本解析方式***************/
//        String nums[] = new String[bytes.length];
//        for (int i = 0; i < bytes.length; i++) {
//            if (bytes[i] >= 0) {
//                nums[i] = Integer.toHexString(bytes[i]);
//            } else {
//                nums[i] = Integer.toHexString(256 + bytes[i]);
//            }
//            nums[i] = nums[i].toUpperCase();
//        }
//        Logs.i("ParseData","转换之后："+ Arrays.toString(nums));
//
//        StringBuffer sb = new StringBuffer();
//        for (int i = 2 ; i< nums.length-1; i++){
//            sb.append(nums[i]);
//        }
//        Logs.i("ParseData","sb.toString:"+sb.toString());
//        long cardNum = Long.parseLong(sb.toString(),16);
//        Logs.i("ParseData","cardNum: "+cardNum);
//        return String.valueOf(cardNum);

        ParseResponse response = new ParseResponse();
        //IC卡
        if (Command.isSerialPortCardStatus) {
            String nums[] = new String[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] >= 0) {
                    nums[i] = Integer.toHexString(bytes[i]);
                } else {
                    nums[i] = Integer.toHexString(256 + bytes[i]);
                }
                Logs.e("ParseData", "nums[i]:" + nums[i]);
                nums[i] = nums[i].toUpperCase();
            }
            Logs.i("ParseData", "转换之后：" + Arrays.toString(nums));
            StringBuffer sb = new StringBuffer();
            for (int i = 2; i < nums.length; i++) {
                sb.append(nums[i]);
            }
            String temp = sb.toString();
            int index = temp.length();
            if (temp.contains("BD")) {
                index = temp.lastIndexOf("BD");
            } else {
                if (temp.contains("00")) {
                    index = temp.indexOf("00");
                }
            }
            temp = temp.substring(0, index);
            Logs.i("ParseData", "sb.toString:" + temp);
            long cardNum = Long.parseLong(temp, 16);
            Logs.i("ParseData", "cardNum: " + cardNum);
            response.setCode(PARSE_SUCCESS);
            response.setData(cardNum + "");
            return response;
        }
        //二维码
        else {
            String cardNum = "";
            int length = bytes.length;
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] < 128) {
                    cardNum += (char) (bytes[i] & 0xFF);
                }
            }
            //截掉后边3位
            cardNum = cardNum.substring(0, cardNum.length() - 3);
            //判断二维码是否正确
            if ((bytes[length - 3] & 0xff) != 35 || (bytes[length - 2] & 0xff) != 43 || (bytes[length - 1] & 0xff) != 95) {
                response.setCode(PARSE_WRONG);
                response.setData("非法二维码！");
                return response;
            }
            //判断二维码是否过期
            String tmpTimeStamp = cardNum.substring(cardNum.length() - 10, cardNum.length());
            try {
                long codeTime = Long.parseLong(tmpTimeStamp);
                //精确到秒
                long current = System.currentTimeMillis() / 1000;
                int offset = (int) ((current - codeTime) / 60);
                if (offset > 5) {
                    response.setCode(PARSE_WRONG);
                    response.setData("二维码已过期！");
                    return response;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                response.setCode(PARSE_WRONG);
                response.setData("二维码时间错误！");
                return response;
            }
            //判断二维码长度
            cardNum = cardNum.substring(0, cardNum.length() - 10);
            if (cardNum.length() > 20) {
                response.setCode(PARSE_WRONG);
                response.setData("二维码长度超过限制！");
                return response;
            }
            response.setCode(PARSE_SUCCESS);
            response.setData(cardNum);
            return response;
        }

    }
}

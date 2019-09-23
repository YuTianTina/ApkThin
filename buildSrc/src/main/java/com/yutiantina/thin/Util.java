package com.yutiantina.thin;

/**
 * @author yutiantian email: yutiantina@gmail.com
 * @since 2019-04-28
 */
public class Util {
    public static String byteArrayToHex(byte[] data) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] str = new char[data.length * 2];
        int k = 0;
        for (int i = 0; i < data.length; i++) {
            byte byte0 = data[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);
    }
}

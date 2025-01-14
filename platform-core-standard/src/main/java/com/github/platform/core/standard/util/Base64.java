package com.github.platform.core.standard.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author yxkong
 * @Description: Base64编码解码
 */
public class Base64 {

    private static final char[] LEGALCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    public static byte[] encodeUrlSafe(byte[] src) {
        return src.length == 0 ? src : java.util.Base64.getUrlEncoder().encode(src);
    }
    public static String encodeToUrlSafeString(byte[] src) {
        return new String(encodeUrlSafe(src), StandardCharsets.UTF_8);
    }
    public static byte[] decodeUrlSafe(byte[] src) {
        return src.length == 0 ? src : java.util.Base64.getUrlDecoder().decode(src);
    }
    /**
     * 使用系统base64
     * @param data
     * @return
     */
    public static String encodeToString(byte[] data){
        return java.util.Base64.getEncoder().encodeToString(data);
    }
    /**
     * data[]进行编码
     *
     * @param data
     * @return
     */
    public static String encode(byte[] data) {
        int start = 0;
        int len = data.length;
        StringBuffer buf = new StringBuffer(data.length * 3 / 2);

        int end = len - 3;
        int i = start;
        int n = 0;

        while (i <= end) {
            int d = ((((int) data[i]) & 0x0ff) << 16)
                    | ((((int) data[i + 1]) & 0x0ff) << 8)
                    | (((int) data[i + 2]) & 0x0ff);

            buf.append(LEGALCHARS[(d >> 18) & 63]);
            buf.append(LEGALCHARS[(d >> 12) & 63]);
            buf.append(LEGALCHARS[(d >> 6) & 63]);
            buf.append(LEGALCHARS[d & 63]);

            i += 3;

            if (n++ >= 14) {
                n = 0;
                buf.append(" ");
            }
        }

        if (i == start + len - 2) {
            int d = ((((int) data[i]) & 0x0ff) << 16)
                    | ((((int) data[i + 1]) & 255) << 8);

            buf.append(LEGALCHARS[(d >> 18) & 63]);
            buf.append(LEGALCHARS[(d >> 12) & 63]);
            buf.append(LEGALCHARS[(d >> 6) & 63]);
            buf.append("=");
        } else if (i == start + len - 1) {
            int d = (((int) data[i]) & 0x0ff) << 16;

            buf.append(LEGALCHARS[(d >> 18) & 63]);
            buf.append(LEGALCHARS[(d >> 12) & 63]);
            buf.append("==");
        }

        return buf.toString();
    }

    /**
     * 使用系统base64 decode
     * @param s
     * @return
     */
    public static byte[] decodeBySys(String s) {
        return java.util.Base64.getDecoder().decode(s);
    }

    /**
     * Base64解码
     * @param s
     * @return
     */
    public static byte[] decode(String s) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            decode(s, bos);
        } catch (IOException e) {
            throw new RuntimeException();
        }
        byte[] decodedBytes = bos.toByteArray();
        try {
            bos.close();
            bos = null;
        } catch (IOException ex) {
            System.err.println("Error while decoding BASE64: " + ex.toString());
        }
        return decodedBytes;
    }

    private static void decode(String s, OutputStream os) throws IOException {
        int i = 0;

        int len = s == null ? 0 : s.length();

        while (true) {
            while (i < len && s.charAt(i) <= ' ') {
                i++;
            }

            if (i == len) {
                break;
            }
            int tri = (decode(s.charAt(i)) << 18)
                    + (decode(s.charAt(i + 1)) << 12)
                    + (decode(s.charAt(i + 2)) << 6)
                    + (decode(s.charAt(i + 3)));

            os.write((tri >> 16) & 255);
            if (s.charAt(i + 2) == '=') {
                break;
            }
            os.write((tri >> 8) & 255);
            if (s.charAt(i + 3) == '=') {
                break;
            }
            os.write(tri & 255);

            i += 4;
        }
    }

    private static int decode(char c) {
        if (c >= 'A' && c <= 'Z') {
            return ((int) c) - 65;
        } else if (c >= 'a' && c <= 'z') {
            return ((int) c) - 97 + 26;
        } else if (c >= '0' && c <= '9') {
            return ((int) c) - 48 + 26 + 26;
        } else {
            switch (c) {
                case '+':
                    return 62;
                case '/':
                    return 63;
                case '=':
                    return 0;
                default:
                    throw new RuntimeException("unexpected code: " + c);
            }
        }
    }
}

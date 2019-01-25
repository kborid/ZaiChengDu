package com.prj.sdk.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Display;

import com.prj.sdk.app.AppContext;

import org.apache.http.NameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 全局公共类
 *
 * @author Liao
 */
public class Utils {

    /**
     * @param subFolder cache的文件夹
     * @return
     */
    public static String getFolderDir(String subFolder) {
        String rootDir = null;
        if (subFolder == null) {
            subFolder = "";
        }
        if (isSDCardEnable()) {
            // SD-card available
            rootDir = Environment.getExternalStorageDirectory()
                    .getAbsolutePath()
                    + "/Android/data/"
                    + AppContext.mMainContext.getPackageName()
                    + File.separator
                    + subFolder + File.separator;

            // 创建文件目录
            File file = new File(rootDir);
            if (!file.exists()) // 创建目录
            {
                file.mkdirs();
            }
        } else {
            rootDir = AppContext.mMainContext.getCacheDir() + File.separator
                    + subFolder + File.separator;
            // 创建文件目录
            File file = new File(rootDir);
            if (!file.exists()) // 创建目录
            {
                file.mkdirs();
            }
        }

        return rootDir;
    }

    /**
     * 判断文件是否存在
     *
     * @param subFolder
     * @return
     */
    public static boolean isFolderDir(String subFolder) {
        String rootDir = null;
        if (subFolder == null) {
            subFolder = "";
        }
        if (Utils.isSDCardEnable()) {
            // SD-card available
            rootDir = Environment.getExternalStorageDirectory()
                    .getAbsolutePath()
                    + "/Android/data/"
                    + AppContext.mMainContext.getPackageName()
                    + File.separator
                    + subFolder + File.separator;

            File file = new File(rootDir);
            if (!file.exists()) {
                return false;
            }
        } else {
            rootDir = AppContext.mMainContext.getCacheDir() + File.separator
                    + subFolder + File.separator;
            File file = new File(rootDir);
            if (!file.exists()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断文件路径是否存在
     *
     * @param filePath
     * @return true 存在 false 不存在
     */
    public static boolean isExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static String getAPKDir(String subFolder) {
        String rootDir = null;
        if (isSDCardEnable()) {
            // SD-card available
            rootDir = Environment.getExternalStorageDirectory()
                    .getAbsolutePath()
                    + "/Android/body/"
                    + subFolder
                    + File.separator;

            // 创建文件目录
            File file = new File(rootDir);
            if (!file.exists()) // 创建目录
            {
                file.mkdirs();
            }
        }

        return rootDir;
    }

    /**
     * SD是否可用
     *
     * @return
     */
    public static boolean isSDCardEnable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 验证url的合法性
     *
     * @param url
     * @return
     */
    public static boolean checkUrl(String url) {
        return url.matches("^[a-zA-z]+://[^\\s]*$");
    }

    /**
     * 验证手机号是否合法
     *
     * @param phone
     * @return boolean
     */
    public static boolean isMobile(String phone) {
        String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(phone);
        return m.matches();
    }

    /**
     * 验证身份证是否合法
     *
     * @param cardNum
     * @return boolean
     */
    public static boolean isIdCard(String cardNum) {
        if (TextUtils.isEmpty(cardNum)) {
            return false;
        }
        // 定义判别用户身份证号的正则表达式（15位或者18位，最后一位可以为字母）
        String regularExpression = "(^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|" +
                "(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)";
        //假设18位身份证号码:41000119910101123X  410001 19910101 123X
        //^开头
        //[1-9] 第一位1-9中的一个      4
        //\\d{5} 五位数字           10001（前六位省市县地区）
        //(18|19|20)                19（现阶段可能取值范围18xx-20xx年）
        //\\d{2}                    91（年份）
        //((0[1-9])|(10|11|12))     01（月份）
        //(([0-2][1-9])|10|20|30|31)01（日期）
        //\\d{3} 三位数字            123（第十七位奇数代表男，偶数代表女）
        //[0-9Xx] 0123456789Xx其中的一个 X（第十八位为校验值）
        //$结尾

        //假设15位身份证号码:410001910101123  410001 910101 123
        //^开头
        //[1-9] 第一位1-9中的一个      4
        //\\d{5} 五位数字           10001（前六位省市县地区）
        //\\d{2}                    91（年份）
        //((0[1-9])|(10|11|12))     01（月份）
        //(([0-2][1-9])|10|20|30|31)01（日期）
        //\\d{3} 三位数字            123（第十五位奇数代表男，偶数代表女），15位身份证不含X
        //$结尾


        boolean matches = cardNum.matches(regularExpression);

        //判断第18位校验值
        if (matches) {
            if (cardNum.length() == 18) {
                try {
                    char[] charArray = cardNum.toCharArray();
                    //前十七位加权因子
                    int[] idCardWi = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
                    //这是除以11后，可能产生的11位余数对应的验证码
                    String[] idCardY = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
                    int sum = 0;
                    for (int i = 0; i < idCardWi.length; i++) {
                        int current = Integer.parseInt(String.valueOf(charArray[i]));
                        int count = current * idCardWi[i];
                        sum += count;
                    }
                    char idCardLast = charArray[17];
                    int idCardMod = sum % 11;
                    if (idCardY[idCardMod].toUpperCase().equals(String.valueOf(idCardLast).toUpperCase())) {
                        return true;
                    } else {
                        return false;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

        }
        return matches;
    }

    /**
     * 验证邮箱是否合法
     *
     * @param strEmail
     * @return boolean
     */
    public static boolean isEmail(String strEmail) {
        String strPattern = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(strEmail);
        return m.matches();
    }

    /*
     * 提取url中的参数
     */
    public static String getParameter(String url, String name) {
        if (url == null || name == null) {
            return null;
        }

        int start = url.indexOf(name + "=");
        if (start == -1)
            return null;
        int len = start + name.length() + 1;
        int end = url.indexOf("&", len);
        if (end == -1) {
            end = url.length();
        }

        return url.substring(len, end);
    }

    /**
     * 获取参数值
     *
     * @param name
     * @return
     */
    public static String getParamValue(List<NameValuePair> params, String name) {
        try {
            for (NameValuePair pair : params) {
                if (name.equals(pair.getName())) {
                    return pair.getValue();
                }
            }
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * 获取拼接参数
     *
     * @param params
     * @return String
     */
    public static String getParamsStr(List<NameValuePair> params) {
        try {
            if (params == null) {
                return "";
            }

            StringBuffer sb = new StringBuffer();
            sb.append("?");
            for (NameValuePair pair : params) {
                sb.append(pair.getName()).append("=").append(pair.getValue())
                        .append("&");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * bitmap转换
     *
     * @param bm
     * @return
     */
    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 校验银行卡卡号
     *
     * @param cardId
     * @return
     */
    public static boolean checkBankCard(String cardId) {
        if (cardId != null && !"".equals(cardId)) {
            char bit = getBankCardCheckCode(cardId.substring(0,
                    cardId.length() - 1));
            if (bit == 'N') {
                return false;
            }
            return cardId.charAt(cardId.length() - 1) == bit;
        }
        return false;
    }

    public static String convertHiddenPhoneStars(String phone, int start, int end) {
        if (!TextUtils.isEmpty(phone)) {
            if (start < 0 || end > 11 || end <= start) {
                return phone;
            }

            StringBuilder str = new StringBuilder();
            for (int i = 0; i < phone.length(); i++) {
                if (i < start || i > end) {
                    str.append(phone.charAt(i));
                } else {
                    str.append("*");
                }
            }
            return str.toString();
        }
        return phone;
    }

    /**
     * 从不含校验位的银行卡卡号采用Luhm校验算法获得校验位
     *
     * @param nonCheckCodeCardId
     * @return
     */
    public static char getBankCardCheckCode(String nonCheckCodeCardId) {
        if (nonCheckCodeCardId == null
                || nonCheckCodeCardId.trim().length() == 0
                || !nonCheckCodeCardId.matches("\\d+")) {
            // 如果传的不是数据返回N
            return 'N';
        }
        char[] chs = nonCheckCodeCardId.trim().toCharArray();
        int luhmSum = 0;
        for (int i = chs.length - 1, j = 0; i >= 0; i--, j++) {
            int k = chs[i] - '0';
            if (j % 2 == 0) {
                k *= 2;
                k = k / 10 + k % 10;
            }
            luhmSum += k;
        }
        return (luhmSum % 10 == 0) ? '0' : (char) ((10 - luhmSum % 10) + '0');
    }

    /**
     * 启动web页面
     *
     * @param context
     * @param url
     * @return
     */
    public static final boolean startWebView(Context context, String url) {
        if (url != null && Utils.checkUrl(url)) {
            Intent viewIntent = new Intent("android.intent.action.VIEW",
                    Uri.parse(url));
            context.startActivity(viewIntent);
            return true;
        }
        return false;
    }

    /**
     * 转化英文格式
     *
     * @param input
     * @return
     */
    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

}
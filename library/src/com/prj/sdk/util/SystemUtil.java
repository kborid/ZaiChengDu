package com.prj.sdk.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;

import com.prj.sdk.app.AppContext;

public class SystemUtil {

    /**
     * 获取手机的imei号
     *
     * @return
     */
    public static final String getIMEI() {
        String android_id = "";
        try {
            TelephonyManager telManager = (TelephonyManager) AppContext.mMainContext
                    .getSystemService(Context.TELEPHONY_SERVICE);
            android_id = telManager.getDeviceId();
            if (android_id == null) {
                android_id = android.provider.Settings.System.getString(
                        AppContext.mMainContext.getContentResolver(),
                        "android_id");
            }
        } catch (Exception e) {
        }
        return android_id;
    }

    /**
     * 是否为IMEI
     *
     * @param IMEI
     * @return
     */
    public static final boolean IsIMEI(String IMEI) {
        int s = IMEI.length();
        if (s != 15) {
            return false;
        }
        try {
            char[] imeiChar = IMEI.toCharArray();
            int resultInt = 0;
            for (int i = 0; i < 14; i++) {
                int a = Integer.parseInt(String.valueOf(imeiChar[i]));
                i++;
                final int temp = Integer.parseInt(String.valueOf(imeiChar[i])) * 2;
                final int b = temp < 10 ? temp : temp - 9;
                resultInt += a + b;
            }
            resultInt %= 10;
            resultInt = resultInt == 0 ? 0 : 10 - resultInt;
            if (resultInt == Integer.parseInt(String.valueOf(imeiChar[14]))) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 获取WIFI MAC地址
     */
    public static String getWifiMac() {
        WifiInfo wifiInfo = null;
        try {
            WifiManager wifiManager = (WifiManager) AppContext.mMainContext
                    .getSystemService(Context.WIFI_SERVICE);
            wifiInfo = wifiManager.getConnectionInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (wifiInfo != null && wifiInfo.getMacAddress() != null)
            return wifiInfo.getMacAddress(); // 获取WIFI_MAC地址
        else
            return "";
    }

    /**
     * 获取ip地址
     *
     * @return
     */
    public static String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) AppContext.mMainContext
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        if (ipAddress == 0) {
            return null;
        }
        // 对int类型的ip地址做一次转换
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }

    /**
     * 获取application中指定的meta-data
     *
     * @return 如果没有获取成功(没有对应值 ， 或者异常)，则返回值为空
     */
    public static String getAppMetaData(Context ctx, String key) {
        if (ctx == null || TextUtils.isEmpty(key)) {
            return null;
        }
        String resultData = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager
                        .getApplicationInfo(ctx.getPackageName(),
                                PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.getString(key);
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return resultData;
    }

    /**
     * 比较系统版本号
     *
     * @param v1
     * @param v2
     * @return 0--等；-1--小；1--大
     */
    public static int compareVersion(String v1, String v2) {
        if (v1 == null) {
            return -1;
        }

        if (v2 == null) {
            return 1;
        }

        if (v1.equals(v2)) {
            return 0;
        }

        String[] arrayStr1 = v1.split("\\.");
        String[] arrayStr2 = v2.split("\\.");

        int leng1 = arrayStr1.length;
        int leng2 = arrayStr2.length;
        int leng = (leng1 > leng2) ? leng2 : leng1;

        int result = 0;
        for (int i = 0; i < leng; i++) {
            result = arrayStr1[i].length() - arrayStr2[i].length();
            if (result != 0) {
                return result > 0 ? 1 : -1;
            }
            result = arrayStr1[i].compareTo(arrayStr2[i]);
            if (result != 0) {
                return result > 0 ? 1 : -1;
            }
        }

        if (result == 0) {
            if (leng1 > leng2) {
                result = 1;
            } else if (leng1 < leng2) {
                result = -1;
            }
        }

        return result;
    }

    /**
     * 关闭软键盘
     *
     * @param mContext
     */
    public static final void closeSoftInputMode(Activity mContext) {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isActive()) {
            try {
                inputMethodManager.hideSoftInputFromWindow((mContext)
                        .getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    /**
     * 判断GPS是否打开
     *
     * @return
     */
    public static final boolean isGPSOPen() {
        LocationManager locationManager = (LocationManager) AppContext.mMainContext
                .getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps) {
            return true;
        }
        return false;
    }

    /**
     * GPS开关
     */
    public static final void openGPS() {
        Intent gpsIntent = new Intent();
        gpsIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
        gpsIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent
                    .getBroadcast(AppContext.mMainContext, 0, gpsIntent, 0)
                    .send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}

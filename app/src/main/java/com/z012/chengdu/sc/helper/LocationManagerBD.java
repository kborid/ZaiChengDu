package com.z012.chengdu.sc.helper;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.util.LogUtil;

/**
 * 设备定位
 *
 * @author Liao
 */
public class LocationManagerBD {
    private static final String TAG = "LocationManagerBD";

    private LocationClient mBDLocationClient = null;
    private static LocationManagerBD mBDLocationManager = null;
    private long startTime = 0;
    private LocationCallback mLocationCallback;

    public static LocationManagerBD getIns() {
        if (mBDLocationManager == null) {
            synchronized (LocationManagerBD.class) {
                if (mBDLocationManager == null) {
                    mBDLocationManager = new LocationManagerBD();
                }
            }
        }
        return mBDLocationManager;
    }

    public void initLocation() {
        if (mBDLocationClient == null) {
            mBDLocationClient = new LocationClient(AppContext.mMainContext);
            LocationClientOption option = new LocationClientOption();
            option.setOpenGps(true); // 打开gps
            option.setCoorType("bd09ll"); // 设置坐标类型为bd09ll/gcj02
            option.setLocationMode(LocationMode.Hight_Accuracy);
            option.setProdName("zaichengdu"); // 设置产品线名称
            option.setScanSpan(300000); // 定时定位，每隔5分钟定位一次。
            option.setIsNeedAddress(true);

            mBDLocationClient.setLocOption(option);
            mBDLocationClient.registerLocationListener(new BDLocationListenerImpl());
        }
    }

    public synchronized void startLocation(LocationCallback mLocationCallback) {
        if ((System.currentTimeMillis() - startTime) < 3000) {
            return;
        } else {
            startTime = System.currentTimeMillis();
        }
        this.mLocationCallback = mLocationCallback;
        LogUtil.i(TAG, "startBaiduLocation");
        initLocation();
        mBDLocationClient.start();
    }

    public void stopLocation() {
        LogUtil.i(TAG, "stopBaiduLocation");
        if (mBDLocationClient != null) {
            mBDLocationClient.stop();
            mBDLocationClient = null;
        }
    }

    public boolean isStart() {
        boolean isStarted = false;
        if (mBDLocationClient != null) {
            isStarted = mBDLocationClient.isStarted();
        }
        return isStarted;
    }

    public class BDLocationListenerImpl implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location) {
                int locType = location.getLocType();
                LogUtil.i(TAG, "loc type:" + location.getLocType() + " lat:" + location.getLatitude() + "lon:" + location.getLongitude());
                if (locType != 61 && locType != 65 && locType != 66 && locType != 68 && locType != 161) {
                    return;
                }
                LogUtil.i(TAG, "获取到定位:" + location.getLocType() + " lat:" + location.getLatitude() + "lon:" + location.getLongitude());
                mLocationCallback.onLocationInfo(location);
            }
        }
    }

    /**
     * 回调定位信息
     *
     * @author LiaoBo
     */
    public interface LocationCallback {
        public void onLocationInfo(BDLocation locationInfo);
    }
}

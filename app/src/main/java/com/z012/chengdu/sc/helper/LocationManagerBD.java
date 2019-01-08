package com.z012.chengdu.sc.helper;

import android.content.Context;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.NetworkUtil;
import com.prj.sdk.util.Utils;

/**
 * 设备定位
 * 
 * @author Liao
 * 
 */
public class LocationManagerBD {
	private static final String			TAG					= "LocationManagerBD";

	private LocationClient				mBDLocationClient	= null;
	private static LocationManagerBD	mBDLocationManager	= null;
	private MyBDLocationListenner		mLocationListenner	= null;
	private long						startTime			= 0;
	private LocationCallback			mLocationCallback;

	public static LocationManagerBD getIns() {
		if (mBDLocationManager == null) {
			// 增加类锁,保证只初始化一次
			synchronized (LocationManagerBD.class) {
				if (mBDLocationManager == null) {
					mBDLocationManager = new LocationManagerBD();
				}
			}
		}
		return mBDLocationManager;
	}

	public void initBaiduLocation(Context cnt) {
		if (mBDLocationClient == null) {
			mBDLocationClient = new LocationClient(cnt);
			LocationClientOption option = new LocationClientOption();
			option.setOpenGps(true); // 打开gps
			option.setCoorType("bd09ll"); // 设置坐标类型为bd09ll/gcj02
			option.setLocationMode(LocationMode.Hight_Accuracy);
			option.setProdName("zaichengdu"); // 设置产品线名称
			option.setScanSpan(300000); // 定时定位，每隔5分钟定位一次。
			option.setIsNeedAddress(true);

			mBDLocationClient.setLocOption(option);
			mLocationListenner = new MyBDLocationListenner();
			mBDLocationClient.registerLocationListener(mLocationListenner);
		}
	}

	public synchronized void startBaiduLocation(Context cnt, LocationCallback mLocationCallback) {
		if ((System.currentTimeMillis() - startTime) < 3000) {
			return;
		} else {
			startTime = System.currentTimeMillis();
		}
		this.mLocationCallback = mLocationCallback;
		LogUtil.i(TAG, "startBaiduLocation");
		initBaiduLocation(cnt);
		mBDLocationClient.start();
//		int result = mBDLocationClient.requestLocation();
//		LogUtil.i(TAG, "requestLocation result: " + result);
	}

	public void stopBaiduLocation() {
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

	public void startGps() {
		if (!Utils.isGPSOPen()) {
			LogUtil.d(TAG, "start normal GPS");
			Utils.openGPS();
		}
	}

	public class MyBDLocationListenner implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (!NetworkUtil.isNetworkAvailable()) {
				startGps();
			}

			if (location == null)
				return;
			try {
				int locType = location.getLocType();
				LogUtil.i(TAG, "loc type:" + location.getLocType() + " lat:" + location.getLatitude() + "lon:" + location.getLongitude());
				if (locType != 61 && locType != 65 && locType != 66 && locType != 68 && locType != 161) {
					return;
				}
				LogUtil.i(TAG, "获取到定位:" + location.getLocType() + " lat:" + location.getLatitude() + "lon:" + location.getLongitude());

				// double lat = Arith.round(location.getLatitude(), 10);
				// double lon = Arith.round(location.getLongitude(), 10);

				// SharedPreferenceUtil.getInstance().setString(Const.LOCATION_LAT, String.valueOf(lat));
				// SharedPreferenceUtil.getInstance().setString(Const.LOCATION_LON, String.valueOf(lon));
				
				mLocationCallback.onLocationInfo(location);
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.e(TAG, e.getMessage());
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
			return;
		}
	}

	/**
	 * 回调定位信息
	 * 
	 * @author LiaoBo
	 * 
	 */
	public interface LocationCallback {
		public void onLocationInfo(BDLocation locationInfo);
	}
}

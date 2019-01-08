package com.z012.chengdu.sc.ui.JSBridge.functions;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.baidu.location.BDLocation;
import com.z012.chengdu.sc.helper.LocationManagerBD;
import com.z012.chengdu.sc.helper.LocationManagerBD.LocationCallback;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBHandler;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;

public class getLocation implements WVJBHandler {

	private Context context;
	private String lon;
	private String lat;

	public getLocation(Context context) {
		this.context = context;
	}

	@Override
	public void request(Object data, final WVJBResponseCallback callback) {
		// TODO Auto-generated method stub
		if (null != callback) {
			final JSONObject json = new JSONObject();
			LocationManagerBD.getIns().startBaiduLocation(context,
					new LocationCallback() {

						@Override
						public void onLocationInfo(BDLocation locationInfo) {
							// TODO Auto-generated method stub
							lon = String.valueOf(locationInfo.getLongitude());
							lat = String.valueOf(locationInfo.getLatitude());

							try {
								json.put("longitude", lon);
								json.put("latitude", lat);
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							callback.callback(json.toString());
						}
					});
		}
	}
}

package com.z012.chengdu.sc.ui.JSBridge.functions;

import com.baidu.location.BDLocation;
import com.z012.chengdu.sc.helper.LocationManagerBD;
import com.z012.chengdu.sc.helper.LocationManagerBD.LocationCallback;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBHandler;
import com.z012.chengdu.sc.ui.JSBridge.WVJBWebViewClient.WVJBResponseCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class getLocation implements WVJBHandler {

    private String lon;
    private String lat;

    @Override
    public void request(Object data, final WVJBResponseCallback callback) {
        if (null != callback) {
            final JSONObject json = new JSONObject();
            LocationManagerBD.getIns().startLocation(new LocationCallback() {

                @Override
                public void onLocationInfo(BDLocation locationInfo) {
                    lon = String.valueOf(locationInfo.getLongitude());
                    lat = String.valueOf(locationInfo.getLatitude());

                    try {
                        json.put("longitude", lon);
                        json.put("latitude", lat);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    callback.callback(json.toString());
                }
            });
        }
    }
}

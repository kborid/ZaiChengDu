package com.z012.chengdu.sc.net.request;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSON;
import com.prj.sdk.algo.MD5Tool;
import com.prj.sdk.app.AppContext;
import com.z012.chengdu.sc.BuildConfig;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.encrypt.Algorithm3DES;
import com.z012.chengdu.sc.encrypt.AlgorithmData;
import com.z012.chengdu.sc.encrypt.Base64;

import java.util.HashMap;
import java.util.Map;

import okhttp3.internal.Util;

/**
 * 构建请求
 *
 * @author kborid
 */
public class RequestBuilder {

    private HashMap<String, Object> mData = new HashMap<>();
    private Map<String, String> head = new HashMap<>();
    private Map<String, String> body = new HashMap<>();

    private RequestBuilder(boolean isNeedTicket) {
        mData.put("head", head);
        mData.put("body", body);
        head.put("appid", AppConst.APPID);
        head.put("version", AppConst.VERSION);
        head.put("siteid", SessionContext.getAreaInfo(1));
        head.put("appversion", BuildConfig.VERSION_NAME);
        if (isNeedTicket) {
            if (!SessionContext.isLogin()) {
                LocalBroadcastManager.getInstance(AppContext.mMainContext).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
            }
            head.put("accessTicket", SessionContext.getTicket());
        }
    }

    public static RequestBuilder create(boolean isNeedTicket) {
        return new RequestBuilder(isNeedTicket);
    }

    public RequestBuilder addBody(String key, String value) {
        body.put(key, value);
        return this;
    }

    public RequestBuilder addBody(HashMap<String, Object> params) {
        for (String key : params.keySet()) {
            addBody(key, (String) params.get(key));
        }
        return this;
    }

    private String sign() {
        AlgorithmData data = new AlgorithmData();
        try {
            // 先对body进行base64
            String bodyText = JSON.toJSONString(body);
            // 对报文进行BASE64编码，避免中文处理问题
            String base64Text = new String(Base64.encodeBase64((AppConst.APPID + bodyText).getBytes(Util.UTF_8), false));
            // MD5摘要，生成固定长度字符串用于加密
            String destText = MD5Tool.getMD5(base64Text);
            data.setDataMing(destText);
            data.setKey(AppConst.APPKEY);
            // 3DES加密
            Algorithm3DES.encryptMode(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data.getDataMi();
    }

    private String signForMgr() {
        String destText = "";
        try {
            String srcText = JSON.toJSONString(body);
            String base64Text = new String(Base64.encodeBase64((AppConst.APPID + srcText + AppConst.APPKEY).getBytes(Util.UTF_8), false));
            destText = MD5Tool.getMD5(base64Text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return destText;
    }

    public HashMap<String, Object> build(boolean isMgr) {
        if (isMgr) {
            head.put("sign", signForMgr());
        } else {
            head.put("sign", sign());
        }
        return mData;
    }

    public HashMap<String, Object> build() {
        return build(false);
    }

    public String toJson() {
        return JSON.toJSONString(mData);
    }
}

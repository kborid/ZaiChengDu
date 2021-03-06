package com.z012.chengdu.sc.net.request;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSON;
import com.prj.sdk.algo.MD5Tool;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.util.StringUtil;
import com.z012.chengdu.sc.BuildConfig;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.prj.sdk.algo.Algorithm3DES;
import com.prj.sdk.algo.AlgorithmData;
import com.prj.sdk.algo.Base64;
import com.z012.chengdu.sc.net.InfoType;

import java.util.HashMap;
import java.util.Map;

/**
 * 构建请求处理
 * 
 * @author LiaoBo
 */
public class RequestBeanBuilder {

	private Map<String, Object> head;
	private Map<String, Object> body;

	private RequestBeanBuilder(boolean isNeedTicket) {
		head = new HashMap<String, Object>();
		body = new HashMap<String, Object>();
		if (isNeedTicket) {
			if (StringUtil.empty(SessionContext.getTicket())) {// 如果ticket为空则发送登录广播
				LocalBroadcastManager.getInstance(AppContext.mMainContext).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
			}
			addHeadToken(SessionContext.getTicket());
		}
	}

	/**
	 * 构建请求
	 * 
	 * @param isNeedTicket
	 *            是否需要ticket ,如果需要登录就需要ticket
	 * @return
	 */
	public static RequestBeanBuilder create(boolean isNeedTicket) {
		return new RequestBeanBuilder(isNeedTicket);
	}

	public RequestBeanBuilder addHeadToken(String token) {
		return addHead("accessTicket", token);
	}

	public RequestBeanBuilder addHead(String key, Object value) {
		head.put(key, value);
		return this;
	}

	public RequestBeanBuilder addBody(String key, Object value) {
		body.put(key, value);
		return this;
	}

	private String sign() {
		AlgorithmData data = new AlgorithmData();
		try {
			// 先对body进行base64
			String bodyText = JSON.toJSONString(body);
			// 对报文进行BASE64编码，避免中文处理问题
			String base64Text = new String(Base64.encodeBase64(
					(AppConst.APPID + bodyText).getBytes("utf-8"), false));
			// MD5摘要，生成固定长度字符串用于加密
			String destText = MD5Tool.getMD5(base64Text);
			data.setDataMing(destText);
			data.setKey(AppConst.APPKEY);
			// 3DES加密
			Algorithm3DES.encryptMode(data);
		} catch (Exception e) {
		}
		return data.getDataMi();
	}

	/**
	 * 获取访问mgr的签名
	 * 
	 * @return
	 * @throws Exception
	 */
	private String signRequestForMgr() {
		String destText = "";
		try {

			String srcText = JSON.toJSONString(body);
			String base64Text = new String(Base64.encodeBase64((AppConst.APPID
					+ srcText + AppConst.APPKEY).getBytes("utf-8"), false));

			destText = MD5Tool.getMD5(base64Text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return destText;
	}

	/**
	 * 请求数据的json字符串
	 * 
	 * @return
	 */
	public String toJson(boolean isMgr) {
		HashMap<String, Object> json = new HashMap<String, Object>();
		json.put("head", head);
		json.put("body", body);

		head.put("appid", AppConst.APPID);
		if (isMgr) {
			head.put("sign", signRequestForMgr());
		} else {
			head.put("sign", sign());
		}
		head.put("version", AppConst.VERSION);
		head.put("siteid", SessionContext.getAreaInfo(1));
		head.put("appversion", BuildConfig.VERSION_NAME);
		return JSON.toJSONString(json);
	}

	/**
	 * 请求数据
	 * 
	 * @return
	 */
	public ResponseData syncRequest(RequestBeanBuilder builder) {
		ResponseData data = new ResponseData();
		data.data = builder.toJson(false);
		data.type = InfoType.POST_REQUEST.toString();
		return data;
	}

	// /**
	// * 请求mgr接口数据
	// *
	// * @return
	// */
	// public ResponseData syncRequestMgr(RequestBeanBuilder builder) {
	// ResponseData data = new ResponseData();
	// data.data = builder.toJson(true);
	// data.type = InfoType.POST_REQUEST.toString();
	// return data;
	// }

}

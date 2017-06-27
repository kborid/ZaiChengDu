package com.common.share;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.prj.sdk.widget.CustomToast;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.bean.StatusCode;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.SmsHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;
import com.z012.chengdu.sc.R;

/**
 * 分享操作
 * 
 * @author LiaoBo
 */
public class ShareUtil {
	private final UMSocialService	mController;
	private static ShareUtil		mInstance	= null;
	private Activity				mAct;

	private ShareUtil(Activity activity) {
		mController = UMServiceFactory.getUMSocialService("com.umeng.share");
		mAct = activity;
	}

	/**
	 * 获得实例的唯一全局访问点
	 * 
	 * @return
	 */
	public static ShareUtil getInstance(Activity activity) {
		if (mInstance == null) {
			// 增加类锁,保证只初始化一次
			synchronized (ShareUtil.class) {
				if (mInstance == null) {
					mInstance = new ShareUtil(activity);
				}
			}
		}
		return mInstance;
	}

	/**
	 * @功能描述 : 添加QQ平台支持 QQ分享的内容， 包含四种类型， 即单纯的文字、图片、音乐、视频. 参数说明 : title, summary, image url中必须至少设置一个, targetUrl必须设置,网页地址必须以"http://"开头 . title : 要分享标题 summary : 要分享的文字概述 image url :
	 *       图片地址 [以上三个参数至少填写一个] targetUrl : 用户点击该分享时跳转到的目标地址 [必填] ( 若不填写则默认设置为友盟主页 )
	 * @return
	 */
	public void addQQQZonePlatform() {
		String appId = mAct.getString(R.string.qq_appid);
		String appKey = mAct.getString(R.string.qq_appkey);
		// 添加QQ支持, 并且设置QQ分享内容的target url
		UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(mAct, appId, appKey);
		qqSsoHandler.setTargetUrl("http://www.umeng.com/social");
		qqSsoHandler.addToSocialSDK();

		// 添加QZone平台
		// QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(mAct, appId, appKey);
		// qZoneSsoHandler.addToSocialSDK();
	}

	/**
	 * @功能描述 : 添加微信平台分享
	 * @return
	 */
	public void addWXPlatform() {
		String appId = mAct.getString(R.string.wx_appid);
		String appSecret = mAct.getString(R.string.wx_appsecret);
		// 添加微信平台
		UMWXHandler wxHandler = new UMWXHandler(mAct, appId, appSecret);
		wxHandler.addToSocialSDK();

		// 支持微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(mAct, appId, appSecret);
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();
	}

	/**
	 * 添加短信平台</br>
	 */
	public void addSMS() {
		// 添加短信
		SmsHandler smsHandler = new SmsHandler();
		smsHandler.addToSocialSDK();
	}

	/**
	 * 发短信：调用系统短信分享，不使用友盟，因为友盟只要进入页面，页面就自动提示分享中、发送成功、分享成功
	 */
	public void sendSMS(String shareContent) {
		Uri smsToUri = Uri.parse("smsto:");
		Intent sendIntent = new Intent(Intent.ACTION_VIEW, smsToUri);
		// sendIntent.putExtra("address", "123456"); // 电话号码，这行去掉的话，默认就没有电话
		// 短信内容
		sendIntent.putExtra("sms_body", shareContent);
		sendIntent.setType("vnd.android-dir/mms-sms");
		mAct.startActivity(sendIntent);
	}

	/**
	 * 根据不同的平台设置不同的分享内容</br>
	 * 
	 * @param shareContent
	 *            分享文本类容
	 * @param title
	 *            分享标题
	 * @param targetUrl
	 *            目标地址
	 * @param imageUrl
	 *            图片地址
	 */
	public void setShareContent(String shareContent, String title, String targetUrl, int imgId) {
		try {
			UMImage urlImage = new UMImage(mAct, imgId);
			// 微信
			WeiXinShareContent weixinContent = new WeiXinShareContent();
			weixinContent.setShareContent(shareContent);
			weixinContent.setTitle(title);
			weixinContent.setTargetUrl(targetUrl);
			weixinContent.setShareMedia(urlImage);
			mController.setShareMedia(weixinContent);

			// 设置朋友圈分享的内容
			CircleShareContent circleMedia = new CircleShareContent();
			circleMedia.setShareContent(shareContent);
			circleMedia.setTitle(title);
			circleMedia.setShareMedia(urlImage);
			circleMedia.setTargetUrl(targetUrl);
			mController.setShareMedia(circleMedia);

			// QQ
			QQShareContent qqShareContent = new QQShareContent();
			qqShareContent.setShareContent(shareContent);
			qqShareContent.setTitle(title);
			qqShareContent.setShareMedia(urlImage);
			qqShareContent.setTargetUrl(targetUrl);
			mController.setShareMedia(qqShareContent);

			// 设置短信分享内容
//			SmsShareContent sms = new SmsShareContent();
//			sms.setShareContent(shareContent);
//			sms.setShareImage(urlImage);
//			mController.setShareMedia(sms);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 直接分享，底层分享接口。如果分享的平台是新浪、腾讯微博、豆瓣、人人，则直接分享，无任何界面弹出； 其它平台分别启动客户端分享</br>
	 * 
	 * @param platform
	 *            分享的平台
	 */
	public void directShare(SHARE_MEDIA platform) {
		mController.directShare(mAct, platform, new SnsPostListener() {

			@Override
			public void onStart() {
			}

			@Override
			public void onComplete(SHARE_MEDIA platform, int eCode, SocializeEntity entity) {
				String showText = "分享成功";
				if (eCode != StatusCode.ST_CODE_SUCCESSED) {
					showText = "分享失败 ";
				}
				CustomToast.show(showText, Toast.LENGTH_SHORT);
			}
		});
	}

	/**
	 * 销毁Listeners
	 */
	public void destroy() {
		mController.getConfig().cleanListeners();
	}
}

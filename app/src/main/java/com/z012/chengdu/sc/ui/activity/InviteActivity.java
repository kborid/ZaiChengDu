package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.common.share.ShareUtil;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.net.image.ImageLoader.ImageCallback;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.net.bean.AppInfoBean;
import com.z012.chengdu.sc.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 邀请好友
 * 
 * @author kborid
 */
public class InviteActivity extends BaseActivity {
	@BindView(R.id.iv_qr_code) ImageView iv_qr_code;
	private String mSMSContent;

	@Override
	protected int getLayoutResId() {
		return R.layout.ui_share_act;
	}

	@Override
	public void initParams() {
		super.initParams();
		tv_center_title.setText("邀请好友");
		tv_right_title.setText("邀请人列表");

		ShareUtil.getInstance(this).addQQQZonePlatform();
		ShareUtil.getInstance(this).addWXPlatform();
		// ShareUtil.getInstance(this).addSMS();
		String app_info = SharedPreferenceUtil.getInstance().getString(AppConst.APP_INFO, "", false);
		if (StringUtil.notEmpty(app_info)) {
			AppInfoBean json = JSON.parseObject(app_info, AppInfoBean.class);
			String inviteLink = json.invitationLink + SessionContext.mUser.USERBASIC.id;// 拼接邀请链接
			mSMSContent = json.shareCopywriter +"　"+ inviteLink;
			ShareUtil.getInstance(this).setShareContent(json.shareCopywriter, getText(R.string.app_name).toString(), inviteLink, R.drawable.icon);
			setQR(inviteLink);
		}
	}

	@OnClick(R.id.ly_share_qq) void shareQQ() {
		ShareUtil.getInstance(this).directShare(SHARE_MEDIA.QQ);
	}

	@OnClick(R.id.ly_share_weixin) void shareWX() {
		ShareUtil.getInstance(this).directShare(SHARE_MEDIA.WEIXIN);
	}

	@OnClick(R.id.ly_share_sms) void shareSMS() {
		ShareUtil.getInstance(this).sendSMS(mSMSContent);
	}

	@OnClick(R.id.tv_right_title) void right() {
		startActivity(new Intent(this, InviteListActivity.class));
	}

	/**
	 * 生成邀请二维码，使用联图二维码开放平台
	 * 
	 */
	public void setQR(String url) {
		try {
			if (StringUtil.notEmpty(url)) {
				url = url.replace("&", "%26").replaceAll("\n", "%0A");// x内容出现 & 符号时，请用 %26 代替,换行符使用 %0A

				StringBuilder sbUrl = new StringBuilder();
				sbUrl.append("http://qr.liantu.com/api.php?text=").append(url).append("&m=10");
				ImageLoader.getInstance().loadBitmap(new ImageCallback() {
					@Override
					public void imageCallback(Bitmap bm, String url, String imageTag) {
						if (bm != null) {
							iv_qr_code.setImageBitmap(bm);
						}
					}

				}, sbUrl.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ShareUtil.getInstance(this).destroy();
	}

}

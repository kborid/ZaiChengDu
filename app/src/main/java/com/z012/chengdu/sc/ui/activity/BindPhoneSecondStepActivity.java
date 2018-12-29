package com.z012.chengdu.sc.ui.activity;

import java.net.ConnectException;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.jpush.android.api.JPushInterface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.UserInfo;
import com.z012.chengdu.sc.tools.SHA1;
import com.z012.chengdu.sc.ui.base.BaseActivity;

/**
 * 绑定手机第二步操作
 * 
 * @author LiaoBo
 */
public class BindPhoneSecondStepActivity extends BaseActivity implements DataCallback, DialogInterface.OnCancelListener {
	private EditText		et_yzm, et_pwd;
	private Button			btn_getYZM, btn_next;
	private TextView		tv_describe, tv_agreement, tv_login_phone, tv_forget_pwd;
	private CheckBox		checkBox;
	private boolean			isOccupy;//是否已注册
	private String			phoneNum;
	private CountDownTimer	mCountDownTimer;
	private LinearLayout	layoutAgreement, layoutForget, layoutYZM;
	private String			thirdpartusername, thirdpartuserheadphotourl, openid, unionid, mPlatform,usertoken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_bind_phone_second_step_act);
		initViews();
		initParams();
		initListeners();
	}

	@Override
	public void initViews() {
		super.initViews();
		tv_center_title.setText("手机绑定");
		tv_center_title.setTextColor(0xffffffff);
		findViewById(R.id.comm_title_rl).setBackgroundResource(R.color.transparent);
		tv_right_title.setVisibility(View.GONE);
		tv_login_phone = (TextView) findViewById(R.id.tv_login_phone);
		btn_next = (Button) findViewById(R.id.btn_next);
		btn_getYZM = (Button) findViewById(R.id.btn_getYZM);
		tv_describe = (TextView) findViewById(R.id.tv_describe);
		tv_agreement = (TextView) findViewById(R.id.tv_agreement);
		checkBox = (CheckBox) findViewById(R.id.checkBox);
		tv_forget_pwd = (TextView) findViewById(R.id.tv_forget_pwd);
		layoutAgreement = (LinearLayout) findViewById(R.id.layoutAgreement);
		layoutForget = (LinearLayout) findViewById(R.id.layoutForget);
		layoutYZM = (LinearLayout) findViewById(R.id.layoutYZM);
		et_pwd = (EditText) findViewById(R.id.et_pwd);
		et_yzm = (EditText) findViewById(R.id.et_yzm);
	}

	@Override
	public void initParams() {
		super.initParams();
		setCountDownTimer(60 * 1000, 1000);
		dealIntent();
	}

	@Override
	public void dealIntent() {
		super.dealIntent();
		try {
			isOccupy = getIntent().getBooleanExtra("isOccupy", false);
			phoneNum = getIntent().getStringExtra("phoneNum");
			tv_login_phone.setText(phoneNum);
			if (isOccupy) {
				tv_describe.setText(R.string.bind_phone_second_tip);//判断您已经注册过在成都,请登录绑定第三方帐号信息
				layoutForget.setVisibility(View.VISIBLE);
			} else {
				tv_describe.setText("请填写验证码，并设置登录密码");
				layoutAgreement.setVisibility(View.VISIBLE);
				layoutYZM.setVisibility(View.VISIBLE);
				findViewById(R.id.viewLine).setVisibility(View.VISIBLE);
			}

			thirdpartusername = getIntent().getExtras().getString("thirdpartusername");
			thirdpartuserheadphotourl = getIntent().getExtras().getString("thirdpartuserheadphotourl");
			openid = getIntent().getExtras().getString("openid");
			unionid = getIntent().getExtras().getString("unionid");
			mPlatform = getIntent().getExtras().getString("platform");
			usertoken = getIntent().getExtras().getString("usertoken");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initListeners() {
		super.initListeners();
		btn_next.setOnClickListener(this);
		btn_getYZM.setOnClickListener(this);
		tv_agreement.setOnClickListener(this);
		tv_forget_pwd.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.btn_next :
				if (isOccupy) {// 绑定新手机
					if (checkBox.isChecked()) {// 手机号已经注册，直接绑定
						bindThridAccess();
					} else {
						CustomToast.show("请先阅读《注册协议》", 0);
					}
				} else {//注册然后绑定
					registerPhone();
				}
				break;
			case R.id.btn_getYZM :
				loadYZM();
				break;
			case R.id.tv_agreement :
				Intent mIntent = new Intent(this, WebViewActivity.class);
				mIntent.putExtra("title", "注册协议");
				mIntent.putExtra("path", NetURL.REGISTER_URL);
				startActivity(mIntent);
				break;
			case R.id.tv_forget_pwd :
				Intent intent2 = new Intent();
				intent2.setClass(this, ForgetPwdActivity.class);
				startActivity(intent2);
				break;
			default :
				break;
		}

	}

	/**
	 * 加载验证码
	 */
	private void loadYZM() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		builder.addBody("BUSINESSTYPE", "40658");// 业务类型01手机绑定；02邮箱绑定；03注册绑定手机；04找回密码；05市民卡注册绑定；06市民卡实名认证
		builder.addBody("MOBILENUM", phoneNum);

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.GET_YZM;
		data.flag = 1;

		if (!isProgressShowing()) {
			showProgressDialog(getString(R.string.loading), true);
		}
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	/**
	 * 注册手机
	 */
	private void registerPhone() {
		String CODE = et_yzm.getText().toString().trim();
		String PASSWORD = et_pwd.getText().toString().trim();
		if (StringUtil.empty(phoneNum)) {
			CustomToast.show("请输入手机号码", 0);
			return;
		}
		if (!Utils.isMobile(phoneNum)) {
			CustomToast.show("请输入正确的手机号码", 0);
			return;
		}
		if (StringUtil.empty(CODE)) {
			CustomToast.show("请输入验证码", 0);
			return;
		}
		if (StringUtil.empty(PASSWORD)) {
			CustomToast.show("密码不允许为空", 0);
			return;
		}
		if (PASSWORD.length() < 6) {
			CustomToast.show("请输入6-20个字符的密码", 0);
			return;
		}
		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		builder.addBody("MOBILENUM", phoneNum);
		builder.addBody("CODE", CODE);
		builder.addBody("PWDSTRENGTH", "1");
		SHA1 sha1 = new SHA1();
		builder.addBody("PASSWORD", sha1.getDigestOfString(PASSWORD.getBytes()));

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.REGISTER;
		data.flag = 2;

		if (!isProgressShowing()) {
			showProgressDialog(getString(R.string.present), true);
		}
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	/**
	 * 绑定第三方帐号
	 */
	private void bindThridAccess() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		builder.addBody("mobile", phoneNum);
		builder.addBody("thirdpartusername", thirdpartusername);
		builder.addBody("thirdpartuserheadphotourl", thirdpartuserheadphotourl);
		builder.addBody("openid", openid);
		builder.addBody("unionid", unionid);
		builder.addBody("platform", mPlatform);
		builder.addBody("usertoken", usertoken);
		
		SHA1 sha1 = new SHA1();
		builder.addBody("password", sha1.getDigestOfString(et_pwd.getText().toString().trim().getBytes()));

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.BIND_ACCESS;
		data.flag = 3;

		if (!isProgressShowing()) {
			showProgressDialog(getString(R.string.present), true);
		}
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	/**
	 * 获取用户信息
	 * 
	 * @param ticket
	 *            票据
	 */
	private void getUserInfo(String ticket) {
		RequestBeanBuilder builder = RequestBeanBuilder.create(true);

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.GET_USER_INFO;
		data.flag = 4;
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
		
		if (request.flag == 2) {//注册成功，绑定第三方数据
			bindThridAccess();
		} else if (request.flag == 1) {
			removeProgressDialog();
			CustomToast.show("验证码已发送，请稍候...", Toast.LENGTH_LONG);
			btn_getYZM.setEnabled(false);
			mCountDownTimer.start();// 启动倒计时
		} else if (request.flag == 3) {// 绑定成功
			JSONObject mJson = JSON.parseObject(response.body.toString());
			String accessTicket = mJson.getString("accessTicket");
			// 记录登录ticket
			SharedPreferenceUtil.getInstance().setString(AppConst.ACCESS_TICKET, accessTicket, true);
			SessionContext.setTicket(accessTicket);
			getUserInfo(accessTicket);
		} else if (request.flag == 4) {// 成功获取用户信息
			removeProgressDialog();
			if (StringUtil.empty(response.body.toString()) || response.body.toString().equals("{}")) {
				CustomToast.show("获取用户信息失败，请重试", 0);
				return;
			}
			SessionContext.mUser = JSON.parseObject(response.body.toString(), UserInfo.class);
			if (SessionContext.mUser == null || StringUtil.empty(SessionContext.mUser.USERBASIC)) {
				CustomToast.show("获取用户信息失败，请重试", 0);
				return;
			}
			SharedPreferenceUtil.getInstance().setString(AppConst.USER_INFO, response.body.toString(), true);
			SharedPreferenceUtil.getInstance().setString(AppConst.LAST_LOGIN_DATE, DateUtil.getCurDateStr(null), false);// 保存登录时间
			SharedPreferenceUtil.getInstance().setString(AppConst.USER_PHOTO_URL, SessionContext.mUser.USERBASIC != null ? SessionContext.mUser.USERBASIC.getHeadphotourl() : "", false);
			SharedPreferenceUtil.getInstance().setString(AppConst.USER_INFO, response.body.toString(), true);
			JPushInterface.setAlias(AppContext.mMainContext, SessionContext.mUser.USERAUTH.mobilenum, null);
			CustomToast.show("绑定成功", 0);
			Intent intent = new Intent(AppConst.ACTION_DYNAMIC_USER_INFO);
			LocalBroadcastManager.getInstance(AppContext.mMainContext).sendBroadcast(intent);
			// 跳出到首页
			Intent mIntent = new Intent(this, MainFragmentActivity.class);
			mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(mIntent);
		}
	}
	@Override
	public void notifyError(ResponseData request, ResponseData response, Exception e) {
		removeProgressDialog();

		String message;
		if (e != null && e instanceof ConnectException) {
			message = getString(R.string.dialog_tip_net_error);
		} else {
			message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_bind_phone_error);
		}
		CustomToast.show(message, Toast.LENGTH_LONG);

	}

	@Override
	public void onCancel(DialogInterface dialog) {
		DataLoader.getInstance().clear(requestID);
		removeProgressDialog();
	}

	/**
	 * 设置倒计时
	 * 
	 * @param millisInFuture
	 * @param countDownInterval
	 */
	private void setCountDownTimer(long millisInFuture, long countDownInterval) {
		mCountDownTimer = new CountDownTimer(millisInFuture, countDownInterval) {

			@Override
			public void onTick(long millisUntilFinished) {
				btn_getYZM.setText("重新获取(" + (millisUntilFinished / 1000) + ")");
			}

			@Override
			public void onFinish() {
				btn_getYZM.setEnabled(true);
				btn_getYZM.setText("获取验证码");
			}
		};
	}
}
package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.common.widget.custom.CircleImageView;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
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
import com.z012.chengdu.sc.tools.CountDownTimerImpl;
import com.z012.chengdu.sc.ui.base.BaseActivity;

import java.net.ConnectException;

import cn.jpush.android.api.JPushInterface;

/**
 * 手机绑定
 * 
 * @author kborid
 */
public class BindPhoneActivity extends BaseActivity implements DataCallback {

    private CircleImageView iv_head;
    private TextView tv_name;
	private EditText et_phone, et_yzm;
	private TextView tv_yzm;
	private Button btn_bind;

	private CountDownTimerImpl countDownTimer;
	private String thirdpartusername, thirdpartuserheadphotourl, openid, unionid, mPlatform,usertoken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_bind_phone_act);
		initViews();
		initParams();
		initListeners();
	}

	@Override
	public void initViews() {
		super.initViews();
		tv_center_title.setText("手机绑定");
		tv_right_title.setVisibility(View.GONE);
		title_line.setVisibility(View.GONE);
        iv_head = (CircleImageView) findViewById(R.id.iv_head);
        tv_name = (TextView) findViewById(R.id.tv_name);
		et_phone = (EditText) findViewById(R.id.et_phone);
		et_yzm = (EditText) findViewById(R.id.et_yzm);
		tv_yzm = (TextView) findViewById(R.id.tv_yzm);
		btn_bind = (Button) findViewById(R.id.btn_bind);
	}

	@Override
	public void initParams() {
		super.initParams();
		dealIntent();
		iv_head.setBorderColor(getResources().getColor(R.color.defaultBg));
        Glide.with(this).load(thirdpartuserheadphotourl).placeholder(R.drawable.iv_def_photo).crossFade().into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                if (null != resource) {
                    iv_head.setImageDrawable(resource);
                }
            }
        });
        tv_name.setText(String.format("Hi, %1$s", thirdpartusername));
	}

	@Override
	public void dealIntent() {
		super.dealIntent();
		Bundle bundle = getIntent().getExtras();
		if (null != bundle) {
            thirdpartusername = bundle.getString("thirdpartusername");
            thirdpartuserheadphotourl = bundle.getString("thirdpartuserheadphotourl");
            openid = bundle.getString("openid");
            unionid = bundle.getString("unionid");
            mPlatform = bundle.getString("platform");
            usertoken= bundle.getString("usertoken");
            LogUtil.i("dw", "name = " + thirdpartusername);
            LogUtil.i("dw", "url = " + thirdpartuserheadphotourl);
            LogUtil.i("dw", "openid = " + openid);
            LogUtil.i("dw", "unionid = " + unionid);
            LogUtil.i("dw", "mPlatform = " + mPlatform);
            LogUtil.i("dw", "usertoken = " + usertoken);
        }
	}

	@Override
	public void initListeners() {
		super.initListeners();
		btn_bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = et_phone.getText().toString();
                String yzm = et_yzm.getText().toString();
                if (TextUtils.isEmpty(phone) && Utils.isMobile(phone)) {
                    CustomToast.show("请输入正确的手机号码", Toast.LENGTH_SHORT);
                    return;
                }

                if (TextUtils.isEmpty(yzm)) {
                    CustomToast.show("请输入验证码", Toast.LENGTH_SHORT);
                    return;
                }

                requestBindThirdAccess();
            }
        });

		tv_yzm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = et_phone.getText().toString();
                if (TextUtils.isEmpty(phone) && Utils.isMobile(phone)) {
                    CustomToast.show("请输入正确的手机号码", Toast.LENGTH_SHORT);
                    return;
                }
                requestYZM();
            }
        });
	}

    /**
     * 加载验证码
     */
    private void requestYZM() {
        RequestBeanBuilder builder = RequestBeanBuilder.create(false);
        builder.addBody("BUSINESSTYPE", "40658");// 业务类型01手机绑定；02邮箱绑定；03注册绑定手机；04找回密码；05市民卡注册绑定；06市民卡实名认证
        builder.addBody("MOBILENUM", et_phone.getText().toString());

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.GET_YZM;
        data.flag = 1;

        if (!isProgressShowing()) {
            showProgressDialog(getString(R.string.loading), true);
        }
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    /**
     * 绑定第三方帐号
     */
    private void requestBindThirdAccess() {
        RequestBeanBuilder builder = RequestBeanBuilder.create(false);
        builder.addBody("mobile", et_phone.getText().toString());
        builder.addBody("thirdpartusername", thirdpartusername);
        builder.addBody("thirdpartuserheadphotourl", thirdpartuserheadphotourl);
        builder.addBody("openid", openid);
        builder.addBody("unionid", unionid);
        builder.addBody("platform", mPlatform);
        builder.addBody("usertoken", usertoken);

//        SHA1 sha1 = new SHA1();
//        builder.addBody("password", sha1.getDigestOfString(et_pwd.getText().toString().trim().getBytes()));

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.BIND_ACCESS;
        data.flag = 2;

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
        data.flag = 3;
        requestID = DataLoader.getInstance().loadData(this, data);
    }

	@Override
	public void preExecute(ResponseData request) {
	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (request.flag == 1) {
            removeProgressDialog();
            CustomToast.show("验证码已发送，请稍候...", Toast.LENGTH_LONG);
            tv_yzm.setEnabled(false);
            countDownTimer = new CountDownTimerImpl(60, new CountDownTimerImpl.CountDownTimerListener() {
                @Override
                public void onTick(long time) {
                    tv_yzm.setText(String.format("%1$ss后重试", time / CountDownTimerImpl.SEC));
                }

                @Override
                public void onFinish() {
                    tv_yzm.setEnabled(true);
                    tv_yzm.setText("获取验证码");
                }
            });
            countDownTimer.start();// 启动倒计时
        } else if (request.flag == 2) {// 绑定成功
            JSONObject mJson = JSON.parseObject(response.body.toString());
            String accessTicket = mJson.getString("accessTicket");
            // 记录登录ticket
            SharedPreferenceUtil.getInstance().setString(AppConst.ACCESS_TICKET, accessTicket, true);
            SessionContext.setTicket(accessTicket);
            getUserInfo(accessTicket);
        } else if (request.flag == 3) {// 成功获取用户信息
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
		if (e instanceof ConnectException) {
			message = getString(R.string.dialog_tip_net_error);
		} else {
			message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
		}
		CustomToast.show(message, Toast.LENGTH_LONG);
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != countDownTimer) {
            countDownTimer.stop();
            countDownTimer = null;
        }
    }
}

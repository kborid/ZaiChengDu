package com.z012.chengdu.sc.ui.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.umeng.analytics.MobclickAgent;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.CertUserAuth;
import com.z012.chengdu.sc.net.bean.ThirdPartyBindListBean;
import com.z012.chengdu.sc.ui.base.BaseActivity;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.List;

/**
 * 账户安全
 * 
 * @author kborid
 */
public class AccountSecurityActivity extends BaseActivity implements DataCallback, DialogInterface.OnCancelListener {
	private TextView	tv_certification, tv_phone_number, tv_email, tv_change_password;
	private Button		btn_logout;
	private TableRow	tr_certification, tr_phone_number, tr_email, tr_third_party, tr_change_password;
	private ImageView	iv_qq, iv_wx, iv_wb;
	private final int	MODIFY_THIRD_PARTY	= 100;
	private boolean mIsAuth = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_account_security_act);
		initViews();
		initParams();
		initListeners();
	}

	@Override
	public void initViews() {
		super.initViews();
		tv_center_title.setText("账户安全");
		tv_certification = (TextView) findViewById(R.id.tv_certification);
		tv_phone_number = (TextView) findViewById(R.id.tv_phone_number);
		tv_email = (TextView) findViewById(R.id.tv_email);
		tv_change_password = (TextView) findViewById(R.id.tv_change_password);
		btn_logout = (Button) findViewById(R.id.btn_logout);
		tr_certification = (TableRow) findViewById(R.id.tr_certification);
		tr_phone_number = (TableRow) findViewById(R.id.tr_phone_number);
		tr_email = (TableRow) findViewById(R.id.tr_email);
		tr_third_party = (TableRow) findViewById(R.id.tr_third_party);
		tr_change_password = (TableRow) findViewById(R.id.tr_change_password);
		iv_qq = (ImageView) findViewById(R.id.iv_qq);
		iv_wx = (ImageView) findViewById(R.id.iv_wx);
		iv_wb = (ImageView) findViewById(R.id.iv_wb);
	}

	@Override
	public void initParams() {
		super.initParams();
		CertUserAuth auth = SessionContext.mCertUserAuth;
		if (null == auth || !auth.isAuth) {
			tv_certification.setText("未认证");
		} else {
			tv_certification.setText("已认证");
		}

        String phone = SessionContext.mUser.USERAUTH.mobilenum;
        if (StringUtil.notEmpty(phone)) {
            tv_phone_number.setText(Utils.convertHiddenPhoneStars(phone, 3, 8));
        }

        String data = SharedPreferenceUtil.getInstance().getString(AppConst.THIRDPARTYBIND, null, false);
        if (!TextUtils.isEmpty(data)) {
            setThirdPartyBind(data, false);
        }
		loadThirdPartyBindList();

		mIsAuth = null != SessionContext.mCertUserAuth && SessionContext.mCertUserAuth.isAuth;
		tv_certification.setText(mIsAuth ? "已认证" : "未认证");
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	public void initListeners() {
		super.initListeners();

		tr_certification.setOnClickListener(mIsAuth ? null : this);
		tr_phone_number.setOnClickListener(this);
		tr_email.setOnClickListener(this);
		tr_third_party.setOnClickListener(this);
		tr_change_password.setOnClickListener(this);
		btn_logout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent mIntent = null;
		super.onClick(v);
		switch (v.getId()) {
			case R.id.tr_certification :// 实名认证
				mIntent = new Intent(this, CertificateOneActivity.class);
				startActivity(mIntent);
				break;
			case R.id.tr_phone_number :
				mIntent = new Intent(this, ChangePhoneNoActivity.class);
				mIntent.putExtra("num", tv_phone_number.getText().toString());
				startActivity(mIntent);
				break;
			case R.id.tr_email :

				break;
			case R.id.tr_third_party :
				mIntent = new Intent(this, BindThirdPartyActivity.class);
				startActivityForResult(mIntent, MODIFY_THIRD_PARTY);
				break;
			case R.id.tr_change_password :
				mIntent = new Intent(this, UpdataLoginPwdActivity.class);
				startActivity(mIntent);
				break;
			case R.id.btn_logout :
				cancellationTicket();
				break;

			default :
				break;
		}
	}

	/**
	 * 退出登录
	 */
	public void logout() {
		// 添加友盟自定义事件
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("userId", SessionContext.mUser.USERBASIC.id);
		MobclickAgent.onEvent(this, "UserLogoutSuccess", map);
		SessionContext.cleanUserInfo();
		if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("Tag", false) == true) {
			this.setResult(RESULT_OK, null);
		}
		this.finish();
	}

	/**
	 * 注销票据
	 */
	public void cancellationTicket() {

		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		builder.addBody("accessTicket", SessionContext.getTicket());

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.REMOVE_TICKET;
		data.flag = 1;
		if (!isProgressShowing()) {
			showProgressDialog("正在注销，请稍候...", true);
		}
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	/**
	 * 加载第三方的绑定列表
	 */
	public void loadThirdPartyBindList() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(true);

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.BIND_LIST;
		data.flag = 2;

		if (!isProgressShowing()) {
			showProgressDialog("正在加载，请稍候...", true);
		}
		requestID = DataLoader.getInstance().loadData(this, data);

	}

	@Override
	public void onCancel(DialogInterface dialog) {
		DataLoader.getInstance().clear(requestID);
		removeProgressDialog();
	}

	@Override
	public void preExecute(ResponseData request) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (request.flag == 1) {
            removeProgressDialog();
            logout();
        } else if (request.flag == 2) {
            removeProgressDialog();
            setThirdPartyBind(response.body.toString(), true);
        }
    }

	@Override
	public void notifyError(ResponseData request, ResponseData response, Exception e) {
		removeProgressDialog();
		if (request.flag == 1) {
			logout();
		} else {
			String message;
			if (e instanceof ConnectException) {
				message = getString(R.string.dialog_tip_net_error);
			} else {
				message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
			}
			CustomToast.show(message, Toast.LENGTH_LONG);
		}
	}

	/**
	 * 设置三方绑定
	 *
	 * @param data
	 * @param isSave
	 */
	public void setThirdPartyBind(String data, boolean isSave) {
	    if (!TextUtils.isEmpty(data) && !"[]".equals(data)) {
            List<ThirdPartyBindListBean> temp = JSON.parseArray(data, ThirdPartyBindListBean.class);
            if (temp != null && !temp.isEmpty()) {
                for (ThirdPartyBindListBean bean : temp) {
                    if ("01".equals(bean.platform)) {
                        iv_wb.setVisibility(View.VISIBLE);
                    } else if ("02".equals(bean.platform)) {
                        iv_qq.setVisibility(View.VISIBLE);
                    } else {
                        iv_wx.setVisibility(View.VISIBLE);
                    }

                }
            }
        }

        if (isSave)
            SharedPreferenceUtil.getInstance().setString(AppConst.THIRDPARTYBIND, data, false);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		try {
			super.onActivityResult(requestCode, resultCode, intent);
			if (resultCode != Activity.RESULT_OK) {
				return;
			}
			switch (requestCode) {
				case MODIFY_THIRD_PARTY ://重置状态
					iv_wb.setVisibility(View.GONE);
					iv_qq.setVisibility(View.GONE);
					iv_wx.setVisibility(View.GONE);
					String data = SharedPreferenceUtil.getInstance().getString(AppConst.THIRDPARTYBIND, null, false);
					if (StringUtil.notEmpty(data)) {
						// 已绑定的列表
						List<ThirdPartyBindListBean> mList = JSON.parseArray(data, ThirdPartyBindListBean.class);
						if (mList != null && !mList.isEmpty()) {
							for (int i = 0; i < mList.size(); i++) {
								ThirdPartyBindListBean temp = mList.get(i);
								if ("01".equals(temp.platform)) {
									iv_wb.setVisibility(View.VISIBLE);
								} else if ("02".equals(temp.platform)) {
									iv_qq.setVisibility(View.VISIBLE);
								} else {
									iv_wx.setVisibility(View.VISIBLE);
								}
							}
						}
					}
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

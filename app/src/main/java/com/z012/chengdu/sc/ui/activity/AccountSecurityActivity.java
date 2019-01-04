package com.z012.chengdu.sc.ui.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
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

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 账户安全
 * 
 * @author kborid
 */
public class AccountSecurityActivity extends BaseActivity implements DataCallback, DialogInterface.OnCancelListener {
	private final int	MODIFY_THIRD_PARTY	= 100;
	private boolean mIsAuth = false;

	@BindView(R.id.tv_certification)
    TextView tv_certification;
	@BindView(R.id.tv_phone_number)
    TextView tv_phone_number;
    @BindView(R.id.tr_certification)
    TableRow tr_certification;
    @BindView(R.id.iv_qq)
    ImageView iv_qq;
    @BindView(R.id.iv_wx)
    ImageView iv_wx;
    @BindView(R.id.iv_wb)
    ImageView iv_wb;

	@Override
	protected int getLayoutResId() {
		return R.layout.ui_account_security_act;
	}

	@Override
	public void initParams() {
		super.initParams();
        tv_center_title.setText("账户安全");
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
		tr_certification.setEnabled(!mIsAuth);
		tv_certification.setText(mIsAuth ? "已认证" : "未认证");
		requestCertResult();
	}

	@OnClick(R.id.tr_certification)
    void certClick() {
	    startActivity(new Intent(this, CertificateOneActivity.class));
    }

	@OnClick(R.id.tr_third_party)
    void thirdPartyClick() {
	    Intent intent = new Intent(this, BindThirdPartyActivity.class);
        startActivityForResult(intent, MODIFY_THIRD_PARTY);
    }

    @OnClick(R.id.tr_phone_number)
    void phoneNumClick() {
        Intent intent = new Intent(this, ChangePhoneNoActivity.class);
        intent.putExtra("num", tv_phone_number.getText().toString());
        startActivity(intent);
    }

    @OnClick(R.id.tr_change_password)
    void changePwdClick() {
	    startActivity(new Intent(this, UpdataLoginPwdActivity.class));
    }

	@OnClick(R.id.btn_logout)
    void logoutClick() {
        cancellationTicket();
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

	private void requestCertResult() {
		RequestBeanBuilder b = RequestBeanBuilder.create(true);
		b.addBody("uid", SessionContext.mUser.LOCALUSER.id);

		ResponseData d = b.syncRequest(b);
		d.path = NetURL.CERT_STATUS_BY_UID;
		d.flag = 3;

		if (!isProgressShowing()) {
			showProgressDialog("正在加载，请稍候...", true);
		}

		requestID = DataLoader.getInstance().loadData(this, d);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		DataLoader.getInstance().clear(requestID);
		removeProgressDialog();
	}

	@Override
	public void preExecute(ResponseData request) {
	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (request.flag == 1) {
            removeProgressDialog();
            logout();
        } else if (request.flag == 2) {
            removeProgressDialog();
            setThirdPartyBind(response.body.toString(), true);
        } else if (request.flag == 3) {
            removeProgressDialog();
            if (null != response && response.body != null) {
                System.out.println(response.body.toString());
                SessionContext.mCertUserAuth = JSON.parseObject(response.body.toString(), CertUserAuth.class);
                mIsAuth = null != SessionContext.mCertUserAuth && SessionContext.mCertUserAuth.isAuth;
                tv_certification.setText(mIsAuth ? "已认证" : "未认证");
                tr_certification.setEnabled(!mIsAuth);
            }
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

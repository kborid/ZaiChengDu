package com.z012.chengdu.sc.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.ui.base.BaseActivity;

import java.net.ConnectException;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 手机绑定
 * 
 * @author kborid
 */
public class BindPhoneActivity extends BaseActivity implements DataCallback, DialogInterface.OnCancelListener {

    @BindView(R.id.et_login_phone) EditText et_login_phone;
	@BindView(R.id.btn_next) Button btn_next;

	private String phoneNum, thirdpartusername, thirdpartuserheadphotourl, openid, unionid, mPlatform,usertoken;

	@Override
	protected int getLayoutResId() {
		return R.layout.ui_bind_phone_act;
	}

	@Override
	public void initParams() {
		super.initParams();
		tv_center_title.setText("手机绑定");
		tv_center_title.setTextColor(0xffffffff);
		findViewById(R.id.comm_title_rl).setBackgroundResource(R.color.transparent);
		tv_right_title.setVisibility(View.GONE);
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
            usertoken = bundle.getString("usertoken");
        }
	}

	@OnClick(R.id.btn_next) void next() {
        phoneNum = et_login_phone.getText().toString();
        if (!Utils.isMobile(phoneNum)) {
            CustomToast.show("请输入正确的手机号码", 0);
            return;
        }
        CheckPhoneNumber();
    }

	/**
	 * 检测手机号是否已经注册
	 */
	public void CheckPhoneNumber() {

		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		builder.addBody("MOBILENUM", phoneNum);

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.CHECK_PHONE;
		data.flag = 10;

		if (!isProgressShowing()) {
			showProgressDialog(getString(R.string.loading), true);
		}
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	@Override
	public void preExecute(ResponseData request) {
	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
		removeProgressDialog();
		if (request.flag == 10) {
			if (response.body instanceof Boolean) {
				Intent intent = new Intent();
				intent.setClass(this, BindPhoneSecondStepActivity.class);
				intent.putExtra("isOccupy", !(Boolean) response.body);
				intent.putExtra("phoneNum", phoneNum);

				intent.putExtra("thirdpartusername", thirdpartusername);
				intent.putExtra("thirdpartuserheadphotourl", thirdpartuserheadphotourl);
				intent.putExtra("openid", openid);
				intent.putExtra("unionid", unionid);
				intent.putExtra("platform", mPlatform);
				intent.putExtra("usertoken", usertoken);
				startActivity(intent);
			}
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
	public void onCancel(DialogInterface dialog) {
		DataLoader.getInstance().clear(requestID);
		removeProgressDialog();
	}

}

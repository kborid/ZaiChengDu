package com.z012.chengdu.sc.ui.activity;

import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.tools.SHA1;
import com.z012.chengdu.sc.ui.base.BaseActivity;

import java.net.ConnectException;

/**
 * 更改登录密码
 * 
 * @author LiaoBo
 * 
 */
public class UpdataLoginPwdActivity extends BaseActivity implements DataCallback, DialogInterface.OnCancelListener {
	private EditText	et_old_pwd, et_new_pwd, et_new_pwd2;
	private Button		btn_save;
	private String		mPassword;

	@Override
	protected int getLayoutResId() {
		return R.layout.ui_register_act;
	}

	@Override
	public void initParams() {
		super.initParams();
		tv_center_title.setText("更改登录密码");
		tv_right_title.setVisibility(View.GONE);

		et_old_pwd = (EditText) findViewById(R.id.et_old_pwd);
		btn_save = (Button) findViewById(R.id.btn_save);
		et_new_pwd = (EditText) findViewById(R.id.et_new_pwd);
		et_new_pwd2 = (EditText) findViewById(R.id.et_new_pwd2);
	}

	@Override
	public void initListeners() {
		super.initListeners();
		btn_save.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.btn_save:
			loadData();
			break;

		default:
			break;
		}

	}

	/**
	 * 加载数据
	 */
	private void loadData() {

		String OLDPWD = et_old_pwd.getText().toString().trim();
		mPassword = et_new_pwd.getText().toString().trim();
		String NEWPWD2 = et_new_pwd2.getText().toString().trim();
		if (StringUtil.empty(OLDPWD)) {
			CustomToast.show("请输入原密码", 0);
			return;
		}
		if (StringUtil.empty(mPassword)) {
			CustomToast.show("请输入新密码", 0);
			return;
		}
		if (mPassword.length() < 6) {
			CustomToast.show("请输入6-20个字符的新密码", 0);
			return;
		}
		if (!mPassword.equals(NEWPWD2)) {
			CustomToast.show("两次密码不一致", 0);
			return;
		}
		if(OLDPWD.equals("mPassword")){
			CustomToast.show("新密码和原密码不能一致", 0);
			return;
		}
		
		RequestBeanBuilder builder = RequestBeanBuilder.create(true);
		
		builder.addBody("PWDSTRENGTH", "1");//密码强弱
		SHA1 sha1 = new SHA1();
		builder.addBody("OLDPASSWORD", sha1.getDigestOfString(OLDPWD.getBytes()));
		builder.addBody("NEWPASSWORD", sha1.getDigestOfString(mPassword.getBytes()));

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.UPDATA_LOGIN_PWD;
		data.flag = 1;

		if (!isProgressShowing()) {
			showProgressDialog(getString(R.string.loading), true);
		}
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	@Override
	public void preExecute(ResponseData request) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
		removeProgressDialog();
		if (request.flag == 1) {
			CustomToast.show("修改成功", 0);
			this.finish();
		}

	}

	@Override
	public void notifyError(ResponseData request, ResponseData response, Exception e) {
		removeProgressDialog();

		String message;
		if (e != null && e instanceof ConnectException) {
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

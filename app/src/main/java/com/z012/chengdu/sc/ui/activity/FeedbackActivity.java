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
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.ui.base.BaseActivity;

import java.net.ConnectException;

/**
 * 意见反馈
 * 
 * @author LiaoBo
 * 
 */
public class FeedbackActivity extends BaseActivity implements DataCallback, DialogInterface.OnCancelListener {
	private EditText	et_content;
	private Button		btn_sbmit;

	@Override
	protected int getLayoutResId() {
		return R.layout.ui_feedback_act;
	}

	@Override
	public void initParams() {
		super.initParams();
		tv_center_title.setText("意见反馈");
		tv_right_title.setVisibility(View.GONE);
		et_content = (EditText) findViewById(R.id.et_content);
		btn_sbmit = (Button) findViewById(R.id.btn_sbmit);
	}

	@Override
	public void initListeners() {
		super.initListeners();
		btn_sbmit.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.btn_sbmit :
				if (StringUtil.notEmpty(et_content.getText().toString().trim())) {
					if (StringUtil.containsEmoji(et_content.getText().toString())) {
						CustomToast.show("不支持输入Emoji表情符号", 0);
						return;
					}
					loadData();
				} else {
					CustomToast.show("内容不允许为空", 0);
				}
				break;
			default :
				break;
		}

	}

	/**
	 * 加载数据
	 */
	private void loadData() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		builder.addBody("content", et_content.getText().toString().trim()).addBody("login", StringUtil.doEmpty(SessionContext.mUser.USERAUTH.login));

		ResponseData requster = builder.syncRequest(builder);
		requster.flag = 1;
		requster.path = NetURL.FEEDBACK;

		if (!isProgressShowing()) {
			showProgressDialog(getString(R.string.present), true);
		}
		requestID = DataLoader.getInstance().loadData(this, requster);
	}

	@Override
	public void preExecute(ResponseData request) {
	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
		removeProgressDialog();
		CustomToast.show("提交成功", 0);
		this.finish();
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

package com.z012.chengdu.sc.ui.activity;

import java.net.ConnectException;
import java.util.HashMap;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.widget.CustomToast;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.ui.base.BaseActivity;

/**
 * 系统设置
 * 
 * @author LiaoBo
 * 
 */
public class SystemSetActivity extends BaseActivity implements DataCallback, DialogInterface.OnCancelListener {
	private LinearLayout	ly_feedback, ly_problem, ly_update, ly_guide, ly_about;
	private Button			btn_exit_login;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_sys_set_act);

		initViews();
		initParams();
		initListeners();
	}

	@Override
	public void initViews() {
		super.initViews();
		tv_center_title.setText("系统设置");
		tv_right_title.setVisibility(View.GONE);
		ly_feedback = (LinearLayout) findViewById(R.id.ly_feedback);
		ly_problem = (LinearLayout) findViewById(R.id.ly_problem);
		ly_update = (LinearLayout) findViewById(R.id.ly_update);
		ly_guide = (LinearLayout) findViewById(R.id.ly_guide);
		ly_about = (LinearLayout) findViewById(R.id.ly_about);
		btn_exit_login = (Button) findViewById(R.id.btn_exit_login);
	}

	@Override
	public void initParams() {
		super.initParams();
		if (SessionContext.isLogin()) {
			btn_exit_login.setVisibility(View.VISIBLE);
		} else {
			btn_exit_login.setVisibility(View.GONE);
		}
	}

	@Override
	public void initListeners() {
		super.initListeners();
		ly_feedback.setOnClickListener(this);
		ly_problem.setOnClickListener(this);
		ly_update.setOnClickListener(this);
		ly_guide.setOnClickListener(this);
		ly_about.setOnClickListener(this);
		btn_exit_login.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		Intent mIntent = null;
		switch (v.getId()) {
			case R.id.ly_feedback :
				mIntent = new Intent();
				if (SessionContext.isLogin()) {
					mIntent.setClass(this, FeedbackActivity.class);
					startActivity(mIntent);
				} else {
					sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
				}
				break;
			case R.id.ly_problem :
				mIntent = new Intent(this, WebViewActivity.class);
				String url = SharedPreferenceUtil.getInstance().getString(AppConst.PROBLEM, "", true);
				mIntent.putExtra("path", url);
				mIntent.putExtra("title", "常见问题");
				startActivity(mIntent);
				break;
			case R.id.ly_update :
				showProgressDialog("检测中，请等待...", true);
				UmengUpdateAgent.setUpdateAutoPopup(false);// 自定义更新检查回调，所以禁止弹出更新提示，避免重复显示
				UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
					@Override
					public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
						removeProgressDialog();
						switch (updateStatus) {
							case UpdateStatus.Yes : // has update
								UmengUpdateAgent.showUpdateDialog(SystemSetActivity.this, updateInfo);// 显示更新对话框
								break;
							case UpdateStatus.No : // has no update
								CustomToast.show("已经是最新版本", Toast.LENGTH_SHORT);
								break;
							case UpdateStatus.NoneWifi : // none wifi
								CustomToast.show("没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT);
								break;
							case UpdateStatus.Timeout : // time out
								CustomToast.show("更新检查失败，请检查网络", Toast.LENGTH_SHORT);
								break;
						}
					}

				});
				UmengUpdateAgent.forceUpdate(this);// 手动检测更新
				break;
			case R.id.ly_guide :
				mIntent = new Intent();
				mIntent.setClass(this, UserGuideActivity.class);
				startActivity(mIntent);
				break;
			case R.id.ly_about :
				mIntent = new Intent(this, AboutActivity.class);
				startActivity(mIntent);
				break;
			case R.id.btn_exit_login :
				cancellationTicket();
				break;
			default :
				break;
		}
	}

	/**
	 * 注销票据
	 */
	public void cancellationTicket() {

		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		builder.addBody("accessTicket", SessionContext.getTicket());

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.REMOVE_TICKET;

		if (!isProgressShowing()) {
			showProgressDialog("正在注销，请稍候...", true);
		}
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
		removeProgressDialog();
		logout();
	}

	@Override
	public void notifyError(ResponseData request, ResponseData response, Exception e) {
		removeProgressDialog();
		// String message;
		if (e != null && e instanceof ConnectException) {
			// message = getString(R.string.dialog_tip_net_error);
			// CustomToast.show(message, Toast.LENGTH_LONG);
			logout();
		} else {
			logout();
		}

	}

	/**
	 * 退出登录
	 */
	public void logout() {
		SessionContext.cleanUserInfo();
		if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("Tag", false) == true) {
			this.setResult(RESULT_OK, null);
		}
		this.finish();
		// 添加友盟自定义事件
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("userId", SessionContext.mUser.USERBASIC.id);
		MobclickAgent.onEvent(this, "UserLogoutSuccess", map);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		DataLoader.getInstance().clear(requestID);
		removeProgressDialog();
	}

}

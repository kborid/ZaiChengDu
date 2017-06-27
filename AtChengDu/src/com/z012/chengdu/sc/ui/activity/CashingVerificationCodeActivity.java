package com.z012.chengdu.sc.ui.activity;

import java.net.ConnectException;
import java.text.DecimalFormat;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.SelectBankBean;
import com.z012.chengdu.sc.ui.base.BaseActivity;
import com.z012.chengdu.sc.ui.dialog.CustomDialogUtil;
import com.z012.chengdu.sc.ui.dialog.CustomDialogUtil.onCallBackListener;

/**
 * 提现-验证码提交页面
 * 
 * @author LiaoBo
 */
public class CashingVerificationCodeActivity extends BaseActivity implements DataCallback {
	private TextView		tv_phone_num;
	private EditText		et_verification_code;
	private Button			btn_confirm, btn_getYZM;
	private String			thirdbindId, cashoutFee;	// 绑定的三方id；提现费用
	private SelectBankBean	mItem;
	private CountDownTimer	mCountDownTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_cashing_verification_code_act);
		initViews();
		initParams();
		initListeners();
	}

	@Override
	public void initViews() {
		super.initViews();
		tv_center_title.setText("提现");
		tv_phone_num = (TextView) findViewById(R.id.tv_phone_num);
		et_verification_code = (EditText) findViewById(R.id.et_verification_code);
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
		btn_getYZM = (Button) findViewById(R.id.btn_getYZM);
	}

	@Override
	public void initParams() {
		super.initParams();
		dealIntent();
		setCountDownTimer(60 * 1000, 1000);
	}

	@Override
	public void dealIntent() {
		super.dealIntent();
		try {
			mItem = (SelectBankBean) getIntent().getExtras().getSerializable("item");

			String mobilenum = getIntent().getExtras().getString("mobilenum");
			tv_phone_num.setText(mobilenum);

			thirdbindId = getIntent().getExtras().getString("thirdbindId");
			cashoutFee = getIntent().getExtras().getString("cashoutFee");
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Override
	public void initListeners() {
		super.initListeners();
		btn_confirm.setOnClickListener(this);
		btn_getYZM.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.btn_confirm :
				loadData();
				break;
			case R.id.btn_getYZM :
				loadYZM();
				break;

			default :
				break;
		}
	}

	/**
	 * 加载验证码数据
	 */
	public void loadYZM() {
		if (!SessionContext.isLogin()) {
			sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
			return;
		}
		try {
			RequestBeanBuilder builder = RequestBeanBuilder.create(true);
			builder.addBody("MOBILENUM", SessionContext.mUser.USERAUTH.mobilenum);
			builder.addBody("BUSINESSTYPE", "60000");
			StringBuilder sb = new StringBuilder();
			if ("000".equals(mItem.thirdtype)) {// 支付宝
				String count = null;
				if (StringUtil.notEmpty(mItem.thirdaccount)) {// 屏蔽手机号中间位数
					if (Utils.isEmail(mItem.thirdaccount)) {
						String regex = "(\\w{4})(\\w+)(\\w{0})(@\\w+)";
						count = mItem.thirdaccount.replaceAll(regex, "$1***$3$4");
					} else {
						count = mItem.thirdaccount.substring(0, mItem.thirdaccount.length() - (mItem.thirdaccount.substring(3)).length()) + "****" + mItem.thirdaccount.substring(7);
					}
				}
				sb.append("支付宝").append(count);
			} else if ("002".equals(mItem.thirdtype)) {// 银行
				if (StringUtil.notEmpty(mItem.thirdaccount)) {
					sb.append("尾号").append(mItem.thirdaccount.substring(12));
				}
			}

			builder.addBody("account", sb.toString());
			
			DecimalFormat decimalFormat = new DecimalFormat("0.00");// 构造方法的字符格式这里如果小数不足2位,会以0补足.
			String price = decimalFormat.format(Float.parseFloat(cashoutFee));// format 返回的是字符串
			builder.addBody("amount", price);
			ResponseData data = builder.syncRequest(builder);
			data.path = NetURL.GET_YZM;
			data.flag = 1;
			if (!isProgressShowing())
				showProgressDialog(getString(R.string.loading), false);
			requestID = DataLoader.getInstance().loadData(this, data);
		} catch (Exception e) {
			e.printStackTrace();
			CustomToast.show("获取验证码失败", 0);
		}
	}

	/**
	 * 加载数据
	 */
	public void loadData() {
		try {
			if (!SessionContext.isLogin()) {
				sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
				return;
			}

			String code = et_verification_code.getText().toString().trim();
			if (StringUtil.empty(code)) {
				CustomToast.show("验证码不允许为空", 0);
				return;
			}

			RequestBeanBuilder builder = RequestBeanBuilder.create(true);
			builder.addBody("checkCode", code);
			builder.addBody("phone", SessionContext.mUser.USERAUTH.mobilenum);
			builder.addBody("userId", SessionContext.mUser.USERBASIC.id);
			builder.addBody("cashoutFee", cashoutFee);
			builder.addBody("siteId", SessionContext.getAreaInfo(1));
			builder.addBody("thirdbindId", thirdbindId);
			ResponseData data = builder.syncRequest(builder);
			data.path = NetURL.CASHING;
			data.flag = 2;
			if (!isProgressShowing())
				showProgressDialog(getString(R.string.loading), false);
			requestID = DataLoader.getInstance().loadData(this, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 操作成功，到账时间提示对话框
	 */
	public void showTipDialog() {
		CustomDialogUtil dialog = new CustomDialogUtil(this);
		dialog.setBtnText("确定", null);
		dialog.setCanceled(false);
		dialog.show("您的提现操作已成功，预计1-2个工作日到账！");
		dialog.setListeners(new onCallBackListener() {

			@Override
			public void rightBtn(CustomDialogUtil dialog) {
				dialog.dismiss();
			}

			@Override
			public void leftBtn(CustomDialogUtil dialog) {
				Intent mIntent = new Intent(CashingVerificationCodeActivity.this, BalanceInquireActivity.class);
				mIntent.putExtra("isCashingSucc", true);
				mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(mIntent);
				dialog.dismiss();
			}
		});
	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
		removeProgressDialog();

		if (request.flag == 1) {
			CustomToast.show("验证码已发送，请稍候...", Toast.LENGTH_LONG);
			btn_getYZM.setEnabled(false);
			mCountDownTimer.start();// 启动倒计时
		} else {
			showTipDialog();
//			double balance = SessionContext.mUser.USERBASIC.amount;
//			SessionContext.mUser.USERBASIC.amount = balance - Double.valueOf(cashoutFee);// 重置余额
//			Intent mIntent = new Intent(AppConst.ACTION_DYNAMIC_USER_INFO); //发送余额变更广播，刷新首页数据
//			LocalBroadcastManager.getInstance(AppContext.mMainContext).sendBroadcast(mIntent);
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
				btn_getYZM.setText((millisUntilFinished / 1000) + " s");
			}

			@Override
			public void onFinish() {
				btn_getYZM.setEnabled(true);
				btn_getYZM.setText("获取验证码");
			}
		};
	}
}

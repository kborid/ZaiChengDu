package com.z012.chengdu.sc.ui.activity;

import java.net.ConnectException;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.ui.base.BaseActivity;

/**
 * 绑定银行卡或支护宝
 * 
 * @author LiaoBo
 */
public class BindBankCardActivity extends BaseActivity implements DataCallback {
	private TableRow	tr_choice;
	private TextView	tv_choice, tv_tip;
	private EditText	et_card_number, et_name;
	private EditText	et_pay_number, et_pay_name;
	private TableLayout	tl_band_card, tl_alipay;
	private Button		btn_complete;
	private boolean		isBindAlipay	= true;	// 默认选择绑定支护宝
	private int			mCardType;					// 银行卡类型0：银行卡1：支护宝
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_bind_bank_card_act);
		initViews();
		initParams();
		initListeners();
	}

	@Override
	public void initViews() {
		super.initViews();
		tv_center_title.setText("绑定账户");
		tr_choice = (TableRow) findViewById(R.id.tr_choice);
		tv_choice = (TextView) findViewById(R.id.tv_choice);
		tv_tip = (TextView) findViewById(R.id.tv_tip);
		et_card_number = (EditText) findViewById(R.id.et_card_number);
		et_name = (EditText) findViewById(R.id.et_name);
		tl_band_card = (TableLayout) findViewById(R.id.tl_band_card);
		tl_alipay = (TableLayout) findViewById(R.id.tl_alipay);
		et_pay_number = (EditText) findViewById(R.id.et_pay_number);
		et_pay_name = (EditText) findViewById(R.id.et_pay_name);
		btn_complete = (Button) findViewById(R.id.btn_complete);
	}

	@Override
	public void initParams() {
		super.initParams();
	}

	@Override
	public void initListeners() {
		super.initListeners();
		tr_choice.setOnClickListener(this);
		btn_complete.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.tr_choice :
				showChoiceDialog();
				break;
			case R.id.btn_complete :
				completet();
				break;
			default :
				break;
		}
	}

	/**
	 * 完成后验证并提交数据
	 */
	public void completet() {
		if (!SessionContext.isLogin()) {
			sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
			return;
		}
		String pay_number, pay_name, type;
		if (isBindAlipay) {
			pay_number = et_pay_number.getText().toString().trim();
			pay_name = et_pay_name.getText().toString().trim();
			if (StringUtil.empty(pay_number)) {
				CustomToast.show("请输入支付宝账号", 0);
				return;
			}
			if (StringUtil.empty(pay_name)) {
				CustomToast.show("请输入姓名", 0);
				return;
			}
			type = "000";
		} else {
			pay_number = et_card_number.getText().toString().trim();
			pay_name = et_name.getText().toString().trim();
			if (StringUtil.empty(pay_number)) {
				CustomToast.show("请输入银行卡号", 0);
				return;
			}
			if (StringUtil.empty(pay_name)) {
				CustomToast.show("请输入开户人姓名", 0);
				return;
			}
			type = "002";
		}

		RequestBeanBuilder builder = RequestBeanBuilder.create(true);
		builder.addBody("userid", SessionContext.mUser.USERBASIC.id);//
		builder.addBody("bindtype", "001");// 000:商户号；001: 普通账户
		builder.addBody("thirdaccount", pay_number);// thirdaccount第三方账户ID
		builder.addBody("realname", pay_name);// realname第三方账户真实姓名
		builder.addBody("thirdtype", type);// （第三方平台 000-支付宝 001-微信 622588-招商银行等银行卡前缀）
		builder.addBody("operatetype", "0");// operatetype（0绑定、1解绑）

		ResponseData requster = builder.syncRequest(builder);
		requster.flag = 1;
		requster.path = NetURL.BIND_BANK;

		if (!isProgressShowing()) {
			showProgressDialog(getString(R.string.present), true);
		}
		requestID = DataLoader.getInstance().loadData(this, requster);
	}
	/**
	 * 显示选择绑定内容对话框
	 */
	public void showChoiceDialog() {
		Builder builder = new android.app.AlertDialog.Builder(this);
		// 设置对话框的标题
		builder.setTitle("选择绑定内容");
		final String[] str = new String[]{"支付宝", "银行卡"};
		builder.setSingleChoiceItems(str, mCardType, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				tv_choice.setText(str[which]);
				if (which == 0) {
					tl_band_card.setVisibility(View.GONE);
					tv_tip.setVisibility(View.GONE);
					tl_alipay.setVisibility(View.VISIBLE);
					isBindAlipay = true;
					mCardType = 0;
				} else {
					tl_band_card.setVisibility(View.VISIBLE);
					tv_tip.setVisibility(View.VISIBLE);
					tl_alipay.setVisibility(View.GONE);
					isBindAlipay = false;
					mCardType = 1;
				}
				dialog.dismiss();
			}
		});

		// builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int which) {
		// dialog.dismiss();
		// }
		// });
		builder.create().show();
	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
		removeProgressDialog();
		CustomToast.show("添加成功", 0);
		this.setResult(RESULT_OK, null);
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
}

package com.z012.chengdu.sc.ui.activity;

import java.text.DecimalFormat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.net.bean.SelectBankBean;
import com.z012.chengdu.sc.ui.base.BaseActivity;

/**
 * 提现
 * 
 * @author LiaoBo
 */
public class CashingActivity extends BaseActivity {
	private TextView		tv_card_name, tv_type;
	private EditText		et_money;
	private Button			btn_confirm;
	private SelectBankBean	mItem;
	private double			mAmount;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_cashing_act);
		initViews();
		initParams();
		initListeners();
	}

	@Override
	public void initViews() {
		super.initViews();
		tv_center_title.setText("提现");
		tv_card_name = (TextView) findViewById(R.id.tv_card_name);
		tv_type = (TextView) findViewById(R.id.tv_type);
		et_money = (EditText) findViewById(R.id.et_money);
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
	}

	@Override
	public void initParams() {
		super.initParams();
		dealIntent();
	}

	@Override
	public void dealIntent() {
		super.dealIntent();
		try {
			mItem = (SelectBankBean) getIntent().getExtras().getSerializable("item");
			mAmount = getIntent().getExtras().getDouble("amount");

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
				tv_type.setText("支付宝");
				tv_card_name.setText(count);
			} else if ("002".equals(mItem.thirdtype)) {// 银行
				tv_card_name.setText(StringUtil.doEmpty(mItem.bankname, "未知银行"));
				tv_type.setText("储值卡");
			}
			DecimalFormat decimalFormat = new DecimalFormat("0.00");// 构造方法的字符格式这里如果小数不足2位,会以0补足.
			String price = decimalFormat.format(mAmount);// format 返回的是字符串
			et_money.setHint("当前账户余额" + price);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void initListeners() {
		super.initListeners();
		btn_confirm.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.btn_confirm :

				if (!SessionContext.isLogin()) {
					sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
					return;
				}

				String money = et_money.getText().toString().trim();
				if (StringUtil.empty(money)) {
					CustomToast.show("提现金额不允许为空", 0);
					return;
				}

				try {
					float price = Float.parseFloat(money);
					if (price <= 0) {
						CustomToast.show("提现金额不允许为0", 0);
						return;
					}

					if (price > mAmount) {
						CustomToast.show("您的余额不足", 0);
						return;
					}
				} catch (Exception e) {
				}

				String phone = SessionContext.mUser.USERAUTH.mobilenum;
				if (StringUtil.notEmpty(phone)) {
					phone = phone.substring(0, phone.length() - (phone.substring(3)).length()) + "****" + phone.substring(7);
				}
				Intent intent = new Intent(this, CashingVerificationCodeActivity.class);
				intent.putExtra("mobilenum", phone);
				intent.putExtra("thirdbindId", mItem.id);
				intent.putExtra("cashoutFee", money);
				intent.putExtra("item", mItem);
				startActivity(intent);
				break;

			default :
				break;
		}
	}

}

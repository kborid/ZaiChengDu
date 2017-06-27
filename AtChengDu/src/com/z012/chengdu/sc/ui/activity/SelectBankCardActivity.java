package com.z012.chengdu.sc.ui.activity;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.SelectBankBean;
import com.z012.chengdu.sc.ui.adapter.SelectBankAdapter;
import com.z012.chengdu.sc.ui.base.BaseActivity;

/**
 * 选择银行卡
 * 
 * @author LiaoBo
 */
public class SelectBankCardActivity extends BaseActivity implements DataCallback {
	private final int				BIND_BANK_CODE	= 100;
	private Button					btn_add_other;
	private ListView				mBandListView, mAlipayListView;
	private BaseAdapter				bankAdapter, alipayAdapter;
	private List<SelectBankBean>	mBankList		= new ArrayList<SelectBankBean>();
	private List<SelectBankBean>	mAliList		= new ArrayList<SelectBankBean>();
	private TextView				tv_bank, tv_ali, tv_bind_tip;
	private boolean					isBannedClick;
	private double					mAmount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_select_bank_card_act);
		initViews();
		initParams();
		initListeners();
	}

	@Override
	public void initViews() {
		super.initViews();
		tv_center_title.setText("选择银行卡");
		btn_add_other = (Button) findViewById(R.id.btn_add_other);
		mBandListView = (ListView) findViewById(R.id.listView);
		mAlipayListView = (ListView) findViewById(R.id.ali_listView);
		tv_bank = (TextView) findViewById(R.id.tv_bank);
		tv_ali = (TextView) findViewById(R.id.tv_ali);
		tv_bind_tip = (TextView) findViewById(R.id.tv_bind_tip);
	}

	@Override
	public void initParams() {
		super.initParams();
		loadData();
		dealIntent();
		bankAdapter = new SelectBankAdapter(this, isBannedClick, mBankList, 2,mAmount);
		alipayAdapter = new SelectBankAdapter(this, isBannedClick, mAliList, 0,mAmount);
		mBandListView.setAdapter(bankAdapter);
		mAlipayListView.setAdapter(alipayAdapter);
	}

	public void loadData() {
		if (!SessionContext.isLogin()) {
			sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
			return;
		}
		RequestBeanBuilder builder = RequestBeanBuilder.create(true);
		builder.addBody("userid", SessionContext.mUser.USERBASIC.id);
		ResponseData requster = builder.syncRequest(builder);
		requster.flag = 1;
		requster.path = NetURL.BIND_BANK_LIST;

		if (!isProgressShowing()) {
			showProgressDialog(getString(R.string.loading), true);
		}
		requestID = DataLoader.getInstance().loadData(this, requster);
	}

	@Override
	public void initListeners() {
		super.initListeners();
		btn_add_other.setOnClickListener(this);
	}

	@Override
	public void dealIntent() {
		super.dealIntent();
		try {
			isBannedClick = getIntent().getExtras().getBoolean("bannedClick", false);
			mAmount = getIntent().getExtras().getDouble("amount");
		} catch (Exception e) {
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		Intent mIntent = null;
		switch (v.getId()) {
			case R.id.btn_add_other :// 添加其他绑定
				mIntent = new Intent(this, BindBankCardActivity.class);
				startActivityForResult(mIntent, BIND_BANK_CODE);
				break;

			default :
				break;
		}
	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
		removeProgressDialog();
		JSONObject mJson = JSON.parseObject(response.body.toString());
		String json = mJson.getString("dataList");
		List<SelectBankBean> temp = JSON.parseArray(json, SelectBankBean.class);
		if (temp != null && !temp.isEmpty()) {
			mBankList.clear();
			mAliList.clear();
			for (int i = 0; i < temp.size(); i++) {
				if (temp.get(i).thirdtype.equals("000")) {// 支付宝
					mAliList.add(temp.get(i));
				} else if (temp.get(i).thirdtype.equals("001")) {// 微信

				} else {// 银行
					mBankList.add(temp.get(i));
				}
			}
			if (!mBankList.isEmpty()) {
				tv_bank.setVisibility(View.VISIBLE);
				tv_bind_tip.setVisibility(View.GONE);
				btn_add_other.setText("添加其他绑定");
			}
			if (!mAliList.isEmpty()) {
				tv_ali.setVisibility(View.VISIBLE);
				tv_bind_tip.setVisibility(View.GONE);
				btn_add_other.setText("添加其他绑定");
			}
			bankAdapter.notifyDataSetChanged();
			alipayAdapter.notifyDataSetChanged();
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
			case BIND_BANK_CODE :
				loadData();// 刷新页面数据
				break;

			default :
				break;
		}

	}

}

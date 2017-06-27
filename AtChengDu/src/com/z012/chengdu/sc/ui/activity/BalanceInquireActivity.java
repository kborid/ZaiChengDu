package com.z012.chengdu.sc.ui.activity;

import java.net.ConnectException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.BalanceInquireBean;
import com.z012.chengdu.sc.ui.adapter.BalanceInquireAdapter;
import com.z012.chengdu.sc.ui.base.BaseActivity;
import com.z012.chengdu.sc.ui.dialog.CustomDialogUtil;
import com.z012.chengdu.sc.ui.dialog.CustomDialogUtil.onCallBackListener;

/**
 * 余额查询
 * 
 * @author LiaoBo
 */
public class BalanceInquireActivity extends BaseActivity implements DataCallback, OnRefreshListener2<ListView> {
	private PullToRefreshListView			listView;
	private BalanceInquireAdapter			mAdapter;
	private List<BalanceInquireBean.Result>	mBeans	= new ArrayList<BalanceInquireBean.Result>();
	private Button							btn_cashing;
	private TextView						tv_balance;
	private int								page_index;
	private double							mAmount;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_balance_inquire_act);
		initViews();
		initParams();
		initListeners();
	}
	@Override
	public void initViews() {
		super.initViews();
		listView = (PullToRefreshListView) findViewById(R.id.listView);
		btn_cashing = (Button) findViewById(R.id.btn_cashing);
		tv_balance = (TextView) findViewById(R.id.tv_balance);
	}

	@Override
	public void initParams() {
		super.initParams();
		// initData();
		mAdapter = new BalanceInquireAdapter(this, mBeans);
		listView.setAdapter(mAdapter);
		listView.setEmptyView(findViewById(R.id.emptyView));
	}

	@Override
	public void initListeners() {
		super.initListeners();
		btn_cashing.setOnClickListener(this);
		listView.setOnRefreshListener(this);
		tv_right_title.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// if (getIntent().getBooleanExtra("isCashingSucc", false)) {//如果提现成功，重新加载数据
		// initData();
		// }

		loadData(false);

	}

	/**
	 * 初始化界面数据
	 */
	public void initData() {
		// try {
		// StringBuilder sb = new StringBuilder();
		// sb.append("￥ ");
		// if (SessionContext.isLogin() && SessionContext.mUser.USERBASIC != null) {
		// DecimalFormat decimalFormat = new DecimalFormat("0.00");// 构造方法的字符格式这里如果小数不足2位,会以0补足.
		// String price = decimalFormat.format(SessionContext.mUser.USERBASIC.amount);// format 返回的是字符串
		// sb.append(price);
		// }
		// tv_balance.setText(sb.toString());
		//
		// if (mAdapter == null) {
		// mAdapter = new BalanceInquireAdapter(this, mBeans);
		// listView.setAdapter(mAdapter);
		// listView.setEmptyView(findViewById(R.id.emptyView));
		// }
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	/**
	 * 加载数据
	 * 
	 * @param isMore
	 */
	public void loadData(boolean isMore) {
		RequestBeanBuilder builder = RequestBeanBuilder.create(true);
		if (!isMore) {
			page_index = 0;
		}
		builder.addBody("pageIndex", String.valueOf(page_index));
		builder.addBody("pageSize", AppConst.COUNT);
		builder.addBody("type", "");
		builder.addBody("starttime", "");
		builder.addBody("endtime", "");
		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.TRANSACTION_LIST;
		if (isMore) {
			data.flag = 2;
		} else {
			data.flag = 1;
		}
		if (!isProgressShowing())
			showProgressDialog(getString(R.string.loading), false);
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.btn_cashing :
				dealCashing();
				break;
			case R.id.tv_right_title :// 查看银行卡
				Intent intent = new Intent(BalanceInquireActivity.this, SelectBankCardActivity.class);
				intent.putExtra("bannedClick", true);// 查看银行卡，禁止点击
				startActivity(intent);
				break;

			default :
				break;
		}
	}

	/**
	 * 处理提现
	 */
	public void dealCashing() {
		try {
			if (!SessionContext.isLogin()) {
				sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
				return;
			}

			if (mAmount <= 0) {
				CustomToast.show("当前可提现金额为0", 0);
				return;
			}

			if (!"03".equals(SessionContext.mUser.USERBASIC.levelstatus)) {// 如果没有实名认证
				CustomDialogUtil dialog = new CustomDialogUtil(this);
				dialog.setBtnText("去认证", "取消");
				dialog.show("为了您的帐号安全，提现操作需要进行实名认证");
				dialog.setListeners(new onCallBackListener() {

					@Override
					public void rightBtn(CustomDialogUtil dialog) {
						dialog.dismiss();
					}

					@Override
					public void leftBtn(CustomDialogUtil dialog) {
//						Intent intent = new Intent(BalanceInquireActivity.this, IdentityVerificationActivity.class);
						Intent mIntent = new Intent(BalanceInquireActivity.this, HtmlActivity.class);
						mIntent.putExtra("title", "实名认证");
						mIntent.putExtra("path", NetURL.IDENTITY_H5);
						startActivity(mIntent);
						dialog.dismiss();
					}
				});
			} else if (false) {// 如果没有绑定银行卡

			} else {
				Intent intent = new Intent(BalanceInquireActivity.this, SelectBankCardActivity.class);
				intent.putExtra("amount", mAmount);
				startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
		removeProgressDialog();
		listView.onRefreshComplete();
		JSONObject mJson = JSON.parseObject(response.body.toString());
		String json = mJson.getString("page");// 分页列表
		BalanceInquireBean temp = JSON.parseObject(json, BalanceInquireBean.class);
		page_index = temp.pageNo + 1;
		if (request.flag == 1) {
			mBeans.clear();
			mBeans.addAll(temp.result);
			mAdapter.notifyDataSetChanged();

			// 余额设置
			mAmount = mJson.getDouble("amount");
			StringBuilder sb = new StringBuilder();
			sb.append("￥ ");
			DecimalFormat decimalFormat = new DecimalFormat("0.00");// 构造方法的字符格式这里如果小数不足2位,会以0补足.
			String price = decimalFormat.format(mAmount);// format 返回的是字符串
			sb.append(price);
			tv_balance.setText(sb.toString());
		} else {
			mBeans.addAll(temp.result);
			mAdapter.notifyDataSetChanged();
		}

		if (mBeans.size() >= temp.totalCount) {
			// no more
			listView.setMode(Mode.PULL_FROM_START);
		} else {
			listView.setMode(Mode.BOTH);
		}
	}

	@Override
	public void notifyError(ResponseData request, ResponseData response, Exception e) {
		removeProgressDialog();
		listView.onRefreshComplete();
		String message;
		if (e != null && e instanceof ConnectException) {
			message = getString(R.string.dialog_tip_net_error);
		} else {
			message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
		}
		CustomToast.show(message, Toast.LENGTH_LONG);
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		loadData(false);
	}
	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		loadData(true);
	}
}

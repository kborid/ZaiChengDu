package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.UserAddrs;
import com.z012.chengdu.sc.ui.adapter.AddressManageAdapter;
import com.z012.chengdu.sc.ui.base.BaseActivity;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

/**
 * 地址管理
 * 
 * @author LiaoBo
 */
public class AddressManageActivity extends BaseActivity implements DataCallback {
	private ListView				listView;
	private AddressManageAdapter	mAdapter;
	private LinearLayout			layoutEmptyView, btn_add;
	private List<UserAddrs>			mUserAddrs;

	@Override
	protected int getLayoutResId() {
		return R.layout.ui_addressmanage_act;
	}

	@Override
	public void initParams() {
		super.initParams();
		tv_center_title.setText("地址管理");
		tv_right_title.setText("新增");
		tv_right_title.setVisibility(View.GONE);
		listView = (ListView) findViewById(R.id.listView);
		layoutEmptyView = (LinearLayout) findViewById(R.id.layoutEmptyView);
		btn_add = (LinearLayout) findViewById(R.id.btn_add);

		mUserAddrs = new ArrayList<UserAddrs>();
		mAdapter = new AddressManageAdapter(this, mUserAddrs);
		listView.setAdapter(mAdapter);
	}

	@Override
	public void initListeners() {
		super.initListeners();
		btn_add.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (SessionContext.isLogin()) {
			loadData();
		} else {
			this.finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 加载地址数组
	 */
	private void loadData() {
		mUserAddrs.clear();
		RequestBeanBuilder builder = RequestBeanBuilder.create(true);
		// builder.addBody("getConfForMgr", "YES");

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.SELECT_ADDRESS;
		// data.path = "http://192.168.1.187:8080/cd_portal/service/UA0004";
		data.flag = 1;
		if (!isProgressShowing()) {
			showProgressDialog("正在加载，请稍候...", true);
		}
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		Intent mIntent = null;
		switch (v.getId()) {
			case R.id.btn_add :
				mIntent = new Intent(this, AddressEditActivity.class);
				startActivity(mIntent);
				break;
			case R.id.tv_right_title :
				if (StringUtil.empty(tv_right_title.getText())) {
					return;
				}
				mIntent = new Intent(this, AddressEditActivity.class);
				startActivity(mIntent);
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
		String json = mJson.getString("userAddrs");
		List<UserAddrs> temp = JSON.parseArray(json, UserAddrs.class);
		if (temp != null && !temp.isEmpty()) {
			mUserAddrs.addAll(temp);
			mAdapter.notifyDataSetChanged();
			tv_right_title.setVisibility(View.VISIBLE);
			temp = null;
		} else {
			listView.setEmptyView(layoutEmptyView);
		}

	}

	@Override
	public void notifyError(ResponseData request, ResponseData response, Exception e) {
		removeProgressDialog();
		String message;
		if (e != null && e instanceof ConnectException) {
			message = getString(R.string.dialog_tip_net_error);
		} else {
			// message = getString(R.string.dialog_tip_null_error);
			message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
		}
		CustomToast.show(message, Toast.LENGTH_LONG);
	}

}

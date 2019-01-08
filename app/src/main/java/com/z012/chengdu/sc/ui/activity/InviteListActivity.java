package com.z012.chengdu.sc.ui.activity;

import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.InviteListBean;
import com.z012.chengdu.sc.ui.adapter.InviteListAdapter;
import com.z012.chengdu.sc.ui.base.BaseActivity;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

/**
 * 邀请人员列表
 * 
 * @author LiaoBo
 */
public class InviteListActivity extends BaseActivity implements DataCallback {
	private ListView				listView;
	private List<InviteListBean>	mBena	= new ArrayList<InviteListBean>();
	private InviteListAdapter		mAdpter;

	@Override
	protected int getLayoutResId() {
		return R.layout.ui_share_list_act;
	}

	@Override
	public void initParams() {
		super.initParams();
        tv_center_title.setText("邀请人列表");
        listView = (ListView) findViewById(R.id.listView);

		mAdpter = new InviteListAdapter(this, mBena);
		listView.setAdapter(mAdpter);
		listView.setEmptyView(findViewById(R.id.emptyView));
		loadInviteList();
	}

	@Override
	public void initListeners() {
		super.initListeners();
	}

	/**
	 * 加载邀请人员列表
	 */
	public void loadInviteList() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(true);
		// builder.addBody("getConfForMgr", "YES");

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.INVITE_LIST;
		data.flag = 1;
		if (!isProgressShowing())
			showProgressDialog(getString(R.string.loading), false);
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
		removeProgressDialog();
		JSONObject json = JSON.parseObject(response.body.toString());
		String mJson = json.getString("recommendeduserlist");
		List<InviteListBean> temp = JSON.parseArray(mJson, InviteListBean.class);
		mBena.addAll(temp);
		mAdpter.notifyDataSetChanged();
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

package com.z012.chengdu.sc.ui.fragment;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.AllServiceColumnBean;
import com.z012.chengdu.sc.net.bean.AllServiceColumnBean.AppList;
import com.z012.chengdu.sc.ui.activity.SearchActivity;
import com.z012.chengdu.sc.ui.adapter.ServiceColumnAdapter;
import com.z012.chengdu.sc.ui.adapter.ServiceListAdapter;
import com.z012.chengdu.sc.ui.base.BaseFragment;

/**
 * TAB2服务
 * 
 * @author LiaoBo
 */
public class Tab2ServiceFragment extends BaseFragment implements DataCallback, OnItemClickListener {

	private TextView					tv_search;
	private ListView					listViewLeft, listViewRight;
	private ServiceColumnAdapter		mServiceColumnAdapter;
	private ServiceListAdapter			mServiceListAdapter;
	private List<AllServiceColumnBean>	mCatalogBean	= new ArrayList<AllServiceColumnBean>();
	private List<AppList>				mAppBean		= new ArrayList<AppList>();
	private boolean						isFail;													// 是否是加载失败

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_tab2_service, container, false);
		initViews(view);
		initParams();
		initListeners();
		return view;
	}

	protected void onInits() {
	}

	protected void onVisible() {
		super.onVisible();
		if (isFail) {// 如果加载失败，回到当前页就重新加载
			loadData();
		}
	}
	@Override
	protected void initViews(View view) {
		super.initViews(view);
		// showProgressDialog(getString(R.string.loading), false);
		tv_search = (TextView) view.findViewById(R.id.tv_search);
		listViewLeft = (ListView) view.findViewById(R.id.listViewLeft);
		listViewRight = (ListView) view.findViewById(R.id.listViewRight);
	}

	@Override
	protected void initParams() {
		super.initParams();
		mServiceColumnAdapter = new ServiceColumnAdapter(getActivity(), mCatalogBean);
		mServiceListAdapter = new ServiceListAdapter(getActivity(), mAppBean);
		listViewLeft.setAdapter(mServiceColumnAdapter);
		listViewRight.setAdapter(mServiceListAdapter);

		try {
			byte[] data = DataLoader.getInstance().getCacheData(NetURL.ALL_SERVICE_COLUMN);
			if (data != null) {
				String json = new String(data, "UTF-8");
				ResponseData response = JSON.parseObject(json, ResponseData.class);
				if(response != null && response.body != null)
				refreshData(response.body.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		loadData();
	}

	@Override
	public void initListeners() {
		super.initListeners();
		tv_search.setOnClickListener(this);
		listViewLeft.setOnItemClickListener(this);
		listViewRight.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
			case R.id.tv_search :
				intent = new Intent(getActivity(), SearchActivity.class);
				startActivity(intent);
				break;

			default :
				break;
		}
	}
	/**
	 * 加载所有栏目和服务
	 */
	public void loadData() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		// builder.addBody("getConfForMgr", "YES");
		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.ALL_SERVICE_COLUMN;
		data.flag = 1;

		if (!isProgressShowing())
			showProgressDialog(getString(R.string.loading), true);
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	/**
	 * 刷新数据
	 */
	public void refreshData(String responseBody) {
		JSONObject mJson = JSON.parseObject(responseBody);
		String json = mJson.getString("list_catalog");
		List<AllServiceColumnBean> temp = JSON.parseArray(json, AllServiceColumnBean.class);
		if (temp != null) {
			mCatalogBean.clear();
			mCatalogBean.addAll(temp);
			mServiceColumnAdapter.notifyDataSetChanged();
			AllServiceColumnBean app = temp.get(0);
			mAppBean.clear();
			mAppBean.addAll(app.applist);
			mServiceListAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
		removeProgressDialog();
		if (request.flag == 1) {
			isFail = false;
			refreshData(response.body.toString());
		}

	}
	@Override
	public void notifyError(ResponseData request, ResponseData response, Exception e) {
		removeProgressDialog();
		String message;
		isFail = true;
		if (e != null && e instanceof ConnectException) {
			message = getString(R.string.dialog_tip_net_error);
			CustomToast.show(message, Toast.LENGTH_LONG);
		} else {
			message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
		}
		CustomToast.show(message, Toast.LENGTH_LONG);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		try {
			Object adapter = arg0.getAdapter();
			if (adapter instanceof ServiceColumnAdapter) {
				ServiceColumnAdapter data = (ServiceColumnAdapter) adapter;
				data.recordDefCheckedItem(arg2);
				mServiceColumnAdapter.notifyDataSetChanged();
				AllServiceColumnBean temp = (AllServiceColumnBean) data.getItem(arg2);
				mAppBean.clear();
				mAppBean.addAll(temp.applist);
				mServiceListAdapter.notifyDataSetChanged();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
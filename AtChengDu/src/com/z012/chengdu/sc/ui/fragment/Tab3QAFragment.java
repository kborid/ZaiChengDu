package com.z012.chengdu.sc.ui.fragment;

import java.net.ConnectException;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.widget.custom.BadgeView;
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
import com.z012.chengdu.sc.net.bean.QAListBean;
import com.z012.chengdu.sc.ui.activity.MyQAActivity;
import com.z012.chengdu.sc.ui.activity.QAISayActivity;
import com.z012.chengdu.sc.ui.adapter.QAListAdapter;
import com.z012.chengdu.sc.ui.base.BaseFragment;

/**
 * TAB3 问答
 * 
 * @author LiaoBo
 */
public class Tab3QAFragment extends BaseFragment implements DataCallback, OnRefreshListener2<ListView> {
	private PullToRefreshListView			listView;
	private QAListAdapter						mAdapter;
	private ArrayList<QAListBean.Result>	mBean	= new ArrayList<QAListBean.Result>();
	private int								page_index;
	private ImageView						iv_ask_question;
	private TextView						tv_right_title;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_tab_question, container, false);
	}

	protected void onInits() {
		initViews(getView());
		initParams();
		initListeners();
	}

	public void onVisible() {
		super.onVisible();
	}

	@Override
	protected void initViews(View view) {
		super.initViews(view);
		showProgressDialog(getString(R.string.loading), false);
		listView = (PullToRefreshListView) view.findViewById(R.id.listView);
		iv_ask_question = (ImageView) view.findViewById(R.id.iv_ask_question);
		tv_right_title = (TextView) view.findViewById(R.id.tv_right_title);
	}

	@Override
	protected void initParams() {
		super.initParams();
		mAdapter = new QAListAdapter(getActivity(), mBean);
		listView.setAdapter(mAdapter);
		loadData(false);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void initListeners() {
		super.initListeners();
		listView.setOnRefreshListener(this);
		iv_ask_question.setOnClickListener(this);
		tv_right_title.setOnClickListener(this);
	}

	/**
	 * 设置tag
	 */
	public void setTag() {
		BadgeView mMsgBadge = new BadgeView(getActivity());
		mMsgBadge.setBadgeGravity(Gravity.TOP | Gravity.RIGHT);
		mMsgBadge.setBadgeMargin(0, 10, 10, 0);
		mMsgBadge.setTargetView(tv_right_title);
		mMsgBadge.setText("");
		mMsgBadge.setBackgroundResource(R.drawable.ic_message_alert);
		mMsgBadge.setVisibility(View.VISIBLE);
	}

	/**
	 * 加载问答数据
	 * 
	 * @return
	 */
	private void loadData(boolean isMore) {
		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		if (!isMore) {
			page_index = 1;
		}
		builder.addBody("PAGE_INDEX", String.valueOf(page_index)).addBody("PAGE_COUNT", AppConst.COUNT);

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.WG_ALL;
		if (isMore) {
			data.flag = 2;
		} else {
			data.flag = 1;
		}
		requestID = DataLoader.getInstance().loadData(this, data);

	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
			case R.id.iv_ask_question :
				if (!SessionContext.isLogin()) {
					getActivity().sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
					return;
				}
				intent = new Intent(getActivity(), QAISayActivity.class);
				startActivity(intent);
				break;
			case R.id.tv_right_title :
				if (SessionContext.isLogin()) {
					intent = new Intent(getActivity(), MyQAActivity.class);
					startActivity(intent);
				} else {
					getActivity().sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
				}
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
		listView.onRefreshComplete();
		JSONObject mJson = JSON.parseObject(response.body.toString());
		String json = mJson.getString("page");
		QAListBean temp = JSON.parseObject(json, QAListBean.class);
		page_index = temp.pageNo + 1;
		if (request.flag == 1) {
			mBean.clear();
			mBean.addAll(temp.result);
			mAdapter.notifyDataSetChanged();
		} else {
			mBean.addAll(temp.result);
			mAdapter.notifyDataSetChanged();
		}

		if (mBean.size() >= temp.totalCount) {
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
			CustomToast.show(message, Toast.LENGTH_LONG);
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
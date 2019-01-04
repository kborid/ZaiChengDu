package com.z012.chengdu.sc.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.prj.sdk.constants.InfoType;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.QAListBean;
import com.z012.chengdu.sc.ui.adapter.QAListAdapter;
import com.z012.chengdu.sc.ui.adapter.ViewPagerAdapter;
import com.z012.chengdu.sc.ui.base.BaseActivity;

import java.net.ConnectException;
import java.util.ArrayList;

/**
 * 我的问答
 * 
 * @author LiaoBo
 */
public class MyQAActivity extends BaseActivity implements DataCallback, DialogInterface.OnCancelListener, OnScrollListener, OnRefreshListener2<ListView>, OnPageChangeListener {

	private PullToRefreshListView			mReleListView, mAtteListView;
	private QAListAdapter					mReleasedAdapter, mAttentionAdapter;
	private ArrayList<QAListBean.Result>	mFBBean	= new ArrayList<QAListBean.Result>();
	private ArrayList<QAListBean.Result>	mGZBean	= new ArrayList<QAListBean.Result>();
	private int								page_index;
	private TextView						tv_btn_left, tv_btn_right;
	private ViewPager						mViewPager;
	private ViewPagerAdapter				mVPAdapter;
	private boolean							isFirstLoadRele	= true, isFirstLoadAtte = true;
	private String							mURL;
	private View							mReleaseView, mAttentionView;
	private ArrayList<View>					mView			= new ArrayList<View>();		// viewpager视图集合
	private LinearLayout					layout_type;

	@Override
	protected int getLayoutResId() {
		return R.layout.ui_my_qa_act;
	}

	@Override
	public void initParams() {
		super.initParams();
		tv_center_title.setText("有问必答");
		mReleaseView = getLayoutInflater().inflate(R.layout.view_my_release, null);
		mAttentionView = getLayoutInflater().inflate(R.layout.view_my_attention, null);
		mReleListView = (PullToRefreshListView) mReleaseView.findViewById(R.id.listView);
		mAtteListView = (PullToRefreshListView) mAttentionView.findViewById(R.id.listView);
		tv_btn_left = (TextView) findViewById(R.id.tv_btn_left);
		tv_btn_right = (TextView) findViewById(R.id.tv_btn_right);
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		layout_type = (LinearLayout) findViewById(R.id.layout_type);

		mURL = NetURL.WG_MY_RELEASED;

		loadData(true);

		mReleasedAdapter = new QAListAdapter(this, mFBBean);
		mReleListView.setAdapter(mReleasedAdapter);

		mAttentionAdapter = new QAListAdapter(this, mGZBean);
		mAtteListView.setAdapter(mAttentionAdapter);
		mView.clear();
		mView.add(mReleaseView);
		mView.add(mAttentionView);
		mVPAdapter = new ViewPagerAdapter(this, mView);
		mViewPager.setAdapter(mVPAdapter);
		mViewPager.setCurrentItem(0);
		mVPAdapter.notifyDataSetChanged();
	}
	@Override
	protected void onResume() {
		super.onResume();
		// if (!SessionContext.isLogin()) {
		// mView.remove(mReleaseView);
		// mView.remove(mAttentionView);
		// layout_type.setVisibility(View.GONE);
		// iv_view_line.setVisibility(View.GONE);
		// mVPAdapter.notifyDataSetChanged();
		// mFBBean.clear();
		// mGZBean.clear();
		// mReleasedAdapter.notifyDataSetChanged();
		// mAttentionAdapter.notifyDataSetChanged();
		// } else {
		// if (mView.size() < 2) {
		//
		// mViewPager.setCurrentItem(0);
		// layout_type.setVisibility(View.VISIBLE);
		// iv_view_line.setVisibility(View.VISIBLE);
		// mVPAdapter.notifyDataSetChanged();
		// isFirstLoadRele = true;
		// isFirstLoadAtte = true;
		// }
		// }
	}

	@Override
	public void initListeners() {
		super.initListeners();
		mReleListView.setOnRefreshListener(this);
		mAtteListView.setOnRefreshListener(this);

		mReleListView.setOnScrollListener(this);
		mAtteListView.setOnScrollListener(this);

		tv_btn_left.setOnClickListener(this);
		tv_btn_right.setOnClickListener(this);
		mViewPager.setOnPageChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		Intent mIntent = new Intent();
		switch (v.getId()) {
			case R.id.tv_right_title :
//				mIntent.setClass(this, QADetailsActivity.class);
//				startActivity(mIntent);
//				finish();
				break;
			case R.id.tv_btn_left :
				if (!SessionContext.isLogin()) {
					sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
					return;
				}
				mViewPager.setCurrentItem(0);
				setTextColor(tv_btn_left);
				break;
			case R.id.tv_btn_right :
				if (!SessionContext.isLogin()) {
					sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
					return;
				}
				mViewPager.setCurrentItem(1);
				setTextColor(tv_btn_right);
				break;
			default :
				break;
		}
	}

	/**
	 * 设置文本背景颜色
	 * 
	 * @param tv
	 */
	public void setTextColor(TextView tv) {
		tv_btn_left.setTextColor(getResources().getColor(R.color.white));
		tv_btn_right.setTextColor(getResources().getColor(R.color.white));
		tv_btn_left.setBackgroundColor(0xff0000);
		tv_btn_right.setBackgroundColor(0xff0000);
		tv.setTextColor(getResources().getColor(R.color.home_tab_text_checked));
		tv.setBackgroundResource(R.drawable.index_search);
	}

	/**
	 * 加载数据
	 * 
	 * @return
	 */
	private void loadData(boolean showProgress) {
		RequestBeanBuilder builder = RequestBeanBuilder.create(SessionContext.isLogin());
		builder.addBody("PAGE_INDEX", "1").addBody("PAGE_COUNT", AppConst.COUNT);

		ResponseData data = builder.syncRequest(builder);
		data.path = mURL;
		data.flag = 1;
		String tip;
		if (showProgress) {
			tip = getString(R.string.loading);
		} else {
			tip = "";
		}
		if (!isProgressShowing())
			showProgressDialog(tip, true);
		requestID = DataLoader.getInstance().loadData(this, data);

	}

	/**
	 * 加载更多数据
	 * 
	 * @return
	 */
	private void loadMoreData() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(SessionContext.isLogin());
		builder.addBody("PAGE_INDEX", String.valueOf(page_index)).addBody("PAGE_COUNT", AppConst.COUNT);

		ResponseData data = builder.syncRequest(builder);
		data.path = mURL;
		data.type = InfoType.POST_REQUEST.toString();
		data.flag = 2;

		requestID = DataLoader.getInstance().loadData(this, data);

	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
		removeProgressDialog();
		JSONObject mJson = JSON.parseObject(response.body.toString());
		String json = mJson.getString("page");
		QAListBean temp = JSON.parseObject(json, QAListBean.class);
		if (temp == null || temp.result == null) {
			return;
		}
		page_index = temp.pageNo + 1;

		if (request.path.equals(NetURL.WG_MY_RELEASED)) {
			mReleListView.onRefreshComplete();
			if (request.flag == 1) {
				mFBBean.clear();
				mFBBean.addAll(temp.result);
				mReleasedAdapter.notifyDataSetChanged();
				if (mFBBean.size() >= temp.totalCount) {
					// no more
					mReleListView.setMode(Mode.PULL_FROM_START);
				} else {
					mReleListView.setMode(Mode.BOTH);
				}
			} else if (request.flag == 2) {
				mFBBean.addAll(temp.result);
				mReleasedAdapter.notifyDataSetChanged();
				if (mFBBean.size() >= temp.totalCount) {
					// no more
					mReleListView.setMode(Mode.PULL_FROM_START);
				} else {
					mReleListView.setMode(Mode.BOTH);
				}
			}
		} else if (request.path.equals(NetURL.WG_MY_ATTENTION)) {
			mAtteListView.onRefreshComplete();
			if (request.flag == 1) {
				mGZBean.clear();
				mGZBean.addAll(temp.result);
				mAttentionAdapter.notifyDataSetChanged();
				if (mGZBean.size() >= temp.totalCount) {
					// no more
					mAtteListView.setMode(Mode.PULL_FROM_START);
				} else {
					mAtteListView.setMode(Mode.BOTH);
				}
			} else if (request.flag == 2) {
				mGZBean.addAll(temp.result);
				mAttentionAdapter.notifyDataSetChanged();
				if (mGZBean.size() >= temp.totalCount) {
					// no more
					mAtteListView.setMode(Mode.PULL_FROM_START);
				} else {
					mAtteListView.setMode(Mode.BOTH);
				}
			}
		}
	}
	@Override
	public void notifyError(ResponseData request, ResponseData response, Exception e) {
		removeProgressDialog();
		if (request.path.equals(NetURL.WG_MY_RELEASED)) {
			mReleListView.onRefreshComplete();
		} else if (request.path.equals(NetURL.WG_MY_ATTENTION)) {
			mAtteListView.onRefreshComplete();
		}

		String message;
		if (e != null && e instanceof ConnectException) {
			message = getString(R.string.dialog_tip_net_error);
		} else if (response != null && "999999".equals(response.code) && SessionContext.isLogin()) {
			long lag = System.currentTimeMillis() - DateUtil.str2Date(SessionContext.mUser.USERBASIC.lastlogintime).getTime();
			if (lag / 1000 / 60 / 60 / 24 >= 1) {// 登录时间间隔1天，重新登录
				message = "登录超时，请重新登录";
				sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
			} else {
				message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
			}
		} else {
			message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
		}
		if (request.path.equals(mURL))
			CustomToast.show(message, Toast.LENGTH_LONG);

	}

	@Override
	public void onCancel(DialogInterface dialog) {
		DataLoader.getInstance().clear(requestID);
		removeProgressDialog();
		mReleListView.onRefreshComplete();
		mAtteListView.onRefreshComplete();
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		loadData(false);
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		loadMoreData();
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		switch (arg0) {
			case 0 :
				if (!SessionContext.isLogin()) {
					mViewPager.setCurrentItem(0);
					sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
					return;
				}
				mURL = NetURL.WG_MY_RELEASED;
				if (isFirstLoadRele) {
					isFirstLoadRele = false;
					loadData(true);
				}
				setTextColor(tv_btn_left);
				break;
			case 1 :
				if (!SessionContext.isLogin()) {
					mViewPager.setCurrentItem(0);
					sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
					return;
				}
				mURL = NetURL.WG_MY_ATTENTION;
				if (isFirstLoadAtte) {
					isFirstLoadAtte = false;
					loadData(true);
				}
				setTextColor(tv_btn_right);
				break;

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mViewPager.removeAllViews();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mURL.equals(NetURL.WG_MY_RELEASED)) {
			mReleasedAdapter.listenStateChange(view, scrollState);
		} else {
			mAttentionAdapter.listenStateChange(view, scrollState);
		}

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}
}

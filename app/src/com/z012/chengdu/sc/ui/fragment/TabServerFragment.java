package com.z012.chengdu.sc.ui.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.NetworkUtil;
import com.prj.sdk.util.UIHandler;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.AllServiceColumnBean;
import com.z012.chengdu.sc.ui.activity.SearchActivity;
import com.z012.chengdu.sc.ui.adapter.ServiceDetailAdapter;
import com.z012.chengdu.sc.ui.base.BaseFragment;
import com.z012.chengdu.sc.ui.widge.tablayout.TabLayout;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 服务
 * 
 * @author kborid
 */
public class TabServerFragment extends BaseFragment implements DataCallback {

	private TextView tv_search;
	private TabLayout tabs;
	private ScrollView mScrollView;
	private LinearLayout service_lay;
	private List<AllServiceColumnBean> mCatalogBean	= new ArrayList<AllServiceColumnBean>();
	private boolean isFail; // 是否是加载失败
    private int mCurrentPosition = 0;
    private boolean tabInterceptTouchEventTag = true;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_tab_service, container, false);
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
		tv_search = (TextView) view.findViewById(R.id.tv_search);
		tabs = (TabLayout) view.findViewById(R.id.tabs);
        mScrollView = (ScrollView) view.findViewById(R.id.scrollview);
        service_lay = (LinearLayout) view.findViewById(R.id.service_lay);
	}

	@Override
	protected void initParams() {
		super.initParams();
		if (NetworkUtil.isNetworkAvailable()) {
		    loadData();
        } else {
            try {
                byte[] data = DataLoader.getInstance().getCacheData(NetURL.ALL_SERVICE_COLUMN);
                if (data != null) {
                    String json = new String(data, "UTF-8");
                    ResponseData response = JSON.parseObject(json, ResponseData.class);
                    if (response != null && response.body != null)
                        refreshData(response.body.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

    @SuppressLint("ClickableViewAccessibility")
    @TargetApi(Build.VERSION_CODES.M)
    @Override
	public void initListeners() {
		super.initListeners();
		tv_search.setOnClickListener(this);

		tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                tabInterceptTouchEventTag = true;
                LogUtil.i("dw", "flag = " + tabInterceptTouchEventTag);
                if (isVisible()) {
                    mCurrentPosition = tab.getPosition();
                    UIHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            View view = service_lay.getChildAt(mCurrentPosition);
                            int[] loc = new int[2];
                            view.getLocationOnScreen(loc);
                            int[] tabLoc = new int[2];
                            tabs.getLocationOnScreen(tabLoc);
                            LogUtil.i("dw", "item = " + Arrays.toString(loc));
                            LogUtil.i("dw", "tabLoc = " + Arrays.toString(tabLoc));
                            mScrollView.smoothScrollTo(0, loc[1] - tabLoc[1]);
                        }
                    }, 200);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

		mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                tabInterceptTouchEventTag = false;
                return false;
            }
        });

		mScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                LogUtil.i("dw", "flag = " + tabInterceptTouchEventTag);
            }
        });
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
        mCatalogBean = JSON.parseArray(json, AllServiceColumnBean.class);
        refreshServiceLayout();
	}

	private void refreshServiceLayout() {
	    if (null == mCatalogBean) {
	        return;
        }
        tabs.removeAllTabs();
	    service_lay.removeAllViews();
        for (int i = 0; i < mCatalogBean.size(); i++) {
            tabs.addTab(tabs.newTab().setText(mCatalogBean.get(i).catalogname));
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.lv_service_item, null);
            service_lay.addView(view);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
            GridView gridview = (GridView) view.findViewById(R.id.gridview);
            tv_name.setText(mCatalogBean.get(i).catalogname);
            ServiceDetailAdapter adapter = new ServiceDetailAdapter(getActivity(), mCatalogBean.get(i).applist);
            gridview.setAdapter(adapter);
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
		if (e instanceof ConnectException) {
			message = getString(R.string.dialog_tip_net_error);
			CustomToast.show(message, Toast.LENGTH_LONG);
		} else {
			message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
		}
		CustomToast.show(message, Toast.LENGTH_LONG);
	}
}
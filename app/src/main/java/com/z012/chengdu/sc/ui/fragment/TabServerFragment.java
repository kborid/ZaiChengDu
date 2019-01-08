package com.z012.chengdu.sc.ui.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import com.prj.sdk.util.NetworkUtil;
import com.prj.sdk.util.UIHandler;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.net.RequestBeanBuilder;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.AllServiceColumnBean;
import com.z012.chengdu.sc.ui.activity.SearchActivity;
import com.z012.chengdu.sc.ui.adapter.ServiceDetailAdapter;
import com.z012.chengdu.sc.ui.base.BaseFragment;
import com.z012.chengdu.sc.ui.widge.tablayout.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTouch;

/**
 * 服务
 * 
 * @author kborid
 */
public class TabServerFragment extends BaseFragment implements DataCallback {

	@BindView(R.id.tabs) TabLayout tabs;
	@BindView(R.id.scrollview) ScrollView mScrollView;
	@BindView(R.id.service_lay) LinearLayout service_lay;

    private List<AllServiceColumnBean> mCatalogBean	= new ArrayList<>();
    private boolean isFail; // 是否是加载失败
    private int mCurrentPosition = 0;
    private boolean tabInterceptTouchEventTag = true;
    private int[] gridViewLoc;
    private int[] itemViewLoc, itemViewHeight;
    private int mScrollViewY;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_tab_service;
    }

    @Override
    protected void onInit() {
        EventBus.getDefault().register(this);
	}

	protected void onVisible() {
		super.onVisible();
		if (isFail) {// 如果加载失败，回到当前页就重新加载
			loadData();
		}
	}

	private View getPlaceHolderView() {
	    View placeHolderView = new View(getActivity());
	    placeHolderView.setBackgroundResource(R.color.white);
	    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    llp.height = Utils.mScreenHeight - mScrollViewY - Utils.dip2px(100);
	    llp.bottomMargin = Utils.dip2px(5);
	    placeHolderView.setLayoutParams(llp);
	    return placeHolderView;
    }

	@TargetApi(Build.VERSION_CODES.M)
    @Override
	protected void initParams() {
		super.initParams();

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

		if (NetworkUtil.isNetworkAvailable()) {
		    loadData();
        }

        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int[] loc = new int[2];
                mScrollView.getLocationOnScreen(loc);
                mScrollViewY = loc[1];
            }
        }, 100);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                if(!tabInterceptTouchEventTag){
                    return;
                }

                if (isVisible()) {
                    mCurrentPosition = tab.getPosition();
                    UIHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mScrollView.smoothScrollTo(0, gridViewLoc[mCurrentPosition] - mScrollViewY);
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

        mScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (tabInterceptTouchEventTag) {
                    return;
                }

                int size = itemViewLoc.length;
                for (int i = 0; i < size; i++) {
                    if (scrollY < itemViewLoc[i] + itemViewHeight[i] - mScrollViewY) {
                        tabs.setScrollPosition(i, 0, true);
//                        tabs.getTabAt(i).select();
                        break;
                    }
                }
            }
        });
	}

	@OnClick(R.id.tv_search) void search() {
        startActivity(new Intent(getActivity(), SearchActivity.class));
    }

    @OnTouch(R.id.touchFrame) boolean touchFrame() {
        if (!tabInterceptTouchEventTag) {
            tabInterceptTouchEventTag = true;
        }
        return false;
    }

    @OnTouch(R.id.scrollview) boolean scrollViewTouch() {
        if (tabInterceptTouchEventTag) {
            tabInterceptTouchEventTag = false;
        }
        return false;
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

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void change(String name) {
	    if (!TextUtils.isEmpty(name)) {
            for (int i = 0; i < tabs.getTabCount(); i++) {
                TabLayout.Tab tab = tabs.getTabAt(i);
                if (null != tab && !TextUtils.isEmpty(tab.getText())) {
                    if (name.equals(tab.getText().toString())) {
                        jumpTabIndex(tab.getPosition());
                        break;
                    }
                }
            }
        }
    }

	private void jumpTabIndex(final int position) {
        tabs.setScrollPosition(position, 0, true);
        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScrollView.smoothScrollTo(0, gridViewLoc[position] - mScrollViewY);
            }
        }, 100);
    }

	private void refreshServiceLayout() {
	    if (null == mCatalogBean) {
	        return;
        }

        itemViewLoc = new int[mCatalogBean.size()];
        gridViewLoc = new int[mCatalogBean.size()];
        itemViewHeight = new int[mCatalogBean.size()];

        tabs.removeAllTabs();
	    service_lay.removeAllViews();
        for (int i = 0; i < mCatalogBean.size(); i++) {
            tabs.addTab(tabs.newTab().setText(mCatalogBean.get(i).catalogname));
            final View view = LayoutInflater.from(getActivity()).inflate(R.layout.lv_service_item, null);
            if (i > 0) {
                view.setPadding(0, Utils.dip2px(24), 0, 0);
            } else {
                view.setPadding(0, 0, 0, 0);
            }
            service_lay.addView(view);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
            final GridView gridview = (GridView) view.findViewById(R.id.gridview);
            tv_name.setText(mCatalogBean.get(i).catalogname);
            ServiceDetailAdapter adapter = new ServiceDetailAdapter(getActivity(), mCatalogBean.get(i).applist);
            gridview.setAdapter(adapter);

            final int finalI = i;
            UIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    int[] loc = new int[2];
                    gridview.getLocationOnScreen(loc);
                    gridViewLoc[finalI] = loc[1];
                    int[] loc1 = new int[2];
                    view.getLocationOnScreen(loc1);
                    itemViewLoc[finalI] = loc1[1];
                    itemViewHeight[finalI] = view.getMeasuredHeight();
                }
            }, 100);
        }

        service_lay.addView(getPlaceHolderView());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
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
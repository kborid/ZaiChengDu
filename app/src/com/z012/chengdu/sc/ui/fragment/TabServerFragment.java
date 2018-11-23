package com.z012.chengdu.sc.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import com.prj.sdk.util.Utils;
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
	private List<AllServiceColumnBean> mCatalogBean	= new ArrayList<>();
	private boolean isFail; // 是否是加载失败
    private int mCurrentPosition = 0;
    private FrameLayout touchFrame;
    private boolean tabInterceptTouchEventTag = true;
    private int[] gridViewLoc;
    private int[] itemViewLoc, itemViewHeight;
    private int mScrollViewY;

	@RequiresApi(api = Build.VERSION_CODES.M)
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
        touchFrame = (FrameLayout) view.findViewById(R.id.touchFrame);
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

        UIHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int[] loc = new int[2];
                mScrollView.getLocationOnScreen(loc);
                mScrollViewY = loc[1];
            }
        }, 100);
	}

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    @Override
	public void initListeners() {
		super.initListeners();
		tv_search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), SearchActivity.class);
				startActivity(intent);
			}
		});

        touchFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!tabInterceptTouchEventTag) {
                    tabInterceptTouchEventTag = true;
                }
                return false;
            }
        });

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

		mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (tabInterceptTouchEventTag) {
                    tabInterceptTouchEventTag = false;
                }
                return false;
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
                        break;
                    }
                }
            }
        });
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

        itemViewLoc = new int[mCatalogBean.size()];
        gridViewLoc = new int[mCatalogBean.size()];
        itemViewHeight = new int[mCatalogBean.size()];

        tabs.removeAllTabs();
	    service_lay.removeAllViews();
        for (int i = 0; i < mCatalogBean.size(); i++) {
            tabs.addTab(tabs.newTab().setText(mCatalogBean.get(i).catalogname));
            final View view = LayoutInflater.from(getActivity()).inflate(R.layout.lv_service_item, null);
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
                    LogUtil.i("dw", "loc = " + loc[1]);
                    LogUtil.i("dw", "height = " + view.getMeasuredHeight());
                }
            }, 100);
        }

        service_lay.addView(getPlaceHolderView());
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
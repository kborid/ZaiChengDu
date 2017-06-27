package com.z012.chengdu.sc.ui.activity;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.widget.custom.ColumnTitleTextView;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.AppListBean;
import com.z012.chengdu.sc.ui.adapter.ColumnAdapter;
import com.z012.chengdu.sc.ui.adapter.ViewPagerAdapter;
import com.z012.chengdu.sc.ui.base.BaseActivity;

/**
 * 服务超市
 * 
 * @author LiaoBo
 * 
 */
public class SupermarketActivity extends BaseActivity implements DataCallback, OnPageChangeListener {

	private ViewPager				mViewPager;
	private ViewGroup				mViewGroup;
	private ViewPagerAdapter		mAdapter;
	private int						mPreSelectItem;
	private List<String>			mTabTitle	= new ArrayList<String>();
	private List<Integer>			mTabId		= new ArrayList<Integer>();
	private List<AppListBean>	mColumnData	= new ArrayList<AppListBean>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_service_supermarke);
		initViews();
		initParams();
		initListeners();
	}

	public void initViews() {
		super.initViews();
		tv_center_title.setText("服务超市");
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mViewGroup = (ViewGroup) findViewById(R.id.viewgroup);
	}

	@Override
	public void initParams() {
		super.initParams();
		loadData(true);
	}

	public void loadData(boolean isShowProgress) {
		RequestBeanBuilder builder = RequestBeanBuilder.create(false);
		builder.addBody("getConfForMgr", "YES");

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.MORE_COLUMN;
		data.flag = 1;

		if (isShowProgress && !isProgressShowing())
			showProgressDialog(getString(R.string.loading), false);
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	/**
	 * 获取我的服务下的栏目id以及栏目名称：（倒数第2个元素是我的服务）
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void getColumnId(List<AppListBean> columnData) {
		try {
			ArrayList<String> mColumnId = new ArrayList<String>();// 栏目id
			if (columnData.size() < 1) {
				CustomToast.show("暂无数据", 0);
				return;
			}

			for (int j = 0; j < columnData.size(); j++) {
				AppListBean temp = columnData.get(j);
				if (temp.menutype == 1) {
					mTabTitle.add(temp.name);
					mTabId.add(Integer.getInteger(temp.id));
				}
			}
			
			addViewPagerView();
			// int size;
			// for (size = 0; size < SessionContext.getMenuList().size(); size++) {// 我的服务
			// if ("我的服务".equals(SessionContext.getMenuList().get(size).name)) {
			// break;
			// }
			//
			// }
			//
			// if (SessionContext.getMenuList().get(size).childrenid instanceof List<?>) {
			// mColumnId.addAll((List<String>) SessionContext.getMenuList().get(size).childrenid);
			// for (int i = 0; i < SessionContext.getMenuList().size(); i++) {
			// for (String id : mColumnId) {
			// if (id.equals(SessionContext.getMenuList().get(i).id)) {// 根据order排序
			// mTabItems.put(SessionContext.getMenuList().get(i).order, SessionContext.getMenuList().get(i).name);
			// }
			// }
			// }
			// mColumnId.clear();
			// mColumnId = null;
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取指定栏目下的应用
	 * 
	 * @param i
	 */
	private ArrayList<AppListBean> getColumnApp(int pid) {
		ArrayList<AppListBean> mBean = new ArrayList<AppListBean>();
		for (int i = 0; i < mColumnData.size(); i++) {
			if (pid == mColumnData.get(i).pid) {
				mBean.add(mColumnData.get(i));
			}

		}

		// int i;
		// for (i = 0; i < mColumnData.size(); i++) {
		// if (name.equals(SessionContext.getMenuList().get(i).name)) {
		// break;
		// }
		// }
		// 查询栏目下对应的应用
		// if (SessionContext.getMenuList().get(i).childrenid instanceof List<?>) {
		// @SuppressWarnings("unchecked")
		// List<String> s = (List<String>) SessionContext.getMenuList().get(i).childrenid;
		// for (AppListBean appItem : SessionContext.getAllAppList()) {
		// for (String string : s) {
		// if (appItem.id.equals(string)) {
		// mBean.add(appItem);
		// }
		// }
		// }
		//
		// }
		// Comparator<AppListBean> comparator = new Comparator<AppListBean>() {
		// public int compare(AppListBean s1, AppListBean s2) {
		// return s1.order - s2.order;
		// }
		// };
		// 这里就会自动根据规则进行排序
		// Collections.sort(mBean, comparator);
		return mBean;
	}

	@Override
	public void initListeners() {
		super.initListeners();
		mViewPager.setOnPageChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.tv_right_title :
				Intent intent = new Intent(this, SearchActivity.class);
				startActivity(intent);
				break;
			default :
				Object o = v.getTag();
				if (o != null && o instanceof Integer) {
					mViewPager.setCurrentItem((Integer) o);
				}
				break;
		}
	}

	/**
	 * 添加视图
	 * 
	 * @param size
	 *            视图数量
	 */
	private void addViewPagerView() {

		LayoutInflater inflater = this.getLayoutInflater();
		ArrayList<View> mView = new ArrayList<View>();// viewpager视图集合

		for (int i = 0; i < mTabTitle.size(); i++) {

			String label = mTabTitle.get(i);

			View v = inflater.inflate(R.layout.ui_column_view_pager_item, null);
			ListView mListView = ((ListView) v.findViewById(R.id.listView));

			ColumnAdapter mAdapter = new ColumnAdapter(this, getColumnApp(mTabId.get(i)));
			mListView.setAdapter(mAdapter);
			mView.add(v);

			ColumnTitleTextView tv = new ColumnTitleTextView(this);
			int itemWidth = (int) tv.getPaint().measureText(label);
			tv.setLayoutParams(new LinearLayout.LayoutParams((itemWidth * 2), -1));
			tv.setText(label);
			tv.setHorizontalineColor(getResources().getColor(R.color.text_color_on));
			tv.setGravity(Gravity.CENTER);
			if (i == 0) {
				tv.setTextColor(getResources().getColor(R.color.text_color_on));
				tv.setIsHorizontaline(true);
			} else {
				tv.setTextColor(getResources().getColor(R.color.text_color_def));
				tv.setIsHorizontaline(false);
			}
			tv.setTag(i);
			tv.setOnClickListener(this);
			mViewGroup.addView(tv);
			// 如果实际宽度小于或等于屏幕宽,则等比划分分显示
			if (mViewGroup.getMeasuredWidth() <= Utils.mScreenWidth) {
				int wdith = Utils.mScreenWidth / mViewGroup.getChildCount();
				for (int p = 0; p < mViewGroup.getChildCount(); p++) {
					ColumnTitleTextView itemView = (ColumnTitleTextView) mViewGroup.getChildAt(p);
					itemView.setLayoutParams(new LinearLayout.LayoutParams(wdith, -1));
				}
			}
		}

		mAdapter = new ViewPagerAdapter(this, mView);
		mViewPager.setAdapter(mAdapter);

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int arg0) {
		moveTitleLabel(arg0);

	}

	/*
	 * 点击栏目分类的tabbar，使点击的bar居中显示到屏幕中间
	 */
	@SuppressLint("NewApi")
	private void moveTitleLabel(int position) {

		// 点击当前按钮所有左边按钮的总宽度
		int visiableWidth = 0;
		// HorizontalScrollView的宽度
		int scrollViewWidth = 0;

		mViewGroup.measure(mViewGroup.getMeasuredWidth(), -1);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mViewGroup.getMeasuredWidth(), -1);
		params.gravity = Gravity.CENTER_VERTICAL;
		mViewGroup.setLayoutParams(params);
		for (int i = 0; i < mViewGroup.getChildCount(); i++) {
			ColumnTitleTextView itemView = (ColumnTitleTextView) mViewGroup.getChildAt(i);
			int width = itemView.getMeasuredWidth();
			if (i < position) {
				visiableWidth += width;
			}
			scrollViewWidth += width;

			if (i == mViewGroup.getChildCount()) {
				break;
			}
			if (position != i) {
				itemView.setTextColor(getResources().getColor(R.color.text_color_def));
				itemView.setIsHorizontaline(false);
			} else {
				itemView.setTextColor(getResources().getColor(R.color.text_color_on));
				itemView.setIsHorizontaline(true);
			}
		}
		// 当前点击按钮的宽度
		int titleWidth = mViewGroup.getChildAt(position).getMeasuredWidth();
		int nextTitleWidth = 0;
		if (position > 0) {
			// 当前点击按钮相邻右边按钮的宽度
			nextTitleWidth = position == mViewGroup.getChildCount() - 1 ? 0 : mViewGroup.getChildAt(position - 1).getMeasuredWidth();
		}
		int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		final int move = visiableWidth - (screenWidth - titleWidth) / 2;
		if (mPreSelectItem < position) {// 向屏幕右边移动
			if ((visiableWidth + titleWidth + nextTitleWidth) >= (screenWidth / 2)) {
				// new Handler().post(new Runnable() {
				//
				// @Override
				// public void run() {
				// ((HorizontalScrollView) mViewGroup.getParent()).setScrollX(move);
				((HorizontalScrollView) mViewGroup.getParent()).scrollTo(move, 0);
				// }
				// });

			}
		} else {// 向屏幕左边移动
			if ((scrollViewWidth - visiableWidth) >= (screenWidth / 2)) {
				// ((HorizontalScrollView) mViewGroup.getParent()).setScrollX(move);
				((HorizontalScrollView) mViewGroup.getParent()).scrollTo(move, 0);
			}
		}
		mPreSelectItem = position;
	}

	@Override
	public void preExecute(ResponseData request) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
		removeProgressDialog();
		if (request.flag == 1) {
			JSONObject mJson = JSON.parseObject(response.body.toString());
			String json = mJson.getString("datalist");
			mColumnData = JSON.parseArray(json, AppListBean.class);
			getColumnId(mColumnData);
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

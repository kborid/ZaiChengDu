package com.z012.chengdu.sc.ui.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;

import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.net.image.ImageLoader.ImageCallback;
import com.prj.sdk.util.Utils;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.AppListBean;
import com.z012.chengdu.sc.ui.adapter.ColumnAdapter;
import com.z012.chengdu.sc.ui.base.BaseActivity;

/**
 * 栏目页面
 * 
 * @author LiaoBo
 * 
 */
public class ColumnActivity extends BaseActivity {
	private ImageView				mImgView;
	private int						mID;
	private ColumnAdapter			mAdapter;
	private ArrayList<AppListBean>	mBean	= new ArrayList<AppListBean>();
	private ListView				mListView;
	private View					headerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_column);

		initViews();
		dealIntent();
		initParams();
		initListeners();
	}

	public void initViews() {
		super.initViews();
		mListView = (ListView) findViewById(R.id.listView);

		headerView = getLayoutInflater().inflate(R.layout.view_column_headerview, null, false);
		mImgView = (ImageView) headerView.findViewById(R.id.im_headimage);
		LayoutParams lp = mImgView.getLayoutParams();
		lp.height = Utils.mScreenHeight * 1 / 4;
		mImgView.setLayoutParams(lp);

		mListView.addHeaderView(headerView, null, false);//添加headview并且不可选择
	}

	public void dealIntent() {
		super.dealIntent();
		if (getIntent().getExtras() != null) {
			if (getIntent().getExtras().getString("title") != null) {
				tv_center_title.setText(getIntent().getExtras().getString("title"));
			} else {
				tv_center_title.setText("");
			}
			mID = getIntent().getIntExtra("id", 0);// 点击了第几个栏目
			String url = NetURL.API_LINK + getIntent().getStringExtra("headUrl");
			setHeadImg(url);
		}
	}

	public void initParams() {
		super.initParams();

		try {
			for (int i = 0; i < SessionContext.getPushColumn().size(); i++) {
				AppListBean temp = SessionContext.getPushColumn().get(i);
				if (temp.pid == mID) {
					mBean.add(temp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// try {
		// // 查询栏目下对应的应用
		// if (SessionContext.getMenuSpecial().get(mFlag).childrenid instanceof List<?>) {
		// List<String> childrenid = (List<String>) SessionContext.getMenuSpecial().get(mFlag).childrenid;
		// for (String string : childrenid) {
		// for (int j = 1; j < SessionContext.getAllAppList().size(); j++) {// 根据顺序读取值，排序并保存
		// AppListBean temp = SessionContext.getAllAppList().get(j);
		// if (temp.id.equals(string)) {
		// mBean.add(temp);
		// break;
		// }
		// }
		// }
		//
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		// Comparator<AppListBean> comparator = new Comparator<AppListBean>() {
		// public int compare(AppListBean s1, AppListBean s2) {
		// return s1.order - s2.order;
		// }
		// };
		// // 这里就会自动根据规则进行排序
		// Collections.sort(mBean, comparator);
		mAdapter = new ColumnAdapter(this, mBean);
		mListView.setAdapter(mAdapter);
	}
	public void initListeners() {
		super.initListeners();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.tv_right_title :
				Intent intent = new Intent(this, SearchActivity.class);
				startActivity(intent);
				break;
		}
	}

	public void setHeadImg(String url) {
		mImgView.setImageResource(R.drawable.loading);
		if (url != null && url.length() > 0) {
			ImageLoader.getInstance().loadBitmap(new ImageCallback() {
				@Override
				public void imageCallback(Bitmap bm, String url, String imageTag) {
					if (bm != null) {
						mImgView.setImageBitmap(bm);
					}
				}

			}, url);
		}
	}
}

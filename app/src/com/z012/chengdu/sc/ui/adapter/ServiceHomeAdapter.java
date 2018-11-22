package com.z012.chengdu.sc.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.net.bean.AllServiceColumnBean;
import com.z012.chengdu.sc.ui.activity.HtmlActivity;
import com.z012.chengdu.sc.ui.activity.MainFragmentActivity;

import java.util.List;

public class ServiceHomeAdapter extends BaseAdapter {
	private Context				mContext;
	private List<AllServiceColumnBean.AppList>	mBeans;

	public ServiceHomeAdapter(Context context, List<AllServiceColumnBean.AppList> mBeans) {
		this.mBeans = mBeans;
		this.mContext = context;
	}

	public int getCount() {
		return mBeans.size();
	}

	// 列表项
	public Object getItem(int position) {
		return mBeans.get(position);
	}

	// 列表id
	public long getItemId(int position) {
		return position;
	}

	public final class ViewHolder {
		private TextView	tv_title;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		final AllServiceColumnBean.AppList temp = mBeans.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.gv_home_service_item, null);
			holder = new ViewHolder();
			holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.tv_title.setText(temp.appname);

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!temp.appurls.equals("ShowAllService")) {
					Intent mIntent = new Intent(mContext, HtmlActivity.class);
					mIntent.putExtra("title", temp.appname);
					mIntent.putExtra("path", temp.appurls);
					mIntent.putExtra("id", temp.id);
					mContext.startActivity(mIntent);
				} else {
					((MainFragmentActivity) mContext).changeTabService();
				}
			}
		});

		return convertView;
	}
}

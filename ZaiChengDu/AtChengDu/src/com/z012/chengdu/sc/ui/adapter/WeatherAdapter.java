package com.z012.chengdu.sc.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.prj.sdk.util.DateUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.net.bean.WeatherFutureInfoBean;
import com.z012.chengdu.sc.tools.WeatherInfoController;

public class WeatherAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<WeatherFutureInfoBean> list;

	public WeatherAdapter(Context context, ArrayList<WeatherFutureInfoBean> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder;
		if (null == convertView) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.lv_weather_day_item, null);
			viewHolder = new ViewHolder();
			viewHolder.tv_week = (TextView) convertView
					.findViewById(R.id.tv_week);
			viewHolder.iv_weather = (ImageView) convertView
					.findViewById(R.id.iv_weather);
			viewHolder.tv_temp1 = (TextView) convertView
					.findViewById(R.id.tv_temp1);
			viewHolder.tv_temp2 = (TextView) convertView
					.findViewById(R.id.tv_temp2);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		String week = DateUtil.dateToWeek(DateUtil.str2Date(
				list.get(position).date, "yyyy-MM-dd"));
		viewHolder.tv_week.setText(week);
		viewHolder.tv_temp1.setText(list.get(position).tmp.min + "º");
		viewHolder.tv_temp2.setText(list.get(position).tmp.max + "º");
		// 未来天气只显示白天天气
		viewHolder.iv_weather.setImageResource(WeatherInfoController
				.getWeatherResForDay(list.get(position).cond.txt_d));

		return convertView;
	}

	private class ViewHolder {
		TextView tv_week;
		ImageView iv_weather;
		TextView tv_temp1;
		TextView tv_temp2;
	}

}

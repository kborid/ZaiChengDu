package com.z012.chengdu.sc.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.net.entity.WeatherCityInfo;

import java.util.List;

public class WeatherCityAdapter extends BaseAdapter implements SectionIndexer {

    private List<WeatherCityInfo> list = null;
    private Context mContext;

    public WeatherCityAdapter(Context mContext, List<WeatherCityInfo> list) {
        this.mContext = mContext;
        this.list = list;
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     *
     * @param list
     */
    public void updateListView(List<WeatherCityInfo> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup arg2) {
        ViewHolder viewHolder = null;
        final WeatherCityInfo info = list.get(position);
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.lv_weather_city_item, null);
            viewHolder.tv_title = (TextView) view.findViewById(R.id.tv_title);
            viewHolder.tv_letter = (TextView) view.findViewById(R.id.tv_letter);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        // 根据position获取分类的首字母的char ascii值
        int section = getSectionForPosition(position);

        // 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        if (position == getPositionForSection(section)) {
            viewHolder.tv_letter.setVisibility(View.VISIBLE);
            viewHolder.tv_letter.setText(info.fl);
        } else {
            viewHolder.tv_letter.setVisibility(View.GONE);
        }

        viewHolder.tv_title.setText(this.list.get(position).name);

        return view;

    }

    final static class ViewHolder {
        TextView tv_title;
        TextView tv_letter;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的char ascii值
     */
    public int getSectionForPosition(int position) {
        return list.get(position).fl.charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getCount(); i++) {
            String sortStr = list.get(i).fl;
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public Object[] getSections() {
        return null;
    }
}
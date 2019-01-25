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
import com.z012.chengdu.sc.entity.WebInfoEntity;
import com.z012.chengdu.sc.helper.ForbidFastClickHelper;
import com.z012.chengdu.sc.net.entity.AppAllServiceInfoBean;
import com.z012.chengdu.sc.ui.activity.HtmlActivity;
import com.z012.chengdu.sc.ui.activity.MainFragmentActivity;

import java.util.List;

public class ServiceHomeAdapter extends BaseAdapter {
    private Context mContext;
    private List<AppAllServiceInfoBean> mBeans;

    public ServiceHomeAdapter(Context context, List<AppAllServiceInfoBean> mBeans) {
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
        private TextView tv_title;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final AppAllServiceInfoBean temp = mBeans.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gv_home_service_item, null);
            holder = new ViewHolder();
            holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_title.setText(temp.name);

        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ForbidFastClickHelper.isForbidFastClick()) {
                    return;
                }
                if (!temp.linkurls.equals("ShowAllService")) {
                    Intent intent = new Intent(mContext, HtmlActivity.class);
                    intent.putExtra("webEntity", new WebInfoEntity(String.valueOf(temp.id), temp.name, temp.linkurls));
                    mContext.startActivity(intent);
                } else {
                    ((MainFragmentActivity) mContext).changeTabService();
                }
            }
        });

        return convertView;
    }
}

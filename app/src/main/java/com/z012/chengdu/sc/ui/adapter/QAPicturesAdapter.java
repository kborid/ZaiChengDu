package com.z012.chengdu.sc.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.bumptech.glide.Glide;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.ui.activity.ImageScaleActivity;
import com.z012.chengdu.sc.ui.activity.qa.QADetailsActivity;

import java.util.ArrayList;

/**
 * 问答图片适配器
 *
 * @author LiaoBo
 */
public class QAPicturesAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> mBeans;
    private LayoutInflater inflater;
    private final int TYP1 = 0, TYP2 = 1, TYP3 = 2;

    public QAPicturesAdapter(Context context, ArrayList<String> mBean) {
        this.mContext = context;
        this.mBeans = mBean;
        this.inflater = LayoutInflater.from(mContext);
        if (mBean == null) {
            return;
        }
        int size = mBeans.size();
        if (size == 2 || size == 4) {
            ((QADetailsActivity) mContext).mQAGridView.setNumColumns(2);
            ((QADetailsActivity) mContext).mQAGridView.setLayoutParams(new LinearLayout.LayoutParams(Utils.dip2px(151), LayoutParams.WRAP_CONTENT));
        } else if (size == 1) {
            ((QADetailsActivity) mContext).mQAGridView.setNumColumns(1);
        } else {
            ((QADetailsActivity) mContext).mQAGridView.setNumColumns(3);
        }
    }

    @Override
    public int getCount() {
        if (mBeans != null) {
            return mBeans.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mBeans != null && position >= 0) {
            return mBeans.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        int size = mBeans.size();
        if (size == 1) {
            return TYP1;
        } else if (size == 2 || size == 4) {
            return TYP2;
        } else {
            return TYP3;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    private final class viewHolder1 {
        ImageView imageView;
    }

    private final class viewHolder2 {
        ImageView imageView;
    }

    private final class viewHolder3 {
        ImageView imageView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        viewHolder1 holder1 = null;
        viewHolder2 holder2 = null;
        viewHolder3 holder3 = null;
        int type = getItemViewType(position);
        String tempUrl = mBeans.get(position);

        if (convertView == null) {
            switch (type) {
                case TYP1:
                    convertView = inflater.inflate(R.layout.lv_qa_pictures_item, parent, false);
                    holder1 = new viewHolder1();
                    holder1.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                    LayoutParams params = (LayoutParams) holder1.imageView.getLayoutParams();
                    params.height = Utils.dip2px(150);
                    params.width = Utils.dip2px(150);
                    holder1.imageView.setLayoutParams(params);

                    convertView.setTag(holder1);
                    break;
                case TYP2:
                    convertView = inflater.inflate(R.layout.lv_qa_pictures_item, parent, false);
                    holder2 = new viewHolder2();
                    holder2.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                    convertView.setTag(holder2);
                    break;
                case TYP3:
                    convertView = inflater.inflate(R.layout.lv_qa_pictures_item, parent, false);
                    holder3 = new viewHolder3();
                    holder3.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                    convertView.setTag(holder3);
                    break;
            }
        } else {
            switch (type) {
                case TYP1:
                    holder1 = (viewHolder1) convertView.getTag();
                    break;
                case TYP2:
                    holder2 = (viewHolder2) convertView.getTag();
                    break;
                case TYP3:
                    holder3 = (viewHolder3) convertView.getTag();
                    break;
            }
        }

        switch (type) {
            case TYP1:
                setImgView(holder1.imageView, tempUrl);
                holder1.imageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        setIntent(position);
                    }
                });
                break;
            case TYP2:
                setImgView(holder2.imageView, tempUrl);
                holder2.imageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        setIntent(position);
                    }
                });
                break;
            case TYP3:
                setImgView(holder3.imageView, tempUrl);
                holder3.imageView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        setIntent(position);
                    }
                });
                break;
        }

        return convertView;
    }

    public void setIntent(int postion) {
        Intent intent = new Intent(mContext, ImageScaleActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("currentItem", postion);
        bundle.putStringArrayList("url", mBeans);// 传递当前点击图片设置的uri
        intent.putExtras(bundle);
        mContext.startActivity(intent);
    }

    private void setImgView(final ImageView view, String url) {
        if (StringUtil.empty(url)) {
            return;
        }
        if (!url.startsWith("http")) {
            url = NetURL.API_LINK + url;
        }
        Glide.with(mContext).load(url).placeholder(R.drawable.loading).crossFade().into(view);
    }
}

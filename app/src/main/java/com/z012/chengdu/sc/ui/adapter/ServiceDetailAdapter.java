package com.z012.chengdu.sc.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.prj.sdk.util.UIHandler;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.entity.WebInfoEntity;
import com.z012.chengdu.sc.helper.ForbidFastClickHelper;
import com.z012.chengdu.sc.net.entity.AllServiceInfoBean;
import com.z012.chengdu.sc.ui.activity.HtmlActivity;
import com.z012.chengdu.sc.ui.activity.MainFragmentActivity;

import java.util.List;

public class ServiceDetailAdapter extends BaseAdapter {

    private static final int MAX_LENGTH = 6;

    private Context mContext;
    private List<AllServiceInfoBean.AppList> mBeans;

    public ServiceDetailAdapter(Context context, List<AllServiceInfoBean.AppList> mBeans) {
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
        private ImageView imageView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final AllServiceInfoBean.AppList temp = mBeans.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gv_hot_service_item, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.img);
            holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String name = temp.getAppname();
        if (!TextUtils.isEmpty(name)) {
            if (name.length() >= MAX_LENGTH) {
                name = name.substring(0, 5);
                name += "...";
            }
        }
        holder.tv_title.setText(name);

        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ForbidFastClickHelper.isForbidFastClick()) {
                    return;
                }

                if (!temp.getAppurls().equals("ShowAllService")) {
                    Intent intent = new Intent(mContext, HtmlActivity.class);
                    intent.putExtra("webEntity", new WebInfoEntity(temp.getId(), temp.getAppname(), temp.getAppurls()));
                    mContext.startActivity(intent);
                } else {
                    ((MainFragmentActivity) mContext).changeTabService();
                }
            }
        });

        // 图片绑定
        UIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (temp.getAppurls().equals("ShowAllService")) {
                    Glide.with(mContext).load(R.drawable.iv_service_all).into(holder.imageView);
                } else {
                    String url = null;
                    if (!TextUtils.isEmpty(temp.getImgurls())) {
                        if (!temp.getImgurls().startsWith("http")) {
                            url = NetURL.API_LINK + temp.getImgurls();
                        }
                    }
                    Glide.with(mContext).load(url).placeholder(R.drawable.round_loading).into(holder.imageView);
//                    holder.imageView.setImageResource(R.drawable.round_loading);
//                    ImageLoader.getInstance().loadBitmap(new ImageLoader.ImageCallback() {
//                        @Override
//                        public void imageCallback(Bitmap bm, String url, String imageTag) {
//                            if (null != bm) {
//                                holder.imageView.setImageBitmap(bm);
//                            }
//                        }
//                    }, url);
                }
            }
        });

        return convertView;
    }
}

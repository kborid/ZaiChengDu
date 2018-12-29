package com.z012.chengdu.sc.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.util.UIHandler;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.AllServiceColumnBean;
import com.z012.chengdu.sc.tools.ClickUtils;
import com.z012.chengdu.sc.ui.activity.HtmlActivity;
import com.z012.chengdu.sc.ui.activity.MainFragmentActivity;

import java.util.List;

public class ServiceDetailAdapter extends BaseAdapter {

    private static final int MAX_LENGTH = 6;

	private Context mContext;
	private List<AllServiceColumnBean.AppList> mBeans;

	public ServiceDetailAdapter(Context context, List<AllServiceColumnBean.AppList> mBeans) {
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
		private ImageView	imageView;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		final AllServiceColumnBean.AppList temp = mBeans.get(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.gv_hot_service_item, null);
			holder = new ViewHolder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.img);
			holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		String name = temp.appname;
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
                if (ClickUtils.isForbidFastClick()) {
                    return;
                }

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

		// 图片绑定
		UIHandler.post(new Runnable() {
			@Override
			public void run() {
				if (temp.appurls.equals("ShowAllService")) {
                    Glide.with(mContext).load(R.drawable.iv_service_all).into(holder.imageView);
				} else {
					String url = null;
					if (!TextUtils.isEmpty(temp.imgurls)) {
						if (!temp.imgurls.startsWith("http")) {
							url = NetURL.API_LINK + temp.imgurls;
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

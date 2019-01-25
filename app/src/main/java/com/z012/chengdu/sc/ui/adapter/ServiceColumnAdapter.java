package com.z012.chengdu.sc.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.prj.sdk.util.BitmapUtils;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.entity.AllServiceInfoBean;

import java.util.List;

/**
 * 服务栏目适配器
 *
 * @author LiaoBo
 */
public class ServiceColumnAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater inflater;
    private List<AllServiceInfoBean> mBeans;
    // 默认选中
    private int defCheckedPosition = 0;

    public ServiceColumnAdapter(Context context, List<AllServiceInfoBean> mBeans) {
        this.mBeans = mBeans;
        this.mContext = context;
        this.inflater = LayoutInflater.from(mContext);
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

    /**
     * 记录默认选择项
     */
    public final void recordDefCheckedItem(int position) {
        defCheckedPosition = position;
    }

    public final class ViewHolder {
        private TextView tv_column;
        private ImageView iv_icon, iv_pitch;
        private View pitch_view_line;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final AllServiceInfoBean temp = mBeans.get(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.lv_service_column_item, null);
            holder = new ViewHolder();
            holder.tv_column = (TextView) convertView.findViewById(R.id.tv_column);
            holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.iv_pitch = (ImageView) convertView.findViewById(R.id.iv_pitch);
            holder.pitch_view_line = (View) convertView.findViewById(R.id.pitch_view_line);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_column.setText(temp.getCatalogname());
        loadBandImage(holder.iv_icon, temp.getImgurls1());

        if (position == defCheckedPosition) {
            holder.tv_column.setTextColor(0xff5056cd);
            holder.iv_pitch.setVisibility(View.VISIBLE);
            holder.pitch_view_line.setVisibility(View.VISIBLE);
            Bitmap bm = BitmapUtils.getAlphaBitmap(holder.iv_icon.getDrawable(), 0xff5056cd);
            holder.iv_icon.setImageBitmap(bm);
        } else {
            holder.tv_column.setTextColor(0xffa0a0a0);
            holder.iv_pitch.setVisibility(View.INVISIBLE);
            holder.pitch_view_line.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    private void loadBandImage(ImageView imageView, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (!url.startsWith("http")) {
            url = NetURL.API_LINK + url;
        }

        Glide.with(mContext).load(url).placeholder(R.drawable.round_loading).into(imageView);
    }
}
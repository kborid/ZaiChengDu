package com.z012.chengdu.sc.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.prj.sdk.util.StringUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.entity.QAListBean;
import com.z012.chengdu.sc.ui.activity.qa.QADetailsActivity;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 有问必答 适配器
 *
 * @author LiaoBo
 */
public class QAListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater inflater;
    private List<QAListBean.Result> mBeans;

    public QAListAdapter(Context context, List<QAListBean.Result> Beans) {
        this.mBeans = Beans;
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

    public final class ViewHolder {
        private TextView tv_ask, tv_answer, tv_date, tv_praise;
        private ImageView iv_answer;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final QAListBean.Result temp = mBeans.get(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.lv_qa_item, null);
            holder = new ViewHolder();
            holder.tv_ask = (TextView) convertView.findViewById(R.id.tv_ask);
            holder.tv_answer = (TextView) convertView.findViewById(R.id.tv_answer);
            holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            holder.tv_praise = (TextView) convertView.findViewById(R.id.tv_praise);
            holder.iv_answer = (ImageView) convertView.findViewById(R.id.iv_answer);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_ask.setText(StringUtil.doEmpty(temp.content));
        holder.tv_answer.setText(StringUtil.doEmpty(temp.replyComment, "该问题暂未回复"));
        holder.tv_praise.setText(new StringBuilder().append("关注 ").append(temp.supportAmount));
        String formatDate = new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(temp.happenTimeDate);
        holder.tv_date.setText(formatDate);
        if (temp.status.equals("03")) {
            holder.iv_answer.setImageResource(R.drawable.ic_answer);
            holder.tv_answer.setTextColor(0xff565656);
        } else if (temp.status.equals("04")) {
            holder.iv_answer.setImageResource(R.drawable.ic_answer);
            holder.tv_answer.setTextColor(0xffa9a9a9);
            holder.tv_answer.setText("该问题已驳回");
        } else {
            holder.iv_answer.setImageResource(R.drawable.ic_answer2);
            holder.tv_answer.setTextColor(0xffa9a9a9);
        }

        // if (StringUtil.empty(temp.location)) {
        // holder.tv_location.setVisibility(View.GONE);
        // } else {
        // holder.tv_location.setVisibility(View.VISIBLE);
        // holder.tv_location.setText(temp.location);
        // }

        // if (StringUtil.notEmpty(temp.status)) {// 状态
        // if (temp.status.equals("03")) {
        // holder.tv_status.setText("已回复");
        // holder.tv_status.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.tab2_status_b));
        // } else if (temp.status.equals("02")) {
        // holder.tv_status.setText("待审核 ");
        // holder.tv_status.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.tab2_status_a));
        // } else if (temp.status.equals("04")) {
        // holder.tv_status.setText("已驳回");
        // holder.tv_status.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.tab2_status_a));
        // }
        // }
        // 图片绑定
        // StringBuilder url;
        // if (temp.userPhotoUrl != null && temp.userPhotoUrl.length() > 0) {
        // url = new StringBuilder();
        // url.append(NetURL.API_LINK).append(temp.userPhotoUrl);
        // loadImage(holder.iv_photo, url.toString(), (url.toString() + position));
        // } else {
        // url = new StringBuilder();
        // url.append(NetURL.AVATAR).append(temp.userId).append(".jpg");
        // loadImage(holder.iv_photo, url.toString(), (url.toString() + position));
        // }
        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, QADetailsActivity.class);
                intent.putExtra("ITEM", temp);
                mContext.startActivity(intent);
            }
        });

        return convertView;
    }

    /**
     * 加载图片
     *
     * @param view
     * @param scrollState
     */
    private boolean mBusy = false;    // 标识是否存在滚屏操作

    public void listenStateChange(AbsListView view, int scrollState) {
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE: // Idle态，进行实际数据的加载显示
                mBusy = false;
                // int first = view.getFirstVisiblePosition();
                int count = view.getChildCount();
                for (int i = 0; i < count; i++) {
                    ViewGroup layoutView = (ViewGroup) view.getChildAt(i);
                    ImageView temp = (ImageView) layoutView.findViewById(R.id.iv_photo);
                    if (temp != null && temp.getTag() != null) { // 非null说明需要加载数据
                        loadImage(temp, temp.getTag(R.id.image_url).toString());
                    }
                }
                break;
            case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                mBusy = true;
                break;
            case OnScrollListener.SCROLL_STATE_FLING:
                mBusy = true;
                break;
            default:
                break;
        }
    }

    private void loadImage(ImageView view, String url) {
        if (StringUtil.empty(url)) {
            return;
        }
        if (!url.startsWith("http")) {
            url = NetURL.API_LINK + url;
        }
        Glide.with(mContext).load(url).placeholder(R.drawable.iv_def_photo).crossFade().into(view);
    }

}

package com.z012.chengdu.sc.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.entity.WebInfoEntity;
import com.z012.chengdu.sc.net.entity.WDDetailsBean;
import com.z012.chengdu.sc.ui.activity.HtmlActivity;
import com.z012.chengdu.sc.ui.activity.qa.QADetailsActivity;
import com.z012.chengdu.sc.ui.activity.qa.QAPursueErrorCorrectionActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 问答详情适配器
 *
 * @author LiaoBo
 */
public class QADetailsAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater inflater;
    private List<WDDetailsBean.MoReply> mBeans;
    private List<WDDetailsBean.MoRecommandApp> mRecommand;
    private Button btn_praise;
    private int agreeCount;

    public QADetailsAdapter(Context context,
                            List<WDDetailsBean.MoReply> replyList,
                            List<WDDetailsBean.MoRecommandApp> recommandList) {
        this.mContext = context;
        this.mBeans = replyList;
        this.mRecommand = recommandList;
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
     * 重置赞按钮
     */
    public void resetPraiseClickable() {
        StringBuilder sb = new StringBuilder().append("赞 ");
        agreeCount = agreeCount + 1;
        if (agreeCount > 9999) {
            sb.append("9999").append("+");
        } else {
            sb.append(agreeCount);
        }
        btn_praise.setText(sb.toString());
        btn_praise.setClickable(false);
        btn_praise
                .setBackgroundResource(R.drawable.common_round_rectangle_gray_bg);
    }

    public final class ViewHolder {
        private TextView tv_reply_content, tv_reply_date;
        private Button btn_praise, btn_error_correction;
        private LinearLayout layoutRecommand;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final WDDetailsBean.MoReply temp = mBeans.get(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.lv_qa_detail_item, null);
            holder = new ViewHolder();
            holder.tv_reply_content = (TextView) convertView
                    .findViewById(R.id.tv_reply_content);
            holder.tv_reply_date = (TextView) convertView
                    .findViewById(R.id.tv_reply_date);
            holder.btn_praise = (Button) convertView
                    .findViewById(R.id.btn_praise);
            holder.btn_error_correction = (Button) convertView
                    .findViewById(R.id.btn_error_correction);
            holder.layoutRecommand = (LinearLayout) convertView
                    .findViewById(R.id.layoutRecommand);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv_reply_content.setText(StringUtil.doEmpty(temp.replyContent,
                "该问题暂未回复"));
        StringBuilder sb = new StringBuilder().append("赞 ");
        if (temp.agreeCount > 9999) {
            sb.append("9999").append("+");
        } else {
            sb.append(temp.agreeCount);
        }
        holder.btn_praise.setText(sb.toString());
        if (temp.isAgree) {// 是否赞过
            holder.btn_praise
                    .setBackgroundResource(R.drawable.common_round_rectangle_gray_bg);
            holder.btn_praise.setClickable(false);
        } else {
            holder.btn_praise.setClickable(true);
            holder.btn_praise
                    .setBackgroundResource(R.drawable.common_round_rectangle_cerulean_bg);
            // 赞监听
            holder.btn_praise.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    btn_praise = holder.btn_praise;
                    agreeCount = temp.agreeCount;
                    ((QADetailsActivity) mContext).praise(temp.replyId);
                }
            });
        }
        Date replyData = DateUtil.str2Date(temp.replyTime, "yyyy-MM-dd HH:mm");
        String formatDate = new SimpleDateFormat("yyyy年MM月dd日 HH:mm")
                .format(replyData.getTime());
        holder.tv_reply_date.setText(formatDate);

        if (position + 1 == mBeans.size()) {
            holder.btn_error_correction.setVisibility(View.VISIBLE);
            holder.btn_error_correction
                    .setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {// 追问纠错
                            if (!SessionContext.isLogin()) {
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
                                return;
                            }
                            Intent intent = new Intent(mContext,
                                    QAPursueErrorCorrectionActivity.class);
                            intent.putExtra("observeId", temp.observeId);
                            mContext.startActivity(intent);
                        }
                    });
            if (mRecommand != null && !mRecommand.isEmpty()) {
                holder.layoutRecommand.setVisibility(View.VISIBLE);
                View v = inflater.inflate(R.layout.lv_qa_detail_recommand_item,
                        null);
                // TextView tv = (TextView) v.findViewById(R.id.tv_recommand);
                int padding = Utils.dip2px(15);
                for (int i = 0; i < mRecommand.size(); i++) {
                    View line = new View(mContext);
                    line.setLayoutParams(new LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT, Utils.dip2px(0.5f)));
                    line.setBackgroundColor(0xffdcdcdc);

                    holder.layoutRecommand.addView(line);// 添加线条

                    final WDDetailsBean.MoRecommandApp mData = mRecommand
                            .get(i);
                    TextView tv = new TextView(mContext);
                    tv.setTextColor(0xff565656);
                    tv.setTextSize(14);
                    tv.setGravity(Gravity.CENTER_VERTICAL);
                    tv.setPadding(padding, 0, padding, 0);
                    tv.setBackgroundResource(R.drawable.common_item_selector);
                    tv.setLayoutParams(new LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT, Utils.dip2px(49)));
                    tv.setText(mData.appname);
                    holder.layoutRecommand.addView(tv);// 添加text

                    if (i + 1 == mRecommand.size()) {
                        View line2 = new View(mContext);
                        line2.setLayoutParams(new LinearLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT, Utils
                                .dip2px(0.5f)));
                        line2.setBackgroundColor(0xffdcdcdc);
                        holder.layoutRecommand.addView(line2);// 添加线条
                    }
                    tv.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, HtmlActivity.class);
                            intent.putExtra("webEntity", new WebInfoEntity(mData.appurls));
                            mContext.startActivity(intent);
                        }
                    });

                    ((QADetailsActivity) mContext)
                            .setAnimation(holder.layoutRecommand);
                }
            }
        }

        // if (temp.status.equals("03")) {
        // holder.iv_answer.setImageResource(R.drawable.ic_answer);
        // holder.tv_answer.setTextColor(0xff565656);
        // } else {
        // holder.iv_answer.setImageResource(R.drawable.ic_answer2);
        // holder.tv_answer.setTextColor(0xffa9a9a9);
        // }

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
        // loadImage(holder.iv_photo, url.toString(), (url.toString() +
        // position));
        // } else {
        // url = new StringBuilder();
        // url.append(NetURL.AVATAR).append(temp.userId).append(".jpg");
        // loadImage(holder.iv_photo, url.toString(), (url.toString() +
        // position));
        // }
        // convertView.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // Intent intent = new Intent(mContext, QADetailsActivity.class);
        // intent.putExtra("ITEM", temp);
        // mContext.startActivity(intent);
        // }
        // });

        return convertView;
    }
}
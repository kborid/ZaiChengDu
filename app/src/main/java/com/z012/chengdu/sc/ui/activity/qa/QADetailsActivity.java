package com.z012.chengdu.sc.ui.activity.qa;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.common.widget.tagcloudview.TagCloudView;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ToastUtil;
import com.thunisoft.ui.util.ScreenUtils;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.entity.QAListBean;
import com.z012.chengdu.sc.net.entity.WDDetailsBean;
import com.z012.chengdu.sc.net.entity.WDDetailsBean.MoRecommandApp;
import com.z012.chengdu.sc.net.entity.WDDetailsBean.MoReply;
import com.z012.chengdu.sc.net.request.RequestBeanBuilder;
import com.z012.chengdu.sc.ui.BaseActivity;
import com.z012.chengdu.sc.ui.activity.MainFragmentActivity;
import com.z012.chengdu.sc.ui.adapter.QADetailsAdapter;
import com.z012.chengdu.sc.ui.adapter.QAPicturesAdapter;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

/**
 * 有问必答详情
 *
 * @author LiaoBo
 */
public class QADetailsActivity extends BaseActivity implements DataCallback, TagCloudView.OnTagClickListener, DialogInterface.OnCancelListener {
    private TextView tv_address, tv_content, tv_date;
    private String observeId;
    private QAListBean.Result mItemData;
    private LinearLayout layoutContent;
    private ImageView iv_photo;
    public GridView mQAGridView;
    private QAPicturesAdapter mQAAdapter;
    private Button btn_support;
    private final String FORMAT = "yyyy年MM月dd日 HH:mm";
    // 关注数
    private String supportAmount;
    // tag id
    private ArrayList<String> mTagId = new ArrayList<String>();
    private ArrayList<String> mTagValue = new ArrayList<String>();
    private QADetailsAdapter mAdapter;
    private ListView mListView;
    private View headerView;
    private ImageView iv_shrink;
    // 是否展开
    private boolean isSpread;
    private final int SHOWHEIGHT = ScreenUtils.dp2px(300);
    private List<MoReply> mMoReplyList = new ArrayList<MoReply>();
    private List<MoRecommandApp> mMoRecommandApp = new ArrayList<MoRecommandApp>();

    private Button btn_qa_more, btn_qa_say;

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_ywbd_details;
    }

    @Override
    public void initParams() {
        super.initParams();
        showProgressDialog(getString(R.string.loading), true);
        tv_center_title.setText("问题详情");
        mListView = (ListView) findViewById(R.id.mListView);
        headerView = getLayoutInflater().inflate(R.layout.lv_qa_ask_headerview, mListView, false);
        tv_address = (TextView) headerView.findViewById(R.id.tv_address);
        tv_content = (TextView) headerView.findViewById(R.id.tv_content);
        tv_date = (TextView) headerView.findViewById(R.id.tv_date);
        iv_photo = (ImageView) headerView.findViewById(R.id.iv_photo);
        mQAGridView = (GridView) headerView.findViewById(R.id.gridView);
        btn_support = (Button) headerView.findViewById(R.id.btn_support);
        iv_shrink = (ImageView) headerView.findViewById(R.id.iv_shrink);
        layoutContent = (LinearLayout) headerView.findViewById(R.id.layout_content);
        btn_qa_more = (Button) findViewById(R.id.btn_qa_more);
        btn_qa_say = (Button) findViewById(R.id.btn_qa_say);

        try {
            if (SessionContext.isLogin() && !SessionContext.mUser.USERBASIC.id.equals(mItemData.userId)) {
                attentionDeal(NetURL.WG_STATUS, 1);
            } else {
                setSupportStatus(1);
            }
            ViewTreeObserver vto = headerView.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    headerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    int headHeight = headerView.getHeight() + ScreenUtils.mStateBarHeight + ScreenUtils.dp2px(56);
                    if (headHeight > SHOWHEIGHT) {
                        LayoutParams params = (LayoutParams) layoutContent.getLayoutParams();
                        params.height = SHOWHEIGHT - ScreenUtils.dp2px(90) - ScreenUtils.mStateBarHeight - ScreenUtils.dp2px(56);
                        layoutContent.setLayoutParams(params);
                        iv_shrink.setVisibility(View.VISIBLE);
                    }
                }
            });
            // 添加头部view：必须放在adapter前面不然会报错
            mListView.addHeaderView(headerView);
            mAdapter = new QADetailsAdapter(this, mMoReplyList, mMoRecommandApp);
            mListView.setAdapter(mAdapter);
            loadReplyDetail();// 获取回复详情
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置云标签
     */
    public void setTagCloudView(List<String> tags) {
        if (tags.isEmpty()) {
            return;
        }
        TagCloudView tagCloudView = (TagCloudView) headerView.findViewById(R.id.tag_cloud_view);
        tagCloudView.setTags(tags);
        tagCloudView.setOnTagClickListener(this);
    }

    /**
     * 设置头像
     *
     * @param url
     */
    public void initPhoto(String url) {
        if (StringUtil.notEmpty(url)) {
            if (!url.startsWith("http")) {
                url = NetURL.API_LINK + url;
            }
        }
        Glide.with(this).load(url).placeholder(R.drawable.iv_def_photo).crossFade().into(iv_photo);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        if (getIntent().getExtras() != null && getIntent().getExtras().getSerializable("ITEM") != null) {
            mItemData = (QAListBean.Result) getIntent().getExtras().getSerializable("ITEM");
            observeId = mItemData.observeId;
            // tv_title.setText(StringUtil.doEmpty(mItemData.title));
            tv_content.setText(StringUtil.doEmpty(mItemData.content));
            tv_date.setText(DateUtil.getMinutes(mItemData.happenTimeDate, FORMAT));
            initPhoto(mItemData.userPhotoUrl);
            if (StringUtil.notEmpty(mItemData.photoUrl)) {// 问答图片
                String[] p = mItemData.photoUrl.split(",");
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < p.length; i++) {
                    list.add(p[i]);
                }
                mQAAdapter = new QAPicturesAdapter(this, list);
                mQAGridView.setAdapter(mQAAdapter);
            }
            if (StringUtil.notEmpty(mItemData.location)) {
                tv_address.setText(mItemData.location);
            } else {
                tv_address.setVisibility(View.GONE);
            }
            if (StringUtil.notEmpty(mItemData.question_area)) {
                mTagValue.add(mItemData.question_area);
                mTagId.add(mItemData.question_area_value);
            }
            if (StringUtil.notEmpty(mItemData.question_type)) {
                mTagValue.add(mItemData.question_type);
                mTagId.add(mItemData.question_type_value);
            }
            setTagCloudView(mTagValue);
            supportAmount = mItemData.supportAmount;
        }
    }

    /**
     * 加载回复详情
     */
    private void loadReplyDetail() {
        if (mItemData.status != null && mItemData.status.equals("04")) {
            removeProgressDialog();
            WDDetailsBean.MoReply reply = new WDDetailsBean.MoReply();
            reply.replyContent = "驳回理由:" + StringUtil.doEmpty(mItemData.auditComment);// 驳回评论
            reply.replyTime = StringUtil.doEmpty(mItemData.auditTime);
            mMoReplyList.add(reply);
            mAdapter.notifyDataSetChanged();

            // setAnimation(mLayoutAnswer);
            return;
        } else if (mItemData.status != null && mItemData.status.equals("02")) {
            // tv_reply_content.setText("正在办理中，请耐心等待！");
            removeProgressDialog();
            return;
        }
        RequestBeanBuilder builder;
        if (SessionContext.isLogin()) {
            builder = RequestBeanBuilder.create(true);
        } else {
            builder = RequestBeanBuilder.create(false);
        }
        builder.addBody("OBSERVE_ID", observeId);

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.WG_DETAILS;
        data.flag = 0;

        if (!isProgressShowing())
            showProgressDialog(getString(R.string.loading), true);
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SessionContext.isLogin()) {
                    LocalBroadcastManager.getInstance(QADetailsActivity.this).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
                    return;
                }
                if (v.getTag() != null) {
                    if (Integer.parseInt(v.getTag().toString()) == 1) {// 关注
                        attentionDeal(NetURL.WG_ATTENTION, 2);
                    } else if (Integer.parseInt(v.getTag().toString()) == 2) {// 取消关注
                        attentionDeal(NetURL.WG_CANCEL_ATTENTION, 3);
                    }
                }
            }
        });

        iv_shrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSpread = !isSpread;
                shrink();
            }
        });

        btn_qa_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QADetailsActivity.this, MainFragmentActivity.class);
                intent.putExtra("moreQA", true);
                startActivity(intent);
            }
        });

        btn_qa_say.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SessionContext.isLogin()) {
                    LocalBroadcastManager.getInstance(QADetailsActivity.this).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
                    return;
                }
                Intent intent = new Intent(QADetailsActivity.this, QAISayActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 收缩操作
     */
    public void shrink() {
        if (!isSpread) {
            LayoutParams params = (LayoutParams) layoutContent.getLayoutParams();
            params.height = SHOWHEIGHT - ScreenUtils.dp2px(90) - ScreenUtils.mStateBarHeight - ScreenUtils.dp2px(56);
            layoutContent.setLayoutParams(params);
            iv_shrink.setImageResource(R.drawable.ic_shrink_a_1);
        } else {
            layoutContent.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            iv_shrink.setImageResource(R.drawable.ic_shrink_b_1);
        }
    }

    /**
     * 赞
     */
    public void praise(String reply_id) {
        if (!SessionContext.isLogin()) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
            return;
        }
        RequestBeanBuilder builder = RequestBeanBuilder.create(true);
        builder.addBody("observe_id", observeId);
        builder.addBody("reply_id", reply_id);

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.QA_PRAISE;
        data.flag = 4;

        if (!isProgressShowing())
            showProgressDialog(getString(R.string.loading), true);
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    /**
     * 关注处理
     *
     * @param url  url
     * @param flag 1：获取状态；2：关注；3：取消关注
     */
    private void attentionDeal(String url, int flag) {
        RequestBeanBuilder builder = RequestBeanBuilder.create(true);
        builder.addBody("ID", observeId);

        ResponseData data = builder.syncRequest(builder);
        data.path = url;
        data.flag = flag;

        if (!isProgressShowing())
            showProgressDialog(getString(R.string.loading), true);
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {

        if (request.flag == 1) {
            if (response.body.toString() != null && response.body.toString().equals("未关注")) {
                setSupportStatus(1);
            } else {
                setSupportStatus(2);
            }
        } else if (request.flag == 2) {
            removeProgressDialog();
            ToastUtil.show("关注成功", 0);
            setSupportStatus(2);
        } else if (request.flag == 3) {
            removeProgressDialog();
            ToastUtil.show("已取消关注", 0);
            setSupportStatus(1);
        } else if (request.flag == 0) {
            removeProgressDialog();
            WDDetailsBean temp = JSON.parseObject(response.body.toString(), WDDetailsBean.class);
            mMoReplyList.clear();
            mMoRecommandApp.clear();
            mMoReplyList.addAll(temp.moReplyList);
            mMoRecommandApp.addAll(temp.moRecommandAppList);
            mAdapter.notifyDataSetChanged();
        } else if (request.flag == 4) {
            removeProgressDialog();
            mAdapter.resetPraiseClickable();
        }

    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        removeProgressDialog();
        String message;
        if (e != null && e instanceof ConnectException) {
            message = getString(R.string.dialog_tip_net_error);
        } else {
            // message = getString(R.string.dialog_tip_null_error);
            message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
        }
        ToastUtil.show(message, Toast.LENGTH_LONG);

    }

    /**
     * 设置关注状态 1:未关注 2：已关注
     */
    public void setSupportStatus(int tag) {
        if (tag == 1) {
            StringBuilder sb = new StringBuilder().append("关注 ");
            if (supportAmount != null && supportAmount.length() > 5) {
                sb.append("9999").append("+");
            } else {
                sb.append(supportAmount);
            }
            btn_support.setText(sb.toString());
            btn_support.setBackgroundResource(R.drawable.common_blue_rounded_btn_bg);
        } else {
            btn_support.setText("取消关注");
            btn_support.setBackgroundResource(R.drawable.common_round_rectangle_gray_bg);
        }
        btn_support.setTag(tag);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        DataLoader.getInstance().clear(requestID);
        removeProgressDialog();
    }

    /**
     * 设置加载回复动画
     *
     * @param v
     */
    public void setAnimation(View v) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.push_up_in);
        v.startAnimation(animation);
    }

    @Override
    public void onTagClick(int position) {
        // ToastUtil.show("点击 position : " + mTagId.get(position), Toast.LENGTH_SHORT);
        Intent intent = new Intent(this, QATagAboutListActivity.class);
        intent.putExtra("name", mTagValue.get(position));
        if (position == 0)
            intent.putExtra("questionAreaValue", mTagId.get(position));
        else
            intent.putExtra("questionTypeValue", mTagId.get(position));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMoReplyList.clear();
        mMoRecommandApp.clear();
        mMoReplyList = null;
        mQAGridView = null;
        mListView = null;
        mMoRecommandApp = null;
        mQAAdapter = null;
        mAdapter = null;
    }

}

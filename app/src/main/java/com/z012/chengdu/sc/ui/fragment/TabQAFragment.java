package com.z012.chengdu.sc.ui.fragment;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.widget.custom.BadgeView;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.ToastUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.entity.QAListBean;
import com.z012.chengdu.sc.net.request.RequestBeanBuilder;
import com.z012.chengdu.sc.ui.BaseFragment;
import com.z012.chengdu.sc.ui.activity.qa.MyQAActivity;
import com.z012.chengdu.sc.ui.activity.qa.QAISayActivity;
import com.z012.chengdu.sc.ui.adapter.QAListAdapter;

import java.net.ConnectException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 问答
 *
 * @author kborid
 */
public class TabQAFragment extends BaseFragment implements DataCallback/*, OnRefreshListener2<ListView>*/ {

    //	@BindView(R.id.listView) PullToRefreshListView listView;
    @BindView(R.id.iv_ask_question)
    ImageView iv_ask_question;
    @BindView(R.id.tv_right_title)
    TextView tv_right_title;
    private QAListAdapter mAdapter;
    private ArrayList<QAListBean.Result> mBean = new ArrayList<QAListBean.Result>();
    private int page_index;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_tab_question;
    }

    @Override
    protected void initParams() {
        super.initParams();
        showProgressDialog(getString(R.string.loading), false);
        mAdapter = new QAListAdapter(getActivity(), mBean);
//		listView.setAdapter(mAdapter);
//        listView.setOnRefreshListener(this);
        loadData(false);
    }

    @OnClick(R.id.iv_ask_question)
    void ask() {
        if (!SessionContext.isLogin()) {
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
            return;
        }
        startActivity(new Intent(getActivity(), QAISayActivity.class));
    }

    @OnClick(R.id.tv_title_right)
    void add() {
        if (SessionContext.isLogin()) {
            startActivity(new Intent(getActivity(), MyQAActivity.class));
        } else {
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
        }
    }

    /**
     * 设置tag
     */
    public void setTag() {
        BadgeView mMsgBadge = new BadgeView(getActivity());
        mMsgBadge.setBadgeGravity(Gravity.TOP | Gravity.RIGHT);
        mMsgBadge.setBadgeMargin(0, 10, 10, 0);
        mMsgBadge.setTargetView(tv_right_title);
        mMsgBadge.setText("");
        mMsgBadge.setBackgroundResource(R.drawable.ic_message_alert);
        mMsgBadge.setVisibility(View.VISIBLE);
    }

    /**
     * 加载问答数据
     *
     * @return
     */
    private void loadData(boolean isMore) {
        RequestBeanBuilder builder = RequestBeanBuilder.create(false);
        if (!isMore) {
            page_index = 1;
        }
        builder.addBody("PAGE_INDEX", String.valueOf(page_index)).addBody("PAGE_COUNT", AppConst.COUNT);

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.WG_ALL;
        if (isMore) {
            data.flag = 2;
        } else {
            data.flag = 1;
        }
        requestID = DataLoader.getInstance().loadData(this, data);

    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        removeProgressDialog();
//		listView.onRefreshComplete();
        JSONObject mJson = JSON.parseObject(response.body.toString());
        String json = mJson.getString("page");
        QAListBean temp = JSON.parseObject(json, QAListBean.class);
        page_index = temp.pageNo + 1;
        if (request.flag == 1) {
            mBean.clear();
            mBean.addAll(temp.result);
            mAdapter.notifyDataSetChanged();
        } else {
            mBean.addAll(temp.result);
            mAdapter.notifyDataSetChanged();
        }

        if (mBean.size() >= temp.totalCount) {
            // no more
//			listView.setMode(Mode.PULL_FROM_START);
        } else {
//			listView.setMode(Mode.BOTH);
        }

    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        removeProgressDialog();
//		listView.onRefreshComplete();
        String message;
        if (e != null && e instanceof ConnectException) {
            message = getString(R.string.dialog_tip_net_error);
            ToastUtil.show(message, Toast.LENGTH_LONG);
        } else {
            message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
        }
        ToastUtil.show(message, Toast.LENGTH_LONG);
    }

//	@Override
//	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
//		loadData(false);
//	}

//	@Override
//	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
//		loadData(true);
//	}
}
package com.z012.chengdu.sc.ui.activity.qa;

import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.ToastUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.request.RequestBeanBuilder;
import com.z012.chengdu.sc.net.entity.QAListBean;
import com.z012.chengdu.sc.ui.BaseActivity;
import com.z012.chengdu.sc.ui.adapter.QAListAdapter;

import java.net.ConnectException;
import java.util.ArrayList;

/**
 * 问答--标签相关问题
 *
 * @author LiaoBo
 */
public class QATagAboutListActivity extends BaseActivity implements DataCallback/*, OnRefreshListener2<ListView>*/ {
    //	private PullToRefreshListView			listView;
    private QAListAdapter mAdapter;
    private ArrayList<QAListBean.Result> mBean = new ArrayList<QAListBean.Result>();
    private int page_index;
    private String questionAreaValue, questionTypeValue;

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_qa_tag_about_list_act;
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        questionAreaValue = getIntent().getExtras().getString("questionAreaValue");
        questionTypeValue = getIntent().getExtras().getString("questionTypeValue");
        String name = getIntent().getExtras().getString("name");
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(name);
        }
        sb.append("相关问题");
        tv_center_title.setText(sb.toString());
    }

    @Override
    public void initParams() {
        super.initParams();
        showProgressDialog(getString(R.string.loading), false);
        tv_center_title.setText("相关问题");
//        listView = (PullToRefreshListView) findViewById(R.id.listView);

        mAdapter = new QAListAdapter(this, mBean);
//		listView.setAdapter(mAdapter);
        loadData(false);
    }

    @Override
    public void initListeners() {
        super.initListeners();
//		listView.setOnRefreshListener(this);
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
        builder.addBody("QUESTION_AREA", questionAreaValue).addBody("QUESTION_TYPE", questionTypeValue);
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

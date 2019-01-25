package com.z012.chengdu.sc.ui.activity;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.umeng.analytics.MobclickAgent;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.net.entity.AppListBean;
import com.z012.chengdu.sc.ui.BaseActivity;
import com.z012.chengdu.sc.ui.adapter.ColumnAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 搜索服务
 *
 * @author LiaoBo
 */
public class SearchActivity extends BaseActivity implements TextWatcher, OnItemClickListener {

    private ColumnAdapter mAdapter;
    private ArrayList<AppListBean> mBean = new ArrayList<>();

    @BindView(R.id.listView)
    ListView mListView;
    @BindView(R.id.listHistory)
    ListView listHistory;
    @BindView(R.id.tv_empty)
    TextView emptyView;
    @BindView(R.id.auto_text)
    AutoCompleteTextView mAuto_text;

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_search;
    }

    @Override
    public void initParams() {
        super.initParams();
        mAuto_text.requestFocus();
        mAdapter = new ColumnAdapter(this, mBean);
        mAdapter.isSearchHistory(true);
        mListView.setAdapter(mAdapter);
        emptyView.setText("暂时没有找到相关服务");
        mListView.setEmptyView(emptyView);
        View footer = LayoutInflater.from(this).inflate(R.layout.view_search_footview, null);
        listHistory.addFooterView(footer);
        showHistory();
    }

    @Override
    public void initListeners() {
        mAuto_text.addTextChangedListener(this);
        listHistory.setOnItemClickListener(this);
    }

    @OnClick(R.id.tv_title_right)
    public void cancelSearch(View view) {
        finish();
    }

    @Override
    public void afterTextChanged(Editable s) {
        String name = s.toString().trim();
        mBean.clear();
        if (!TextUtils.isEmpty(name)) {
            hideListView();
            for (AppListBean bean : SessionContext.getAllAppList()) {
                if (bean.appname.contains(name)) {
                    mBean.add(bean);
                }
            }
            mAdapter.notifyDataSetChanged();
        } else {
            showHistory();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    /**
     * 获取历史
     */
    public void showHistory() {
        listHistory.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        // 获取搜索记录文件内容
        String history = SharedPreferenceUtil.getInstance().getString("history", "", false);
        if (TextUtils.isEmpty(history)) {
            hideListView();
        } else {
            listHistory.setAdapter(new ArrayAdapter<String>(this, R.layout.lv_searc_history_item, history.split(",")));
        }
    }

    /**
     * 隐藏搜索历史listview
     */
    private void hideListView() {
        listHistory.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
    }

    /**
     * 清除搜索记录
     */
    public void cleanHistory(View v) {
        SharedPreferenceUtil.getInstance().setString("history", "", false);
        hideListView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String keyword = mAuto_text.getText().toString().trim();
        if (StringUtil.notEmpty(keyword)) {
            // 添加友盟自定义事件
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("keyword", keyword);
            MobclickAgent.onEvent(this, "Search", map);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAuto_text.setText((String) parent.getAdapter().getItem(position));
    }
}

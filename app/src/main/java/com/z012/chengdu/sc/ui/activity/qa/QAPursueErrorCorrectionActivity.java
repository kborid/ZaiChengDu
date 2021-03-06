package com.z012.chengdu.sc.ui.activity.qa;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ToastUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.request.RequestBeanBuilder;
import com.z012.chengdu.sc.ui.BaseActivity;

import java.net.ConnectException;

/**
 * 问答--追问纠错
 *
 * @author LiaoBo
 */
public class QAPursueErrorCorrectionActivity extends BaseActivity implements DataCallback {

    private EditText et_content;
    private String observeId;

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_qa_pursue_error_correcyion_act;
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        observeId = getIntent().getStringExtra("observeId");
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("追问纠错");
        tv_right_title.setText("完成");
        et_content = (EditText) findViewById(R.id.et_content);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_right_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
    }

    /**
     * 加载数据
     */
    private void loadData() {
        String content = et_content.getText().toString().trim();
        if (StringUtil.empty(content)) {
            ToastUtil.show("输入内容不允许为空", 0);
            return;
        }
        if (StringUtil.containsEmoji(et_content.getText().toString())) {
            ToastUtil.show("问题描述不能包含Emoji表情符号", 0);
            return;
        }
        RequestBeanBuilder builder = RequestBeanBuilder.create(true);
        builder.addBody("observe_id", observeId).addBody("content", content);
        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.QA_PURSUE_ERROR;

        if (!isProgressShowing())
            showProgressDialog(getString(R.string.loading), true);
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        removeProgressDialog();
        ToastUtil.show("提交成功", 0);
        finish();
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        removeProgressDialog();

        String message;
        if (e != null && e instanceof ConnectException) {
            message = getString(R.string.dialog_tip_net_error);
        } else {
            message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
        }
        ToastUtil.show(message, Toast.LENGTH_LONG);
    }

}

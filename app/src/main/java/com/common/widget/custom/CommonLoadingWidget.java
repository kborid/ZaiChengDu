package com.common.widget.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.z012.chengdu.sc.R;

/***
 * 公共加载页面
 *
 * @author kborid
 */
public class CommonLoadingWidget extends LinearLayout {

    private LinearLayout waitting_layout, retry_layout;
    protected TextView tv_retry_msg;
    private Button btn_refersh;
    private ProgressWheel progress_wheel;

    public CommonLoadingWidget(Context context) {
        this(context, null);
    }

    public CommonLoadingWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.common_loading, this);
        initViews();
    }

    private void initViews() {
        waitting_layout = (LinearLayout) findViewById(R.id.dialog_view);
        retry_layout = (LinearLayout) findViewById(R.id.retry_layout_bg);
        tv_retry_msg = (TextView) findViewById(R.id.tv_retry_msg);
        btn_refersh = (Button) findViewById(R.id.btn_refersh);
        progress_wheel = (ProgressWheel) findViewById(R.id.progress_wheel);
    }

    public void initListener(OnClickListener mListener) {
        if (mListener != null) {
            btn_refersh.setOnClickListener(mListener);
        }
    }

    /**
     * 初始化loading界面
     */
    public void startLoading() {
        if (!progress_wheel.isSpinning()) {
            progress_wheel.spin();
        }
        this.setVisibility(View.VISIBLE);
        waitting_layout.setVisibility(View.VISIBLE);
        retry_layout.setVisibility(View.GONE);
    }

    /**
     * 重新加载页面
     */
    public void retryLoading() {
        this.setVisibility(View.VISIBLE);
        waitting_layout.setVisibility(View.GONE);
        retry_layout.setVisibility(View.VISIBLE);
    }

    /**
     * 设置重新加载页面错误描述
     */
    public void setRetryLoadingTitle(String msg) {
        tv_retry_msg.setText(msg);
    }

    /**
     * 关闭加载页面
     */
    public void closeLoading() {
        this.setVisibility(View.GONE);
        progress_wheel.stopSpinning();
    }

}

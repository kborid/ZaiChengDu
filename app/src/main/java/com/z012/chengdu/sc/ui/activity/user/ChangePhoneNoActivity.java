package com.z012.chengdu.sc.ui.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.ui.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 更改手机号
 *
 * @author kborid
 */
public class ChangePhoneNoActivity extends BaseActivity {

    @BindView(R.id.tv_phone)
    TextView tv_phone;

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_change_phone_number_act;
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("更改手机号");
    }

    @Override
    protected void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle && null != bundle.getString("num")) {
            tv_phone.setText(bundle.getString("num"));
        }
    }

    @OnClick(R.id.btn_sbmit)
    void submit() {
        startActivity(new Intent(ChangePhoneNoActivity.this, ChangePhoneNoBindActivity.class));
    }
}

package com.z012.chengdu.sc.ui.widget.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.prj.sdk.util.SharedPreferenceUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.ui.activity.user.ForgetPwdActivity;

public class PasswordLoginLayout extends LoginLayout {

    private EditText et_phone, et_pwd;
    private CheckBox cb_phone, cb_pwd;
    private View v_phone, v_pwd;
    private TextView tv_forget_pwd;

    public PasswordLoginLayout(Context context) {
        this(context, null);
    }

    public PasswordLoginLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordLoginLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_login_pwd, this);
        initView();
        initListeners();
    }

    protected void initView() {
        super.initView();
        et_phone = (EditText) findViewById(R.id.et_phone);
        cb_phone = (CheckBox) findViewById(R.id.cb_phone);
        v_phone = findViewById(R.id.v_phone);

        et_pwd = (EditText) findViewById(R.id.et_pwd);
        cb_pwd = (CheckBox) findViewById(R.id.cb_pwd);
        v_pwd = findViewById(R.id.v_pwd);
        tv_forget_pwd = (TextView) findViewById(R.id.tv_forget_pwd);

        String name = SharedPreferenceUtil.getInstance().getString(AppConst.USERNAME, "", true);
        if (!TextUtils.isEmpty(name)) {
            et_phone.setText(name);// 设置默认用户名
        }
    }

    protected void initListeners() {
        super.initListener();

        tv_forget_pwd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent();
                intent2.setClass(context, ForgetPwdActivity.class);
                context.startActivity(intent2);
            }
        });

        et_phone.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    cb_phone.setVisibility(View.VISIBLE);
                    v_phone.setBackgroundColor(getResources().getColor(R.color.mainColor));
                } else {
                    cb_phone.setVisibility(View.GONE);
                    v_phone.setBackgroundColor(Color.parseColor("#DDDDDD"));
                }
            }
        });

        cb_phone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                et_phone.setText("");
            }
        });

        et_pwd.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    cb_pwd.setVisibility(View.VISIBLE);
                    v_pwd.setBackgroundColor(getResources().getColor(R.color.mainColor));
                } else {
                    cb_pwd.setVisibility(View.GONE);
                    v_pwd.setBackgroundColor(Color.parseColor("#DDDDDD"));
                }
            }
        });

        cb_pwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 设置为明文显示
                    et_pwd.setTransformationMethod(HideReturnsTransformationMethod
                            .getInstance());
                } else {
                    // 设置为密文显示
                    et_pwd.setTransformationMethod(PasswordTransformationMethod
                            .getInstance());
                }
                et_pwd.setSelection(et_pwd.getText().length());// 设置光标位置
            }
        });
    }

    @Override
    public String getPhone() {
        return et_phone.getText().toString();
    }


    @Override
    public String getPassword() {
        return et_pwd.getText().toString();
    }

    @Override
    public void destroy() {
        super.destroy();

    }
}

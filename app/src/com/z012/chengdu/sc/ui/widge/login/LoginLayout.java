package com.z012.chengdu.sc.ui.widge.login;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prj.sdk.util.LogUtil;
import com.z012.chengdu.sc.R;

public abstract class LoginLayout extends LinearLayout {

    protected Context context;
    protected Button btn_login;
    protected TextView tv_change;

    public LoginLayout(Context context) {
        this(context, null);
    }

    public LoginLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoginLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    protected void initView() {
        btn_login = (Button) findViewById(R.id.btn_login);
        tv_change = (TextView) findViewById(R.id.tv_change);
    }

    protected void initListener() {
        btn_login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i("dw", "login button click");
                if (null != loginListener) {
                    loginListener.onLogin();
                }
            }
        });

        tv_change.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i("dw", "change tv click");
                if (null != loginListener) {
                    loginListener.onChange();
                }
            }
        });
    }

    private ILoginListener loginListener = null;

    public void setLoginListener(ILoginListener listener) {
        this.loginListener = listener;
    }

    protected IProcessListener processListener = null;

    public void setProcessListener(IProcessListener listener) {
        this.processListener = listener;
    }

    public abstract String getPhone();
    public abstract String getPassword();

    public void destroy() {
        if (null != loginListener) {
            loginListener = null;
        }

        if (null != processListener) {
            processListener = null;
        }
    }
}

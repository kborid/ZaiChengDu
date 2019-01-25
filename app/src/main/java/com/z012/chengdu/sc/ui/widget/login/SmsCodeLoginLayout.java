package com.z012.chengdu.sc.ui.widget.login;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.ToastUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.impl.CountDownTimerImpl;
import com.z012.chengdu.sc.net.request.RequestBeanBuilder;

import java.net.ConnectException;

public class SmsCodeLoginLayout extends LoginLayout implements DataCallback {

    private EditText et_phone, et_yzm;
    private CheckBox cb_phone, cb_yzm;
    private View v_phone, v_yzm;
    private TextView tv_yzm;

    private CountDownTimerImpl countDownTimer;

    public SmsCodeLoginLayout(Context context) {
        this(context, null);
    }

    public SmsCodeLoginLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmsCodeLoginLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_login_sms, this);
        initView();
        initListeners();
    }

    protected void initView() {
        super.initView();
        et_phone = (EditText) findViewById(R.id.et_phone);
        cb_phone = (CheckBox) findViewById(R.id.cb_phone);
        v_phone = findViewById(R.id.v_phone);

        et_yzm = (EditText) findViewById(R.id.et_yzm);
        cb_yzm = (CheckBox) findViewById(R.id.cb_yzm);
        v_yzm = findViewById(R.id.v_yzm);
        tv_yzm = (TextView) findViewById(R.id.tv_yzm);

        String name = SharedPreferenceUtil.getInstance().getString(AppConst.USERNAME, "", true);
        if (!TextUtils.isEmpty(name)) {
            et_phone.setText(name);// 设置默认用户名
        }
    }

    protected void initListeners() {
        super.initListener();

        tv_yzm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                requestYzm();
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

        et_yzm.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    cb_yzm.setVisibility(View.VISIBLE);
                    v_yzm.setBackgroundColor(getResources().getColor(R.color.mainColor));
                } else {
                    cb_yzm.setVisibility(View.GONE);
                    v_yzm.setBackgroundColor(Color.parseColor("#DDDDDD"));
                }
            }
        });

        cb_yzm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                et_yzm.setText("");
            }
        });
    }

    private void requestYzm() {
        RequestBeanBuilder b = RequestBeanBuilder.create(false);
        b.addBody("BUSINESSTYPE", "40659");
        b.addBody("MOBILENUM", getPhone());

        ResponseData data = b.syncRequest(b);
        data.path = NetURL.GET_YZM;
        data.flag = 1;

        if (null != processListener && !processListener.isProgressShowing()) {
            processListener.showProgressDialog("", true);
        }

        DataLoader.getInstance().loadData(this, data);
    }

    @Override
    public String getPhone() {
        return et_phone.getText().toString();
    }


    @Override
    public String getPassword() {
        return et_yzm.getText().toString();
    }

    private CountDownTimerImpl.CountDownTimerListener countDownTimerListener = new CountDownTimerImpl.CountDownTimerListener() {
        @Override
        public void onTick(long time) {
            tv_yzm.setText(String.format("%1$ss后重试", time / CountDownTimerImpl.SEC));
        }

        @Override
        public void onFinish() {
            tv_yzm.setEnabled(true);
            tv_yzm.setText("获取验证码");
        }
    };

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (request.flag == 1) {
            if (null != processListener) {
                processListener.removeProgressDialog();
            }
            ToastUtil.show("验证码已发送，请稍候...", Toast.LENGTH_LONG);
            tv_yzm.setEnabled(false);
            countDownTimer = new CountDownTimerImpl(60, countDownTimerListener);
            countDownTimer.start();// 启动倒计时
        }
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        if (null != processListener) {
            processListener.removeProgressDialog();
        }

        String message;
        if (e instanceof ConnectException) {
            message = context.getResources().getString(R.string.dialog_tip_net_error);
        } else {
            message = response != null && response.data != null ? response.data.toString() : context.getResources().getString(R.string.dialog_tip_null_error);
        }
        ToastUtil.show(message, Toast.LENGTH_LONG);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (null != countDownTimer) {
            countDownTimer.stop();
            countDownTimer = null;
            tv_yzm.setEnabled(true);
            tv_yzm.setText("获取验证码");
        }
    }
}

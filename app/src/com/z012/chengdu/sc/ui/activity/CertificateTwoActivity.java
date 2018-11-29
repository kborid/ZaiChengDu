package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.ui.base.BaseActivity;

import java.net.ConnectException;

/**
 * 实名认证
 * 
 * @author kborid
 * 
 */
public class CertificateTwoActivity extends BaseActivity implements DataCallback {

	private EditText et_yzm;
	private TextView tv_phone, tv_yzm;
	private Button btn_next;

	private String mPhone = null;
    private CountDownTimer mCountDownTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_certificate_two);

		initViews();
		initParams();
		initListeners();
	}

	@Override
	public void initViews() {
		super.initViews();
		tv_center_title.setText("实名认证");
		tv_right_title.setVisibility(View.GONE);

		et_yzm = (EditText) findViewById(R.id.et_yzm);
		tv_phone = (TextView) findViewById(R.id.tv_phone);
		tv_yzm = (TextView) findViewById(R.id.tv_yzm);
		btn_next = (Button) findViewById(R.id.btn_next);
	}

	@Override
	public void initParams() {
		super.initParams();
        setCountDownTimer(60 * 1000, 1000);
		btn_next.setEnabled(false);
		Bundle bundle = getIntent().getExtras();
		if (null != bundle) {
		    mPhone = bundle.getString("phone");
            tv_phone.setText(Utils.convertHiddenPhoneStars(mPhone, 3, 8));
        }
	}

    private void setCountDownTimer(long millisInFuture, long countDownInterval) {
        mCountDownTimer = new CountDownTimer(millisInFuture, countDownInterval) {

            @Override
            public void onTick(long millisUntilFinished) {
                tv_yzm.setText((millisUntilFinished / 1000) + "s后重试");
            }

            @Override
            public void onFinish() {
                tv_yzm.setEnabled(true);
                tv_yzm.setText("获取验证码");
            }
        };
    }

    @Override
	public void initListeners() {
		super.initListeners();
		tv_yzm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadYZM();
            }
        });

		btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
                Intent intent = new Intent(CertificateTwoActivity.this, CertificateThreeActivity.class);
                startActivity(intent);
            }
        });

		et_yzm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_next.setEnabled(!TextUtils.isEmpty(s));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
	}

    private void loadYZM() {
        RequestBeanBuilder builder = RequestBeanBuilder.create(false);
        builder.addBody("BUSINESSTYPE", "40658");// 业务类型01手机绑定；02邮箱绑定；03注册绑定手机；04找回密码；05市民卡注册绑定；06市民卡实名认证
        builder.addBody("MOBILENUM", mPhone);

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.GET_YZM;
        data.flag = 1;

        requestID = DataLoader.getInstance().loadData(this, data);
    }

    private void loadData() {

    }

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (request.flag == 1) {
            removeProgressDialog();
            CustomToast.show("验证码已发送，请稍候...", Toast.LENGTH_LONG);
            tv_yzm.setEnabled(false);
            mCountDownTimer.start();// 启动倒计时
        } else if (request.flag == 2) {

        }
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
		CustomToast.show(message, Toast.LENGTH_LONG);

	}
}

package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.impl.CountDownTimerImpl;
import com.z012.chengdu.sc.ui.base.BaseActivity;

/**
 * 实名认证
 * 
 * @author kborid
 * 
 */
public class CertificateThreeActivity extends BaseActivity {

    private ImageView iv_icon;
	private TextView tv_ret, tv_tipsTime;
	private Button btn_next;

	private boolean mAuth = false;
	private CountDownTimerImpl countDownTimer;

	@Override
	protected int getLayoutResId() {
		return R.layout.ui_certificate_three;
	}

	@Override
	public void dealIntent() {
		super.dealIntent();
		Bundle bundle = getIntent().getExtras();
		if (null != bundle) {
		    mAuth = bundle.getBoolean("isAuth");
        }
	}

	@Override
	public void initParams() {
		super.initParams();
		tv_center_title.setText("实名认证");
		tv_right_title.setVisibility(View.GONE);
		tv_left_title.setVisibility(View.GONE);

		iv_icon = (ImageView) findViewById(R.id.iv_icon);
		tv_ret = (TextView) findViewById(R.id.tv_ret);
		tv_tipsTime = (TextView) findViewById(R.id.tv_tipsTime);
		btn_next = (Button) findViewById(R.id.btn_next);

		if (mAuth) {
		    iv_icon.setImageResource(R.drawable.iv_cert_succ);
            tv_ret.setText("认证成功");
            countDownTimer = new CountDownTimerImpl(5, new CountDownTimerImpl.CountDownTimerListener() {
                @Override
                public void onTick(long time) {
                    tv_tipsTime.setText(String.format("银行卡认证已经通过(%1$ss)", time / CountDownTimerImpl.SEC));
                }

                @Override
                public void onFinish() {
                    btn_next.performClick();
                }
            });

            countDownTimer.start();
        } else {
		    iv_icon.setImageResource(R.drawable.iv_cert_fail);
            tv_ret.setText("认证失败");
            tv_tipsTime.setText("银行卡认证未通过");
        }
	}

	@Override
	public void initListeners() {
		super.initListeners();
		btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth) {
                    Intent intent = new Intent(CertificateThreeActivity.this, MainFragmentActivity.class);
                    intent.putExtra("certRet", "认证成功");
                    startActivity(intent);
                } else {
                    finish();
                }
            }
        });
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != countDownTimer) {
            countDownTimer.stop();
            countDownTimer = null;
        }
    }
}

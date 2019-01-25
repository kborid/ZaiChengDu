package com.z012.chengdu.sc.ui.activity.certificate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.prj.sdk.util.UIHandler;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.event.CertResultEvent;
import com.z012.chengdu.sc.impl.CountDownTimerImpl;
import com.z012.chengdu.sc.ui.BaseActivity;
import com.z012.chengdu.sc.ui.activity.MainFragmentActivity;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 实名认证
 *
 * @author kborid
 */
public class CertificateThreeActivity extends BaseActivity {

    @BindView(R.id.iv_icon)
    ImageView iv_icon;
    @BindView(R.id.tv_ret)
    TextView tv_ret;
    @BindView(R.id.tv_tipsTime)
    TextView tv_tipsTime;
    @BindView(R.id.btn_next)
    Button btn_next;

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
        tv_left_title.setVisibility(View.GONE);
        tv_center_title.setText("实名认证");

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

    @OnClick(R.id.btn_next)
    void nextClick() {
        if (mAuth) {
            startActivity(new Intent(CertificateThreeActivity.this, MainFragmentActivity.class));
            UIHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    EventBus.getDefault().post(new CertResultEvent("认证成功"));
                }
            }, 300);
        } else {
            finish();
        }
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

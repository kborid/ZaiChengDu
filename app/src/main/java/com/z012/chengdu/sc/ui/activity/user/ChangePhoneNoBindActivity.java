package com.z012.chengdu.sc.ui.activity.user;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ToastUtil;
import com.prj.sdk.util.Utils;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.request.RequestBeanBuilder;
import com.z012.chengdu.sc.ui.BaseActivity;

import java.net.ConnectException;

/**
 * 修改手机号码绑定
 *
 * @author LiaoBo
 */
public class ChangePhoneNoBindActivity extends BaseActivity implements DataCallback, DialogInterface.OnCancelListener {
    private EditText et_yzm, et_phone_new;
    private Button btn_sbmit, btn_getYZM;
    private String mPhoneNum;
    private CountDownTimer mCountDownTimer;

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_change_phone_no_bind_act;
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("更改手机号码");
        et_yzm = (EditText) findViewById(R.id.et_yzm);
        btn_sbmit = (Button) findViewById(R.id.btn_sbmit);

        et_phone_new = (EditText) findViewById(R.id.et_phone_new);
        btn_getYZM = (Button) findViewById(R.id.btn_getYZM);

        setCountDownTimer(60 * 1000, 1000);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        btn_sbmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitData();
            }
        });

        btn_getYZM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhoneNum = et_phone_new.getText().toString().trim();
                if (StringUtil.notEmpty(mPhoneNum)) {
                    if (Utils.isMobile(mPhoneNum)) {
                        loadYZM();
                    } else {
                        ToastUtil.show("请输入正确的手机号", 0);
                    }
                } else {
                    ToastUtil.show("请输入手机号", 0);
                }
            }
        });
    }

    /**
     * 加载验证码数据
     */
    private void loadYZM() {
        RequestBeanBuilder builder = RequestBeanBuilder.create(false);
        builder.addBody("BUSINESSTYPE", "40656");// 业务类型01  40656手机绑定；02邮箱绑定；03注册绑定手机；04找回密码；05市民卡注册绑定；06市民卡实名认证
        builder.addBody("MOBILENUM", mPhoneNum);

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.GET_YZM;
        data.flag = 1;

        if (!isProgressShowing()) {
            showProgressDialog(getString(R.string.loading), true);
        }
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    /**
     * 提交数据
     */
    private void submitData() {
        String code = et_yzm.getText().toString().trim();
        mPhoneNum = et_phone_new.getText().toString().trim();
        if (StringUtil.empty(mPhoneNum)) {
            ToastUtil.show("请输入手机号", 0);
            return;
        }
        if (!Utils.isMobile(mPhoneNum)) {
            ToastUtil.show("请输入正确的手机号", 0);
            return;
        }
        if (StringUtil.empty(code)) {
            ToastUtil.show("请输入验证码", 0);
            return;
        }
        RequestBeanBuilder builder = RequestBeanBuilder.create(true);
        builder.addBody("MOBILENUM", mPhoneNum);
        builder.addBody("CODE", code);

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.UPDATA_PHONE;
        data.flag = 2;

        if (!isProgressShowing()) {
            showProgressDialog("正在加载，请稍候...", true);
        }
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        removeProgressDialog();
        if (request.flag == 1) {
            ToastUtil.show("验证码已发送，请稍后...", 0);
            btn_getYZM.setEnabled(false);
            mCountDownTimer.start();// 启动倒计时
        } else if (request.flag == 2) {
            ToastUtil.show("修改成功", 0);
            SessionContext.mUser.USERAUTH.mobilenum = mPhoneNum;
            Intent mIntent = new Intent();
            mIntent.setClass(this, AccountSecurityActivity.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mIntent);
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
        ToastUtil.show(message, Toast.LENGTH_LONG);

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        DataLoader.getInstance().clear(requestID);
        removeProgressDialog();
    }

    /**
     * 设置倒计时
     *
     * @param millisInFuture
     * @param countDownInterval
     */
    private void setCountDownTimer(long millisInFuture, long countDownInterval) {
        mCountDownTimer = new CountDownTimer(millisInFuture, countDownInterval) {

            @Override
            public void onTick(long millisUntilFinished) {
                btn_getYZM.setText((millisUntilFinished / 1000) + " s");
            }

            @Override
            public void onFinish() {
                btn_getYZM.setEnabled(true);
                btn_getYZM.setText("获取验证码");
            }
        };
    }
}

package com.z012.chengdu.sc.ui.activity.user;

import android.content.DialogInterface;
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
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.encrypt.SHA1;
import com.z012.chengdu.sc.net.request.RequestBeanBuilder;
import com.z012.chengdu.sc.ui.BaseActivity;

import java.net.ConnectException;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 忘记密码
 *
 * @author LiaoBo
 */
public class ForgetPwdActivity extends BaseActivity implements DataCallback, DialogInterface.OnCancelListener {

    @BindView(R.id.et_yzm)
    EditText et_yzm;
    @BindView(R.id.et_phone)
    EditText et_phone;
    @BindView(R.id.et_password)
    EditText et_password;
    @BindView(R.id.et_password2)
    EditText et_password2;
    @BindView(R.id.btn_getYZM)
    Button btn_getYZM;

    private String mPhoneNum;
    private CountDownTimer mCountDownTimer;

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_forget_pwd_act;
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("找回密码");
        setCountDownTimer(60 * 1000, 1000);
    }

    @OnClick(R.id.btn_reset)
    void reset() {
        loadData();
    }

    @OnClick(R.id.btn_getYZM)
    void getYzm() {
        mPhoneNum = et_phone.getText().toString().trim();
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

    /**
     * 加载验证码
     */
    private void loadYZM() {
        RequestBeanBuilder builder = RequestBeanBuilder.create(false);
        builder.addBody("BUSINESSTYPE", "40659")// // 业务类型01手机绑定；02邮箱绑定；03注册绑定手机；04  40659找回密码；05市民卡注册绑定；06市民卡实名认证
                .addBody("MOBILENUM", mPhoneNum);

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.GET_YZM;
        data.flag = 1;

        if (!isProgressShowing()) {
            showProgressDialog("正在加载，请稍候...", true);
        }
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    /**
     * 加载数据
     */
    private void loadData() {
        String MOBILENUM = et_phone.getText().toString().trim();
        String CODE = et_yzm.getText().toString().trim();
        String PASSWORD = et_password.getText().toString().trim();
        String PASSWORD2 = et_password2.getText().toString().trim();
        if (StringUtil.empty(MOBILENUM)) {
            ToastUtil.show("请输入手机号码", 0);
            return;
        }
        if (!Utils.isMobile(MOBILENUM)) {
            ToastUtil.show("请输入正确的手机号码", 0);
            return;
        }
        if (StringUtil.empty(CODE)) {
            ToastUtil.show("请输入验证码", 0);
            return;
        }
        if (StringUtil.empty(PASSWORD)) {
            ToastUtil.show("请输入密码", 0);
            return;
        }
        if (PASSWORD.length() < 6) {
            ToastUtil.show("请输入6-20个字符的密码", 0);
            return;
        }
        if (!PASSWORD.equals(PASSWORD2)) {
            ToastUtil.show("两次密码不一致", 0);
            return;
        }
        // {"accessTicket":""},"body":{"LOGIN":"13568838578","CODEKEY":"13568838578","PASSWORD":"7c4a8d09ca3762af61e59520943dc26494f8941b"}}
        RequestBeanBuilder builder = RequestBeanBuilder.create(false);
        builder.addBody("PWDSTRENGTH", "1");
        builder.addBody("FINDTYPE", "mobile");
        builder.addBody("CODE", CODE);
        builder.addBody("LOGIN", MOBILENUM);
        SHA1 sha1 = new SHA1();
        builder.addBody("PASSWORD", sha1.getDigestOfString(PASSWORD.getBytes()));

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.FORGET_PWD;
        data.flag = 2;

        if (!isProgressShowing()) {
            showProgressDialog("正在提交，请稍候...", true);
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
        } else {
            ToastUtil.show("密码已修改", 0);
            this.finish();
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

    @Override
    public void onCancel(DialogInterface dialog) {
        DataLoader.getInstance().clear(requestID);
        removeProgressDialog();
    }

}

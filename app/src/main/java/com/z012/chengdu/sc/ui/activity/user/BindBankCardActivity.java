package com.z012.chengdu.sc.ui.activity.user;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ToastUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.request.RequestBeanBuilder;
import com.z012.chengdu.sc.ui.BaseActivity;

import java.net.ConnectException;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 绑定银行卡或支护宝
 *
 * @author kborid
 */
public class BindBankCardActivity extends BaseActivity implements DataCallback {

    @BindView(R.id.tv_choice)
    TextView tv_choice;
    @BindView(R.id.tv_tip)
    TextView tv_tip;
    @BindView(R.id.et_card_number)
    EditText et_card_number;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.et_pay_number)
    EditText et_pay_number;
    @BindView(R.id.et_pay_name)
    EditText et_pay_name;
    @BindView(R.id.tl_band_card)
    TableLayout tl_band_card;
    @BindView(R.id.tl_alipay)
    TableLayout tl_alipay;

    private boolean isBindAlipay = true;    // 默认选择绑定支护宝
    private int mCardType;                    // 银行卡类型0：银行卡1：支护宝

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_bind_bank_card_act;
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("绑定账户");
    }

    @OnClick(R.id.tr_choice)
    void choice() {
        showChoiceDialog();
    }

    @OnClick(R.id.btn_complete)
    void complete() {
        completet();
    }

    /**
     * 完成后验证并提交数据
     */
    public void completet() {
        if (!SessionContext.isLogin()) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
            return;
        }
        String pay_number, pay_name, type;
        if (isBindAlipay) {
            pay_number = et_pay_number.getText().toString().trim();
            pay_name = et_pay_name.getText().toString().trim();
            if (StringUtil.empty(pay_number)) {
                ToastUtil.show("请输入支付宝账号", 0);
                return;
            }
            if (StringUtil.empty(pay_name)) {
                ToastUtil.show("请输入姓名", 0);
                return;
            }
            type = "000";
        } else {
            pay_number = et_card_number.getText().toString().trim();
            pay_name = et_name.getText().toString().trim();
            if (StringUtil.empty(pay_number)) {
                ToastUtil.show("请输入银行卡号", 0);
                return;
            }
            if (StringUtil.empty(pay_name)) {
                ToastUtil.show("请输入开户人姓名", 0);
                return;
            }
            type = "002";
        }

        RequestBeanBuilder builder = RequestBeanBuilder.create(true);
        builder.addBody("userid", SessionContext.mUser.USERBASIC.id);//
        builder.addBody("bindtype", "001");// 000:商户号；001: 普通账户
        builder.addBody("thirdaccount", pay_number);// thirdaccount第三方账户ID
        builder.addBody("realname", pay_name);// realname第三方账户真实姓名
        builder.addBody("thirdtype", type);// （第三方平台 000-支付宝 001-微信 622588-招商银行等银行卡前缀）
        builder.addBody("operatetype", "0");// operatetype（0绑定、1解绑）

        ResponseData requster = builder.syncRequest(builder);
        requster.flag = 1;
        requster.path = NetURL.BIND_BANK;

        if (!isProgressShowing()) {
            showProgressDialog(getString(R.string.present), true);
        }
        requestID = DataLoader.getInstance().loadData(this, requster);
    }

    /**
     * 显示选择绑定内容对话框
     */
    public void showChoiceDialog() {
        Builder builder = new android.app.AlertDialog.Builder(this);
        // 设置对话框的标题
        builder.setTitle("选择绑定内容");
        final String[] str = new String[]{"支付宝", "银行卡"};
        builder.setSingleChoiceItems(str, mCardType, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                tv_choice.setText(str[which]);
                if (which == 0) {
                    tl_band_card.setVisibility(View.GONE);
                    tv_tip.setVisibility(View.GONE);
                    tl_alipay.setVisibility(View.VISIBLE);
                    isBindAlipay = true;
                    mCardType = 0;
                } else {
                    tl_band_card.setVisibility(View.VISIBLE);
                    tv_tip.setVisibility(View.VISIBLE);
                    tl_alipay.setVisibility(View.GONE);
                    isBindAlipay = false;
                    mCardType = 1;
                }
                dialog.dismiss();
            }
        });

        // builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
        // public void onClick(DialogInterface dialog, int which) {
        // dialog.dismiss();
        // }
        // });
        builder.create().show();
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        removeProgressDialog();
        ToastUtil.show("添加成功", 0);
        this.setResult(RESULT_OK, null);
        this.finish();
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
}

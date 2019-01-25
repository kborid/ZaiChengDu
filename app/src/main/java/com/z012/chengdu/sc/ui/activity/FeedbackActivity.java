package com.z012.chengdu.sc.ui.activity;

import android.widget.EditText;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ToastUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.net.ApiManager;
import com.z012.chengdu.sc.net.exception.ServerException;
import com.z012.chengdu.sc.net.observe.ObserverImpl;
import com.z012.chengdu.sc.net.request.RequestBuilder;
import com.z012.chengdu.sc.ui.BaseActivity;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 意见反馈
 *
 * @author kborid
 */
public class FeedbackActivity extends BaseActivity {
    private static final String TAG = FeedbackActivity.class.getSimpleName();

    @BindView(R.id.et_content)
    EditText et_content;

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_feedback_act;
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("意见反馈");
    }

    @OnClick(R.id.btn_sbmit)
    void submit() {
        if (StringUtil.notEmpty(et_content.getText().toString().trim())) {
            if (StringUtil.containsEmoji(et_content.getText().toString())) {
                ToastUtil.show("不支持输入Emoji表情符号", 0);
                return;
            }
            submitRequest();
        } else {
            ToastUtil.show("内容不允许为空", 0);
        }
    }

    private void submitRequest() {
        if (!isProgressShowing()) {
            showProgressDialog(getString(R.string.present), true);
        }

        HashMap<String, Object> params = RequestBuilder.create(false)
                .addBody("content", et_content.getText().toString().trim()).addBody("login", StringUtil.doEmpty(SessionContext.mUser.USERAUTH.login))
                .build();
        ApiManager.getFeedBack(params, new ObserverImpl<Object>() {
            @Override
            public void onNext(Object o) {
                super.onNext(o);
                Logger.t(TAG).d("onNext() " + o.toString());
                removeProgressDialog();
                ToastUtil.show("提交成功", Toast.LENGTH_SHORT);
                finish();
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                Logger.t(TAG).d("onError()");
                removeProgressDialog();
                if (e instanceof ServerException) {
                    ToastUtil.show(getString(R.string.dialog_tip_null_error), Toast.LENGTH_SHORT);
                } else {
                    ToastUtil.show(getString(R.string.dialog_tip_net_error), Toast.LENGTH_SHORT);
                }
            }
        });
    }
}

package com.z012.chengdu.sc.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ToastUtil;
import com.prj.sdk.util.Utils;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.entity.WebInfoEntity;
import com.z012.chengdu.sc.helper.ForbidFastClickHelper;
import com.z012.chengdu.sc.net.ApiErrorDef;
import com.z012.chengdu.sc.net.ApiManager;
import com.z012.chengdu.sc.net.entity.CertUserAuth;
import com.z012.chengdu.sc.net.observe.ObserverImpl;
import com.z012.chengdu.sc.net.request.RequestBuilder;
import com.z012.chengdu.sc.net.response.ResponseComm;
import com.z012.chengdu.sc.ui.BaseFragment;
import com.z012.chengdu.sc.ui.activity.AboutActivity;
import com.z012.chengdu.sc.ui.activity.WebViewActivity;
import com.z012.chengdu.sc.ui.activity.address.AddressManageActivity;
import com.z012.chengdu.sc.ui.activity.user.AccountSecurityActivity;
import com.z012.chengdu.sc.ui.activity.user.InviteActivity;
import com.z012.chengdu.sc.ui.activity.user.PersonalDataActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 用户
 *
 * @author kborid
 */
public class TabUserFragment extends BaseFragment {

    private static final String TAG = TabUserFragment.class.getSimpleName();
    private static final int REQ_EXIT = 0x1000;

    @BindView(R.id.userHeader_lay)
    LinearLayout userHeader_lay;
    @BindView(R.id.iv_photo)
    ImageView iv_photo;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_login)
    TextView tv_login;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_tab_user;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateCertInfo(CertUserAuth certUserAuth) {
        LogUtil.i(TAG, "updateCertInfo()");
        if (SessionContext.isLogin()) {
            boolean isAuth = null != certUserAuth && certUserAuth.isAuth && null != certUserAuth.userAuth;
            if (isAuth) {
                tv_name.setText(SessionContext.mCertUserAuth.userAuth.name);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(AppContext.mMainContext).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void initParams() {
        super.initParams();
        if (SessionContext.isLogin()) {// 登录状态下验证票据是否失效，默认登录后6天在做检查
            String lastLoginTime = SharedPreferenceUtil.getInstance().getString(AppConst.LAST_LOGIN_DATE, "", false);
            if (!TextUtils.isEmpty(lastLoginTime)) {
                Date lastLoginDate = DateUtil.str2Date(lastLoginTime);
                if (DateUtil.getGapCount(lastLoginDate, new Date(System.currentTimeMillis())) >= 6) {
                    loadValidateTicketExpire();
                }
            } else {// 如果没有值，则不是4.0.0版本，需要登录
                Intent intent = new Intent(AppConst.ACTION_UNLOGIN);
                intent.putExtra("needLogin", true);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);// 发送登录广播
            }
        }

        //更新用户信息
        updateDynamicUserInfo();
        // 注册刷新广播
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(AppConst.ACTION_DYNAMIC_USER_INFO);
        LocalBroadcastManager.getInstance(AppContext.mMainContext).registerReceiver(mBroadcastReceiver, mIntentFilter);

        LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) userHeader_lay.getLayoutParams();
        llp.width = Utils.mScreenWidth;
        llp.height = (int) ((float) llp.width / 375 * 200);
        userHeader_lay.setLayoutParams(llp);
    }

    /**
     * 加载验证票据是否失效
     */
    public void loadValidateTicketExpire() {
        LogUtil.d(TAG, "loadValidateTicketExpire()");
        ApiManager.getTicketValid(RequestBuilder.create(true).build(), new ObserverImpl<ResponseComm<Object>>() {
            @Override
            public void onNext(ResponseComm<Object> o) {
                super.onNext(o);
                LogUtil.d(TAG, "onNext()");
                LogUtil.d(TAG, o.bodyToString());
                if (null != o.getHead()) {
                    String code = o.getHead().getRtnCode();
                    if (TextUtils.isEmpty(code)
                            || ApiErrorDef.TICKET_ERROR.equals(code)
                            || ApiErrorDef.TICKET_VALID.equals(code)) {
                        ToastUtil.show("登录超时，请重新登录！", Toast.LENGTH_SHORT);
                        updateDynamicUserInfo();
                    }
                }
            }
        });
    }

    /**
     * 更新用户信息
     */
    public void updateDynamicUserInfo() {
        try {
            if (SessionContext.isLogin()) {
                if ("01".equals(SessionContext.mUser.USERBASIC.sex)) {
                    iv_photo.setImageResource(R.drawable.iv_def_photo_logined_male);
                } else if ("02".equals(SessionContext.mUser.USERBASIC.sex)) {
                    iv_photo.setImageResource(R.drawable.iv_def_photo_logined_female);
                } else {
                    iv_photo.setImageResource(R.drawable.iv_def_photo);
                }

                tv_name.setText(StringUtil.doEmpty(
                        SessionContext.mUser.USERBASIC.nickname,
                        SessionContext.mUser.USERBASIC.username));
                tv_login.setVisibility(View.GONE);

                String url = SessionContext.mUser.USERBASIC.getHeadphotourl();

                if (!TextUtils.isEmpty(url)) {
                    if (!url.startsWith("http")) {
                        url = NetURL.API_LINK + url;
                    }
                }
                Glide.with(this).load(url).crossFade().into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        if (null != resource) {
                            iv_photo.setImageDrawable(resource);
                        }
                    }
                });
            } else {
                iv_photo.setImageResource(R.drawable.iv_def_photo);
                tv_name.setText("重庆欢迎您");
                tv_login.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.iv_photo)
    void photo() {
        if (ForbidFastClickHelper.isForbidFastClick()) {
            return;
        }

        if (!SessionContext.isLogin()) {
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
            updateDynamicUserInfo();
            return;
        }
        Intent intent = new Intent(getActivity(), PersonalDataActivity.class);
        startActivityForResult(intent, REQ_EXIT);
    }

    @OnClick(R.id.tv_login)
    void login() {
        if (ForbidFastClickHelper.isForbidFastClick()) {
            return;
        }

        if (!SessionContext.isLogin()) {
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
            updateDynamicUserInfo();
        }
    }

    @OnClick(R.id.tv_account)
    void account() {
        if (ForbidFastClickHelper.isForbidFastClick()) {
            return;
        }

        if (!SessionContext.isLogin()) {
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
            updateDynamicUserInfo();
            return;
        }
        Intent intent = new Intent(getActivity(), AccountSecurityActivity.class);
        intent.putExtra("Tag", true);
        startActivityForResult(intent, REQ_EXIT);
    }

    @OnClick(R.id.tv_address)
    void address() {
        if (ForbidFastClickHelper.isForbidFastClick()) {
            return;
        }

        if (!SessionContext.isLogin()) {
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
            updateDynamicUserInfo();
            return;
        }
        startActivity(new Intent(getActivity(), AddressManageActivity.class));
    }

    @OnClick(R.id.tv_invite)
    void invite() {
        if (ForbidFastClickHelper.isForbidFastClick()) {
            return;
        }

        if (!SessionContext.isLogin()) {
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(AppConst.ACTION_UNLOGIN));
            updateDynamicUserInfo();
            return;
        }
        startActivity(new Intent(getActivity(), InviteActivity.class));
    }

    @OnClick(R.id.tv_problem)
    void problem() {
        if (ForbidFastClickHelper.isForbidFastClick()) {
            return;
        }

        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        String url = SharedPreferenceUtil.getInstance().getString(AppConst.PROBLEM, "", true);
        intent.putExtra("webEntity", new WebInfoEntity("常见问题", url));
        startActivity(intent);
    }

    @OnClick(R.id.tv_about)
    void about() {
        if (ForbidFastClickHelper.isForbidFastClick()) {
            return;
        }

        startActivity(new Intent(getActivity(), AboutActivity.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG, "onActivityResult()");
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQ_EXIT) {
            updateDynamicUserInfo();
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.d(TAG, action);
            if (AppConst.ACTION_DYNAMIC_USER_INFO.equals(action)) {
                updateDynamicUserInfo();
            }
        }
    };
}
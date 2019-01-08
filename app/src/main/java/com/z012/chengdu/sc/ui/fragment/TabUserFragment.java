package com.z012.chengdu.sc.ui.fragment;

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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.constants.Const;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.net.RequestBeanBuilder;
import com.z012.chengdu.sc.PRJApplication;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.CertUserAuth;
import com.z012.chengdu.sc.tools.ClickUtils;
import com.z012.chengdu.sc.ui.activity.AboutActivity;
import com.z012.chengdu.sc.ui.activity.AccountSecurityActivity;
import com.z012.chengdu.sc.ui.activity.AddressManageActivity;
import com.z012.chengdu.sc.ui.activity.InviteActivity;
import com.z012.chengdu.sc.ui.activity.MainFragmentActivity;
import com.z012.chengdu.sc.ui.activity.PersonalDataActivity;
import com.z012.chengdu.sc.ui.activity.WebViewActivity;
import com.z012.chengdu.sc.ui.base.BaseFragment;

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
public class TabUserFragment extends BaseFragment implements DataCallback {

    @BindView(R.id.userHeader_lay) LinearLayout userHeader_lay;
    @BindView(R.id.iv_photo) ImageView iv_photo;
    @BindView(R.id.tv_name) TextView tv_name;
    @BindView(R.id.tv_login) TextView tv_login;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_tab_user;
    }

    @Override
    protected void onInit() {
        EventBus.getDefault().register(this);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void updateCertInfo(CertUserAuth certUserAuth) {
	    LogUtil.i("dw", "updateCertInfo()");
	    if (SessionContext.isLogin()) {
            boolean isAuth = null != certUserAuth && certUserAuth.isAuth && null != certUserAuth.userAuth;
            if (isAuth) {
                tv_name.setText(SessionContext.mCertUserAuth.userAuth.name);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
        LocalBroadcastManager.getInstance(PRJApplication.getInstance()).unregisterReceiver(mBroadcastReceiver);
    }

	@Override
	protected void initParams() {
		super.initParams();
        if (SessionContext.isLogin()) {// 登录状态下验证票据是否失效，默认登录后6天在做检查
            String lastLoginTime = SharedPreferenceUtil.getInstance()
                    .getString(AppConst.LAST_LOGIN_DATE, "", false);
            if (StringUtil.notEmpty(lastLoginTime)) {
                Date lastLoginDate = DateUtil.str2Date(lastLoginTime);
                if (DateUtil.getGapCount(lastLoginDate,
                        new Date(System.currentTimeMillis())) >= 6) {
                    loadValidateTicketExpire();
                }
            } else {// 如果没有值，则不是4.0.0版本，需要登录
                Intent intent = new Intent(Const.UNLOGIN_ACTION);
                intent.putExtra(Const.NEED_SHOW_UNLOGIN_DIALOG, true);
                AppContext.mMainContext.sendBroadcast(intent);// 发送登录广播
            }
        }

        //更新用户信息
        updateDynamicUserInfo();
        // 注册刷新广播
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(AppConst.ACTION_DYNAMIC_USER_INFO);
        LocalBroadcastManager.getInstance(PRJApplication.getInstance()).registerReceiver(mBroadcastReceiver, mIntentFilter);

        LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) userHeader_lay.getLayoutParams();
        llp.width = Utils.mScreenWidth;
        llp.height = (int) ((float) llp.width / 375 * 200);
        userHeader_lay.setLayoutParams(llp);
	}

    /**
     * 加载验证票据是否失效
     */
    public void loadValidateTicketExpire() {
        RequestBeanBuilder builder = RequestBeanBuilder.create(true);

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.VALIDATE_TICKET;
        data.flag = 1;
        DataLoader.getInstance().loadData(this, data);
    }

    /**
     * 更新用户信息
     */
    public void updateDynamicUserInfo() {
        try {
            if (SessionContext.isLogin()) {
                if ("01".equals(SessionContext.mUser.USERBASIC.sex)) {
                    iv_photo.setImageResource(R.drawable.iv_def_photo_logined_male);
                } else if ("02".equals(SessionContext.mUser.USERBASIC.sex)){
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

    @OnClick(R.id.iv_photo) void photo() {
        if (ClickUtils.isForbidFastClick()) {
            return;
        }

        if (!SessionContext.isLogin()) {
            getActivity().sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
            updateDynamicUserInfo();
            return;
        }
        Intent intent = new Intent(getActivity(), PersonalDataActivity.class);
        getActivity().startActivityForResult(intent, MainFragmentActivity.LOGIN_EXIT);
    }

    @OnClick(R.id.tv_login) void login() {
        if (ClickUtils.isForbidFastClick()) {
            return;
        }

        if (!SessionContext.isLogin()) {
            getActivity().sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
            updateDynamicUserInfo();
        }
    }

    @OnClick(R.id.tv_account) void account() {
        if (ClickUtils.isForbidFastClick()) {
            return;
        }

        if (!SessionContext.isLogin()) {
            getActivity().sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
            updateDynamicUserInfo();
            return;
        }
        Intent intent = new Intent(getActivity(), AccountSecurityActivity.class);
        intent.putExtra("Tag", true);
        getActivity().startActivityForResult(intent, MainFragmentActivity.LOGIN_EXIT);
    }

    @OnClick(R.id.tv_address) void address() {
        if (ClickUtils.isForbidFastClick()) {
            return;
        }

        if (!SessionContext.isLogin()) {
            getActivity().sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
            updateDynamicUserInfo();
            return;
        }
        startActivity(new Intent(getActivity(), AddressManageActivity.class));
    }

    @OnClick(R.id.tv_invite) void invite() {
        if (ClickUtils.isForbidFastClick()) {
            return;
        }

        if (!SessionContext.isLogin()) {
            getActivity().sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
            updateDynamicUserInfo();
            return;
        }
        startActivity(new Intent(getActivity(), InviteActivity.class));
    }

    @OnClick(R.id.tv_problem) void problem() {
        if (ClickUtils.isForbidFastClick()) {
            return;
        }

        Intent intent = new Intent(getActivity(), WebViewActivity.class);
        String url = SharedPreferenceUtil.getInstance().getString(AppConst.PROBLEM, "", true);
        intent.putExtra("path", url);
        intent.putExtra("title", "常见问题");
        startActivity(intent);
    }

    @OnClick(R.id.tv_about) void about() {
        if (ClickUtils.isForbidFastClick()) {
            return;
        }

        startActivity(new Intent(getActivity(), AboutActivity.class));
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.d("Broadcast action", action);
            if (AppConst.ACTION_DYNAMIC_USER_INFO.equals(action)) {
                updateDynamicUserInfo();
            }
        }
    };

    @Override
	public void preExecute(ResponseData request) {
	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
	}

	@Override
	public void notifyError(ResponseData request, ResponseData response, Exception e) {
        CustomToast.show("登录超时，请重新登录！", 0);
        updateDynamicUserInfo();
	}
}
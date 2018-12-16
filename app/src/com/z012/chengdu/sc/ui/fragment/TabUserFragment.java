package com.z012.chengdu.sc.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.PRJApplication;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.tools.ForbidFastClickUtils;
import com.z012.chengdu.sc.ui.activity.AboutActivity;
import com.z012.chengdu.sc.ui.activity.AccountSecurityActivity;
import com.z012.chengdu.sc.ui.activity.AddressManageActivity;
import com.z012.chengdu.sc.ui.activity.InviteActivity;
import com.z012.chengdu.sc.ui.activity.MainFragmentActivity;
import com.z012.chengdu.sc.ui.activity.PersonalDataActivity;
import com.z012.chengdu.sc.ui.activity.WebViewActivity;
import com.z012.chengdu.sc.ui.base.BaseFragment;

import java.util.Date;

/**
 * 用户
 *
 * @author kborid
 */
public class TabUserFragment extends BaseFragment implements DataCallback, View.OnClickListener {

    private LinearLayout userHeader_lay;
    private ImageView iv_photo;
    private TextView tv_name, tv_login, tv_userinfo, tv_account, tv_address, tv_invite, tv_problem, tv_about;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_tab_user, container, false);
        initViews(view);
        initParams();
        initListeners();
		return view;
	}

	protected void onInits() {
	}

	public void onVisible() {
		super.onVisible();
		if (SessionContext.isLogin()) {
            updateInfoForCert();
        }
	}

	private void updateInfoForCert() {
	    LogUtil.i("dw", "updateInfoForCert()");
        boolean isAuth = null != SessionContext.mCertUserAuth && SessionContext.mCertUserAuth.isAuth && null != SessionContext.mCertUserAuth.userAuth;
        if (isAuth) {
            tv_name.setText(SessionContext.mCertUserAuth.userAuth.name);
        }
    }

	@Override
	protected void initViews(View view) {
		super.initViews(view);
        userHeader_lay = (LinearLayout) view.findViewById(R.id.userHeader_lay);
        iv_photo = (ImageView) view.findViewById(R.id.iv_photo);
        tv_name = (TextView) view.findViewById(R.id.tv_name);
        tv_login = (TextView) view.findViewById(R.id.tv_login);
//        tv_userinfo = (TextView) view.findViewById(R.id.tv_userinfo);
        tv_account = (TextView) view.findViewById(R.id.tv_account);
        tv_address = (TextView) view.findViewById(R.id.tv_address);
        tv_invite = (TextView) view.findViewById(R.id.tv_invite);
        tv_problem = (TextView) view.findViewById(R.id.tv_problem);
        tv_about = (TextView) view.findViewById(R.id.tv_about);
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
                intent.putExtra(Const.IS_SHOW_TIP_DIALOG, true);
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
                if ("02".equals(SessionContext.mUser.USERBASIC.sex)) {
                    iv_photo.setImageResource(R.drawable.iv_def_photo_logined_female);
                } else {
                    iv_photo.setImageResource(R.drawable.iv_def_photo_logined_male);
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

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void initListeners() {
		super.initListeners();
        iv_photo.setOnClickListener(this);
		tv_login.setOnClickListener(this);
//        tv_userinfo.setOnClickListener(this);
        tv_account.setOnClickListener(this);
        tv_address.setOnClickListener(this);
        tv_invite.setOnClickListener(this);
        tv_problem.setOnClickListener(this);
        tv_about.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
        Intent mIntent = null;
		switch (v.getId()) {
            case R.id.tv_login:
                if (ForbidFastClickUtils.isFastClick()) {
                    return;
                }

                if (!SessionContext.isLogin()) {
                    getActivity().sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
                    updateDynamicUserInfo();
                }
                break;
            case R.id.iv_photo:
//            case R.id.tv_userinfo:// 编辑资料
                if (ForbidFastClickUtils.isFastClick()) {
                    return;
                }

                if (!SessionContext.isLogin()) {
                    getActivity().sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
                    updateDynamicUserInfo();
                    return;
                }
                mIntent = new Intent(getActivity(), PersonalDataActivity.class);
                getActivity().startActivityForResult(mIntent, MainFragmentActivity.LOGIN_EXIT);
                break;
            case R.id.tv_account:// 帐号安全
                if (ForbidFastClickUtils.isFastClick()) {
                    return;
                }

                if (!SessionContext.isLogin()) {
                    getActivity().sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
                    updateDynamicUserInfo();
                    return;
                }
                mIntent = new Intent(getActivity(), AccountSecurityActivity.class);
                mIntent.putExtra("Tag", true);
                getActivity().startActivityForResult(mIntent, MainFragmentActivity.LOGIN_EXIT);
                break;
            case R.id.tv_address:
                if (ForbidFastClickUtils.isFastClick()) {
                    return;
                }

                if (!SessionContext.isLogin()) {
                    getActivity().sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
                    updateDynamicUserInfo();
                    return;
                }
                mIntent = new Intent(getActivity(), AddressManageActivity.class);
                startActivity(mIntent);
                break;
            case R.id.tv_problem:
                if (ForbidFastClickUtils.isFastClick()) {
                    return;
                }

                mIntent = new Intent(getActivity(), WebViewActivity.class);
                String url = SharedPreferenceUtil.getInstance().getString(
                        AppConst.PROBLEM, "", true);
                mIntent.putExtra("path", url);
                mIntent.putExtra("title", "常见问题");
                startActivity(mIntent);
                break;
            case R.id.tv_invite:
                if (ForbidFastClickUtils.isFastClick()) {
                    return;
                }

                if (!SessionContext.isLogin()) {
                    getActivity().sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
                    updateDynamicUserInfo();
                    return;
                }
                mIntent = new Intent(getActivity(), InviteActivity.class);
                startActivity(mIntent);
                break;
            case R.id.tv_about:
                if (ForbidFastClickUtils.isFastClick()) {
                    return;
                }
                
                mIntent = new Intent(getActivity(), AboutActivity.class);
                startActivity(mIntent);
                break;
			default :
				break;
		}
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(PRJApplication.getInstance()).unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                LogUtil.d("Broadcast action", action);
                if (AppConst.ACTION_DYNAMIC_USER_INFO.equals(action)) {
                    updateDynamicUserInfo();
                }
            } catch (Exception e) {
                // TODO: handle exception
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
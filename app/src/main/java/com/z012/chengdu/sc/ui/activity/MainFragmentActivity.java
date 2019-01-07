package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.NetworkUtil;
import com.prj.sdk.widget.CustomToast;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.PRJApplication;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.CertUserAuth;
import com.z012.chengdu.sc.ui.adapter.MainFragmentAdapter;
import com.z012.chengdu.sc.ui.base.BaseActivity;
import com.z012.chengdu.sc.ui.fragment.TabHomeFragment;
import com.z012.chengdu.sc.ui.fragment.TabServerFragment;
import com.z012.chengdu.sc.ui.fragment.TabUserFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnCheckedChanged;

/**
 * main
 *
 * @author kborid
 */
public class MainFragmentActivity extends BaseActivity implements DataCallback {
    public static final int PAGE_HOME = 0;
    public static final int PAGE_SERVER = 1;
    public static final int PAGE_USER = 2;

    public static final int LOGIN_EXIT = 1000;
    private long exitTime = 0;

    @BindView(R.id.radioGroup) RadioGroup radioGroup;
    @BindView(R.id.viewPager) ViewPager viewPager;

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_main_tab;
    }

    @Override
    public void initParams() {
        super.initParams();
        initFragmentView();
        UmengUpdateAgent.update(this);// 友盟渠道版本更新
        if (NetworkUtil.isNetworkAvailable() && SessionContext.isLogin()) {
            requestCertResult();
        }
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            if (bundle.getString("path") != null) {
                LogUtil.d("JPush", "main value = " + bundle.getString("path"));
                Intent intent = new Intent(this, HtmlActivity.class);
                intent.putExtra("path", bundle.getString("path"));
                startActivity(intent);
            }

            if (bundle.getString("certRet") != null) {
                CustomToast.show(bundle.getString("certRet"), Toast.LENGTH_SHORT);
            }
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Note that getIntent() still returns the original Intent. You can use
        // setIntent(Intent) to update it to this new Intent.
        setIntent(intent);
        dealIntent();
    }

    /**
     * 初始化Fragment视图
     */
    private void initFragmentView() {
        List<Fragment> mList = new ArrayList<>();
        mList.add(new TabHomeFragment());
        mList.add(new TabServerFragment());
        mList.add(new TabUserFragment());
        MainFragmentAdapter mAdapter = new MainFragmentAdapter(getSupportFragmentManager(), mList);
        viewPager.setOffscreenPageLimit(mList.size());
        viewPager.setAdapter(mAdapter);
        viewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                radioGroup.getChildAt(position).performClick();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void changeTabService() {
        viewPager.setCurrentItem(PAGE_SERVER);
    }

    @OnCheckedChanged({R.id.qu_btn_01, R.id.qu_btn_02, R.id.qu_btn_03}) void checked(CompoundButton view, boolean isChecked) {
        switch (view.getId()) {
            case R.id.qu_btn_01:
                if (isChecked) {
                    viewPager.setCurrentItem(PAGE_HOME, false);
                }
                break;
            case R.id.qu_btn_02:
                if (isChecked) {
                    viewPager.setCurrentItem(PAGE_SERVER, false);
                }
                break;
            case R.id.qu_btn_03:
                if (isChecked) {
                    viewPager.setCurrentItem(PAGE_USER, false);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次 退出程序",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                SessionContext.destroy();
                MobclickAgent.onKillProcess(this);// 调用Process.kill或者System.exit之类的方法杀死进程前保存统计数据
                ActivityTack.getInstanse().exit();
            }
            return true;

        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case LOGIN_EXIT:
                LocalBroadcastManager.getInstance(PRJApplication.getInstance()).sendBroadcast(new Intent(AppConst.ACTION_DYNAMIC_USER_INFO));// 退出登录或编辑资料，重置界面
                break;

            default:
                break;
        }

    }

    private void requestCertResult() {
        LogUtil.i("dw", "requestCertResult()");
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("uid", SessionContext.mUser.LOCALUSER.id);

        ResponseData d = b.syncRequest(b);
        d.path = NetURL.CERT_STATUS_BY_UID;
        d.flag = 11;

        DataLoader.getInstance().loadData(this, d);
    }

    @Override
    public void preExecute(ResponseData request) {
    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (request.flag == 11) {
            if (null != response && response.body != null) {
                LogUtil.i("dw", response.body.toString());
                SessionContext.mCertUserAuth = JSON.parseObject(response.body.toString(), CertUserAuth.class);
            }
        }
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
    }
}

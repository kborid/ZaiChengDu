package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.NetworkUtil;
import com.prj.sdk.util.ToastUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.entity.WebInfoEntity;
import com.z012.chengdu.sc.event.CertResultEvent;
import com.z012.chengdu.sc.net.ApiManager;
import com.z012.chengdu.sc.net.entity.CertUserAuth;
import com.z012.chengdu.sc.net.observe.ObserverImpl;
import com.z012.chengdu.sc.net.request.RequestBuilder;
import com.z012.chengdu.sc.net.response.ResponseComm;
import com.z012.chengdu.sc.ui.BaseActivity;
import com.z012.chengdu.sc.ui.adapter.MainFragmentAdapter;
import com.z012.chengdu.sc.ui.fragment.TabHomeFragment;
import com.z012.chengdu.sc.ui.fragment.TabServerFragment;
import com.z012.chengdu.sc.ui.fragment.TabUserFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnCheckedChanged;

/**
 * main
 *
 * @author kborid
 */
public class MainFragmentActivity extends BaseActivity {

    private static final String TAG = MainFragmentActivity.class.getSimpleName();

    public static final int PAGE_HOME = 0;
    public static final int PAGE_SERVER = 1;
    public static final int PAGE_USER = 2;

    private long exitTime = 0;

    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    @BindView(R.id.viewPager)
    ViewPager viewPager;

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
            ApiManager.getCertResultByUID(RequestBuilder.create(true).addBody("uid", SessionContext.mUser.LOCALUSER.id).build(), new ObserverImpl<ResponseComm<CertUserAuth>>() {
                @Override
                public void onNext(ResponseComm<CertUserAuth> o) {
                    super.onNext(o);
                    LogUtil.i(TAG, o.bodyToString());
                    SessionContext.mCertUserAuth = o.getBody();
                }
            });
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
                intent.putExtra("webEntity", new WebInfoEntity(bundle.getString("path")));
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Note that getIntent() still returns the original Intent. You can use
        // setIntent(Intent) to update it to this new Intent.
        setIntent(intent);
        dealIntent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void certResult(CertResultEvent certResultEvent) {
        ToastUtil.show(certResultEvent.getRetMsg(), Toast.LENGTH_SHORT);
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

    @OnCheckedChanged({R.id.qu_btn_01, R.id.qu_btn_02, R.id.qu_btn_03})
    void checked(CompoundButton view, boolean isChecked) {
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
                ToastUtil.show("再按一次 退出程序", Toast.LENGTH_SHORT);
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
}

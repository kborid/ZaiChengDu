package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.LogUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.app.PRJApplication;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.ui.adapter.MainFragmentAdapter;
import com.z012.chengdu.sc.ui.base.BaseFragmentActivity;
import com.z012.chengdu.sc.ui.fragment.TabHomeFragment;
import com.z012.chengdu.sc.ui.fragment.TabServerFragment;
import com.z012.chengdu.sc.ui.fragment.TabUserFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * main
 *
 * @author kborid
 */
public class MainFragmentActivity extends BaseFragmentActivity implements OnPageChangeListener {
    public static final int PAGE_HOME = 0;
    public static final int PAGE_SERVER = 1;
    public static final int PAGE_USER = 2;

    public static final int LOGIN_EXIT = 1000;

    private RadioGroup radioGroup;
    private ViewPager viewPager;
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_main_tab);
        initViews();
        dealIntent();
        initParams();
        initListeners();
    }

    public void initViews() {
        super.initViews();
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
    }

    @Override
    public void initParams() {
        super.initParams();
        initFragmentView();
        UmengUpdateAgent.update(this);// 友盟渠道版本更新
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        if (getIntent().getExtras() != null
                && getIntent().getExtras().getString("path") != null) {
            LogUtil.d("JPush", "main value = "
                    + getIntent().getExtras().getString("path"));
            Intent intent = new Intent(this, HtmlActivity.class);
            intent.putExtra("path", getIntent().getExtras().getString("path"));
            startActivity(intent);
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
    }

    @Override
    public void initListeners() {
        super.initListeners();
        radioGroup.setOnCheckedChangeListener(this);
        viewPager.setOnPageChangeListener(this);
    }

    public void changeTabService() {
        viewPager.setCurrentItem(PAGE_SERVER);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        try {
            switch (checkedId) {
                case R.id.qu_btn_01:
                    viewPager.setCurrentItem(PAGE_HOME, false);
                    break;
                case R.id.qu_btn_02:
                    viewPager.setCurrentItem(PAGE_SERVER, false);
                    break;
                case R.id.qu_btn_03:
                    viewPager.setCurrentItem(PAGE_USER, false);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {
        radioGroup.getChildAt(arg0).performClick();
    }
}

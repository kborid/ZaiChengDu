package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.Utils;
import com.z012.chengdu.sc.BuildConfig;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.ui.BaseActivity;
import com.z012.chengdu.sc.ui.adapter.ViewPagerAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 用户引导页面
 *
 * @author kborid
 */
public class UserGuideActivity extends BaseActivity {

    @BindView(R.id.guide_view)
    ViewPager mViewPager;
    @BindView(R.id.btn_go)
    Button btn_go;

    private int flaggingWidth; // 互动翻页所需滚动的长度是当前屏幕宽度的1/3
    private GestureDetector gestureDetector; // 用户滑动
    private int currentItem; // 当前位置

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_user_guide;
    }

    @Override
    public void initListeners() {
        super.initListeners();
        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                if (arg0 == 2) {
                    btn_go.setVisibility(View.VISIBLE);
                } else {
                    btn_go.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageSelected(int arg0) {
                currentItem = arg0;
            }
        });
    }

    @Override
    public void initParams() {
        super.initParams();
        final int[] resIds = {R.drawable.user_guide_1,
                R.drawable.user_guide_2, R.drawable.user_guide_3};
        ArrayList<View> list = new ArrayList<View>();
        for (int resId : resIds) {
            ImageView view = new ImageView(this);
            view.setBackgroundResource(resId);
            list.add(view);
        }
        ViewPagerAdapter mAdapter = new ViewPagerAdapter(list);
        mViewPager.setAdapter(mAdapter);
        gestureDetector = new GestureDetector(this, new GuideViewTouch());
        flaggingWidth = Utils.mScreenWidth / 3;
    }

    @OnClick(R.id.btn_go)
    void go() {
        gotoMain();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            event.setAction(MotionEvent.ACTION_CANCEL);
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 手势监听，最后一页滑动进入首页
     *
     * @author LiaoBo
     */
    private class GuideViewTouch extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            if (currentItem == 2) {
                if (Math.abs(e1.getX() - e2.getX()) > Math.abs(e1.getY()
                        - e2.getY())
                        && (e1.getX() - e2.getX() <= (-flaggingWidth) || e1
                        .getX() - e2.getX() >= flaggingWidth)) {
                    if (e1.getX() - e2.getX() >= flaggingWidth) {
                        gotoMain();
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * 跳转到首页
     */
    public void gotoMain() {
        SharedPreferenceUtil.getInstance().setInt(AppConst.LAST_USE_VERSIONCODE, BuildConfig.VERSION_CODE);
        startActivity(new Intent(this, MainFragmentActivity.class));
    }
}

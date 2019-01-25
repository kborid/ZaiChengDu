package com.z012.chengdu.sc.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prj.sdk.util.LogUtil;
import com.umeng.analytics.MobclickAgent;
import com.z012.chengdu.sc.ui.dialog.MyProgressDialog;

import butterknife.ButterKnife;

/**
 * fragment基类，提供公共属性
 *
 * @author LiaoBo
 */
public abstract class BaseFragment extends Fragment implements IBaseView {

    private static final String TAG = BaseFragment.class.getSimpleName();

    private MyProgressDialog mProgressDialog;
    protected static String requestID;
    /**
     * Fragment当前状态是否可见
     */
    protected boolean isVisible;
    private boolean isPrepared = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        isPrepared = true;
        View view = inflater.inflate(getLayoutResId(), container, false);
        ButterKnife.bind(this, view);
        initParams();
        return view;
    }

    protected abstract int getLayoutResId();

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isResumed()) {
            onVisibilityChangedToUser(isVisibleToUser, true);
        }

        if (isPrepared) {
            isPrepared = false;
        }
    }

    /**
     * 可见
     */
    protected void onVisible() {
    }

    /**
     * 不可见
     */
    protected void onInvisible() {
    }

    // 参数设置
    protected void initParams() {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            onVisibilityChangedToUser(true, false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getUserVisibleHint()) {
            onVisibilityChangedToUser(false, false);
        }
    }

    /**
     * 当Fragment对用户的可见性发生了改变的时候就会回调此方法
     *
     * @param isVisibleToUser                      true：用户能看见当前Fragment；false：用户看不见当前Fragment
     * @param isHappenedInSetUserVisibleHintMethod true：本次回调发生在setUserVisibleHintMethod方法里；false：
     *                                             发生在onResume或onPause方法里
     */
    public void onVisibilityChangedToUser(boolean isVisibleToUser,
                                          boolean isHappenedInSetUserVisibleHintMethod) {
        if (isVisibleToUser) {
            isVisible = true;
            onVisible();
            MobclickAgent.onPageStart(this.getClass().getName());
            LogUtil.d(TAG, " - display - "
                    + (isHappenedInSetUserVisibleHintMethod ? "setUserVisibleHint"
                    : "onResume"));
        } else {
            isVisible = false;
            onInvisible();
            MobclickAgent.onPageEnd(this.getClass().getName());
            LogUtil.d(TAG, " - hidden - "
                    + (isHappenedInSetUserVisibleHintMethod ? "setUserVisibleHint"
                    : "onPause"));
        }
    }

    @Override
    public void showProgressDialog(String msg, boolean isCancelable) {
        if (null == mProgressDialog) {
            mProgressDialog = new MyProgressDialog(getActivity());
        }
        mProgressDialog.setMessage(msg);
        mProgressDialog.setCanceledOnTouchOutside(isCancelable);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    public boolean isProgressShowing() {
        return null != mProgressDialog && mProgressDialog.isShowing();
    }

    @Override
    public void removeProgressDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
        }
    }

}

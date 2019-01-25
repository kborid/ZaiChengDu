package com.prj.sdk.util;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/**
 * Activity栈管理类：包括退出管理
 *
 * @author LiaoBo
 */
public class ActivityTack {

    // Activity集合
    private List<Activity> mList = new LinkedList<Activity>();
    private static ActivityTack ActivityTack = new ActivityTack();

    private ActivityTack() {
    }

    /**
     * 单例获取Activity管理实例
     *
     * @return
     */
    public static ActivityTack getInstanse() {
        return ActivityTack;
    }

    /**
     * 添加组件
     *
     * @param activity
     */
    public final void addActivity(Activity activity) {
        mList.add(activity);
    }

    /**
     * 去除组件
     *
     * @param activity
     */
    public final void removeActivity(Activity activity) {
        mList.remove(activity);
    }

    /**
     * 应用退出时调用
     */
    public final void exit() {
        for (Activity activity : mList) {
            if (activity != null) {
                activity.finish();
            }
        }
        System.exit(0);
    }
}

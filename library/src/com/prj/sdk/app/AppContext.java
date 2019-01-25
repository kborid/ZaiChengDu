package com.prj.sdk.app;

import android.content.Context;

import com.prj.sdk.db.DBManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 记录应用全局信息
 */
public final class AppContext {

    public static Map<String, Object> mMemoryMap = null;
    public static DBManager mDBManager = null;
    public static Context mMainContext = null;

    public static void init(Context MainContext) {
        mMainContext = MainContext.getApplicationContext();
        mMemoryMap = new HashMap<String, Object>();
        mDBManager = DBManager.getInstance(MainContext, null);
    }
}

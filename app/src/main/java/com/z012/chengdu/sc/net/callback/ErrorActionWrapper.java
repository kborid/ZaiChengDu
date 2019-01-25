package com.z012.chengdu.sc.net.callback;

import android.widget.Toast;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.util.ToastUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.net.exception.ServerException;

import io.reactivex.functions.Consumer;

public class ErrorActionWrapper implements Consumer<Throwable> {
    @Override
    public void accept(Throwable throwable) throws Exception {
        String msg;
        if (throwable instanceof ServerException) {
            msg = AppContext.mMainContext.getString(R.string.dialog_tip_null_error);
        } else {
            msg = AppContext.mMainContext.getString(R.string.dialog_tip_net_error);
        }

        call(msg);
    }

    protected void call(String msg) {
        ToastUtil.show(msg, Toast.LENGTH_SHORT);
    }
}

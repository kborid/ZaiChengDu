package com.z012.chengdu.sc.tools;

import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.app.PRJApplication;

public class CustomClickableSpan extends ClickableSpan {

    private View.OnClickListener listener = null;

    public CustomClickableSpan(View.OnClickListener onClickListener) {
        this.listener = onClickListener;
    }

    @Override
    public void onClick(@NonNull View widget) {
        if (null != listener) {
            listener.onClick(widget);
        }
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        ds.setUnderlineText(false);
        ds.setColor(PRJApplication.getInstance().getResources().getColor(R.color.mainColor));
    }
}

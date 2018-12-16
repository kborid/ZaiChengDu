package com.z012.chengdu.sc.ui.widge.edittext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

import com.prj.sdk.util.LogUtil;

@SuppressLint("AppCompatCustomView")
public class IdCardCatchEditText extends EditText {

    private String reg = "^[xX0-9]+$";


    public IdCardCatchEditText(Context context) {
        super(context);
    }

    public IdCardCatchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IdCardCatchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new InputConnectionWrapper(super.onCreateInputConnection(outAttrs), false) {
            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                LogUtil.i("dw", "text = " + text.toString());
                String str = text.toString();
                if (str.matches(reg)) {
                    return super.commitText(text, newCursorPosition);
                }
                return false;
            }

            @Override
            public boolean sendKeyEvent(KeyEvent event) {
                return super.sendKeyEvent(event);
            }
        };
    }
}

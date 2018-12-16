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
public class NameCatchEditText extends EditText {

    private String regChina = "^[\\u4E00-\\u9FFF]+$";
    private String regEnglish = "^[a-zA-z · ]+$";
    private String regChar = "^ · +$";


    public NameCatchEditText(Context context) {
        super(context);
    }

    public NameCatchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NameCatchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new InputConnectionWrapper(super.onCreateInputConnection(outAttrs), false) {
            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                LogUtil.i("dw", "text = " + text.toString());
                String str = text.toString();
                if (str.matches(regChina) || str.matches(regEnglish) || str.matches(regChar)) {
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

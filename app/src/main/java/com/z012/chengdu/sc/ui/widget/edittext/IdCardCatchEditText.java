package com.z012.chengdu.sc.ui.widget.edittext;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
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
                if (isMatchReg(text.toString())) {
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

    @Override
    public boolean onTextContextMenuItem(int id) {
        if (android.R.id.paste == id) {
            ClipboardManager clip = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (null != clip) {
                super.onTextContextMenuItem(id);
                String text = clip.getPrimaryClip().getItemAt(0).getText().toString();
                if (!isMatchReg(text)) {
                    text = "";
                }
                setText(text);
                setSelection(text.length());
            }
            return true;
        }
        return super.onTextContextMenuItem(id);
    }

    private boolean isMatchReg(String s) {
        return null != s && s.matches(reg);
    }
}

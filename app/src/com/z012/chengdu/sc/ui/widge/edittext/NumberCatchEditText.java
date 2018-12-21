package com.z012.chengdu.sc.ui.widge.edittext;

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
public class NumberCatchEditText extends EditText {

    private String reg = "^[0-9]+$";


    public NumberCatchEditText(Context context) {
        super(context);
    }

    public NumberCatchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberCatchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
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

    @Override
    public boolean onTextContextMenuItem(int id) {
        if (android.R.id.paste == id) {
            ClipboardManager clip = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (null != clip) {
                super.onTextContextMenuItem(id);
                String text = clip.getPrimaryClip().getItemAt(0).getText().toString();
                StringBuilder sb = new StringBuilder();
                for (char c : text.toCharArray()) {
                    if (!isMatchReg(c)) {
                        sb = new StringBuilder();
                        break;
                    } else {
                        sb.append(c);
                    }
                }
                setText(sb.toString());
                setSelection(sb.length());
            }
            return true;
        }
        return super.onTextContextMenuItem(id);
    }

    private boolean isMatchReg(String s) {
        return null != s && s.matches(reg);
    }

    private boolean isMatchReg(char c) {
        return isMatchReg(String.valueOf(c));
    }
}

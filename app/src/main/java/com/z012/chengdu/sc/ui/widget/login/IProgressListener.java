package com.z012.chengdu.sc.ui.widget.login;

public interface IProgressListener {
    void showProgressDialog(String tip, boolean cancelable);
    boolean isProgressShowing();
    void removeProgressDialog();
}

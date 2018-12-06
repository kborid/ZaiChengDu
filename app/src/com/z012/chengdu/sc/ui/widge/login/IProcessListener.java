package com.z012.chengdu.sc.ui.widge.login;

public interface IProcessListener {
    void showProgressDialog(String tip, boolean cancelable);
    boolean isProgressShowing();
    void removeProgressDialog();
}

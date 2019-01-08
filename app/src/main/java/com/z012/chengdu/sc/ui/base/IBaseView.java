package com.z012.chengdu.sc.ui.base;

public interface IBaseView {
    void showProgressDialog(String msg, boolean isCancelable);
    boolean isProgressShowing();
    void removeProgressDialog();
}

package com.z012.chengdu.sc.net.callback;

public interface ResponseCallback<T> {
    void onSuccess(T t);
    void onFail(String msg);
}

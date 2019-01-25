package com.z012.chengdu.sc.ui.activity;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.common.widget.custom.CommonLoadingWidget;
import com.orhanobut.logger.Logger;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.entity.WebInfoEntity;
import com.z012.chengdu.sc.ui.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class WebViewActivity extends BaseActivity {

    private static final String TAG = WebViewActivity.class.getSimpleName();

    @BindView(R.id.webview)
    WebView mWebView;
    @BindView(R.id.common_loading_widget)
    CommonLoadingWidget common_loading_widget;

    private WebInfoEntity mWebInfoEntity;

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_city_hot;
    }

    @Override
    public void dealIntent() {
        super.dealIntent();

        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            if (bundle.get("webEntity") != null) {
                mWebInfoEntity = (WebInfoEntity) bundle.get("webEntity");
            }
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        Logger.t(TAG).d("initParams()");
        WebSettings webSetting = mWebView.getSettings();
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSetting.setLoadWithOverviewMode(true);// 充满全屏
        mWebView.setHorizontalScrollBarEnabled(false);// 水平不显示
        mWebView.setVerticalScrollBarEnabled(false); // 垂直不显示
        mWebView.setWebViewClient(new MyWebViewClient());
        if (null != mWebInfoEntity) {
             Logger.t(TAG).d("Title:" + mWebInfoEntity.getTitle() + ", Url:" + mWebInfoEntity.getUrl());
            tv_center_title.setText(mWebInfoEntity.getTitle());
            mWebView.loadUrl(mWebInfoEntity.getUrl());
        }
    }

    @OnClick(R.id.tv_left_title)
    void left() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            Logger.t(TAG).d("shouldOverrideUrlLoading() url = " + url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Logger.t(TAG).d("onPageStarted()");
            common_loading_widget.startLoading();
            mWebView.setEnabled(false);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Logger.t(TAG).d("onPageFinished()");
            common_loading_widget.closeLoading();
            mWebView.setEnabled(true);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            Logger.t(TAG).d("onReceivedSslError()");
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Logger.t(TAG).d("onReceivedSslError()");
            handler.proceed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
        common_loading_widget.closeLoading();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            left();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

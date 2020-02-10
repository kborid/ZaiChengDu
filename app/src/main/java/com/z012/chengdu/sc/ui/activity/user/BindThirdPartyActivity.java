package com.z012.chengdu.sc.ui.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ToastUtil;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.socialize.bean.HandlerRequestCode;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.StatusCode;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMDataListener;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.request.RequestBeanBuilder;
import com.z012.chengdu.sc.net.entity.ThirdPartyBindListBean;
import com.z012.chengdu.sc.ui.BaseActivity;

import java.net.ConnectException;
import java.util.List;
import java.util.Map;

/**
 * 三方账号绑定
 *
 * @author LiaoBo
 */
public class BindThirdPartyActivity extends BaseActivity implements DataCallback {
    private Button btn_bind_qq, btn_bind_wx, btn_bind_sina;
    // （01-新浪微博，02-腾讯QQ，03-微信，04-支付宝）
    private String mPlatform;
    // 整个平台的Controller, 负责管理整个SDK的配置、操作等处理
    private UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.login");
    private String thirdpartusername, thirdpartuserheadphotourl, openid, unionid, usertoken;
    private boolean isModify;                                                                // 记录进入该页面时已绑定的列表

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_third_party_act;
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("账户绑定");
        btn_bind_qq = (Button) findViewById(R.id.btn_bind_qq);
        btn_bind_wx = (Button) findViewById(R.id.btn_bind_wx);
        btn_bind_sina = (Button) findViewById(R.id.btn_bind_sina);

        String data = SharedPreferenceUtil.getInstance().getString(AppConst.THIRDPARTYBIND, null, false);
        if (StringUtil.notEmpty(data)) {
            // 已绑定的列表
            List<ThirdPartyBindListBean> mList = JSON.parseArray(data, ThirdPartyBindListBean.class);
            if (mList != null && !mList.isEmpty()) {
                for (int i = 0; i < mList.size(); i++) {
                    ThirdPartyBindListBean temp = mList.get(i);
                    setBtnUnbind(temp.platform, temp.openid);
                }
            }
        }
        addQQQZonePlatform();
        addWXPlatform();
        // 设置新浪SSO handler
        mController.getConfig().setSsoHandler(new SinaSsoHandler());
    }

    @Override
    public void initListeners() {
        super.initListeners();
        tv_left_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(isModify ? RESULT_OK : RESULT_CANCELED);
                finish();
            }
        });

        btn_bind_qq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlatform = "02";
                if (!mController.getConfig().getSsoHandler(HandlerRequestCode.QQ_REQUEST_CODE).isClientInstalled()) {
                    ToastUtil.show("没有安装QQ", 0);
                    return;
                }
                if ("0".equals(v.getTag())) {// 解绑
                    openid = (String) v.getTag(R.id.tag_key);
                    unbindData();
                } else {// 绑定
                    login(SHARE_MEDIA.QQ);
                }
            }
        });

        btn_bind_wx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlatform = "03";
                if (!WXAPIFactory.createWXAPI(BindThirdPartyActivity.this, null).isWXAppInstalled()) {
                    ToastUtil.show("没有安装微信", 0);
                    return;
                }

                if ("0".equals(v.getTag())) {// 解绑
                    openid = (String) v.getTag(R.id.tag_key);
                    unbindData();
                } else {
                    login(SHARE_MEDIA.WEIXIN);
                }
            }
        });

        btn_bind_sina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlatform = "01";
                if ("0".equals(v.getTag())) {// 解绑
                    openid = (String) v.getTag(R.id.tag_key);
                    unbindData();
                } else {
                    login(SHARE_MEDIA.SINA);
                }
            }
        });
    }

    /**
     * 添加qq及qq Zone平台
     */
    private void addQQQZonePlatform() {
        String appId = getString(R.string.qq_appid);
        String appKey = getString(R.string.qq_appkey);
        // 添加QQ支持, 并且设置QQ分享内容的target url
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(this, appId, appKey);
        // qqSsoHandler.setTargetUrl("http://www.umeng.com");
        qqSsoHandler.addToSocialSDK();
        // 添加QZone平台
        // QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(this, appId, appKey);
        // qZoneSsoHandler.addToSocialSDK();
    }

    /**
     * 添加微信平台配置
     */
    private void addWXPlatform() {
        String appId = getString(R.string.wx_appid);
        String appSecret = getString(R.string.wx_appsecret);
        // 添加微信平台
        UMWXHandler wxHandler = new UMWXHandler(this, appId, appSecret);
        wxHandler.addToSocialSDK();
    }

    /**
     * 设置按钮为绑定
     */
    public void setBtnBind(String platform) {
        Button btn = null;
        if ("01".equals(platform)) {
            btn = btn_bind_sina;
        } else if ("02".equals(platform)) {
            btn = btn_bind_qq;
        } else {
            btn = btn_bind_wx;
        }
        btn.setText("绑定");
        btn.setBackgroundResource(R.drawable.common_blue_rounded_btn_bg);
        btn.setTextColor(0xffffffff);
        btn.setTag("1");
    }

    /**
     * 设置按钮为解绑
     */
    public void setBtnUnbind(String platform, String openid) {
        Button btn = null;
        if ("01".equals(platform)) {
            btn = btn_bind_sina;
            // mSinaOpenId =
        } else if ("02".equals(platform)) {
            btn = btn_bind_qq;
            // mQqOpenId
        } else {
            btn = btn_bind_wx;
            // mWxOpenId
        }
        btn.setText("解绑");
        btn.setBackgroundResource(R.drawable.grey_btn);
        btn.setTextColor(0xff565656);
        btn.setTag("0");
        btn.setTag(R.id.tag_key, openid);
    }

    /**
     * 授权。
     *
     * @param platform 平台
     */
    private void login(final SHARE_MEDIA platform) {
        mController.doOauthVerify(this, platform, new UMAuthListener() {

            @Override
            public void onStart(SHARE_MEDIA platform) {
                ToastUtil.show("授权开始", 0);
            }

            @Override
            public void onError(SocializeException e, SHARE_MEDIA platform) {
                ToastUtil.show("授权错误", Toast.LENGTH_SHORT);
            }

            @Override
            public void onComplete(Bundle value, SHARE_MEDIA platform) {
                openid = value.getString("uid");
                unionid = value.getString("unionid");
                usertoken = value.getString("access_token");
                if (!TextUtils.isEmpty(openid)) {
                    getUserInfo(platform);
                } else {
                    ToastUtil.show("授权失败", Toast.LENGTH_SHORT);
                }
                // ToastUtil.show("授权完成", Toast.LENGTH_SHORT);
            }

            @Override
            public void onCancel(SHARE_MEDIA platform) {
                ToastUtil.show("授权取消", Toast.LENGTH_SHORT);
            }
        });
    }

    /**
     * 获取授权平台的用户信息</br>
     */
    private void getUserInfo(final SHARE_MEDIA platform) {
        mController.getPlatformInfo(this, platform, new UMDataListener() {

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(int status, Map<String, Object> info) {
                if (status == StatusCode.ST_CODE_SUCCESSED && info != null) {
                    try {
                        if (platform == SHARE_MEDIA.SINA) { // 新浪微博
                            mPlatform = "01";
                            thirdpartuserheadphotourl = info.get("profile_image_url").toString();
                            thirdpartusername = info.get("screen_name").toString();
                            if (StringUtil.empty(usertoken)) {// 通过web方式登录，需要从用户信息中获取access_token
                                usertoken = info.get("access_token").toString();
                            }
                        } else if (platform == SHARE_MEDIA.QQ) { // QQ
                            mPlatform = "02";
                            thirdpartuserheadphotourl = info.get("profile_image_url").toString();
                            thirdpartusername = info.get("screen_name").toString();
                        } else if (platform == SHARE_MEDIA.WEIXIN) { // 微信
                            mPlatform = "03";
                            thirdpartuserheadphotourl = info.get("headimgurl").toString();
                            thirdpartusername = info.get("nickname").toString();
                            unionid = info.get("unionid").toString();
                        }
                        bindData();
                    } catch (Exception e) {
                        ToastUtil.show("获取用户信息失败", Toast.LENGTH_SHORT);
                    }
                    // ToastUtil.show(info.toString(), Toast.LENGTH_SHORT);

                } else {
                    ToastUtil.show("获取用户信息失败", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    /**
     * 解绑第三方帐号数据
     */
    private void unbindData() {
        RequestBeanBuilder builder = RequestBeanBuilder.create(true);
        builder.addBody("openid", openid);// openid值为uid
        builder.addBody("platform", mPlatform);
        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.UNBIND;
        data.flag = 1;

        if (!isProgressShowing()) {
            showProgressDialog("正在加载，请稍候...", true);
        }
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    /**
     * 绑定第三方帐号数据
     */
    private void bindData() {
        RequestBeanBuilder builder = RequestBeanBuilder.create(true);
        builder.addBody("openid", openid);// openid值为uid
        builder.addBody("unionid", unionid);
        builder.addBody("platform", mPlatform);
        builder.addBody("usertoken", usertoken);
        builder.addBody("thirdpartusername", thirdpartusername);
        builder.addBody("thirdpartuserheadphotourl", thirdpartuserheadphotourl);
        builder.addBody("channel", "02");

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.BIND;
        data.flag = 2;

        if (!isProgressShowing()) {
            showProgressDialog("正在加载，请稍候...", true);
        }
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    /**
     * 加载第三方的绑定列表
     */
    public void loadThirdPartyBindList() {
        RequestBeanBuilder builder = RequestBeanBuilder.create(true);

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.BIND_LIST;
        data.flag = 3;

        if (!isProgressShowing()) {
            showProgressDialog("正在加载，请稍候...", true);
        }
        requestID = DataLoader.getInstance().loadData(this, data);

    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (request.flag == 1) {// 解绑成功
            // ToastUtil.show("解绑成功", 0);
            loadThirdPartyBindList();
        } else if (request.flag == 2) {
            // ToastUtil.show("绑定成功", 0);
            loadThirdPartyBindList();
        } else {
            removeProgressDialog();
            //重置状态
            setBtnBind("01");
            setBtnBind("02");
            setBtnBind("03");
            List<ThirdPartyBindListBean> temp = JSON.parseArray(response.body.toString(), ThirdPartyBindListBean.class);
            if (temp != null) {
                if (!temp.isEmpty()) {// 将绑定的三方账户按钮设置为“解绑”
                    for (int i = 0; i < temp.size(); i++) {
                        setBtnUnbind(temp.get(i).platform, temp.get(i).openid);
                    }
                }
                isModify = true;
                // 更新缓存数据
                SharedPreferenceUtil.getInstance().setString(AppConst.THIRDPARTYBIND, response.body.toString(), false);
            }

        }
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        removeProgressDialog();

        String message;
        if (e != null && e instanceof ConnectException) {
            message = getString(R.string.dialog_tip_net_error);
        } else {
            message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
        }
        ToastUtil.show(message, Toast.LENGTH_LONG);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(isModify ? RESULT_OK : RESULT_CANCELED);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode);
        if (ssoHandler != null) {
            ssoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }
}

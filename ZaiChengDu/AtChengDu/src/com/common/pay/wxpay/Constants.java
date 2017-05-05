package com.common.pay.wxpay;

import com.prj.sdk.app.AppContext;
import com.z012.chengdu.sc.R;

public class Constants {


  // appid
  // 请同时修改  androidmanifest.xml里面，.PayActivityd里的属性<data android:scheme="wxb4ba3c02aa476ea1"/>为新设置的appid
  public static final String APPID = AppContext.mMainContext.getString(R.string.wx_appid);

  // 商户号
   public static final String PARTNERID = AppContext.mMainContext.getString(R.string.wx_partnerId);

   // API密钥，在商户平台设置
   public static final String PRIVATE_KEY = AppContext.mMainContext.getString(R.string.wx_privateKey);



}

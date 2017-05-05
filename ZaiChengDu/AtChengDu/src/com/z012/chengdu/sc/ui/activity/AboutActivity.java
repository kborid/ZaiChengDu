package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.prj.sdk.app.AppContext;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.ui.base.BaseActivity;

/**
 * 关于
 * 
 * @author LiaoBo
 */
public class AboutActivity extends BaseActivity {
	private ImageView about_icon;
	private Button btnAbout, btn_develop;
	private TextView tv_version;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_about_act);
		initViews();
		initParams();
		initListeners();

	}

	@Override
	public void initViews() {
		super.initViews();
		about_icon = (ImageView) findViewById(R.id.about_icon);
		btnAbout = (Button) findViewById(R.id.btn_about);
		tv_version = (TextView) findViewById(R.id.tv_version);
		tv_center_title.setText("关于");
		tv_right_title.setVisibility(View.GONE);
		btn_develop = (Button) findViewById(R.id.btn_develop);
		if (AppConst.ISDEVELOP) {
			btn_develop.setVisibility(View.VISIBLE);
		} else {
			btn_develop.setVisibility(View.GONE);
		}
	}

	@Override
	public void initParams() {
		super.initParams();
		StringBuilder sb = new StringBuilder();
		sb.append(AppContext.getVersion());
		if (AppConst.ISDEVELOP) {
			sb.append(" 渠道名：").append(
					AppContext.getAppMetaData(this, "UMENG_CHANNEL"));
		}
		tv_version.setText(sb);// 设置版本

		String url = SharedPreferenceUtil.getInstance().getString(
				AppConst.ABOUT_ICON, "", true);
		Bitmap bm = ImageLoader.getInstance().getCacheBitmap(url);
		if (bm != null) {
			about_icon.setImageBitmap(bm);
		} else {
			ImageLoader.getInstance().loadBitmap(
					new ImageLoader.ImageCallback() {
						@Override
						public void imageCallback(Bitmap bm, String url,
								String imageTag) {
							if (bm != null) {
								about_icon.setImageBitmap(bm);
							}
						}
					}, url, url);
		}
	}

	@Override
	public void initListeners() {
		super.initListeners();
		btnAbout.setOnClickListener(this);
		btn_develop.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		Intent mIntent = null;
		switch (v.getId()) {
		case R.id.btn_about:
			mIntent = new Intent(this, WebViewActivity.class);
			String url = SharedPreferenceUtil.getInstance().getString(
					AppConst.ABOUT_US, "", true);
			mIntent.putExtra("path", url);
			mIntent.putExtra("title", "关于我们");
			startActivity(mIntent);
			break;
		case R.id.btn_develop:
			mIntent = new Intent(this, HtmlActivity.class);
			mIntent.putExtra("ISDEVELOP", AppConst.ISDEVELOP);
			startActivity(mIntent);

			// AlipayUtil ali = new AlipayUtil(this);
			// ali.pay(ali.getOrderInfo("android测试启动支护宝", "测试信息", "0.01"));
			// WXPayUtil wx = new WXPayUtil(this);
			// wx.initData("1", "android test",
			// String.valueOf(System.currentTimeMillis()));
			break;
		default:
			break;
		}
	}
}

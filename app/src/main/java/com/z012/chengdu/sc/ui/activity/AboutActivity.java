package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prj.sdk.app.AppContext;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 关于
 * 
 * @author kborid
 */
public class AboutActivity extends BaseActivity {

	@BindView(R.id.btn_develop) Button btn_develop;
	@BindView(R.id.tv_version) TextView tv_version;

	@Override
	protected int getLayoutResId() {
		return R.layout.ui_about_act;
	}

	@Override
	public void initParams() {
		super.initParams();
        tv_center_title.setText("关于");
        tv_right_title.setVisibility(View.GONE);
        if (AppConst.ISDEVELOP) {
            btn_develop.setVisibility(View.VISIBLE);
        } else {
            btn_develop.setVisibility(View.GONE);
        }
		StringBuilder sb = new StringBuilder();
		sb.append(AppContext.getVersion());
		if (AppConst.ISDEVELOP) {
			sb.append(" 渠道名：").append(
					AppContext.getAppMetaData(this, "UMENG_CHANNEL"));
		}
		tv_version.setText(sb);// 设置版本
	}

	@OnClick(R.id.btn_about) void about() {
		Intent intent = new Intent(this, WebViewActivity.class);
		intent.putExtra("path", NetURL.ABOUT_URL);
		intent.putExtra("title", "关于我们");
		startActivity(intent);
	}

	@OnClick(R.id.btn_develop) void develop() {
		Intent intent = new Intent(this, HtmlActivity.class);
		intent.putExtra("ISDEVELOP", AppConst.ISDEVELOP);
		startActivity(intent);
	}
}

package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prj.sdk.util.SystemUtil;
import com.z012.chengdu.sc.BuildConfig;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.entity.WebInfoEntity;
import com.z012.chengdu.sc.ui.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 关于
 *
 * @author kborid
 */
public class AboutActivity extends BaseActivity {

    @BindView(R.id.btn_develop)
    Button btn_develop;
    @BindView(R.id.tv_version)
    TextView tv_version;

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_about_act;
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("关于");
        if (AppConst.ISDEVELOP) {
            btn_develop.setVisibility(View.VISIBLE);
        } else {
            btn_develop.setVisibility(View.GONE);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(BuildConfig.VERSION_NAME);
        if (AppConst.ISDEVELOP) {
            sb.append(" 渠道名：").append(SystemUtil.getAppMetaData(this, "UMENG_CHANNEL"));
        }
        tv_version.setText(sb);// 设置版本
    }

    @OnClick(R.id.btn_about)
    void about() {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("webEntity", new WebInfoEntity("关于我们", NetURL.ABOUT_URL));
        startActivity(intent);
    }

    @OnClick(R.id.btn_develop)
    void develop() {
        Intent intent = new Intent(this, HtmlActivity.class);
        intent.putExtra("ISDEVELOP", AppConst.ISDEVELOP);
        startActivity(intent);
    }
}

package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.ui.base.BaseActivity;

/**
 * 实名认证
 * 
 * @author kborid
 * 
 */
public class CertificateThreeActivity extends BaseActivity {

    private ImageView iv_icon;
	private TextView tv_ret, tv_tipsTime;
	private Button btn_next;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_certificate_three);
		initViews();
		initParams();
		initListeners();
	}

	@Override
	public void initViews() {
		super.initViews();
		tv_center_title.setText("实名认证");
		tv_right_title.setVisibility(View.GONE);
		tv_left_title.setVisibility(View.GONE);

		iv_icon = (ImageView) findViewById(R.id.iv_icon);
		tv_ret = (TextView) findViewById(R.id.tv_ret);
		tv_tipsTime = (TextView) findViewById(R.id.tv_tipsTime);
		btn_next = (Button) findViewById(R.id.btn_next);
	}

	@Override
	public void initParams() {
		super.initParams();
	}

	@Override
	public void initListeners() {
		super.initListeners();
		btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CertificateThreeActivity.this, MainFragmentActivity.class);
                intent.putExtra("certRet", "认证成功");
                startActivity(intent);
            }
        });
	}
}

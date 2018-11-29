package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.ui.base.BaseActivity;

import java.net.ConnectException;

/**
 * 实名认证
 * 
 * @author kborid
 * 
 */
public class CertificateOneActivity extends BaseActivity implements DataCallback {

	private EditText et_name, et_id, et_card, et_phone;
	private CheckBox cb_name, cb_id, cb_card, cb_phone;
	private Button btn_next;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_certificate_one);
		initViews();
		initParams();
		initListeners();
	}

	@Override
	public void initViews() {
		super.initViews();
		tv_center_title.setText("实名认证");
		tv_right_title.setVisibility(View.GONE);

		et_name = (EditText) findViewById(R.id.et_name);
		et_id = (EditText) findViewById(R.id.et_id);
		et_card = (EditText) findViewById(R.id.et_card);
		et_phone = (EditText) findViewById(R.id.et_phone);

		cb_name = (CheckBox) findViewById(R.id.cb_name);
        cb_id = (CheckBox) findViewById(R.id.cb_id);
        cb_card = (CheckBox) findViewById(R.id.cb_card);
        cb_phone = (CheckBox) findViewById(R.id.cb_phone);
		btn_next = (Button) findViewById(R.id.btn_next);
	}

	@Override
	public void initParams() {
		super.initParams();
		btn_next.setEnabled(!checkParamsEmpty());
	}

	@Override
	public void initListeners() {
		super.initListeners();
		btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CertificateOneActivity.this, CertificateTwoActivity.class);
//                if (checkParamsValid()) {
                    intent.putExtra("name", et_name.getText().toString());
                    intent.putExtra("id", et_id.getText().toString());
                    intent.putExtra("card", et_card.getText().toString());
                    intent.putExtra("phone", et_phone.getText().toString());
                    startActivity(intent);
//                }
            }
        });

		et_name.addTextChangedListener(watcher);
		et_id.addTextChangedListener(watcher);
		et_card.addTextChangedListener(watcher);
		et_phone.addTextChangedListener(watcher);

		cb_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_name.setText("");
            }
        });

		cb_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_id.setText("");
            }
        });

		cb_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_card.setText("");
            }
        });

		cb_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_phone.setText("");
            }
        });

		et_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    cb_name.setVisibility(View.VISIBLE);
                } else {
                    cb_name.setVisibility(View.GONE);
                }
            }
        });

		et_id.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    cb_id.setVisibility(View.VISIBLE);
                } else {
                    cb_id.setVisibility(View.GONE);
                }
            }
        });

		et_card.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    cb_card.setVisibility(View.VISIBLE);
                } else {
                    cb_card.setVisibility(View.GONE);
                }
            }
        });

		et_phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    cb_phone.setVisibility(View.VISIBLE);
                } else {
                    cb_phone.setVisibility(View.GONE);
                }
            }
        });
	}

	private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            btn_next.setEnabled(!checkParamsEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

	private void requestCertificateCount() {
        RequestBeanBuilder b = RequestBeanBuilder.create(true);
        b.addBody("id", SessionContext.mUser.LOCALUSER.id);

        ResponseData d = b.syncRequest(b);
        d.flag = 1;
        d.path = NetURL.IDENTITY_VERIFICATION;

        if (!isProgressShowing()) {
            showProgressDialog("", false);
        }

        DataLoader.getInstance().loadData(this, d);
    }

	private boolean checkParamsEmpty() {
	    return TextUtils.isEmpty(et_name.getText().toString())
                || TextUtils.isEmpty(et_id.getText().toString())
                || TextUtils.isEmpty(et_card.getText().toString())
                || TextUtils.isEmpty(et_phone.getText().toString());
    }

	private boolean checkParamsValid() {
	    if (checkParamsEmpty()) {
	        CustomToast.show("请填写完整信息", Toast.LENGTH_SHORT);
	        return false;
        }

        String id = et_id.getText().toString();
	    if (id.length() != 18) {
            CustomToast.show("身份证录入有误，请重试", Toast.LENGTH_SHORT);
	        return false;
        }

	    String card = et_card.getText().toString();
	    if (!Utils.checkBankCard(card)) {
            CustomToast.show("银行卡录入有误，请重试", Toast.LENGTH_SHORT);
	        return false;
        }

        String phone = et_phone.getText().toString();
	    if (!Utils.isMobile(phone)) {
	        if (phone.length() < 11) {
                CustomToast.show("请输入11位手机号码", Toast.LENGTH_SHORT);
                return false;
            }
            CustomToast.show("手机号码有误，请重试", Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }

    @Override
    public void preExecute(ResponseData request) {

    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
	    if (request.flag == 1) {
	        removeProgressDialog();
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
        CustomToast.show(message, Toast.LENGTH_LONG);
    }
}

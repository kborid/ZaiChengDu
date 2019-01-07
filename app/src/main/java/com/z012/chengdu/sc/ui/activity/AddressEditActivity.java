package com.z012.chengdu.sc.ui.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.UserAddrs;
import com.z012.chengdu.sc.ui.base.BaseActivity;
import com.z012.chengdu.sc.ui.dialog.AreaWheelDialog;
import com.z012.chengdu.sc.ui.dialog.AreaWheelDialog.AreaWheelCallback;

import java.net.ConnectException;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 地址编辑(新增、编辑)
 * 
 * @author LiaoBo
 */
public class AddressEditActivity extends BaseActivity implements
		DatePickerDialog.OnDateSetListener, DataCallback, AreaWheelCallback {

	@BindView(R.id.et_name) EditText et_name;
	@BindView(R.id.et_phone) EditText et_phone;
	@BindView(R.id.et_address) EditText et_address;
	@BindView(R.id.tv_address_sele) TextView tv_address_sele;
	@BindView(R.id.tv_date) TextView tv_date;
	@BindView(R.id.checkBox) CheckBox checkBox;

	private final int DATE_DIALOG_ID = 0;
	private int mYear, mMonth = 11, mDay = 31;
	private int mType; // 1:新增，2：编辑
	private String province, city, area, id;

	@Override
	protected int getLayoutResId() {
		return R.layout.ui_address_edit_act;
	}

	/**
	 * 处理intent，区分新增和编辑
	 */
	public void dealIntent() {
		super.dealIntent();
		if (getIntent().getExtras() != null) {
			UserAddrs temp = (UserAddrs) getIntent().getExtras()
					.getSerializable("item");
			tv_center_title.setText("编辑地址");
			mType = 2;
			et_name.setText(temp.name);
			et_phone.setText(temp.phone);
			et_address.setText(temp.address);
			StringBuilder sb = new StringBuilder();
			sb.append(temp.province).append("-").append(temp.city).append("-")
					.append(temp.area);
			tv_address_sele.setText(sb.toString());
			checkBox.setChecked(temp.def);

			province = temp.province;
			city = temp.city;
			area = temp.area;
			id = temp.id;
		} else {
			tv_center_title.setText("新增地址");
			mType = 1;
		}
	}

	@Override
	public void initParams() {
		super.initParams();
        tv_right_title.setText("保存");
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		mYear = c.get(Calendar.YEAR) + 1;// 初始化有效期年
		StringBuilder date = new StringBuilder().append(mYear).append("-")
				.append(mMonth + 1).append("-").append(mDay);
		tv_date.setText(date);
	}

	@OnClick(R.id.tv_address_sele) void address() {
        AreaWheelDialog dialog = new AreaWheelDialog(this, this);
        dialog.show();
    }

    @OnClick(R.id.tv_date) void date() {
        showDialog(DATE_DIALOG_ID);
    }

    @OnClick(R.id.tv_right_title) void right() {
        submitData();
    }

	/**
	 * 当Activity调用showDialog函数时会触发该函数的调用：
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case DATE_DIALOG_ID:
			dialog = new DatePickerDialog(this, this, mYear, mMonth, mDay);
			break;
		}
		return dialog;
	}

	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		mYear = year;
		mMonth = monthOfYear;
		mDay = dayOfMonth;
		DateUtil.getCurDateStr();
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		int n_year = c.get(Calendar.YEAR);
		int n_month = c.get(Calendar.MONTH);
		int n_day = c.get(Calendar.DAY_OF_MONTH);
		if (mYear < n_year) {
			CustomToast.show("有效期不能小于当前日期", 0);
			return;
		}
		if (mYear == n_year) {
			if (mMonth < n_month) {
				CustomToast.show("有效期不能小于当前日期", 0);
				return;
			}
			if (mMonth == n_month && mDay < n_day) {
				CustomToast.show("有效期不能小于当前日期", 0);
				return;
			}
		}
		// 设置文本的内容：
		// 月份+1，因为从0开始
		if (mMonth < 9) {
			if (mDay < 10) {
				tv_date.setText(new StringBuilder().append(mYear).append("-")
						.append("0").append(mMonth + 1).append("-").append("0")
						.append(mDay).toString());
			} else {
				tv_date.setText(new StringBuilder().append(mYear).append("-")
						.append("0").append((mMonth + 1)).append("-")
						.append(mDay).toString());
			}
		} else {
			if (mDay < 10) {
				tv_date.setText(new StringBuilder().append(mYear).append("-")
						.append(mMonth + 1).append("-").append("0")
						.append(mDay).toString());
			} else {
				tv_date.setText(new StringBuilder().append(mYear).append("-")
						.append(mMonth + 1).append("-").append(mDay).toString());
			}
		}
	}

	/**
	 * 提交数据
	 * 
	 */
	private void submitData() {
		String name = et_name.getText().toString();
		String phone = et_phone.getText().toString();
		String address = et_address.getText().toString();
		if (StringUtil.empty(name)) {
			CustomToast.show("联系人不允许为空", 0);
			return;
		}
		if (StringUtil.empty(phone)) {
			CustomToast.show("联系电话不允许为空", 0);
			return;
		}
		if (StringUtil.empty(address)) {
			CustomToast.show("请输入详细地址", 0);
			return;
		}
		if (!Utils.isMobile(phone)) {
			CustomToast.show("请输入正确的手机号码", 0);
			return;
		}
		if (StringUtil.containsEmoji(et_name.getText().toString()) || StringUtil.containsEmoji(et_phone.getText().toString()) || StringUtil.containsEmoji(et_address.getText().toString())) {
			CustomToast.show("不支持输入Emoji表情符号", 0);
			return;
		}
		if (StringUtil.empty(province) || StringUtil.empty(city)
				|| StringUtil.empty(area)) {
			CustomToast.show("请选择所在地区", 0);
			return;
		}
		// tv_date

		RequestBeanBuilder builder = RequestBeanBuilder.create(true);

		builder.addBody("name", name);
		builder.addBody("phone", phone);
		builder.addBody("province", province);
		builder.addBody("city", city);
		builder.addBody("area", area);
		builder.addBody("address", address);
		builder.addBody("default", String.valueOf(checkBox.isChecked()));
		if (mType == 2) {
			builder.addBody("id", id);
		}
		ResponseData data = builder.syncRequest(builder);
		if (mType == 1) {
			data.path = NetURL.ADD_ADDRESS;
			// data.path = "http://192.168.1.187:8080/cd_portal/service/UA0001";
		} else {
			data.path = NetURL.EDIT_ADDRESS;
			// data.path = "http://192.168.1.187:8080/cd_portal/service/UA0002";
		}

		if (!isProgressShowing()) {
			showProgressDialog("正在保存，请稍候...", true);
		}
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response)
			throws Exception {
		removeProgressDialog();
		CustomToast.show("保存成功", 0);
		this.finish();
	}

	@Override
	public void notifyError(ResponseData request, ResponseData response,
			Exception e) {
		removeProgressDialog();
		String message;
		if (e != null && e instanceof ConnectException) {
			message = getString(R.string.dialog_tip_net_error);
		} else {
			message = response != null && response.data != null ? response.data
					.toString() : getString(R.string.dialog_tip_null_error);
		}
		CustomToast.show(message, Toast.LENGTH_LONG);
	}

	@Override
	public void onAreaWheelInfo(String ProviceName, String CityName,
			String AreaName) {
		province = ProviceName;
		city = CityName;
		area = AreaName;
		StringBuilder sb = new StringBuilder();
		sb.append(ProviceName).append("-");
		if (ProviceName.equals(CityName)) {
			sb.append(AreaName);
		} else if (CityName.equals(AreaName)) {
			sb.append(AreaName);
		} else {
			sb.append(CityName);
			if (AreaName != null && AreaName.length() > 0) {
				sb.append("-" + AreaName);
			}
		}
		tv_address_sele.setText(sb);
	}

}

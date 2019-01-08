package com.z012.chengdu.sc.ui.adapter;

import java.net.ConnectException;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.net.RequestBeanBuilder;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.UserAddrs;
import com.z012.chengdu.sc.ui.activity.AddressEditActivity;
import com.z012.chengdu.sc.ui.dialog.MyProgressDialog;

/**
 * 地址管理适配器
 * 
 * @author LiaoBo
 */
public class AddressManageAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater inflater;
	private List<UserAddrs> mBeans;
	private MyProgressDialog mProgressDialog;

	public AddressManageAdapter(Context context, List<UserAddrs> mBeans) {
		this.mBeans = mBeans;
		this.mContext = context;
		this.inflater = LayoutInflater.from(mContext);
	}

	public int getCount() {
		return mBeans.size();
	}

	// 列表项
	public Object getItem(int position) {
		return mBeans.get(position);
	}

	// 列表id
	public long getItemId(int position) {
		return position;
	}

	public final class ViewHolder {
		private TextView tv_name, tv_phone, tv_address, tv_edit, tv_delete;
		private LinearLayout layoutDefault;
		private CheckBox checkBox;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		final UserAddrs temp = mBeans.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.lv_address_manage_item,
					null);
			holder = new ViewHolder();
			holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.tv_phone = (TextView) convertView
					.findViewById(R.id.tv_phone);
			holder.tv_address = (TextView) convertView
					.findViewById(R.id.tv_address);
			holder.checkBox = (CheckBox) convertView
					.findViewById(R.id.checkBox);
			holder.tv_edit = (TextView) convertView.findViewById(R.id.tv_edit);
			holder.tv_delete = (TextView) convertView
					.findViewById(R.id.tv_delete);
			holder.layoutDefault = (LinearLayout) convertView
					.findViewById(R.id.layoutDefault);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		StringBuffer sbf = new StringBuffer();
		sbf.append(temp.province).append(temp.city).append(temp.area)
				.append(temp.address);

		holder.tv_name.setText(temp.name);
		holder.tv_phone.setText(temp.phone);
		holder.tv_address.setText(sbf.toString());

		if (temp.def) {
			holder.layoutDefault.setVisibility(View.VISIBLE);
			holder.checkBox.setChecked(true);
		} else {
			holder.layoutDefault.setVisibility(View.GONE);
			holder.checkBox.setChecked(false);
		}

		holder.tv_edit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent mIntent = new Intent(mContext, AddressEditActivity.class);
				mIntent.putExtra("item", temp);
				mContext.startActivity(mIntent);
			}
		});

		holder.tv_delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle("提示").setMessage("是否确定删除？").create();
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								loadData(position, temp.id);
							}
						});
				builder.setNegativeButton("取消", null);
				builder.show();
			}
		});

		holder.checkBox.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				editData(position, temp);
			}
		});

		return convertView;
	}

	/**
	 * 删除
	 */
	public void loadData(final int position, String id) {
		RequestBeanBuilder builder = RequestBeanBuilder.create(true);
		builder.addBody("id", id);
		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.DELETE_ADDRESS;
		// data.path = "http://192.168.1.187:8080/cd_portal/service/UA0003";

		if (!isProgressShowing()) {
			showProgressDialog(mContext, "正在删除，请稍候...", true, null);
		}

		DataLoader.getInstance().loadData(new DataCallback() {

			@Override
			public void preExecute(ResponseData request) {

			}

			@Override
			public void notifyMessage(ResponseData request,
					ResponseData response) throws Exception {
				removeProgressDialog();
				mBeans.remove(position);
				notifyDataSetChanged();
				CustomToast.show("删除成功", 0);
			}

			@Override
			public void notifyError(ResponseData request,
					ResponseData response, Exception e) {
				removeProgressDialog();
				String message;
				if (e != null && e instanceof ConnectException) {
					message = mContext.getString(R.string.dialog_tip_net_error);
				} else {
					// message = getString(R.string.dialog_tip_null_error);
					message = response != null && response.data != null ? response.data
							.toString() : mContext
							.getString(R.string.dialog_tip_null_error);
				}
				CustomToast.show(message, Toast.LENGTH_LONG);
			}
		}, data);
	}

	/**
	 * 编辑默认
	 */
	public void editData(final int index, final UserAddrs temp) {
		RequestBeanBuilder builder = RequestBeanBuilder.create(true);

		builder.addBody("name", temp.name);
		builder.addBody("phone", temp.phone);
		builder.addBody("province", temp.province);
		builder.addBody("city", temp.city);
		builder.addBody("area", temp.area);
		builder.addBody("address", temp.address);
		builder.addBody("default", temp.def ? "false" : "true");
		builder.addBody("id", temp.id);
		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.EDIT_ADDRESS;

		if (!isProgressShowing())
			showProgressDialog(mContext, "正在保存，请稍候...", true, null);
		DataLoader.getInstance().loadData(new DataCallback() {

			@Override
			public void preExecute(ResponseData request) {

			}

			@Override
			public void notifyMessage(ResponseData request,
					ResponseData response) throws Exception {
				removeProgressDialog();
				for (int i = 0; i < mBeans.size(); i++) {
					UserAddrs item = mBeans.get(i);
					if (index == i) {
						item.def = !temp.def;
					} else {
						item.def = false;
					}
				}
				notifyDataSetChanged();
				CustomToast.show("提交成功", 0);
			}

			@Override
			public void notifyError(ResponseData request,
					ResponseData response, Exception e) {
				removeProgressDialog();
				String message;
				if (e != null && e instanceof ConnectException) {
					message = mContext.getString(R.string.dialog_tip_net_error);
				} else {
					// message = getString(R.string.dialog_tip_null_error);
					message = response != null && response.data != null ? response.data
							.toString() : mContext
							.getString(R.string.dialog_tip_null_error);
				}
				CustomToast.show(message, Toast.LENGTH_LONG);
			}
		}, data);
	}

	/**
	 * 显示loading对话框
	 */
	public final void showProgressDialog(Context cxt, String tip,
			boolean cancelable, DialogInterface.OnCancelListener mCancel) {
		if (mProgressDialog == null) {
			mProgressDialog = new MyProgressDialog(cxt);
		}
		mProgressDialog.setMessage(tip);
		mProgressDialog.setCanceledOnTouchOutside(false);
		// mProgressDialog.setCancelable(cancelable);
		mProgressDialog.setCancelable(false);
		if (cancelable) {
			mProgressDialog.setOnCancelListener(mCancel);
		}
		mProgressDialog.show();
	}

	public final boolean isProgressShowing() {
		if (mProgressDialog != null) {
			return mProgressDialog.isShowing();
		} else {
			return false;
		}
	}

	/**
	 * 销毁loading对话框
	 */
	public final void removeProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}
}

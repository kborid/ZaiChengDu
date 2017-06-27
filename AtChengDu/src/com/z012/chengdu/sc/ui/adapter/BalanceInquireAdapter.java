package com.z012.chengdu.sc.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.StringUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.net.bean.BalanceInquireBean;

/**
 * 余额查询
 * 
 * @author LiaoBo
 */
public class BalanceInquireAdapter extends BaseAdapter {
	private Context							mContext;
	private LayoutInflater					inflater;
	private List<BalanceInquireBean.Result>	mBeans;

	public BalanceInquireAdapter(Context context, List<BalanceInquireBean.Result> mBeans) {
		this.mBeans = mBeans;
		this.mContext = context;
		this.inflater = LayoutInflater.from(context);
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
		private TextView	tv_money, tv_depict, tv_state, tv_date;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		final BalanceInquireBean.Result temp = mBeans.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.lv_balance_inquire_item, null);
			holder = new ViewHolder();
			holder.tv_money = (TextView) convertView.findViewById(R.id.tv_money);
			holder.tv_depict = (TextView) convertView.findViewById(R.id.tv_depict);
			holder.tv_state = (TextView) convertView.findViewById(R.id.tv_state);
			holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		StringBuilder sb = new StringBuilder();
		if (temp.negativeOrPositive == 1) {
			holder.tv_money.setTextColor(0xffeb3f73);// 退
			sb.append("+ ");
		} else {
			holder.tv_money.setTextColor(0xff1ab65f);// 支护
			sb.append("- ");
		}
		holder.tv_money.setText(sb.append(temp.amount));
		holder.tv_depict.setText(StringUtil.doEmpty(temp.typechar));
		holder.tv_state.setText(StringUtil.doEmpty(temp.statuschar));
		holder.tv_date.setText(DateUtil.getMillon(temp.createtimemills));

		return convertView;
	}

}

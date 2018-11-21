package com.z012.chengdu.sc.ui.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.net.image.ImageLoader.ImageCallback;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ThumbnailUtil;
import com.prj.sdk.util.Utils;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.SelectBankBean;
import com.z012.chengdu.sc.ui.activity.CashingActivity;

/**
 * 选择银行适配器
 * 
 * @author LiaoBo
 */
public class SelectBankAdapter extends BaseAdapter {

	private Context					mContext;
	private LayoutInflater			inflater;
	private List<SelectBankBean>	mBeans;
	private int						mType;
	private boolean					isBannedClick;	// 是否禁止点击
	private double					mAmount;

	/**
	 * @param context
	 * @param Beans
	 * @param type
	 *            2 银行；1微信；0支护宝
	 * @param amount 余额
	 */
	public SelectBankAdapter(Context context, boolean isBannedClick, List<SelectBankBean> Beans, int type,double amount) {
		this.mBeans = Beans;
		this.mContext = context;
		this.inflater = LayoutInflater.from(mContext);
		this.mType = type;
		this.isBannedClick = isBannedClick;
		this.mAmount = amount;
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
		private TextView	tv_bank_name, tv_card_number, tv_name;
		private ImageView	iv_bank_icon;
		private TextView	tv_alipay_accounts;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		final SelectBankBean temp = mBeans.get(position);
		try {
			if (mType == 2) {// 银行
				if (convertView == null) {
					convertView = inflater.inflate(R.layout.lv_select_bank_card_item, null);
					holder = new ViewHolder();
					holder.tv_bank_name = (TextView) convertView.findViewById(R.id.tv_bank_name);
					holder.tv_card_number = (TextView) convertView.findViewById(R.id.tv_card_number);
					holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
					holder.iv_bank_icon = (ImageView) convertView.findViewById(R.id.iv_bank_icon);
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}
				holder.tv_bank_name.setText(StringUtil.doEmpty(temp.bankname, "未知银行"));
				if (temp.thirdaccount != null && temp.thirdaccount.length() >= 16) {
					holder.tv_card_number.setText("**** **** **** " + temp.thirdaccount.substring(12));
				} else {
					holder.tv_card_number.setText(temp.thirdaccount);
				}
				holder.tv_name.setText(StringUtil.doEmpty(temp.thirdaccountname, "未知"));
				holder.iv_bank_icon.setImageResource(R.drawable.ic_unionpay);

			} else if (mType == 0) {// ali
				if (convertView == null) {
					convertView = inflater.inflate(R.layout.lv_select_alipay_item, null);
					holder = new ViewHolder();
					holder.tv_alipay_accounts = (TextView) convertView.findViewById(R.id.tv_alipay_accounts);
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}

				String count = null;
				if (StringUtil.notEmpty(temp.thirdaccount)) {// 屏蔽手机号中间位数
					if (Utils.isEmail(temp.thirdaccount)) {
						String regex = "(\\w{4})(\\w+)(\\w{0})(@\\w+)";
						count = temp.thirdaccount.replaceAll(regex, "$1***$3$4");
					} else {
						count = temp.thirdaccount.substring(0, temp.thirdaccount.length() - (temp.thirdaccount.substring(3)).length()) + "****" + temp.thirdaccount.substring(7);
					}
				}
				holder.tv_alipay_accounts.setText(count);
			}

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {// 提现
					if (isBannedClick) {
						return;
					}
					Intent intent = new Intent(mContext, CashingActivity.class);
					intent.putExtra("item", temp);
					intent.putExtra("amount", mAmount);
					mContext.startActivity(intent);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return convertView;
	}

	/**
	 * 加载图片
	 * 
	 * @param view
	 * @param scrollState
	 */
	private boolean	mBusy	= false;	// 标识是否存在滚屏操作

	public void listenStateChange(AbsListView view, int scrollState) {
		switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE : // Idle态，进行实际数据的加载显示
				mBusy = false;
				// int first = view.getFirstVisiblePosition();
				int count = view.getChildCount();
				for (int i = 0; i < count; i++) {
					ViewGroup layoutView = (ViewGroup) view.getChildAt(i);
					ImageView temp = (ImageView) layoutView.findViewById(R.id.iv_photo);
					if (temp != null && temp.getTag() != null) { // 非null说明需要加载数据
						loadImage(temp, temp.getTag(R.id.image_url).toString(), temp.getTag().toString());
					}
				}
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL :
				mBusy = true;
				break;
			case OnScrollListener.SCROLL_STATE_FLING :
				mBusy = true;
				break;
			default :
				break;
		}
	}

	/***
	 * 图片绑定
	 */
	public void loadImage(ImageView iView, String url, String tag) {
		if (!url.startsWith("http")) {
			url = NetURL.API_LINK + url;
		}
		Bitmap bm = ImageLoader.getInstance().getCacheBitmap(url);
		if (bm != null) {
			iView.setImageBitmap(ThumbnailUtil.getRoundImage(bm));
			iView.setTag(null);
			iView.setTag(R.id.image_url, null);
		} else {
			iView.setImageResource(R.drawable.iv_def_photo);
			iView.setTag(tag);
			iView.setTag(R.id.image_url, url);
			if (!mBusy) {

				ImageLoader.getInstance().loadBitmap(new ImageCallback() {
					@Override
					public void imageCallback(Bitmap bm, String url, String imageTag) {
						if (bm != null) {
							notifyDataSetChanged();
						}
					}

				}, url, tag);
			}
		}
	}

}
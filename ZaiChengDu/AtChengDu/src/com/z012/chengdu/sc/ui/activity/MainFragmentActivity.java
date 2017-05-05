package com.z012.chengdu.sc.ui.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.common.widget.custom.BadgeView;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.constants.Const;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.net.image.ImageLoader;
import com.prj.sdk.net.image.ImageLoader.ImageCallback;
import com.prj.sdk.util.ActivityTack;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ThumbnailUtil;
import com.prj.sdk.widget.CustomToast;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.ui.adapter.MainFragmentAdapter;
import com.z012.chengdu.sc.ui.base.BaseFragmentActivity;
import com.z012.chengdu.sc.ui.fragment.Tab1HomeFragment;
import com.z012.chengdu.sc.ui.fragment.Tab2ServiceFragment;
import com.z012.chengdu.sc.ui.fragment.Tab3QAFragment;

/**
 * 主函数
 * 
 * @author LiaoBo
 */
public class MainFragmentActivity extends BaseFragmentActivity implements
		OnPageChangeListener, DrawerLayout.DrawerListener, OnClickListener,
		DataCallback {
	private List<Fragment> mList;
	private MainFragmentAdapter mAdapter;
	private RadioGroup radioGroup;
	private ViewPager viewPager;
	private long exitTime = 0;
	private DrawerLayout mDrawerLayout;
	private FrameLayout left_menu;
	private ImageView iv_photo;
	// private RelativeLayout rl_balance;
	private TextView tv_name/* , tv_balance */, tv_userinfo, tv_account/*
																		 * ,
																		 * tv_order
																		 */,
			tv_qa, tv_address, tv_invite, tv_problem, tv_about;
	private boolean isOpen;
	private final int LOGIN_EXIT = 1000;
	private Tab1HomeFragment mTab1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_main_tab);
		initViews();
		dealIntent();
		initParams();
		initListeners();
	}

	public void initViews() {
		super.initViews();
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		left_menu = (FrameLayout) findViewById(R.id.left_menu);
		iv_photo = (ImageView) findViewById(R.id.iv_photo);
		tv_userinfo = (TextView) findViewById(R.id.tv_userinfo);
		tv_name = (TextView) findViewById(R.id.tv_name);
		// tv_balance = (TextView) findViewById(R.id.tv_balance);
		tv_account = (TextView) findViewById(R.id.tv_account);
		// tv_order = (TextView) findViewById(R.id.tv_order);
		tv_qa = (TextView) findViewById(R.id.tv_qa);
		tv_address = (TextView) findViewById(R.id.tv_address);
		tv_invite = (TextView) findViewById(R.id.tv_invite);
		tv_problem = (TextView) findViewById(R.id.tv_problem);
		tv_about = (TextView) findViewById(R.id.tv_about);
		// rl_balance = (RelativeLayout) findViewById(R.id.rl_balance);
	}

	@Override
	public void initParams() {
		super.initParams();
		if (SessionContext.isLogin()) {// 登录状态下验证票据是否失效，默认登录后6天在做检查
			String lastLoginTime = SharedPreferenceUtil.getInstance()
					.getString(AppConst.LAST_LOGIN_DATE, "", false);
			if (StringUtil.notEmpty(lastLoginTime)) {
				Date lastLoginDate = DateUtil.str2Date(lastLoginTime);
				if (DateUtil.getGapCount(lastLoginDate,
						new Date(System.currentTimeMillis())) >= 6) {
					loadValidateTicketExpire();
				}
			} else {// 如果没有值，则不是4.0.0版本，需要登录
				Intent intent = new Intent(Const.UNLOGIN_ACTION);
				intent.putExtra(Const.IS_SHOW_TIP_DIALOG, true);
				AppContext.mMainContext.sendBroadcast(intent);// 发送登录广播
			}
		}
		initFragmentView();
		updateDynamicUserInfo();
		UmengUpdateAgent.update(this);// 友盟渠道版本更新
		// 注册刷新广播
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(AppConst.ACTION_DYNAMIC_USER_INFO);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mBroadcastReceiver, mIntentFilter);
	}

	@Override
	public void dealIntent() {
		super.dealIntent();
		if (getIntent().getExtras() != null
				&& getIntent().getExtras().getBoolean("moreQA")) {
			radioGroup.getChildAt(2).performClick();
			mDrawerLayout
					.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED); // 关闭手势滑动
		}

		if (getIntent().getExtras() != null
				&& getIntent().getExtras().getString("path") != null) {
			LogUtil.d("JPush", "main value = "
					+ getIntent().getExtras().getString("path"));
			Intent intent = new Intent(this, HtmlActivity.class);
			intent.putExtra("path", getIntent().getExtras().getString("path"));
			startActivity(intent);
		}
	}

	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// Note that getIntent() still returns the original Intent. You can use
		// setIntent(Intent) to update it to this new Intent.
		setIntent(intent);
		dealIntent();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	/**
	 * 初始化Fragment视图
	 */
	private void initFragmentView() {
		mList = new ArrayList<Fragment>();
		mTab1 = new Tab1HomeFragment();
		mList.add(mTab1);
		mList.add(new Tab2ServiceFragment());
		mList.add(new Tab3QAFragment());
		mAdapter = new MainFragmentAdapter(getSupportFragmentManager(), mList);
		viewPager.setOffscreenPageLimit(1);
		viewPager.setAdapter(mAdapter);
		radioGroup.getChildAt(0).performClick();
	}

	@Override
	public void initListeners() {
		super.initListeners();
		radioGroup.setOnCheckedChangeListener(this);
		viewPager.setOnPageChangeListener(this);
		mDrawerLayout.setDrawerListener(this);
		// rl_balance.setOnClickListener(this);
		tv_userinfo.setOnClickListener(this);
		tv_account.setOnClickListener(this);
		// tv_order.setOnClickListener(this);
		tv_qa.setOnClickListener(this);
		tv_address.setOnClickListener(this);
		tv_invite.setOnClickListener(this);
		tv_problem.setOnClickListener(this);
		tv_about.setOnClickListener(this);
	}

	/**
	 * 展开侧滑
	 */
	public void openDrawer() {
		mDrawerLayout.openDrawer(left_menu);
	}

	public void changeTabService() {
		viewPager.setCurrentItem(1);
	}

	/**
	 * 更新用户信息
	 */
	public void updateDynamicUserInfo() {
		try {
			mTab1.refreshHeadPortrait();
			if (SessionContext.isLogin()) {
				// rl_balance.setVisibility(View.VISIBLE);
				tv_name.setOnClickListener(null);
				// tv_name.setText("");// 置空
				String url = SessionContext.mUser.USERBASIC.getHeadphotourl();
				if (url != null && url.length() > 0) {
					ImageLoader.getInstance().loadBitmap(new ImageCallback() {
						@Override
						public void imageCallback(Bitmap bm, String url,
								String imageTag) {
							if (bm != null) {
								iv_photo.setImageBitmap(ThumbnailUtil
										.getRoundImage(bm));
							}
						}

					}, url);
				}

				tv_name.setText(StringUtil.doEmpty(
						SessionContext.mUser.USERBASIC.nickname,
						SessionContext.mUser.USERBASIC.username));

				// DecimalFormat decimalFormat = new DecimalFormat("0.00");//
				// 构造方法的字符格式这里如果小数不足2位,会以0补足.
				// String price =
				// decimalFormat.format(SessionContext.mUser.USERBASIC.amount);//
				// format 返回的是字符串
				// tv_balance.setText(String.format(getString(R.string.user_balance),
				// price));// 设置余额
			} else {
				// rl_balance.setVisibility(View.GONE);
				iv_photo.setImageResource(R.drawable.def_photo_b);
				tv_name.setText("登录/注册");
				tv_name.setTextSize(15);
				tv_name.setOnClickListener(this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 加载验证票据是否失效
	 */
	public void loadValidateTicketExpire() {
		RequestBeanBuilder builder = RequestBeanBuilder.create(true);

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.VALIDATE_TICKET;
		data.flag = 1;
		DataLoader.getInstance().loadData(this, data);
	}

	/**
	 * 设置tag
	 */
	public void setTag(String i) {
		BadgeView mMsgBadge = new BadgeView(this);
		mMsgBadge.setBadgeGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		mMsgBadge.setBadgeMargin(0, 8, 23, 8);
		mMsgBadge.setTargetView(tv_qa);
		mMsgBadge.setBackgroundColor(0xffFE4895);
		mMsgBadge.setText(i);
		mMsgBadge.setVisibility(View.VISIBLE);
	}

	// private void setDefaultFragment() {
	// FragmentManager fm = getSupportFragmentManager();
	// FragmentTransaction transaction = fm.beginTransaction();
	// tab1 = new Tab1HomeFragment();
	// // 使用当前Fragment的布局替代id_content的控件
	// transaction.replace(R.id.viewPager, tab1);
	// transaction.commit();
	// }

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		try {
			switch (checkedId) {
			case R.id.qu_btn_01:
				viewPager.setCurrentItem(0);
				mDrawerLayout
						.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED); // 打开手势滑动
				break;
			case R.id.qu_btn_02:
				viewPager.setCurrentItem(1);
				mDrawerLayout
						.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED); // 关闭手势滑动
				break;
			case R.id.qu_btn_03:
				viewPager.setCurrentItem(2);
				mDrawerLayout
						.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED); // 关闭手势滑动
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (isOpen) {// 如果打开侧滑，优先关闭侧滑
				mDrawerLayout.closeDrawer(left_menu);
				return true;
			}
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次 退出程序",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				SessionContext.destroy();
				MobclickAgent.onKillProcess(this);// 调用Process.kill或者System.exit之类的方法杀死进程前保存统计数据
				DataLoader.getInstance().clearRequests();
				ActivityTack.getInstanse().exit();
			}
			return true;

		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mBroadcastReceiver);
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				String action = intent.getAction();
				LogUtil.d("Broadcast action", action);
				if (AppConst.ACTION_DYNAMIC_USER_INFO.equals(action)) {
					updateDynamicUserInfo();
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	};

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		try {
			radioGroup.getChildAt(arg0).performClick();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDrawerClosed(View arg0) {
		isOpen = false;
	}

	@Override
	public void onDrawerOpened(View arg0) {
		isOpen = true;
	}

	@Override
	public void onDrawerSlide(View arg0, float arg1) {
	}

	@Override
	public void onDrawerStateChanged(int arg0) {
	}

	@Override
	public void onClick(View v) {
		Intent mIntent = null;
		try {
			switch (v.getId()) {
			// case R.id.rl_balance :// 余额查询
			// if (!SessionContext.isLogin()) {
			// sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
			// updateDynamicUserInfo();
			// return;
			// }
			// mIntent = new Intent(this, BalanceInquireActivity.class);
			// startActivity(mIntent);
			// break;
			case R.id.tv_name:
				if (!SessionContext.isLogin()) {
					sendBroadcast(new Intent(
							UnLoginBroadcastReceiver.ACTION_NAME));
					updateDynamicUserInfo();
				}
				break;
			case R.id.tv_userinfo:// 编辑资料
				if (!SessionContext.isLogin()) {
					sendBroadcast(new Intent(
							UnLoginBroadcastReceiver.ACTION_NAME));
					updateDynamicUserInfo();
					return;
				}
				mIntent = new Intent(this, PersonalDataActivity.class);
				startActivityForResult(mIntent, LOGIN_EXIT);
				break;
			case R.id.tv_account:// 帐号安全
				if (!SessionContext.isLogin()) {
					sendBroadcast(new Intent(
							UnLoginBroadcastReceiver.ACTION_NAME));
					updateDynamicUserInfo();
					return;
				}
				mIntent = new Intent(this, AccountSecurityActivity.class);
				mIntent.putExtra("Tag", true);
				startActivityForResult(mIntent, LOGIN_EXIT);
				break;
			// case R.id.tv_order :// 订单
			// if (!SessionContext.isLogin()) {
			// sendBroadcast(new Intent(UnLoginBroadcastReceiver.ACTION_NAME));
			// updateDynamicUserInfo();
			// return;
			// }
			// StringBuilder sb = new StringBuilder();
			// sb.append(NetURL.PAYORDER_LIST).append(SessionContext.mUser.USERBASIC.id).append("&siteid=").append(SessionContext.getAreaInfo(1));
			// mIntent = new Intent(this, HtmlActivity.class);
			// mIntent.putExtra("path", sb.toString());
			// startActivity(mIntent);
			// break;
			case R.id.tv_qa:
				if (!SessionContext.isLogin()) {
					sendBroadcast(new Intent(
							UnLoginBroadcastReceiver.ACTION_NAME));
					updateDynamicUserInfo();
					return;
				}
				mIntent = new Intent(this, MyQAActivity.class);
				startActivity(mIntent);
				break;
			case R.id.tv_address:
				if (!SessionContext.isLogin()) {
					sendBroadcast(new Intent(
							UnLoginBroadcastReceiver.ACTION_NAME));
					updateDynamicUserInfo();
					return;
				}
				mIntent = new Intent(this, AddressManageActivity.class);
				startActivity(mIntent);
				break;
			case R.id.tv_problem:
				mIntent = new Intent(this, WebViewActivity.class);
				String url = SharedPreferenceUtil.getInstance().getString(
						AppConst.PROBLEM, "", true);
				mIntent.putExtra("path", url);
				mIntent.putExtra("title", "常见问题");
				startActivity(mIntent);
				break;
			case R.id.tv_invite:
				if (!SessionContext.isLogin()) {
					sendBroadcast(new Intent(
							UnLoginBroadcastReceiver.ACTION_NAME));
					updateDynamicUserInfo();
					return;
				}
				mIntent = new Intent(this, InviteActivity.class);
				startActivity(mIntent);
				break;
			case R.id.tv_about:
				mIntent = new Intent(this, AboutActivity.class);
				startActivity(mIntent);
				break;

			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// mDrawerLayout.closeDrawers();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case LOGIN_EXIT:
			updateDynamicUserInfo();// 退出登录或编辑资料，重置界面
			break;

		default:
			break;
		}

	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response)
			throws Exception {

	}

	@Override
	public void notifyError(ResponseData request, ResponseData response,
			Exception e) {
		CustomToast.show("登录超时，请重新登录！", 0);
		updateDynamicUserInfo();
	}
}

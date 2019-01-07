package com.z012.chengdu.sc.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.prj.sdk.util.Utils;
import com.z012.chengdu.sc.R;

/**
 * 定制一个对话框，统一项目各个页面的对话框风格样式和属性,方便调用
 * 
 * @author kborid
 */
public class CustomDialog extends Dialog {

	private Button tip_left, tip_right;
	private View tip_line;
	private TextView tip_title, tip_content;

	public CustomDialog(Context context) {
		this(context, null);
	}

	/**
	 * @param context 上下文
	 * @param title 窗口标题
	 */
	public CustomDialog(Context context, String title) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tip_dialog_view);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));// 去除窗口透明部分显示的黑色
		LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
		p.width = (int) (Utils.mScreenWidth * 0.89);
		p.height = (int) (p.width * 0.57);
		getWindow().setAttributes(p);

		setCanceled(true);// 点击空白区域默认消失
		initViews();
		setTitle(title);
	}

	/**
	 * 初始化ui
	 */
	private void initViews() {
		tip_line = findViewById(R.id.tip_line);
		tip_left = (Button) findViewById(R.id.tip_left);
		tip_right = (Button) findViewById(R.id.tip_right);
		tip_content = (TextView) findViewById(R.id.tip_content);
		tip_title = (TextView) findViewById(R.id.tip_title);
	}

	/**
	 * 设置点击空白区域是否消失
	 *
	 * @param bool
	 */
	public final void setCanceled(boolean bool) {
		this.setCanceledOnTouchOutside(bool);
	}

	/**
	 * 点击事件
	 *
	 * @param mCallBackListener
	 */
	public final void setListeners(final onCallBackListener mCallBackListener) {
		tip_left.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallBackListener.leftBtn(CustomDialog.this);

			}
		});
		tip_right.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallBackListener.rightBtn(CustomDialog.this);
			}
		});
	}

	/**
	 * 是否显示标题栏
	 *
	 * @param mTitle
	 */
	public void setTitle(String mTitle) {
		if (null == mTitle || "".equals(mTitle)) {
			findViewById(R.id.tip_layout_title).setVisibility(View.GONE);
		} else {
			findViewById(R.id.tip_layout_title).setVisibility(View.VISIBLE);
		}
		tip_title.setText(mTitle);
	}

	/**
	 * 设置显示文本
	 *
	 * @param btn_left
	 * @param btn_right
	 */
	public void setBtnText(String btn_left, String btn_right) {
		if (null == btn_left || "".equals(btn_left)) {
			tip_left.setVisibility(View.GONE);
			tip_line.setVisibility(View.GONE);
		} else {
			tip_left.setVisibility(View.VISIBLE);
		}
		if (null == btn_right || "".equals(btn_right)) {
			tip_right.setVisibility(View.GONE);
			tip_line.setVisibility(View.GONE);
		} else {
			tip_right.setVisibility(View.VISIBLE);
		}

		tip_left.setText(btn_left);
		tip_right.setText(btn_right);
	}

	/***
	 * 设置窗口信息并显示窗口
	 */
	public void show(String message) {
		tip_content.setText(message);
		show();
	}

	/**
	 * 取消窗口显示
	 */
	public void dismiss() {
		super.dismiss();
	}

	public boolean isShowing() {
		return super.isShowing();
	}

	/**
	 * ui层 点击事件 回调
	 */
	public interface onCallBackListener {

		public void leftBtn(CustomDialog dialog);

		public void rightBtn(CustomDialog dialog);
	}

}
package com.z012.chengdu.sc.ui.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

public class SingleSelect extends LinearLayout {

	private ArrayAdapter<String> adpater;
	private List<String> dropDownData;
	private LinearLayout mainLayout;
	private Map<String,String> map = new HashMap<String,String>();
	private PopupWindow popupWindow;
	private LinearLayout dialogLayout;
	private ListView listView;
	private View			mView;
	private Context			mContext;
	
	public SingleSelect(Context context,View paras) {
		super(context);
		mView = paras;
		InitializeComponent();
	}
	
	private void InitializeComponent() {
		this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
		this.setOrientation(LinearLayout.VERTICAL);
		mainLayout = new LinearLayout(mContext);
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER_VERTICAL;
		mainLayout.setLayoutParams(lp);
		mainLayout.setOrientation(LinearLayout.VERTICAL);
		
		popupWindow = new PopupWindow(mContext);
		//设置SelectPicPopupWindow弹出窗体的宽
		popupWindow.setWidth(LayoutParams.FILL_PARENT);
		//设置SelectPicPopupWindow弹出窗体的高
		popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
		//设置SelectPicPopupWindow弹出窗体可点击
		popupWindow.setFocusable(true);
		//设置SelectPicPopupWindow弹出窗体动画效果
//		popupWindow.setAnimationStyle(R.style.AnimBottom);
		
//		dialogLayout = new LinearLayout(mContext);
		dialogLayout = new LinearLayout(mContext);
		dialogLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		dialogLayout.setOrientation(LinearLayout.VERTICAL);
		dialogLayout.setBackgroundColor(Color.WHITE);
		
//		listView = new ListView(mContext);
		listView = new ListView(mContext);
		
		dropDownData = new ArrayList<String>();
		dialogLayout.addView(listView);
		popupWindow.setContentView(dialogLayout);
	}

//	public void SetModel(Map _selectViewModel) {
//        if (_selectViewModel == null) return;
//        for (List<String> _item : _selectViewModel.Items)
//        {
//        	dropDownData.add(_item.Name);
//        	map.put(_item.Name, _item.Value);
//        }
////        adpater=new ArrayAdapter<String>(mContext,
////        		android.R.layout.simple_spinner_dropdown_item,dropDownData);
//		adpater = new ArrayAdapter<String>(mContext,
//        		android.R.layout.simple_spinner_dropdown_item,dropDownData);
//        adpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        listView.setAdapter(adpater);
//        listView.setCacheColorHint(Color.argb(0, 0, 0, 0));
//        listView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//					long arg3) {
//				JSONData _jsonResult = new JSONData();
//                _jsonResult.addStringValue("Name", adpater.getItem(arg2));
//                _jsonResult.addStringValue("Value", map.get(adpater.getItem(arg2)));
//                UIViewMgr.Instance().AddFinishedResult(new InvokeResult(paras, _jsonResult));
//                popupWindow.dismiss();
//			}
//		});
//        
//        popupWindow.showAtLocation(mView, Gravity.BOTTOM, 0, 0);
//	}
	
}

package com.z012.chengdu.sc.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.Base64;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ThumbnailUtil;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.action.LocationManagerBD;
import com.z012.chengdu.sc.action.LocationManagerBD.LocationCallback;
import com.z012.chengdu.sc.api.RequestBeanBuilder;
import com.z012.chengdu.sc.app.SessionContext;
import com.z012.chengdu.sc.broatcast.UnLoginBroadcastReceiver;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.ui.adapter.AddPhotoGrideViewAdapter;
import com.z012.chengdu.sc.ui.base.BaseActivity;
import com.z012.chengdu.sc.ui.dialog.GetPicDialog;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

/**
 * 我说说
 * 
 * @author LiaoBo
 * 
 */
public class QAISayActivity extends BaseActivity implements DataCallback,
		LocationCallback, DialogInterface.OnCancelListener, OnItemClickListener {
	private EditText et_content;
	private double mLatitude, mLongitude;
	private String mLocation;
	private GridView gridView;
	private AddPhotoGrideViewAdapter mAddPhotoAdapter;
	private List<Uri> mImgUris = new ArrayList<Uri>();
	private List<String> mFileName = new ArrayList<String>();
	private Uri mCameraFile;
	private final int IMG_PREVIEW_REQUEST = 11;

	@Override
	protected int getLayoutResId() {
		return R.layout.ui_isay_act;
	}

	@Override
	public void initParams() {
		super.initParams();
		tv_center_title.setText("有问必答");
		tv_right_title.setVisibility(View.VISIBLE);
		tv_right_title.setText("提问");

		et_content = (EditText) findViewById(R.id.et_content);
		gridView = (GridView) findViewById(R.id.gridView);

		if (!LocationManagerBD.getIns().isStart()) {
			LocationManagerBD.getIns().startBaiduLocation(
					this.getApplicationContext(), this);
		}
		mAddPhotoAdapter = new AddPhotoGrideViewAdapter(this, mImgUris);
		gridView.setAdapter(mAddPhotoAdapter);
	}

	@Override
	public void initListeners() {
		super.initListeners();
		gridView.setOnItemClickListener(this);
		tv_right_title.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if (!SessionContext.isLogin()) {
						sendBroadcast(new Intent(
								UnLoginBroadcastReceiver.ACTION_NAME));
						return;
					}

					if (StringUtil.empty(et_content.getText())) {
						CustomToast.show("请输入内容", 0);
						return;
					}
					if (StringUtil.containsEmoji(et_content.getText().toString())) {
						CustomToast.show("问题描述不能包含Emoji表情符号", 0);
						return;
					}

					if (mImgUris == null || mImgUris.isEmpty()) {
						loadData();
					} else {
						for (int i = 0; i < mImgUris.size(); i++) {
							Bitmap bm = MediaStore.Images.Media.getBitmap(
									getContentResolver(), mImgUris.get(i));
							uploadImg(bm);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 添加图片并刷新视图
	 */
	public void addImgUri(Uri uri) {
		mImgUris.add(uri);
		mAddPhotoAdapter.notifyDataSetChanged();
	}

	/**
	 * 处理图片
	 */
	public void dealImg() {
		GetPicDialog picDialog = new GetPicDialog(this);
		mCameraFile = picDialog.getPicPathUri();
		picDialog.showDialog();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK) {
			return;
		}
		try {
			switch (requestCode) {
			case AppConst.ACTIVITY_GET_IMAGE:// 图库
				mCameraFile = data.getData();
				if (mCameraFile != null) {
					// ContentResolver resolver = getContentResolver();
					// Bitmap mBitmap =
					// MediaStore.Images.Media.getBitmap(resolver, mCameraFile);
					// uploadImg(mBitmap);
					addImgUri(mCameraFile);
				}
				break;
			case AppConst.ACTIVITY_IMAGE_CAPTURE:// 相机
				// Bitmap bm =
				// MediaStore.Images.Media.getBitmap(getContentResolver(),
				// mCameraFile);
				// uploadImg(bm);
				addImgUri(mCameraFile);
				break;
			case IMG_PREVIEW_REQUEST:
				List<String> list = data.getStringArrayListExtra("uri");
				for (int i = 0; i < list.size(); i++) {
					mImgUris.remove(Uri.parse(list.get(i)));
				}
				mAddPhotoAdapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			CustomToast.show("获取图片失败,请重试", 0);
		}

	}

	/**
	 * 上传微观图片 。图片缩略图最大为480x800的80%精度质量
	 * 
	 */
	private void uploadImg(Bitmap bm) {

		RequestBeanBuilder builder = RequestBeanBuilder.create(true);

		try {
			// mBitmap = ThumbnailUtil.getImageThumbnail(bm, 180, 240);
			builder.addBody("IMGSTR", Base64.encodeToString(ThumbnailUtil
					.getImageThumbnailBytes(
							ThumbnailUtil.getImageThumbnail(bm, 480, 800), 80)));
		} catch (Exception e) {
			e.printStackTrace();
		}

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.WG_UPLOD_IMG;
		data.flag = 1;

		if (!isProgressShowing()) {
			showProgressDialog("正在上传图片，请稍候...", true);
		}
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	/**
	 * 加载数据
	 */
	private void loadData() {
		String content = et_content.getText().toString().trim();
		if (StringUtil.empty(content)) {
			CustomToast.show("请输入内容", 0);
			removeProgressDialog();
			return;
		}
		RequestBeanBuilder builder = RequestBeanBuilder.create(true);

		builder.addBody("HAPPEN_TIME", DateUtil.getCurDateStr());
		builder.addBody("LOCATION", mLocation == null ? "" : mLocation);// 地理位置
		builder.addBody("IS_PUBLIC", "1");// 公开 1不公开0
		builder.addBody("CONTENT", content);
		// builder.addBody("TITLE", title)
		builder.addBody("CITY_ID", SessionContext.getAreaInfo(1));
		// body.put("USER_ID", "");
		builder.addBody("LONGITUDE", String.valueOf(mLongitude));
		builder.addBody("LATITUDE", String.valueOf(mLatitude));
		builder.addBody("CONTACT_EMAIL", "");// 没有邮箱
		// body.put("CONTACT_MOBILE", "");
		builder.addBody("LOGIN", SessionContext.mUser.USERAUTH.login);

		StringBuilder sb = new StringBuilder();
		// for (Map.Entry<String, String> entry : mFileName.entrySet()) {
		// sb.append(entry.getKey()).append("&");
		// }
		for (String name : mFileName) {
			sb.append(name).append("&");
		}
		String filename = sb.toString();
		if (filename.endsWith("&")) {
			filename = filename.substring(0, sb.length() - 1);
		}

		builder.addBody("FILE_NAME", filename);

		ResponseData data = builder.syncRequest(builder);
		data.path = NetURL.WG_RELEASE;
		data.flag = 2;

		if (!isProgressShowing()) {
			showProgressDialog("正在提交，请稍候...", true);
		}
		requestID = DataLoader.getInstance().loadData(this, data);
	}

	@Override
	public void preExecute(ResponseData request) {

	}

	@Override
	public void notifyMessage(ResponseData request, ResponseData response)
			throws Exception {
		if (request.flag == 1) {
			JSONObject mJson = JSON.parseObject(response.body.toString());
			String fileName = mJson.getString("fileName");
			mFileName.add(fileName);
			if (mFileName.size() == mImgUris.size()) {
				loadData();// 上传成功调用提交功能
			}
			// CustomToast.show("上传成功", 0);
		} else if (request.flag == 2) {
			removeProgressDialog();
			CustomToast.show("提交成功", 0);
			this.finish();
		}

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
	public void onCancel(DialogInterface dialog) {
		DataLoader.getInstance().clear(requestID);
		removeProgressDialog();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// if (mBitmap != null && !mBitmap.isRecycled()) {
		// mBitmap.recycle();
		// mBitmap = null;
		// }
	}

	@Override
	public void onLocationInfo(BDLocation location) {
		if (location == null) {
			return;
		}
		try {
			// StringBuilder sb = new StringBuilder();
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();
			// sb.append(location.getProvince());// 省
			// sb.append(location.getCity());// 市
			// sb.append(location.getDistrict());// 区/县
			if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				String mAddr = location.getAddrStr();
				// et_location.setText(StringUtil.doEmpty(mAddr));
				mLocation = StringUtil.doEmpty(mAddr);
			}
			LocationManagerBD.getIns().stopBaiduLocation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		try {
			if (arg2 >= mImgUris.size()) {
				dealImg();
				return;
			}
			Intent intent = new Intent(QAISayActivity.this,
					ImageScaleActivity.class);
			Bundle bundle = new Bundle();
			bundle.putInt("currentItem", arg2);
			ArrayList<String> list = new ArrayList<String>();
			for (Uri uri : mImgUris) {// 转换uri encoded，方便传值
				list.add(uri.toString());
			}
			bundle.putStringArrayList("uri", list);// 传递当前点击图片设置的uri
			intent.putExtras(bundle);
			startActivityForResult(intent, IMG_PREVIEW_REQUEST);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

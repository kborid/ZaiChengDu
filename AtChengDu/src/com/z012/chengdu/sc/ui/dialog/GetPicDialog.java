package com.z012.chengdu.sc.ui.dialog;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.prj.sdk.util.GUIDGenerator;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.ui.dialog.CustomDialogUtil.onCallBackListener;

/**
 * 获取图片对话框，包括相机和图库2中方式选择
 * 
 * @author LiaoBo
 */
public class GetPicDialog {
	private Activity			mAct;
	private CustomDialogUtil	mTip;
	private File				mCameraFile;

	public GetPicDialog(Activity context) {
		this(context,null);
	}
	
	/**
	 * 获取图片
	 * 
	 * @param context
	 * @param imgName 图片名称，如果为空默认生成唯一uuid作为名称
	 */
	public GetPicDialog(Activity context, String imgName) {
		this.mAct = context;
		mTip = new CustomDialogUtil(mAct);
		if (imgName == null || imgName.length() == 0) {
			mCameraFile = new File(Utils.getFolderDir("pic"), GUIDGenerator.generate() + ".jpg");
		} else {
			mCameraFile = new File(Utils.getFolderDir("pic"), imgName + ".jpg");
		}
		mCameraFile.deleteOnExit();// 虚拟机关闭删除文件
	}

	/**
	 * 获取手机相机图片路径uri
	 */
	public Uri getPicPathUri() {
		return Uri.fromFile(mCameraFile);
	}
	
	/**
	 * 获取手机相机图片路径
	 */
	public File getPicCameraFile() {
		return mCameraFile;
	}

	/**
	 * 显示
	 */
	public void showDialog() {
		mTip.setBtnText("图库", "相机");
		mTip.show("请选择图片获取方式");
		mTip.setListeners(new onCallBackListener() {
			public void rightBtn(CustomDialogUtil dialog) {
				if (Utils.isSDCardEnable()) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, getPicPathUri());
					intent.putExtra("android.intent.extra.screenOrientation", false);
					mAct.startActivityForResult(intent, AppConst.ACTIVITY_IMAGE_CAPTURE);
				} else {
					CustomToast.show("内存卡不可用，请检测内存卡", Toast.LENGTH_LONG);
				}
				dialog.dismiss();

			}

			public void leftBtn(CustomDialogUtil dialog) {
				if (Utils.isSDCardEnable()) {
					Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					i.setType("image/*");
					mAct.startActivityForResult(i, AppConst.ACTIVITY_GET_IMAGE);
				} else {
					CustomToast.show("内存卡不可用，请检测内存卡", Toast.LENGTH_LONG);
				}
				dialog.dismiss();

			}
		});
	}
}

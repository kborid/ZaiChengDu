package com.z012.chengdu.sc.ui.dialog;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.prj.sdk.util.GUIDGenerator;
import com.prj.sdk.util.ToastUtil;
import com.prj.sdk.util.Utils;
import com.z012.chengdu.sc.BuildConfig;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.ui.dialog.CustomDialog.onCallBackListener;

import java.io.File;

/**
 * 获取图片对话框，包括相机和图库2中方式选择
 *
 * @author LiaoBo
 */
public class GetPicDialog {
    private Activity mAct;
    private CustomDialog mTip;
    private File mCameraFile;

    public GetPicDialog(Activity context) {
        this(context, null);
    }

    /**
     * 获取图片
     *
     * @param context
     * @param imgName 图片名称，如果为空默认生成唯一uuid作为名称
     */
    public GetPicDialog(Activity context, String imgName) {
        this.mAct = context;
        mTip = new CustomDialog(mAct);
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
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(mAct, BuildConfig.APPLICATION_ID + ".fileprovider", mCameraFile);
        } else {
            uri = Uri.fromFile(mCameraFile);
        }
        return uri;
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
            public void rightBtn(CustomDialog dialog) {
                if (Utils.isSDCardEnable()) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, getPicPathUri());
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putExtra("android.intent.extra.screenOrientation", false);
                    mAct.startActivityForResult(intent, AppConst.ACTIVITY_IMAGE_CAPTURE);
                } else {
                    ToastUtil.show("内存卡不可用，请检测内存卡", Toast.LENGTH_LONG);
                }
                dialog.dismiss();

            }

            public void leftBtn(CustomDialog dialog) {
                if (Utils.isSDCardEnable()) {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    i.setType("image/*");
                    mAct.startActivityForResult(i, AppConst.ACTIVITY_GET_IMAGE);
                } else {
                    ToastUtil.show("内存卡不可用，请检测内存卡", Toast.LENGTH_LONG);
                }
                dialog.dismiss();

            }
        });
    }
}

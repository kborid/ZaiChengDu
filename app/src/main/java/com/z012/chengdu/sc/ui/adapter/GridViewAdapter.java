package com.z012.chengdu.sc.ui.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.db.SQLiteTemplate;
import com.prj.sdk.db.SQLiteTemplate.RowMapper;
import com.prj.sdk.net.down.DownCallback;
import com.prj.sdk.net.down.DownLoaderTask;
import com.prj.sdk.util.NetworkUtil;
import com.prj.sdk.util.UIHandler;
import com.prj.sdk.util.Utils;
import com.prj.sdk.widget.CustomToast;
import com.prj.sdk.zip.ZipExtractorCallback;
import com.prj.sdk.zip.ZipExtractorTask;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.bean.DownInfoBean;
import com.z012.chengdu.sc.net.bean.PushAppBean;
import com.z012.chengdu.sc.helper.ForbidFastClickHelper;
import com.z012.chengdu.sc.ui.activity.HtmlActivity;
import com.z012.chengdu.sc.ui.activity.MainFragmentActivity;
import com.z012.chengdu.sc.ui.dialog.CustomDialog;
import com.z012.chengdu.sc.ui.dialog.CustomDialog.onCallBackListener;

import java.io.File;
import java.util.List;

/**
 * 首页推荐应用九宫格展示 适配器
 * 
 * @author LiaoBo
 */
public class GridViewAdapter extends BaseAdapter implements DownCallback, ZipExtractorCallback {
	private Context				mContext;
	private LayoutInflater		inflater;
	private List<PushAppBean>	mBeans;
	private ZipExtractorTask	zipTask;
	private DownLoaderTask		downTask;
	private String				entrance;			// html程序入口
	private SQLiteTemplate		mSQLiteTemplate;
	private String				internalver;
	private String				appver;

	public GridViewAdapter(Context context, List<PushAppBean> mBeans) {
		this.mBeans = mBeans;
		this.mContext = context;
		this.inflater = LayoutInflater.from(mContext);
		this.mSQLiteTemplate = SQLiteTemplate.getInstance(AppContext.mDBManager);
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
		private TextView	tv_title;
		private ImageView	imageView;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		final PushAppBean temp = mBeans.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.gv_hot_service_item, null);
			holder = new ViewHolder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.img);
			holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
			// holder.img.setLayoutParams(new GridView.LayoutParams(MDMUtils.mScreenWidth / 4, MDMUtils.mScreenWidth / 4));//
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// if (temp.isshowback != null && temp.isshowback.equals("0")) {// 0不显示
		// return convertView;
		// }

		holder.tv_title.setText(temp.appname);

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ForbidFastClickHelper.isForbidFastClick()) {
					return;
				}

				if (!temp.appurls.equals("ShowAllService")) {
					Intent mIntent = new Intent(mContext, HtmlActivity.class);
					mIntent.putExtra("title", temp.appname);
					mIntent.putExtra("path", temp.appurls);
					mIntent.putExtra("id", temp.id);
					mContext.startActivity(mIntent);
				} else {
					((MainFragmentActivity) mContext).changeTabService();
				}
			}
		});

		UIHandler.post(new Runnable() {
			@Override
			public void run() {
				if (temp.appurls.equals("ShowAllService")) {
					Glide.with(mContext).load(R.drawable.iv_service_all).into(holder.imageView);
				} else {
					String url = null;
					if (temp.imgurls != null) {
						if (!temp.imgurls.startsWith("http")) {
							url = NetURL.API_LINK + temp.imgurls;
						}
						Glide.with(mContext).load(url).placeholder(R.drawable.round_loading).into(holder.imageView);
					}
				}
			}
		});

		return convertView;
	}
	/**
	 * 下载或解压处理或进入应用
	 */
	public void doDeal(String appid, String url) {
		try {
			if (Utils.isFolderDir("resource/" + appid)) {// 存在跳转
				StringBuilder sb = new StringBuilder();
				sb.append(Utils.getFolderDir("resource")).append(appid);
				sb.append(File.separator).append(getFileName(sb.toString()));
				sb.append(entrance);
				sb.insert(0, "file://");
				Intent intent = new Intent(mContext, HtmlActivity.class);// HtmlActivity WebActivity
				// intent.putExtra("path", "file:///android_asset/cd_apps_water/webapp/default" + entryid);
				intent.putExtra("path", sb.toString());
				mContext.startActivity(intent);
			} else if (Utils.isFolderDir("zip/" + appid)) {// 有下载就解压
				if (zipTask == null || zipTask.getStatus() != AsyncTask.Status.RUNNING) {
					zipTask = new ZipExtractorTask(mContext, Utils.getFolderDir("zip") + appid, Utils.getFolderDir("resource") + appid, true, this);
					zipTask.execute();
				}
			} else {// 下载
				downFile(url, appid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void down(String url, String local, int down_status, String filename) {
		if (down_status == 1) {
			try {
				ContentValues contentValues = new ContentValues();
				contentValues.put("id", filename);
				contentValues.put("internalver", internalver);
				contentValues.put("appver", appver);
				if (mSQLiteTemplate.isExistsByField("down_log", "id", filename)) {// 记录数据

					mSQLiteTemplate.updateById("down_log", filename, contentValues);
				} else {
					mSQLiteTemplate.insert("down_log", contentValues);
				}

				if (zipTask == null || zipTask.getStatus() != AsyncTask.Status.RUNNING) {
					zipTask = new ZipExtractorTask(mContext, local, Utils.getFolderDir("resource") + filename, true, this);
					zipTask.execute();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (down_status == 0) {
			// CustomToast.show("下载任务已取消", 0);
		} else {
			CustomToast.show("下载失败，已取消", 0);
		}
	}

	@Override
	public void unZip(String inPath, String outPath, int status) {
		if (status == 1) {
			StringBuilder sb = new StringBuilder();
			sb.append("file://").append(outPath).append(File.separator).append(getFileName(outPath)).append(entrance);
			Intent intent = new Intent(mContext, HtmlActivity.class);
			intent.putExtra("path", sb.toString());
			mContext.startActivity(intent);
		} else if (status == 0) {
			// CustomToast.show("解压任务已取消", 0);
		} else {
			CustomToast.show("解压失败，请重试", 0);
		}
	}

	/**
	 * 获取当前文件目录子目录文件名
	 * 
	 * @param file
	 */
	public String getFileName(String file) {
		File tempFile = new File(file);
		if (tempFile.isDirectory()) // 是不是目录
			return tempFile.list()[0];// 返回该目录下所有文件及文件夹数组中的0个元素的目录名
		return "";
	}

	/**
	 * 判断升级处理
	 */
	public void doJudgeUpgrade(String appver, String appid, String url) {
		try {

			DownInfoBean temp = mSQLiteTemplate.queryForObject(new RowMapper<DownInfoBean>() {
				@Override
				public DownInfoBean mapRow(Cursor cursor, int index) {
					DownInfoBean temp = new DownInfoBean();
					// temp.id=cursor.getString(cursor.getColumnIndex("id"));
					temp.internalver = cursor.getString(cursor.getColumnIndex("internalver"));
					temp.appver = cursor.getString(cursor.getColumnIndex("appver"));
					return temp;
				}
			}, "select * from down_log where id= ? ", new String[]{appid});
			if (temp == null) {// 初次下载，或未解压 ，直接再次下载
				// doDeal(appid, url);
				downFile(url, appid);
			} else {
				if (appver.compareTo(temp.appver) > 0) {// 有更新，重新下载
					downFile(url, appid);
				} else {
					doDeal(appid, url);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 下载文件
	 */
	private void downFile(final String url, final String appid) {
		if (downTask == null || downTask.getStatus() != AsyncTask.Status.RUNNING) {
			if (NetworkUtil.isNetworkAvailable()) {
				if (NetworkUtil.isWifi()) {
					downTask = new DownLoaderTask(mContext, url, appid, true, this);
					downTask.execute();
				} else {
					downTask = new DownLoaderTask(mContext, url, appid, true, this);
					CustomDialog mTip = new CustomDialog(mContext);
					mTip.setBtnText("取消", "确定");
					mTip.show(mContext.getResources().getText(R.string.dialog_tip).toString());
					mTip.setCanceledOnTouchOutside(false);
					mTip.setListeners(new onCallBackListener() {

						@Override
						public void rightBtn(CustomDialog dialog) {
							dialog.dismiss();
							downTask.execute();
						}

						@Override
						public void leftBtn(CustomDialog dialog) {
							dialog.dismiss();
							downTask.cancel(true);
						}
					});
				}
			} else {
				CustomToast.show("暂无网络...", 0);
			}
		}
	}
}

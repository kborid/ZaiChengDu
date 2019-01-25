package com.z012.chengdu.sc.ui.activity.user;

import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.prj.sdk.net.bean.ResponseData;
import com.prj.sdk.net.data.DataCallback;
import com.prj.sdk.net.data.DataLoader;
import com.prj.sdk.util.Base64;
import com.prj.sdk.util.DateUtil;
import com.prj.sdk.util.SharedPreferenceUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.ThumbnailUtil;
import com.prj.sdk.util.ToastUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.SessionContext;
import com.z012.chengdu.sc.constants.AppConst;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.net.request.RequestBeanBuilder;
import com.z012.chengdu.sc.net.entity.UserInfo;
import com.z012.chengdu.sc.ui.BaseActivity;
import com.z012.chengdu.sc.ui.dialog.AreaWheelDialog;
import com.z012.chengdu.sc.ui.dialog.AreaWheelDialog.AreaWheelCallback;
import com.z012.chengdu.sc.ui.dialog.GetPicDialog;

import java.net.ConnectException;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 个人资料
 *
 * @author LiaoBo
 */
public class PersonalDataActivity extends BaseActivity implements DataCallback, DialogInterface.OnCancelListener, DatePickerDialog.OnDateSetListener, AreaWheelCallback {

    @BindView(R.id.tv_birthday)
    TextView tv_birthday;
    @BindView(R.id.tv_address)
    TextView tv_address;
    @BindView(R.id.tv_marriage)
    TextView tv_marriage;
    @BindView(R.id.tv_sex)
    TextView tv_sex;
    @BindView(R.id.et_nickname)
    EditText et_nickname;
    @BindView(R.id.iv_photo)
    ImageView iv_photo;

    private Uri mCameraFile;
    private int mYear = 1990, mMonth = 1, mDay = 1;

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_personal_data;
    }

    @Override
    public void initParams() {
        super.initParams();
        tv_center_title.setText("编辑资料");
        tv_right_title.setText("保存");
        boolean mIsAuth = null != SessionContext.mCertUserAuth && SessionContext.mCertUserAuth.isAuth;
        tv_sex.setEnabled(!mIsAuth);

        setHeadPortrait(SessionContext.mUser.USERBASIC.getHeadphotourl());

        String birthday = SessionContext.mUser.USERBASIC.birthday;
        if (StringUtil.notEmpty(birthday)) {
            tv_birthday.setText(DateUtil.getY_M_D(birthday));
        }

        if (SessionContext.mUser.LOCALUSER != null && SessionContext.mUser.LOCALUSER.residence != null) {
            tv_address.setText(StringUtil.doEmpty(SessionContext.mUser.LOCALUSER.residence));
        } else {
            SessionContext.mUser.LOCALUSER = new UserInfo.LocalUser();
        }

        if (mIsAuth && null != SessionContext.mCertUserAuth.userAuth) {
            et_nickname.setText(SessionContext.mCertUserAuth.userAuth.name);
            et_nickname.setEnabled(false);
        } else {
            et_nickname.setText(!TextUtils.isEmpty(SessionContext.mUser.USERBASIC.nickname) ? SessionContext.mUser.USERBASIC.nickname : SessionContext.mUser.USERBASIC.username);
            et_nickname.setEnabled(true);
        }

        if ("01".equals(SessionContext.mUser.USERBASIC.sex)) {
            tv_sex.setText("男");
        } else if ("02".equals(SessionContext.mUser.USERBASIC.sex)) {
            tv_sex.setText("女");
        }

        if ("01".equals(SessionContext.mUser.USERBASIC.marry)) {
            tv_marriage.setText("已婚");// 婚姻 01：已婚，02：未婚，03：保密
        } else if ("02".equals(SessionContext.mUser.USERBASIC.marry)) {
            tv_marriage.setText("未婚");
        } else if ("03".equals(SessionContext.mUser.USERBASIC.marry)) {
            tv_marriage.setText("保密");
        }
    }

    @OnClick(R.id.tv_birthday)
    void birthday() {
        new DatePickerDialog(this, this, mYear, mMonth, mDay).show();
    }

    @OnClick(R.id.tv_right_title)
    void right() {
        if (StringUtil.containsEmoji(et_nickname.getText().toString())) {
            ToastUtil.show("昵称不能包含Emoji表情符号", 0);
            return;
        }
        checkDataAndLoad();
    }

    @OnClick(R.id.iv_photo)
    void photo() {
        GetPicDialog picDialog = new GetPicDialog(this);
        mCameraFile = picDialog.getPicPathUri();
        picDialog.showDialog();
    }

    @OnClick(R.id.tv_address)
    void address() {
        AreaWheelDialog dialog = new AreaWheelDialog(this, this);
        dialog.show();
    }

    @OnClick(R.id.tv_sex)
    void sex() {
        showSexDialog();
    }

    @OnClick(R.id.tv_marriage)
    void marriage() {
        showMarriageDialog();
    }

    /**
     * 设置头像
     *
     * @param url
     */
    public void setHeadPortrait(String url) {
        if (SessionContext.isLogin()) {
            if ("01".equals(SessionContext.mUser.USERBASIC.sex)) {
                iv_photo.setImageResource(R.drawable.iv_def_photo_logined_male);
            } else if ("02".equals(SessionContext.mUser.USERBASIC.sex)) {
                iv_photo.setImageResource(R.drawable.iv_def_photo_logined_female);
            } else {
                iv_photo.setImageResource(R.drawable.iv_def_photo);
            }

            if (url != null && url.length() > 0) {
                if (!url.startsWith("http")) {
                    url = NetURL.API_LINK + url;
                }
            }
            Glide.with(this).load(url).crossFade().into(iv_photo);
        }
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
                        tailorImg(mCameraFile);
                    }
                    break;
                case AppConst.ACTIVITY_IMAGE_CAPTURE:// 相机
                    tailorImg(mCameraFile);
                    break;
                case AppConst.ACTIVITY_TAILOR:// 剪切
                    uploadPhoto();
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.show("获取图片失败,请重试", 0);
        }

    }

    /**
     * 裁剪图片
     *
     * @param imageUri
     */
    public void tailorImg(Uri imageUri) {
        try {
            Intent intent = new Intent("com.android.camera.action.CROP");

            //这段代码判断，在安卓7.0以前版本是不需要的。特此注意。不然这里也会抛出异常
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.setDataAndType(imageUri, "image/*");
            intent.putExtra("circleCrop", "true");
            // aspectX aspectY 是宽高的比例
            intent.putExtra("aspectY", 1);
            intent.putExtra("aspectX", 1);
            // outputX outputY 是裁剪图片宽高
            intent.putExtra("outputX", 50);
            intent.putExtra("outputY", 50);
//        intent.putExtra("scale",true);//自由截取
            intent.putExtra("return-data", true);
            startActivityForResult(intent, AppConst.ACTIVITY_TAILOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传头像
     */
    private void uploadPhoto() {
        try {
            RequestBeanBuilder builder = RequestBeanBuilder.create(true);

            builder.addBody("HEADPHOTO", Base64.encodeToString(ThumbnailUtil.getImageThumbnailBytes(MediaStore.Images.Media.getBitmap(this.getContentResolver(), mCameraFile), 90)));

            ResponseData data = builder.syncRequest(builder);
            data.path = NetURL.UPDATA_PHOTO;
            data.flag = 1;

            if (!isProgressShowing()) {
                showProgressDialog("正在提交头像，请稍候...", true);
            }
            requestID = DataLoader.getInstance().loadData(this, data);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.show("头像上传失败！", 0);
        }
    }

    private void checkDataAndLoad() {
        if (StringUtil.empty(et_nickname.getText())) {
            ToastUtil.show("昵称不能为空！", 0);
            return;
        }

        if (StringUtil.empty(tv_address.getText())) {
            ToastUtil.show("请选择地址！", 0);
            return;
        }

        if (StringUtil.empty(tv_sex.getText())) {
            ToastUtil.show("请选择性别！", 0);
            return;
        }

        if (StringUtil.empty(tv_birthday.getText())) {
            ToastUtil.show("请选择出生日期！", 0);
            return;
        }

        if (StringUtil.empty(tv_marriage.getText())) {
            ToastUtil.show("请设置婚姻状况！", 0);
            return;
        }

        loadData();
    }

    /**
     * 修改用户信息
     *
     * @throws
     */
    private void loadData() {

        RequestBeanBuilder builder = RequestBeanBuilder.create(true);

        builder.addBody("NICKNAME", et_nickname.getText().toString().trim());
        if (tv_sex.getText() != null && tv_sex.getText().equals("男")) {
            builder.addBody("SEX", "01");// 用户性别 01：男，02：女
        } else if (tv_sex.getText() != null && tv_sex.getText().equals("女")) {
            builder.addBody("SEX", "02");
        }
        builder.addBody("BIRTHDAT", tv_birthday.getText().toString());// DateUtil.str2Date(tv_birthday.getText().toString(), "yyyy-MM-dd").getTime());
        if (tv_marriage.getText() != null && tv_marriage.getText().equals("已婚")) {
            builder.addBody("MARRY", "01");// 婚姻 01：已婚，02：未婚，03：保密
        } else if (tv_marriage.getText() != null && tv_marriage.getText().equals("未婚")) {
            builder.addBody("MARRY", "02");
        } else if (tv_marriage.getText() != null && tv_marriage.getText().equals("保密")) {
            builder.addBody("MARRY", "03");
        }
        builder.addBody("RESIDENCE", tv_address.getText().toString().trim());// 所在地

        ResponseData data = builder.syncRequest(builder);
        data.path = NetURL.UPDATE_USER_INFO;

        if (!isProgressShowing()) {
            showProgressDialog("正在保存，请稍候...", true);
        }
        requestID = DataLoader.getInstance().loadData(this, data);
    }

    /**
     * 显示性别选择对话框
     */
    public void showSexDialog() {
        Builder builder = new android.app.AlertDialog.Builder(this);
        // 设置对话框的标题
        builder.setTitle("请选择性别");
        int checkedItem = -1;
        if ("01".equals(SessionContext.mUser.USERBASIC.sex)) {
            checkedItem = 0;
        } else if ("02".equals(SessionContext.mUser.USERBASIC.sex)) {
            checkedItem = 1;
        }
        builder.setSingleChoiceItems(R.array.sex_array, checkedItem, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String sex = getResources().getStringArray(R.array.sex_array)[which];
                tv_sex.setText(sex);
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 显示婚姻选择对话框
     */
    public void showMarriageDialog() {
        Builder builder = new android.app.AlertDialog.Builder(this);
        // 设置对话框的标题
        builder.setTitle("婚姻");
        int checkedItem = -1;
        final String[] str = new String[]{"已婚", "未婚", "保密"};
        if ("01".equals(SessionContext.mUser.USERBASIC.marry)) {
            checkedItem = 0;// 婚姻 01：已婚，02：未婚，03：保密
        } else if ("02".equals(SessionContext.mUser.USERBASIC.marry)) {
            checkedItem = 1;
        } else if ("03".equals(SessionContext.mUser.USERBASIC.marry)) {
            checkedItem = 02;
        }
        builder.setSingleChoiceItems(str, checkedItem, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                tv_marriage.setText(str[which]);
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void preExecute(ResponseData request) {
    }

    @Override
    public void notifyMessage(ResponseData request, ResponseData response) throws Exception {
        if (request.flag == 1) {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mCameraFile);
            SessionContext.mUser.USERBASIC.headphotourl = response.body.toString();
            iv_photo.setImageBitmap(bitmap);

            removeProgressDialog();
            ToastUtil.show("头像上传成功", 0);
            this.setResult(RESULT_OK, null);
            return;
        }

        removeProgressDialog();
        ToastUtil.show("资料修改成功", 0);
        SessionContext.mUser.USERBASIC.nickname = et_nickname.getText().toString().trim();
        // SessionContext.mUser.USERBASIC.birthday = DateUtil.str2Date(tv_birthday.getText().toString(), "yyyy-MM-dd").getTime();
        SessionContext.mUser.USERBASIC.birthday = tv_birthday.getText().toString();
        SessionContext.mUser.LOCALUSER.residence = tv_address.getText().toString().trim();
        SharedPreferenceUtil.getInstance().setString(AppConst.USER_INFO, JSON.toJSONString(SessionContext.mUser), true);// 重新缓存用户数据
        if (tv_sex.getText() != null && tv_sex.getText().equals("男")) {
            SessionContext.mUser.USERBASIC.sex = "01";// 用户性别 01：男，02：女
        } else if (tv_sex.getText() != null && tv_sex.getText().equals("女")) {
            SessionContext.mUser.USERBASIC.sex = "02";
        }
        if (tv_marriage.getText() != null && tv_marriage.getText().equals("已婚")) {
            // 婚姻 01：已婚，02：未婚，03：保密
            SessionContext.mUser.USERBASIC.marry = "01";
        } else if (tv_marriage.getText() != null && tv_marriage.getText().equals("未婚")) {
            SessionContext.mUser.USERBASIC.marry = "02";
        } else if (tv_marriage.getText() != null && tv_marriage.getText().equals("保密")) {
            SessionContext.mUser.USERBASIC.marry = "03";
        }
        this.setResult(RESULT_OK, null);
        this.finish();
    }

    @Override
    public void notifyError(ResponseData request, ResponseData response, Exception e) {
        removeProgressDialog();

        String message;
        if (e != null && e instanceof ConnectException) {
            message = getString(R.string.dialog_tip_net_error);
        } else {
            message = response != null && response.data != null ? response.data.toString() : getString(R.string.dialog_tip_null_error);
        }
        ToastUtil.show(message, Toast.LENGTH_LONG);

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        DataLoader.getInstance().clear(requestID);
        removeProgressDialog();
    }

    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mYear = year;
        mMonth = monthOfYear;
        mDay = dayOfMonth;
        DateUtil.getCurDateStr();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int n_year = c.get(Calendar.YEAR);
        int n_month = c.get(Calendar.MONTH);
        int n_day = c.get(Calendar.DAY_OF_MONTH);
        if (mYear > n_year) {
            ToastUtil.show("生日不能超过当前日期", 0);
            return;
        }
        if (mYear == n_year) {
            if (mMonth > n_month) {
                ToastUtil.show("生日不能超过当前日期", 0);
                return;
            }
            if (mMonth == n_month && mDay > n_day) {
                ToastUtil.show("生日不能超过当前日期", 0);
                return;
            }
        }
        // 设置文本的内容：
        // 月份+1，因为从0开始
        if (mMonth < 9) {
            if (mDay < 10) {
                tv_birthday.setText(new StringBuilder().append(mYear).append("-").append("0").append(mMonth + 1).append("-").append("0").append(mDay).toString());
            } else {
                tv_birthday.setText(new StringBuilder().append(mYear).append("-").append("0").append((mMonth + 1)).append("-").append(mDay).toString());
            }
        } else {
            if (mDay < 10) {
                tv_birthday.setText(new StringBuilder().append(mYear).append("-").append(mMonth + 1).append("-").append("0").append(mDay).toString());
            } else {
                tv_birthday.setText(new StringBuilder().append(mYear).append("-").append(mMonth + 1).append("-").append(mDay).toString());
            }
        }
    }

    @Override
    public void onAreaWheelInfo(String ProviceName, String CityName, String AreaName) {
        StringBuilder sb = new StringBuilder();
        sb.append(ProviceName).append("-");
        if (ProviceName.equals(CityName)) {
            sb.append(AreaName);
        } else if (CityName.equals(AreaName)) {
            sb.append(AreaName);
        } else {
            sb.append(CityName);
            if (AreaName != null && AreaName.length() > 0) {
                sb.append("-" + AreaName);
            }
        }
        tv_address.setText(sb);
    }

}

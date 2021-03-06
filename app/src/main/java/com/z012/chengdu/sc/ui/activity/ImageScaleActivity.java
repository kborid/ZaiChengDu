package com.z012.chengdu.sc.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.common.widget.scale.GestureImageView;
import com.prj.sdk.util.ThumbnailUtil;
import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.constants.NetURL;
import com.z012.chengdu.sc.ui.BaseActivity;
import com.z012.chengdu.sc.ui.activity.qa.QAISayActivity;
import com.z012.chengdu.sc.ui.adapter.ViewPagerAdapter;
import com.z012.chengdu.sc.ui.dialog.CustomDialog;
import com.z012.chengdu.sc.ui.dialog.CustomDialog.onCallBackListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

/***
 * 图片缩放预览
 *
 * @author LiaoBo
 *
 */
public class ImageScaleActivity extends BaseActivity implements
        OnPageChangeListener {

    private List<String> imgUrl;
    private List<Uri> imgUri;
    private ArrayList<String> listUriToString = new ArrayList<String>();
    private ViewPagerAdapter mVPAdapter;
    private List<View> mView = new ArrayList<View>();
    private ViewPager mViewPager;
    private int mSelection;
    private LinearLayout mIndicatorLayout, tv_right_title_layout;
    private int lastPositon;
    private boolean isModify;
    private CustomDialog mTip;

    @Override
    protected int getLayoutResId() {
        return R.layout.ui_image_scale;
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        try {
            Bundle bundle = getIntent().getExtras();
            mSelection = getIntent().getExtras().getInt("currentItem");
            lastPositon = mSelection;
            imgUrl = bundle.getStringArrayList("url");
            if (bundle.getStringArrayList("uri") != null) {
                tv_right_title_layout.setVisibility(View.VISIBLE);
                imgUri = new ArrayList<Uri>();
                List<String> listUri = bundle.getStringArrayList("uri");
                for (int i = 0; i < listUri.size(); i++) {
                    imgUri.add(Uri.parse(listUri.get(i)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mIndicatorLayout = (LinearLayout) findViewById(R.id.point_indicator);
        tv_right_title_layout = (LinearLayout) findViewById(R.id.tv_right_title_layout);

        mTip = new CustomDialog(this);
        try {
            if (imgUrl != null && imgUrl.size() > 0) {
                for (int i = 0; i < imgUrl.size(); i++) {
                    String url = imgUrl.get(i);
                    if (!url.startsWith("http")) {
                        url = NetURL.API_LINK + url;
                    }
                    GestureImageView view = new GestureImageView(this);
                    view.setTag(url);
                    mView.add(view);
                    Glide.with(this).load(url).placeholder(R.drawable.loading).into(view);
                }
                // mSelection = getIntent().getIntExtra("currentItem", 1);
                mVPAdapter = new ViewPagerAdapter(this, mView);
                mViewPager.setAdapter(mVPAdapter);
                mViewPager.setCurrentItem(mSelection);
                initTopIndicator();
                updateTopGalleryItem(mSelection);
                tv_center_title.setText(new StringBuilder()
                        .append(mSelection + 1).append("/")
                        .append(mView.size()));
                return;
            }

            if (imgUri != null && imgUri.size() > 0) {
                for (int i = 0; i < imgUri.size(); i++) {
                    Bitmap bm = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), imgUri.get(i));
                    GestureImageView v = new GestureImageView(this);
                    v.setImageBitmap(ThumbnailUtil.getImageThumbnail(bm, 480,
                            800));
                    mView.add(v);
                }
                // mSelection = getIntent().getIntExtra("currentItem", 1);
                mVPAdapter = new ViewPagerAdapter(this, mView);
                mViewPager.setAdapter(mVPAdapter);
                mViewPager.setCurrentItem(mSelection);
                initTopIndicator();
                updateTopGalleryItem(mSelection);
                tv_center_title.setText(new StringBuilder()
                        .append(mSelection + 1).append("/")
                        .append(mView.size()));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initListeners() {
        super.initListeners();
        mViewPager.setOnPageChangeListener(this);
    }

    @OnClick(R.id.tv_right_title_layout)
    void delete() {
        mTip.show("是否确定删除这张图片？");
        mTip.setListeners(new onCallBackListener() {

            @Override
            public void rightBtn(CustomDialog dialog) {
                dialog.dismiss();
            }

            @Override
            public void leftBtn(CustomDialog dialog) {
                try {
                    isModify = true;
                    listUriToString.add(imgUri.get(lastPositon).toString());// 记录删除的图集
                    imgUri.remove(lastPositon);
                    mView.remove(lastPositon);
                    mVPAdapter.notifyDataSetChanged();

                    if (mView.size() != 0) {
                        initTopIndicator();
                        updateTopGalleryItem(lastPositon);
                        tv_center_title.setText(new StringBuilder()
                                .append(lastPositon + 1).append("/")
                                .append(mView.size()));
                    } else {
                        executeIntent();
                    }

                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @OnClick(R.id.tv_left_title)
    void left() {
        executeIntent();
    }

    public void executeIntent() {
        if (isModify) {
            Intent intent = new Intent(this, QAISayActivity.class);
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("uri", listUriToString);// 传递删除图集
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    public void initTopIndicator() {
        if (mView.size() == 0) {
            mIndicatorLayout.setVisibility(View.GONE);
            return;
        } else {
            mIndicatorLayout.setVisibility(View.VISIBLE);
        }

        mIndicatorLayout.removeAllViews();
        for (int i = 0; i < mView.size(); i++) {
            ImageView img = new ImageView(this);
            img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            img.setImageResource(R.drawable.img_list);
            img.setPadding(9, 9, 9, 9);
            mIndicatorLayout.addView(img);
        }

        updateTopGalleryItem(0);
    }

    public synchronized void updateTopGalleryItem(int index) {
        for (int i = 0; i < mIndicatorLayout.getChildCount(); i++) {
            if (i == index) {
                ((ImageView) mIndicatorLayout.getChildAt(i))
                        .setImageResource(R.drawable.img_bg);
            } else {
                ((ImageView) mIndicatorLayout.getChildAt(i))
                        .setImageResource(R.drawable.img_list);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {
        updateTopGalleryItem(arg0);
        try {
            if (arg0 != lastPositon && (imgUrl != null || imgUri != null)) {
                if (imgUrl != null) {
                    GestureImageView img = ((GestureImageView) mViewPager
                            .findViewWithTag(String.valueOf(imgUrl.get(arg0))));
                    if (img != null)
                        img.reset();
                }
                lastPositon = arg0;
                tv_center_title.setText(new StringBuilder()
                        .append(lastPositon + 1).append("/")
                        .append(mView.size()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            executeIntent();
        }
        return super.onKeyDown(keyCode, event);
    }

}

package com.z012.chengdu.sc.ui.widget.maqueue;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.z012.chengdu.sc.R;
import com.z012.chengdu.sc.helper.ForbidFastClickHelper;

import java.util.List;

public class UPMarqueeView extends ViewFlipper {
    private Context mContext;
    /**是否开启动画*/
    private boolean isSetAnimDuration = true;
    /**时间间隔*/
    private int interval = 3000;
    /**动画时间 */
    private int animDuration = 800;
    public UPMarqueeView(Context context) {
        this(context, null);
    }
    public UPMarqueeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        setFlipInterval(interval);
        if (isSetAnimDuration) {
            Animation animIn = AnimationUtils.loadAnimation(mContext, R.anim.anim_marquee_in);
            animIn.setDuration(animDuration);
            setInAnimation(animIn);
            Animation animOut = AnimationUtils.loadAnimation(mContext, R.anim.anim_marquee_out);
            animOut.setDuration(animDuration);
            setOutAnimation(animOut);
        }
    }
    /**
     * 设置循环滚动的View数组
     * @param datas
     */
    public void setViews(final List<UPMarqueeBean> datas) {
        if (datas == null || datas.size() == 0) return;
        int size = datas.size();
        for (int i = 0; i < size; i += 2) {
            final int position = i;
            //根布局
            LinearLayout item = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.item_marquee, null);
            //设置监听
            item.findViewById(R.id.rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ForbidFastClickHelper.isForbidFastClick()) {
                        return;
                    }
                    UPMarqueeBean bean = datas.get(position);
                    if (null != listener) {
                        listener.callback(bean);
                    }
                }
            });
            //控件赋值
            ((TextView) item.findViewById(R.id.tv1)).setText(datas.get(position).getText());
            //当数据是奇数时，最后那个item仅有一项
            if (position + 1 < size) {
                item.findViewById(R.id.rl2).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ForbidFastClickHelper.isForbidFastClick()) {
                            return;
                        }
                        UPMarqueeBean bean = datas.get(position + 1);
                        if (null != listener) {
                            listener.callback(bean);
                        }
                    }
                });
                ((TextView) item.findViewById(R.id.tv2)).setText(datas.get(position + 1).getText());
            } else {
                item.findViewById(R.id.rl2).setVisibility(View.GONE);
            }
            addView(item);
        }
    }
    public boolean isSetAnimDuration() {
        return isSetAnimDuration;
    }
    public void setSetAnimDuration(boolean isSetAnimDuration) {
        this.isSetAnimDuration = isSetAnimDuration;
    }
    public int getInterval() {
        return interval;
    }
    public void setInterval(int interval) {
        this.interval = interval;
    }
    public int getAnimDuration() {
        return animDuration;
    }
    public void setAnimDuration(int animDuration) {
        this.animDuration = animDuration;
    }

    public void startAnimal(int size) {
        if (size > 2) {
            startFlipping();
        }
    }

    private IUPMarqueeListener listener = null;
    public void setUPMarqueeListener(IUPMarqueeListener listener) {
        this.listener = listener;
    }
}
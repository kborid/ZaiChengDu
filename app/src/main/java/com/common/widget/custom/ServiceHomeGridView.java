package com.common.widget.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

import com.thunisoft.ui.util.ScreenUtils;

/**
 * 1:解决嵌套，重写ListView与GridView，让其失去滑动特性
 * 2:重写dispatchDraw方法，利用Paint进行绘制网格线
 *
 * @author kborid
 */
public class ServiceHomeGridView extends GridView {

    public ServiceHomeGridView(Context context) {
        super(context);
    }

    public ServiceHomeGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mExpandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, mExpandSpec);
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (getChildAt(0) != null) {
            int space = ScreenUtils.dp2px(10);//间距
            View localView1 = getChildAt(0);
            int column = getWidth() / localView1.getWidth();
            int childCount = getChildCount();
            int row = 0;
            if (childCount % column == 0) {
                row = childCount / column;
            } else {
                row = childCount / column + 1;
            }
            int endAllcolumn = (row - 1) * column;
            Paint localPaint;
            localPaint = new Paint();
            localPaint.setStyle(Paint.Style.STROKE);
            localPaint.setStrokeWidth(2);
            localPaint.setColor(Color.parseColor("#e8e8e8"));
            for (int i = 0; i < childCount; i++) {
                View cellView = getChildAt(i);
                if ((i + 1) % column != 0) {
                    canvas.drawLine(cellView.getRight(), cellView.getTop() + space, cellView.getRight(), cellView.getBottom() - space, localPaint);
                }
//                int inRow = i / column + 1;
//				if (inRow == row) {
//                    canvas.drawLine(cellView.getLeft() + space, cellView.getBottom(), cellView.getRight() - space, cellView.getBottom(), localPaint);
//                }
            }
            if (childCount % column != 0) {
                for (int j = 0; j < (column - childCount % column); j++) {
                    View lastView = getChildAt(childCount - 1);
                    canvas.drawLine(lastView.getRight() + lastView.getWidth() * j, lastView.getTop() + space,
                            lastView.getRight() + lastView.getWidth() * j, lastView.getBottom() - space,
                            localPaint);
                }
            }
        }
    }

}
package com.takiku.nettyim.widget;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

public class StyleAlphaPopWindow extends PopupWindow {
    //ValueAnimator会从1平滑过渡到0的值的效果
    private ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 0f).setDuration(500);

    public StyleAlphaPopWindow(View contentView, int width, int height, boolean focusable) {
        super(contentView, width, height, focusable);
        setBackgroundDrawable(new ColorDrawable(-0x90000000));//设置背景半透明
        update();//刷新update()在以下状态下要更新 ，This include: setClippingEnabled(boolean), setFocusable(boolean), setIgnoreCheekPress(),setInputMethodMode(int), setTouchable(boolean), and setAnimationStyle(int).
        setTouchable(true);// 设置PopupWindow可触摸
        setOutsideTouchable(true);// 设置允许在外点击消失
        contentView.setFocusableInTouchMode(true);//调用View的setFocusableInTouchMode(true)可以使View在Touch Mode模式之下仍然可获得焦点
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        showTransBackground(true);
    }

    @Override
    public void showAsDropDown(View anchor) {
        super.showAsDropDown(anchor);
        showTransBackground(true);
    }

    public void showTransBackground(final boolean isTrans) {
        final Window window = ((Activity) getContentView().getContext()).getWindow();
        if (window == null) {
            return;
        }
        valueAnimator.removeAllListeners();
        valueAnimator.removeAllUpdateListeners();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = (float) animation.getAnimatedValue();
                WindowManager.LayoutParams lp = window.getAttributes();
                if (isTrans) {
                    lp.alpha = 0.7f + 0.3f * fraction;//从不透明平滑过渡到0.7
                } else {
                    lp.alpha = 0.7f + (1 - fraction) * 0.3f;//从0.7平滑过渡到不透明
                }
                window.setAttributes(lp);
            }
        });
        valueAnimator.start();
    }
}

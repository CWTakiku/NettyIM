package com.takiku.nettyim.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.takiku.nettyim.R;

public class MenuItemPopWindow extends StyleAlphaPopWindow {

    public static final int MENU_TYPE_READ = 0x01;

    public static final int MENU_TYPE_RECALL = 0x02;

    public MenuItemPopWindow(Context context,  final MenuItemListenr lis,boolean sender) {
        super(sender==true? LayoutInflater.from(context).inflate(R.layout.popwin_desc_top_menu_item, null):LayoutInflater.from(context).inflate(R.layout.popwin_des_top_menu_receive_item, null),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        if (!sender){
            View read = getContentView().findViewById(R.id.tv_menu_read);

            read.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != lis) {
                        lis.onItem(MENU_TYPE_READ);
                    }
                }
            });
        }else {
            View recall = getContentView().findViewById(R.id.tv_menu_recall);

            recall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != lis) {
                        lis.onItem(MENU_TYPE_RECALL);
                    }
                }
            });
        }
        setOnDismissListener(new OnDismissListener() {//点击空白消失监听
            @Override
            public void onDismiss() {
                showTransBackground(false);
            }
        });
    }
    /**
     * 创建实例
     * @param context
     * @param lis
     * @return
     */
    public static MenuItemPopWindow builder(Context context,  MenuItemListenr lis,boolean sender) {
        return new MenuItemPopWindow(context, lis,sender);
    }

    /**
     * 回调监听
     */
    public interface MenuItemListenr {
        void onItem(int flag);
    }
}

package com.takiku.im_lib.util;

/**
 * author:chengwl
 * Description:
 * Date:2020/4/11
 */
public class StringUtil {
    public static boolean isEmpty(Object o) {
        return (null == o || o.toString().trim().equals("")) ? true : false;
    }
}

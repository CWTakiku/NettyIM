package com.takiku.im_lib.util;

import android.util.Log;

import com.takiku.im_lib.BuildConfig;
import com.takiku.im_lib.client.IMClient;

import java.util.ArrayList;

/**
 * @author chengwl
 * @des
 * @date:2022/11/24
 */
public class LogUtil {
    private static ArrayList<String> getFunctionNames(int layerNumber) {
        ArrayList<String> functionNames = new ArrayList<String>();
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        int index = -1;

        for (StackTraceElement st : sts) {
            if ("LogUtil.java".equals(st.getFileName()) || "LogUtil.kt".equals(st.getFileName())) {
                index = 0;
                continue;
            }
            if (index == -1)
                continue;

            if (index >= 0 && index < layerNumber) {

                functionNames.add(
                        String.format(
                                "at [%s:%s.%s(%s:%d)]",
                                Thread.currentThread().getName(),
                                st.getClassName(),
                                st.getMethodName(),
                                st.getFileName(),
                                st.getLineNumber()));

                index++;
            } else
                break;

        }
        return functionNames;
    }
    public static void i(String tag,String msg){
        if (BuildConfig.DEBUG){
            Log.i(tag,msg);
        }
    }
    public static void e(String tag,String msg){
        if (BuildConfig.DEBUG){
            Log.e(tag,msg);
        }
    }
    public static void debug_e(String msg, int layerNumber) {
        debug_e("custom_debug", msg, layerNumber);

    }
    private static String createMessage(String msg, int layerNumber) {
        ArrayList<String> funs = getFunctionNames(layerNumber);
        StringBuilder funNameSBuild = new StringBuilder();
        for (String fun : funs) {
            funNameSBuild.append(fun);
            funNameSBuild.append("\n");
        }
        return String.format("DEBUG %s \n %s", msg, funNameSBuild);
    }
    public static void debug_e(String tag, String msg, int layerNumber) {
        if (BuildConfig.DEBUG) {
            String newMsg = createMessage(msg, layerNumber);
            Log.e(tag, newMsg);
        }

    }
}

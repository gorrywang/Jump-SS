package com.example.dell.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Dell on 2017/2/9.
 * 常用方法
 */

public class Utils {
    //用户配置文件名
    public static final String USER_INFO = "user_info";


    /**
     * 判断程序是否第一次运行
     *
     * @param context
     * @return
     */
    public static boolean isFirstRun(Context context) {
        SharedPreferences sp = context.getSharedPreferences(USER_INFO, context.MODE_PRIVATE);
        boolean isFirstRun = sp.getBoolean("isFirstRun", true);
        if (isFirstRun) {
            //第一次运行
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isFirstRun", false);
            editor.commit();
        }
        return isFirstRun;
    }
}

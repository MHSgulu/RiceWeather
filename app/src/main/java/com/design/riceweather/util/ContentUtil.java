package com.design.riceweather.util;


import com.design.riceweather.MyApplication;

public class ContentUtil {

    //用户id
    public static final String PUBLIC_ID = "HE2105241048301833";
    //用户key
    public static final String APK_KEY = "74b4a077f3fa4b6faf9c32c9a347fb00";

    //应用设置里的文字
    public static String SYS_LANG = "zh";
    public static String APP_SETTING_LANG = SpUtils.getString(MyApplication.getContext(), "language", "sys");
    public static String APP_SETTING_UNIT = SpUtils.getString(MyApplication.getContext(), "unit", "she");
    public static String APP_SETTING_THEME = SpUtils.getString(MyApplication.getContext(), "theme", "浅色");




}

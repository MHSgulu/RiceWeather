package com.design.riceweather;

import android.app.Application;
import android.content.Context;

import com.design.riceweather.util.ContentUtil;

import java.util.logging.Logger;

import interfaces.heweather.com.interfacesmodule.view.HeConfig;


public class MyApplication extends Application {
    //获取屏幕的高，宽
    private static MyApplication instance = null;

    public Logger log;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //在主线程中new的handler就是主线程的handler
        //初始化Handler
        HeConfig.init(ContentUtil.PUBLIC_ID, ContentUtil.APK_KEY);
        //切换至开发版服务
        HeConfig.switchToDevService();

    }

    /**
     * 获得实例
     *
     * @return
     */
    public static MyApplication getInstance() {
        return instance;
    }

    /**
     * 获取context对象
     */
    public static Context getContext() {
        return instance.getApplicationContext();
    }


}

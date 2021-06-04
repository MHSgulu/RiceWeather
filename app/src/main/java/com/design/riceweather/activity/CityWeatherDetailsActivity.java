package com.design.riceweather.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.design.riceweather.R;
import com.design.riceweather.adapter.RealTimeWeatherAdapter;
import com.design.riceweather.adapter.WeatherDetailsAdapter;
import com.design.riceweather.adapter.WeatherTipsAdapter;
import com.design.riceweather.databinding.ActivityCityWeatherDetailsBinding;
import com.design.riceweather.databse.SingletonRoomDatabase;
import com.design.riceweather.entity.CityWeather;
import com.design.riceweather.entity.IPLocationEntity;
import com.design.riceweather.util.Constant;
import com.design.riceweather.util.ContentUtil;
import com.design.riceweather.util.OkHttpUtils;
import com.design.riceweather.util.SpUtils;
import com.design.riceweather.view.horizonview.ScrollWatched;
import com.design.riceweather.view.horizonview.ScrollWatcher;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import interfaces.heweather.com.interfacesmodule.bean.base.Code;
import interfaces.heweather.com.interfacesmodule.bean.geo.GeoBean;
import interfaces.heweather.com.interfacesmodule.bean.weather.WeatherHourlyBean;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CityWeatherDetailsActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private static final String TAG = "CityWeatherDetailsActiv";
    private ActivityCityWeatherDetailsBinding viewBinding;
    private Context context;

    private String jsonData;
    private CityWeather data;
    private IPLocationEntity ipLocationEntity;

    private ScrollWatched watched;
    List<ScrollWatcher> watcherList = new ArrayList<>();

    private MenuInflater menuInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_city_weather_details);
        viewBinding = ActivityCityWeatherDetailsBinding.inflate(getLayoutInflater());
        View view = viewBinding.getRoot();
        setContentView(view);
        context = viewBinding.getRoot().getContext();
        initUIView();
        initObserver();
        initView();
        initOnClickListener();
        initRequest();
    }

    private void initView() {
        viewBinding.hsv.setToday24HourView(viewBinding.hourly); //加载 动态实时天气
        watched.addWatcher(viewBinding.hourly);
        //横向滚动监听
        viewBinding.hsv.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                watched.notifyWatcher(scrollX);
            }
        });
    }

    private void initUIView() {
        //viewBinding.rootView.setBackgroundColor(getColor(R.color.weather_qing));
        /*
         * 将状态栏的颜色设置为{@code color}.
         *
         * 为了使此效果生效，该窗口必须使用{@link android.view.WindowManager.LayoutParams#FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS}
         * 而且不得设置 {@link android.view.WindowManager.LayoutParams#FLAG_TRANSLUCENT_STATUS}
         *
         * 如果颜色是透明的，考虑设置{@link android.view.View#SYSTEM_UI_FLAG_LAYOUT_STABLE}和{@link android.view.View#SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN}.
         * <p>
         * The transitionName for the view background will be "android:status:background".
         * 视图背景的transitionName将为“ android：status：background”。
         * </p>
         */
        getWindow().setStatusBarColor(getColor(R.color.white)); //设置系统状态栏背景颜色

        /*
         * 将导航栏的颜色设置为{@param color}.
         *
         * 为了使此效果生效，该窗口必须使用以下命令绘制系统栏背景{@link android.view.WindowManager.LayoutParams#FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS}
         * 而且不得设置 {@link android.view.WindowManager.LayoutParams#FLAG_TRANSLUCENT_NAVIGATION}.
         *
         * 如果颜色是透明，请考虑设置{@link android.view.View#SYSTEM_UI_FLAG_LAYOUT_STABLE} 和 {@link android.view.View#SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION}.
         * <p>
         * The transitionName for the view background will be "android:navigation:background".
         * 视图背景的transitionName将为“ android：navigation：background”。
         * </p>
         * @attr ref android.R.styleable#Window_navigationBarColor
         */
        //getWindow().setNavigationBarColor(getColor(R.color.weather_qing)); //设置底部系统导航栏状态栏背景颜色
    }

    /**
     * 初始化横向滚动条的监听
     */
    private void initObserver() {
        Log.d("点位", "initObserver");
        watched = new ScrollWatched() {
            @Override
            public void addWatcher(ScrollWatcher watcher) {
                watcherList.add(watcher);
            }

            @Override
            public void removeWatcher(ScrollWatcher watcher) {
                watcherList.remove(watcher);
            }

            @Override
            public void notifyWatcher(int x) {
                for (ScrollWatcher watcher : watcherList) {
                    watcher.update(x);
                }
            }
        };
    }

    private void initOnClickListener() {
        viewBinding.ivAdd.setOnClickListener(v -> {
            Intent intent = new Intent(context, CityManagementActivity.class);
            startActivity(intent);
        });

        viewBinding.ivMore.setOnClickListener((View v) -> showPopup(viewBinding.ivMore));

    }

    private void initRequest() {
        String cityName = getIntent().getStringExtra(Constant.ARG_CityName);
        //Log.d(TAG, "点位：cityName: " + cityName);
        if (cityName == null){
            getLocation();
        }else{
            requestWeatherData(getIntent().getStringExtra(Constant.ARG_CityName));
            getNowCity(cityName);
        }
    }

    private void getLocation() {
        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.e(TAG, "onFailure: " + e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Toast.makeText(context, "响应错误: " + response, Toast.LENGTH_SHORT).show();
                    throw new IOException("异常: " + response);
                }
                ipLocationEntity = new Gson().fromJson(response.body().string(),IPLocationEntity.class);
                Log.d(TAG, "点位： 当前IP地址所在城市: " + ipLocationEntity.getLocation().getCity());
                String cityName = ipLocationEntity.getLocation().getCity();
                requestWeatherData(cityName);
                getNowCity(cityName);
                //如果数据库没有数据，添加当前地址进去，不可删除
                if (SingletonRoomDatabase.getInstance(getApplicationContext()).getAllCity().size() > 0){
                    Log.d(TAG, "点位： 当前数据库中存在城市数据");
                }else{
                    Log.d(TAG, "点位： 当前数据库中不存在城市数据，添加一条所在IP定位城市数据");
                    SingletonRoomDatabase.getInstance(getApplicationContext()).insertCity(ipLocationEntity.getLocation().getCity());
                }
            }
        };
        OkHttpUtils.getInstance().requestIpLocation(callback);
    }

    private void requestWeatherData(String cityName) {
        Callback callback = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.e(TAG, "onFailure: " + e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Toast.makeText(context, "响应错误: " + response, Toast.LENGTH_SHORT).show();
                    throw new IOException("异常: " + response);
                }
                //OkHttpAssist.PrintRequestHeader(response.headers());
                //System.out.println(response.body().string()); //response.body().string() 只能调用一次
                jsonData = Objects.requireNonNull(response.body()).string();
                data = new Gson().fromJson(jsonData,CityWeather.class);
                //System.out.println("111: " + data.getResult().getResult().getCity());
                //当前回调已不在UI线程也就是Android主线程，需要手动切换
                runOnUiThread();
            }
        };
        OkHttpUtils.getInstance().requestCityWeather(cityName, callback);
    }

    private void runOnUiThread() {

        /*
         * 在UI线程上运行指定的操作。 如果当前线程是UI线程，则立即执行该操作。
         * 如果当前线程不是UI线程，则将操作发布到UI线程的事件队列。
         *
         * @param action 在UI线程上运行的操作
         */
        CityWeatherDetailsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (data.getResult().getStatus() == 0) {
                    String weather = data.getResult().getResult().getWeather();
                    if (weather.contains("晴")) {
                        viewBinding.rootView.setBackgroundColor(getColor(R.color.weather_qing));
                        getWindow().setStatusBarColor(getColor(R.color.weather_qing));
                        getWindow().setNavigationBarColor(getColor(R.color.weather_qing));
                    } else if (weather.contains("多云")) {
                        viewBinding.rootView.setBackgroundColor(getColor(R.color.weather_duoyun));
                        getWindow().setStatusBarColor(getColor(R.color.weather_duoyun));
                        getWindow().setNavigationBarColor(getColor(R.color.weather_duoyun));
                    } else if (weather.contains("阴")) {
                        viewBinding.rootView.setBackgroundColor(getColor(R.color.weather_yin));
                        getWindow().setStatusBarColor(getColor(R.color.weather_yin));
                        getWindow().setNavigationBarColor(getColor(R.color.weather_yin));
                    } else if (weather.contains("雨")) {
                        viewBinding.rootView.setBackgroundColor(getColor(R.color.weather_rain));
                        getWindow().setStatusBarColor(getColor(R.color.weather_rain));
                        getWindow().setNavigationBarColor(getColor(R.color.weather_rain));
                    } else if (weather.contains("浮尘")) {
                        viewBinding.rootView.setBackgroundColor(getColor(R.color.weather_fuchen));
                        getWindow().setStatusBarColor(getColor(R.color.weather_fuchen));
                        getWindow().setNavigationBarColor(getColor(R.color.weather_fuchen));
                    } else {
                        viewBinding.rootView.setBackgroundColor(getColor(R.color.weather_yin));
                        getWindow().setStatusBarColor(getColor(R.color.weather_yin));
                        getWindow().setNavigationBarColor(getColor(R.color.weather_yin));
                    }

                    viewBinding.tvAddress.setText(data.getResult().getResult().getCity());
                    viewBinding.tvWeather.setText(data.getResult().getResult().getWeather());
                    viewBinding.tvTemperature.setText(data.getResult().getResult().getTemp());
                    viewBinding.tvAirQuality.setText(data.getResult().getResult().getAqi().getQuality());

                    viewBinding.tvTimeSunrise.setText(String.format("日出 %s", data.getResult().getResult().getDaily().get(0).getSunrise()));
                    viewBinding.tvTimeSunset.setText(String.format("日落 %s", data.getResult().getResult().getDaily().get(0).getSunset()));

                    viewBinding.tvWindForce.setText(data.getResult().getResult().getWindpower());
                    viewBinding.tvWindDirection.setText(data.getResult().getResult().getWinddirect());
                    viewBinding.tvHumidity.setText(String.format("%s%%", data.getResult().getResult().getHumidity()));
                    viewBinding.tvSomatosensory.setText(String.format("%s °", data.getResult().getResult().getTemp()));
                    viewBinding.tvAirPressure.setText(String.format("%s", data.getResult().getResult().getPressure()));

                    //固定长度为7的一周天气列表
                    //viewBinding.recycleWeather.setLayoutManager(new LinearLayoutManager(context));
                    viewBinding.recycleWeather.setAdapter(new WeatherDetailsAdapter(data.getResult().getResult().getDaily()));

                    //固定长度为24的实时天气列表
                    //viewBinding.recycleTimeWeather.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
                    viewBinding.recycleTimeWeather.setAdapter(new RealTimeWeatherAdapter(data.getResult().getResult().getHourly()));

                    //固定长度为6的天气指数列表
                    //viewBinding.recycleTips.setLayoutManager(new GridLayoutManager(context, 3));
                    //adapter3 = new WeatherTipsAdapter(data.getResult().getResult().getIndex());
                    viewBinding.recycleTips.setAdapter(new WeatherTipsAdapter(data.getResult().getResult().getIndex()));

                } else {
                    Toast.makeText(context, "msg: " + data.getResult().getMsg(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getNowCity(String cityName) {
        Log.d(TAG, "点位：getNowCity");
        HeWeather.getGeoCityLookup(context, cityName, new HeWeather.OnResultGeoListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.d(TAG, "点位：getNowCity onError");
            }

            @Override
            public void onSuccess(GeoBean search) {
                Log.d(TAG, "点位：getNowCity onSuccess");
                Log.d(TAG, "点位：search.getLocationBean().size():  " + search.getLocationBean().size());
                GeoBean.LocationBean basic = search.getLocationBean().get(0);
                String cid = basic.getId();
                Log.d(TAG, "点位：cid:  " + cid);
                requestWeatherHourly(cid);
            }
        });
    }

    private void requestWeatherHourly(String cityID) {
        Log.d(TAG, "点位： 请求获取数据实时数据");
        //第二个参数是城市ID
        HeWeather.getWeather24Hourly(context, cityID, new HeWeather.OnResultWeatherHourlyListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.e("sky", "getWeatherHourly onError: getWeatherHourly");
            }

            @Override
            public void onSuccess(WeatherHourlyBean weatherHourlyBean) {
                if (Code.OK.getCode().equalsIgnoreCase(weatherHourlyBean.getCode())) {
                    Log.d("点位", "getWeatherHourly onSuccess：获取实时天气数据成功");
                    //SpUtils.saveBean(context, "weatherHourly", weatherHourlyBean);
                    getWeatherHourly(weatherHourlyBean);
                }else{
                    Log.d("点位", "获取数据失败");
                    Log.d("点位", "weatherHourlyBean.getCode()：" + weatherHourlyBean.getCode());
                }
            }
        });
    }


    @SuppressLint("DefaultLocale")
    public void getWeatherHourly(WeatherHourlyBean bean) {
        if (bean != null && bean.getHourly() != null) {
            Log.d("点位", "getWeatherHourly 进一步获取数据");
            List<WeatherHourlyBean.HourlyBean> hourlyWeatherList = bean.getHourly();
            List<WeatherHourlyBean.HourlyBean> data = new ArrayList<>();
            if (hourlyWeatherList.size() > 23) {
                Log.d("点位", "数据长度大于23");
                for (int i = 0; i < 24; i++) {
                    data.add(hourlyWeatherList.get(i));
                    String condCode = data.get(i).getIcon();
                    String time = data.get(i).getFxTime();
                    time = time.substring(time.length() - 11, time.length() - 9);
                    int hourNow = Integer.parseInt(time);
                    if (hourNow >= 6 && hourNow <= 19) {
                        data.get(i).setIcon(condCode + "d");
                    } else {
                        data.get(i).setIcon(condCode + "n");
                    }
                }
            }
            else {
                Log.d("点位", "数据长度小于23");
                for (int i = 0; i < hourlyWeatherList.size(); i++) {
                    data.add(hourlyWeatherList.get(i));
                    String condCode = data.get(i).getIcon();
                    String time = data.get(i).getFxTime();
                    time = time.substring(time.length() - 11, time.length() - 9);
                    int hourNow = Integer.parseInt(time);
                    if (hourNow >= 6 && hourNow <= 19) {
                        data.get(i).setIcon(condCode + "d");
                    } else {
                        data.get(i).setIcon(condCode + "n");
                    }
                }
            }

            int minTmp = Integer.parseInt(data.get(0).getTemp());
            int maxTmp = minTmp;
            for (int i = 0; i < data.size(); i++) {
                Log.d("点位", "正在找出最高温度和最低温度...");
                int tmp = Integer.parseInt(data.get(i).getTemp());
                minTmp = Math.min(tmp, minTmp);
                maxTmp = Math.max(tmp, maxTmp);
            }
            //设置当天的最高最低温度
            viewBinding.hourly.setHighestTemp(maxTmp);
            viewBinding.hourly.setLowestTemp(minTmp);
            if (maxTmp == minTmp) {
                Log.d("点位", "最高温和最低温相等");
                viewBinding.hourly.setLowestTemp(minTmp - 1);
            }
            viewBinding.hourly.initData(data);
            viewBinding.tvLineMaxTmp.setText(String.format("%d°", maxTmp));
            viewBinding.tvLineMinTmp.setText(String.format("%d°", minTmp));
        }
    }


    public void showPopup(View v) {
        /*
          构造函数，用于使用锚视图创建新的弹出菜单。

          @param context 运行弹出菜单的上下文，通过它可以访问当前主题，资源等.
         * @param anchor 此弹出窗口的锚定视图。 如果有空间，则弹出窗口将显示在锚点的下方；如果没有空间，则弹出窗口将显示在其上方.
         */
        PopupMenu popup = new PopupMenu(context, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.overflow_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this); //activity实现OnMenuItemClickListener
        popup.show();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_1:
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("气象数据:" +item.getItemId(), jsonData);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                }
                Toast.makeText(context, "气象数据已复制到粘贴板", Toast.LENGTH_SHORT).show();
                return true;
            /*case R.id.option_2:
                Toast.makeText(context, "option_1", Toast.LENGTH_SHORT).show();
                return true;*/
            default:
                return false;
        }
    }




    /**
     * 初始化活动的标准选项菜单的内容.  您应该将菜单项放入<var>menu</var>.
     *
     * <p>第一次显示选项菜单时，仅调用一次.  每次显示菜单时都要更新, see{@link #onPrepareOptionsMenu}.
     *
     * <p>默认实现是使用标准系统菜单项填充菜单.  它们被放置在 {@link Menu#CATEGORY_SYSTEM}组中，以便可以与应用程序定义的菜单项一起正确排序.
     * 派生类应始终调用基本实现.
     *
     * <p>您可以安全地按住 <var>menu</var> 以及从中创建的所有项目), 根据需要对其进行修改, 直到下次调用onCreateOptionsMenu()为止.
     *
     * <p>将项目添加到菜单时, 您可以实施活动的{@link #onOptionsItemSelected}方法在那里处理它们.
     *
     * @param menu 您放置项目的选项菜单.
     *
     * @return 您必须返回true才能显示菜单;如果返回false，则不会显示.
     *
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }



}
package com.pandamama.a01_coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.pandamama.a01_coolweather.gson.Forecast;
import com.pandamama.a01_coolweather.gson.Weather;
import com.pandamama.a01_coolweather.util.HttpUtil;
import com.pandamama.a01_coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/*
在活动中请求天气数据，将数据展示到界面上
 */
public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        实现背景图和状态栏融合到一起的效果（这个功能是 Android 5.0 以上的系统才支持的，做版本号判断）
        调用 getWindow().getDecorview() 方法拿到当前活动的 DecorView，再调用它的 setSystemUiVisibility()
        方法来改变系统 UI 的显示，这里传入 View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN 和 View.SYSTEM_UI_FLAG
        _LAYOUT_STABLE 就表示活动的布局会显示在状态栏上面，最后调用一下 setStatusBarColor() 方法将状态栏设置
        成透明色
         */
        if (Build.VERSION.SDK_INT >= 21) {

            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        // 初始化各种控件
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);

        bingPicImg = findViewById(R.id.bing_pic_img);

        /*
        尝试从本地缓存中读取天气数据
         */

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {

            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {

            // 无缓存时去服务器查询天气
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        /*
        尝试从 SharePreferences 中读取缓存的背景图片，如果有缓存的话直接使用 Glide 来加载这张图片，如果
        没有的话就调用 loadBingPic() 方法去请求今日的必应背景图
         */
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {

            Glide.with(this).load(bingPic).into(bingPicImg);

        } else {
            loadBingPic();
        }

    }

    /*
    处理并展示 Weather 实体类中的数据
     */
    private void showWeatherInfo(Weather weather) {

        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degress = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degress);
        weatherInfoText.setText(weatherInfo);

        forecastLayout.removeAllViews();

        for (Forecast forecast : weather.forecastList) {

            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);

            forecastLayout.addView(view);
        }

        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    /*
    根据天气 id 请求城市天气信息
        1. 拼装出一个接口地址，调用 HttpUtil.sendOkHttpRequest() 方法向该地址发出请求，服务器返回相应城市的天气
    信息以 JSON 格式返回
        2. 在 onResponse() 回调中先调用 Utility.handleWeatherResponse() 方法将返回的 JSON 数据转换成 Weather
    对象，再将当前线程切换到主线程
        3. 进行判断，如果服务器返回的 status 状态是 ok，就说明请求天气成功了，将返回的数据缓存到 SharedPreferences
    当中，并调用  showWeatherInfo() 方法进行内容显示
     */
    private void requestWeather(final String weatherId) {

        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=2745f1bfe2624b1ea68a14317ab640e2";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (weather != null && "ok".equals(weather.status)) {

                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        /*
        在 requestWeather() 方法的最后也需要调用一下 loadBingPic() 方法，这样在每次请求天气的时候同时也会
        刷新背景图片
         */
        loadBingPic();
    }

    /*
    加载必应每日一图。先是调用 HttpUtil.sendOkHttpRequest() 方法获取到必应背景图的链接，然后将这个缓存到
    SharedPreferences 当中，再将当前线程切换到主线程，最后使用 Glide 来加载这张图片就可以了
     */
    private void loadBingPic() {

        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String bingPic = response.body().string();
                SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                edit.putString("bing_pic", bingPic);
                edit.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
}

























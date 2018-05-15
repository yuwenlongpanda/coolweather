package com.pandamama.a01_coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        一开始先从 SharedPreferences 文件中读取缓存数据，如果不为 null 就说明之前已经请求过天气数据了，那么就
        没必要让用户再次选择城市，而是直接跳转到 WeatherActivity 即可
         */
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        if (prefs.getString("weather", null) != null) {
            Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }

    }
}

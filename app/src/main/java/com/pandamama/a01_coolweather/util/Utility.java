package com.pandamama.a01_coolweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.pandamama.a01_coolweather.db.City;
import com.pandamama.a01_coolweather.db.County;
import com.pandamama.a01_coolweather.db.Province;
import com.pandamama.a01_coolweather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
先使用 JSONArray 和 JSONObjcet 讲数据解析出来，然后组装成实体类对象，再调用 save() 方法将数据存储到数据库当中
 */
public class Utility {

    /*
    解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String response) {

        if (!TextUtils.isEmpty(response)) {

            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {

                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    String name = provinceObject.getString("name");
                    int id = provinceObject.getInt("id");

                    Province province = new Province();
                    province.setProvinceName(name);
                    province.setProvinceCode(id);
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId) {

        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObjcet = allCities.getJSONObject(i);
                    String name = cityObjcet.getString("name");
                    int id = cityObjcet.getInt("id");

                    City city = new City();
                    city.setCityName(name);
                    city.setCityCode(id);
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {

            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObjcet = allCounties.getJSONObject(i);
                    String name = countyObjcet.getString("name");
                    String weather_id = countyObjcet.getString("weather_id");

                    County county = new County();
                    county.setCountyName(name);
                    county.setWeatherId(weather_id);
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    将返回的 JSON 数据解析成 Weather 实体类
     */
    public static Weather handleWeatherResponse(String response) {

        try {
            /*
            通过 JSONObject 和 JSONArray 将天气数据中的主体内容解析出来
             */
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();

            /*
            调用 fromJson() 方法直接将 JSON 数据转换成 Weather 对象
             */
            Weather weather = new Gson().fromJson(weatherContent, Weather.class);

            return weather;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
























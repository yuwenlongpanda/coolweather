package com.pandamama.a01_coolweather.util;

import android.text.TextUtils;

import com.pandamama.a01_coolweather.db.City;
import com.pandamama.a01_coolweather.db.County;
import com.pandamama.a01_coolweather.db.Province;

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
}

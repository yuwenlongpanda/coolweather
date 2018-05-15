package com.pandamama.a01_coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/*
在 Weather 类中，对 Basic、AQI、Now、Suggestion 和 Forecast 类进行了引用；daily_forecast 包含的是一个数组，
使用 List 集合来引用 Forecast 类；返回的天气数据包含一项 status 数据，成功返回 ok，失败则会返回具体的原因
 */
public class Weather {

    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;

}

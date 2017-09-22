package com.umbrella.app.weather.models;

/**
 * Created by ibrahimserpici on 9/10/17.
 */

public class Weather {

    private String cityName;
    private String stateName;

    private String fahrenheitValue;
    private String celsiusValue;

    private String skyConditionsString;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getFahrenheitValue() {
        return fahrenheitValue;
    }

    public void setFahrenheitValue(String fahrenheitValue) {
        this.fahrenheitValue = fahrenheitValue;
    }

    public String getCelsiusValue() {
        return celsiusValue;
    }

    public void setCelsiusValue(String celsiusValue) {
        this.celsiusValue = celsiusValue;
    }

    public String getSkyConditionsString() {
        return skyConditionsString;
    }

    public void setSkyConditionsString(String skyConditionsString) {
        this.skyConditionsString = skyConditionsString;
    }
}

package com.umbrella.app.weather.models;

import java.util.Date;

/**
 * Created by ibrahimserpici on 9/22/17.
 */

public class HourlyWeather {

    private String cityName;
    private String stateName;

    private Date date;
    private String timeValue;

    private String conditionString;
    private String conditionResourceID;

    private String temperatureFahrenheitValue;
    private String temperatureCelsiusValue;

    /* Getters and Setters */

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTimeValue() {
        return timeValue;
    }

    public void setTimeValue(String timeValue) {
        this.timeValue = timeValue;
    }

    public String getConditionString() {
        return conditionString;
    }

    public void setConditionString(String conditionString) {
        this.conditionString = conditionString;
    }

    public String getConditionResourceID() {
        return conditionResourceID;
    }

    public void setConditionResourceID(String iconString) {

        // After getting the condition, infer its id, which will be used to choose the proper icon.
        // If the time is minor to 8:00pm, it will take daytime icons, otherwise it will take
        // nighttime icons, which have a prefix "nt_".
        if (timeValue.compareTo("8:00 PM") >= 0) {
            iconString = "nt_" + iconString;
        }

        this.conditionResourceID = "@drawable/" + iconString;
    }

    public String getTemperatureFahrenheitValue() {
        return temperatureFahrenheitValue;
    }

    public void setTemperatureFahrenheitValue(String temperatureFahrenheitValue) {
        this.temperatureFahrenheitValue = temperatureFahrenheitValue;
    }

    public String getTemperatureCelsiusValue() {
        return temperatureCelsiusValue;
    }

    public void setTemperatureCelsiusValue(String temperatureCelsiusValue) {
        this.temperatureCelsiusValue = temperatureCelsiusValue;
    }
}

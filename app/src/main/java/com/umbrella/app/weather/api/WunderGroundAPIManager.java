package com.umbrella.app.weather.api;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.umbrella.app.weather.models.Weather;
import com.umbrella.app.weather.models.HourlyWeather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Created by ibrahimserpici on 9/22/17.
 */

public class WunderGroundAPIManager {

    private static final int DAY_ROW_COUNT = 10;

    public static final String MAIN_URL = "http://api.wunderground.com/api/";                           // Base URL of the weather API
    public static final String WEATHER_API_KEY = "38c5671bdecf03e6";
    public static final String GEOLOOKUP_ENDPOINT_URI = "/" + "geolookup" + "/" + "q/";
    public static final String CONDITIONS_ENDPOINT_URI = "/" + "conditions" + "/" + "q/";
    public static final String HOURLY10DAY_ENDPOINT_URI = "/" + "hourly10day" + "/" + "q/";
    /** UserInputActivity */
    public static final String USERINPUT_PREFERENCE_FRAGMENT_TAG = "UserInputPreferenceFragmentTag";
    // Network status
    public static final String NETWORK_ERROR = "NETWORK_ERROR";
    // Response status
    public static final String RESULT_OK = ConnectionAsyncTask.RESPONSE_OK;
    public static final String RESULT_ERROR = ConnectionAsyncTask.RESPONSE_ERROR;
    public static final String RESULT_TIMEOUT = ConnectionAsyncTask.RESPONSE_TIMEOUT;

    private Context context;

    // Data Models
    private Weather currentWeatherDataModel;
    private List weeklyWeatherList;

    // Listener
    private OnConnectionResultListener onConnectionResultListener;
    /**
     *  Informs that a result from the API connection has been gotten.
     *  */
    public interface OnConnectionResultListener {
        void onConnectionResult(String status, Weather currentWeatherDataModel, List weeklyWeatherList);
    }

    public void setOnConnectionResultListener(OnConnectionResultListener onConnectionResultListener) {
        this.onConnectionResultListener = onConnectionResultListener;
    }

    /** Constructor */
    public WunderGroundAPIManager(Context context) {
        this.context = context;
    }

    /**
     * Connects to the Weather API to retrieve the weather information
     */
    public void connect(String zipCode) {
        connectToGeolookupEndpoint(zipCode);
    }

    /**
     *  Receives the result of the asynchronous task to the API
     *  Since the architectural patter for the API es REST, the connection is given in three parts:
     *  First, connect to an endpoint which receives the ZIP code and sends a response from where we take the city and state.
     *  Second, connect to an endpoint which receives the city and state and sends a response with the information for the current weather.
     *  Third, connect to an endpoint which receives the city and state and sends a response with the forecast information for the next 10 days, but we'll take only 7 days.
     *  */
    private class WeatherAPIConnectionAsyncTaskOnResponseListener implements ConnectionAsyncTask.OnResponseListener {

        @Override
        public void onResponse(String endpointName, String status, String result) {

            if (status.equals(RESULT_ERROR) || status.equals(RESULT_TIMEOUT)) {

                onConnectionResultListener.onConnectionResult(status, null, null);

            } else if (status.equals(RESULT_OK)) {

                if (endpointName.equals("geolookup")) { // First connection

                    processGeolookupEndpointResult(result);
                    connectToConditionsEndpoint();

                } else if (endpointName.equals("conditions")) { // Second connections

                    processConditionsEndpointResult(result);
                    connectToHourly10DayEndpoint();

                } else if (endpointName.equals("hourly10day")) { // Third connection

                    processHourly10DayEndpointResult(result);
                    onConnectionResultListener.onConnectionResult(status, currentWeatherDataModel, weeklyWeatherList);
                }
            }
        }
    }

    /**
     * First connection endpoint:
     *
     * Calls the 'geolookup' endpoint which receives the ZIP code entered by the user and
     * sends the location info
     *
     * Example: http://api.wunderground.com/api/9a232047401b1785/geolookup/q/43081.json
     */
    private void connectToGeolookupEndpoint(String zipCode) {

        if (isNetworkConnectionEstablished()) {

            String endpointName = "geolookup";
            String endpointURI = GEOLOOKUP_ENDPOINT_URI;
            String endpointURL = MAIN_URL + WEATHER_API_KEY + endpointURI + zipCode + "." + "json";

            executeConnectionAsyncTask(endpointName, endpointURL);

        } else {
            onConnectionResultListener.onConnectionResult(NETWORK_ERROR, null, null);
        }
    }
    /**
     * Processing of the first connection endpoint response:
     * Parses the result of the 'geolookup' endpoint call to get both the city and the state
     */
    private void processGeolookupEndpointResult(String result) {

        try {

            JSONObject resultJSONObject = new JSONObject(result);
            JSONObject locationJSONObject = (JSONObject) resultJSONObject.get("location");

            String city = locationJSONObject.getString("city");
            String state = locationJSONObject.getString("state");

            // The instance of the data model is created here, once we're sure that the web service
            // has responded successful. Before this, it is not necessary to instance the data model
            currentWeatherDataModel = new Weather();
            currentWeatherDataModel.setCityName(city);
            currentWeatherDataModel.setStateName(state);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Second connection endpoint response:
     *
     * Calls the 'conditions' endpoint which receives the city and state and
     * sends the current weather
     *
     * Example: http://api.wunderground.com/api/9a232047401b1785/conditions/q/OH/Westerville.json
     */
    private void connectToConditionsEndpoint() {

        if (isNetworkConnectionEstablished()) {

            String endpointName = "conditions";
            String endpointURI = CONDITIONS_ENDPOINT_URI;
            String city = currentWeatherDataModel.getCityName();
            String state = currentWeatherDataModel.getStateName();

            String endpointURL = MAIN_URL + WEATHER_API_KEY + endpointURI + state + "/" + city + "/" + "." + "json";

            executeConnectionAsyncTask(endpointName, endpointURL);

        } else {
            onConnectionResultListener.onConnectionResult(NETWORK_ERROR, null, null);
        }
    }

    /**
     * Processing of the second connection endpoint response:
     * Parses the result of the 'conditions' endpoint call to get temperature and sky condition
     */
    private void processConditionsEndpointResult(String result) {

        try {

            JSONObject resultJSONObject = new JSONObject(result);
            JSONObject currentWeatherJSONObject = (JSONObject) resultJSONObject.get("current_observation");

            String skyCondition = currentWeatherJSONObject.getString("weather");
            String tempFahrenheit = currentWeatherJSONObject.getString("temp_f");
            String tempCelsius = currentWeatherJSONObject.getString("temp_c");

            currentWeatherDataModel.setSkyConditionsString(skyCondition);
            currentWeatherDataModel.setFahrenheitValue(tempFahrenheit);
            currentWeatherDataModel.setCelsiusValue(tempCelsius);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Third connection endpoint response:
     * Calls the 'hourly10day' endpoint which receives the city and state and
     * sends the weather for the next 10 days
     *
     * Example: http://api.wunderground.com/api/9a232047401b1785/hourly10day/q/OH/Westerville.json
     */
    private void connectToHourly10DayEndpoint() {

        if (isNetworkConnectionEstablished()) {

            String endpointName = "hourly10day";
            String endpointURI = HOURLY10DAY_ENDPOINT_URI;
            String city = currentWeatherDataModel.getCityName();
            String state = currentWeatherDataModel.getStateName();

            String endpointURL = MAIN_URL + WEATHER_API_KEY + endpointURI + state + "/" + city + "/" + "." + "json";

            executeConnectionAsyncTask(endpointName, endpointURL);

        } else {
            onConnectionResultListener.onConnectionResult(NETWORK_ERROR, null, null);
        }
    }

    /**
     * Processing of the third connection endpoint response:
     * Parses the result of the 'hourly10day' endpoint call to get hourly temperature and sky condition
     * for the next 7 days.
     */
    private void processHourly10DayEndpointResult(String result) {

        try {

            JSONObject resultJSONObject = new JSONObject(result);
            JSONArray hourlyForecastJSONArray = (JSONArray) resultJSONObject.get("hourly_forecast");

            weeklyWeatherList = new ArrayList();

            List dailyWeatherList = new ArrayList<>();

            int hourlyForecastIndex = 0;
            for (int daysCount = 1; daysCount <= DAY_ROW_COUNT; daysCount++) {

                while (hourlyForecastIndex < hourlyForecastJSONArray.length() - 1) {

                    JSONObject hourlyForecastJSONObject = hourlyForecastJSONArray.getJSONObject(hourlyForecastIndex);

                    JSONObject ftctimeJSONObject = hourlyForecastJSONObject.getJSONObject("FCTTIME");
                    String date = ftctimeJSONObject.getString("epoch");
                    String time = ftctimeJSONObject.getString("civil");

                    JSONObject temperatureJSONObject = hourlyForecastJSONObject.getJSONObject("temp");
                    String tempFahrenheit = temperatureJSONObject.getString("english");
                    String tempCelsius = temperatureJSONObject.getString("metric");

                    String condition = hourlyForecastJSONObject.getString("condition");

                    String iconString = hourlyForecastJSONObject.getString("icon");

                    HourlyWeather hourlyWeatherDataModel = new HourlyWeather();
                    hourlyWeatherDataModel.setDate(new Date(Long.parseLong(date) * 1000));
                    hourlyWeatherDataModel.setTimeValue(time);
                    hourlyWeatherDataModel.setTemperatureFahrenheitValue(tempFahrenheit);
                    hourlyWeatherDataModel.setTemperatureCelsiusValue(tempCelsius);
                    hourlyWeatherDataModel.setConditionResourceID(iconString);
                    hourlyWeatherDataModel.setConditionString(condition);

                    if (dailyWeatherList.size() > 0) {

                        // Get the last date
                        HourlyWeather lastHourlyWeatherDataModel = (HourlyWeather) dailyWeatherList.get(dailyWeatherList.size() - 1);
                        Calendar lastDateCalendar = Calendar.getInstance();
                        lastDateCalendar.setTime(lastHourlyWeatherDataModel.getDate());

                        // Get the current date
                        Calendar currentDateCalendar = Calendar.getInstance();
                        currentDateCalendar.setTime(hourlyWeatherDataModel.getDate());

                        if (currentDateCalendar.get(Calendar.YEAR) == lastDateCalendar.get(Calendar.YEAR) &&
                            currentDateCalendar.get(Calendar.DAY_OF_YEAR) == lastDateCalendar.get(Calendar.DAY_OF_YEAR)) {

                            dailyWeatherList.add(hourlyWeatherDataModel);

                        } else {

                            weeklyWeatherList.add(dailyWeatherList);

                            dailyWeatherList = new ArrayList();
                            dailyWeatherList.add(hourlyWeatherDataModel);

                            hourlyForecastIndex++;
                            break;
                        }

                    } else {

                        dailyWeatherList.add(hourlyWeatherDataModel);
                    }

                    hourlyForecastIndex++;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Creates a new instance of the async task and executes it.
     *
     *  It is called for each connection to the RESTful API endpoint
     **/
    private void executeConnectionAsyncTask(String endpointName, String endpointURL) {

        ConnectionAsyncTask weatherAPIConnectionAsyncTask = new ConnectionAsyncTask();
        weatherAPIConnectionAsyncTask.setOnResponseListener(new WeatherAPIConnectionAsyncTaskOnResponseListener());
        weatherAPIConnectionAsyncTask.execute(endpointName, endpointURL);
    }

    private boolean isNetworkConnectionEstablished() {

        // Check if a network connection is available
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }

        return false;
    }
}

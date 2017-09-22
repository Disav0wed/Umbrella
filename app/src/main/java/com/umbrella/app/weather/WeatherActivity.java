package com.umbrella.app.weather;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umbrella.R;
import com.umbrella.app.dialog.DialogBuilder;
import com.umbrella.app.userinput.UserInputActivity;
import com.umbrella.app.userinput.UserInputManager;
import com.umbrella.app.weather.api.WunderGroundAPIManager;
import com.umbrella.app.weather.models.Weather;

import java.util.List;

/**
 * Created by ibrahimserpici on 9/22/17.
 */

public class WeatherActivity extends FragmentActivity {

    // Views
    private RelativeLayout currentWeatherRelativeLayout;
    private TextView cityTextView;
    private TextView temperatureTextView;
    private TextView skyConditionTextView;

    private Button settingsButton;

    private RecyclerView weeklyWeatherRecyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_result_list_activity);

        UserInputManager.newInstance(this);

        /** Get the components of the content layout */

        // Current weather section
        currentWeatherRelativeLayout = findViewById(R.id.currentWeatherRL);
        cityTextView = findViewById(R.id.cityTV);
        temperatureTextView = findViewById(R.id.temperatureTV);
        skyConditionTextView = findViewById(R.id.conditionTV);

        settingsButton = findViewById(R.id.switchtoUserInputButton);
        settingsButton.setOnClickListener(new SettingsButtonOnClickListener());

        // Weeky weather section
        weeklyWeatherRecyclerView = findViewById(R.id.weeklyWeatherRW);
        weeklyWeatherRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        retrieveForecastInformation();
    }

    /**
     * This method checks if user has already types a ZIP code and if true, creates a new API connection
     * to retrieve the forecast information for the next 7 days. Also sets a OnConnectionResultListener
     * which will be called when the result of the API's secuence connections has finished retrieving
     * the forecast information
     * */
    private void retrieveForecastInformation() {

        String zipCode = UserInputManager.getZIPCode();
        if (zipCode.equals("")) {

            showDialogFragment(this.getString(R.string.zip_code_dialog_fragment_title), this.getString(R.string.zip_code_dialog_fragment_message), new NoZipCodeDialogFragmentPositiveButtonOnClickListener());
            return;
        }

        WunderGroundAPIManager weatherAPIManager = new WunderGroundAPIManager(this);
        weatherAPIManager.setOnConnectionResultListener(new WeatherAPIManagerOnConnectionResultListener());
        weatherAPIManager.connect(zipCode);
    }

    /** Listens if the API has finished retrieving the forecast informations.
     * If there any error, this will be reported to user in a Dialog.
     * If a successful result is received, we'll populate both current weather a forecast sections
     * */
    private class WeatherAPIManagerOnConnectionResultListener implements WunderGroundAPIManager.OnConnectionResultListener {

        @Override
        public void onConnectionResult(String status, Weather currentWeatherDataModel, List weeklyWeatherList) {

            if (status.equals(WunderGroundAPIManager.NETWORK_ERROR)) {
                showDialogFragment(getString(R.string.no_network_dialog_fragment_title), getString(R.string.no_network_dialog_fragment_message), new NoNetworkDialogFragmentPositiveButtonOnClickListener());

            }  else if (status.equals(WunderGroundAPIManager.RESULT_TIMEOUT)) {

                showDialogFragment(getString(R.string.result_timeout_dialog_fragment_title), getString(R.string.result_timeout_dialog_fragment_message), new ResultTmeoutDialogFragmentPositiveButtonOnClickListener());

            } else if (status.equals(WunderGroundAPIManager.RESULT_ERROR)) {

                showDialogFragment(getString(R.string.result_error_dialog_fragment_title), getString(R.string.result_error_dialog_fragment_message), new ResultErrorDialogFragmentPositiveButtonOnClickListener());

            } else if (status.equals(WunderGroundAPIManager.RESULT_OK)) {

                populateCurrentWeatherView(currentWeatherDataModel);
                populateWeeklyWeatherView(weeklyWeatherList);
            }
        }
    }

    /**
     * Populates the Current Weather Sections
     * It takes currentWeatherDataModel to show the ZIP code's city and state,
     * the current temperature and the sky current condition
     * */
    private void populateCurrentWeatherView(Weather currentWeatherDataModel) {

        // Populate location with city and state
        String cityString = currentWeatherDataModel.getCityName();
        String stateString = currentWeatherDataModel.getStateName();
        cityTextView.setText(cityString + ", " + stateString);

        // Populate temperature with the user's preferred units
        String temperatureString;

        // Select preferred units from user's preferences
        String temperatureUnits = UserInputManager.getTemperatureUnits();
        if (temperatureUnits.equals("Fahrenheit")) {
            temperatureString = currentWeatherDataModel.getFahrenheitValue() + "° F";
        } else {
            temperatureString = currentWeatherDataModel.getCelsiusValue() + "° C";
        }
        temperatureTextView.setText(temperatureString);

        // Select background color depending of temperature
        int color;
        double temperature = Double.parseDouble(currentWeatherDataModel.getFahrenheitValue());
        if (temperature < 60.0) {
            color = R.color.coolWeatherColor;
        } else {
            color = R.color.warmWeatherColor;
        }
        currentWeatherRelativeLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), color));

        // Populate sky conditions
        String skyConditionsString = currentWeatherDataModel.getSkyConditionsString();
        skyConditionTextView.setText(skyConditionsString);
    }

    /**
     * Set a RecyclerViewAdapter which will parse the information for the weekly forecast
     * and populates the GridLayout showing the information in a daily basis
     * */
    private void populateWeeklyWeatherView(List weeklyWeatherList) {

        // Specify an adapter
        WeatherWeeklyRecyclerViewAdapter weatherWeeklyRecyclerViewAdapter = new WeatherWeeklyRecyclerViewAdapter(this, weeklyWeatherList);
        weeklyWeatherRecyclerView.setAdapter(weatherWeeklyRecyclerViewAdapter);
    }

    /**
     * Called when the user presses the Settings button.
     * It will open the settings activity
     * */
    private class SettingsButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            startSettingsActivity();
        }
    }

    /**
     * Shows a multi purpose DialogFragment.
     */
    private void showDialogFragment(String title, String message, DialogInterface.OnClickListener onClickListener) {

        DialogBuilder applicationDialogFragment = DialogBuilder.newInstance(this);
        applicationDialogFragment.setTitle(title);
        applicationDialogFragment.setMessage(message);
        applicationDialogFragment.setCancelable(false);
        applicationDialogFragment.setPositiveButtonOnClickListener(onClickListener);
        applicationDialogFragment.show(getFragmentManager(), null);
    }

    /**
     * Called when user presses the OK button in the No ZIP code dialog
     * It will open the settings activity, so the user can enter a ZIP code
     * */
    private class NoZipCodeDialogFragmentPositiveButtonOnClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            startSettingsActivity();
        }
    }

    /**
     * Called when user presses the OK button in the No Network dialog.
     * It will try to connect again
     * */
    private class NoNetworkDialogFragmentPositiveButtonOnClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            retrieveForecastInformation();
        }
    }

    /**
     * Called when user presses the OK button in the Result Error dialog.
     * It will try to connect again
     * */
    private class ResultTmeoutDialogFragmentPositiveButtonOnClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            retrieveForecastInformation();
        }
    }

    /**
     * Called when user presses the OK button in the Result Error dialog.
     * It will try to connect again
     * */
    private class ResultErrorDialogFragmentPositiveButtonOnClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            startSettingsActivity();
        }
    }

    /**
     * Start the Settings Activity
     * */
    private void startSettingsActivity() {

        Intent intent = new Intent(WeatherActivity.this, UserInputActivity.class);
        startActivity(intent);
    }
}

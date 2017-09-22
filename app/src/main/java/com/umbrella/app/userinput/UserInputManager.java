package com.umbrella.app.userinput;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.umbrella.R;

/**
 * Created by ibrahimserpici on 9/22/17.
 */

public class UserInputManager {

    public final static String FAHRENHEIT_UNITS = "Fahrenheit";
    public final static String CELSIUS_UNITS = "Celsius";

    private static Context context;

    private static UserInputManager userInputManager;
    private static SharedPreferences sharedPreferences;

    public static UserInputManager newInstance(Context context) {

        UserInputManager.context = context;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        userInputManager = new UserInputManager();

        return userInputManager;
    }

    /**
     * Returns the ZIP code entered by the user, or null if user has not entered a ZIP code
     */
    public static String getZIPCode() {
        return sharedPreferences.getString(context.getString(R.string.zip_code_preference_key), "");
    }

    /**
     * Returns the temperature units code preferred by the user,
     * or Fahrenheit as default if user has not entered preferred units
     */
    public static String getTemperatureUnits() {

        String value = sharedPreferences.getString(context.getString(R.string.units_preference_key), "0");

        if (value.equals("1")) {
            return UserInputManager.FAHRENHEIT_UNITS;

        } else if (value.equals("2")) {
            return UserInputManager.CELSIUS_UNITS;
        }

        return "";
    }
}

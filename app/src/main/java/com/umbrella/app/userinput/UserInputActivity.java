package com.umbrella.app.userinput;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.umbrella.R;
import com.umbrella.app.weather.api.WunderGroundAPIManager;

/**
 * Created by ibrahimserpici on 9/22/17.
 */

public class UserInputActivity extends FragmentActivity {

    private EditTextPreference zipCodePreference;
    private ListPreference unitsPreference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new UserInputPreferenceFragment(), WunderGroundAPIManager.USERINPUT_PREFERENCE_FRAGMENT_TAG).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        UserInputPreferenceFragment settingsPreferenceFragment = (UserInputPreferenceFragment) getFragmentManager().findFragmentByTag(WunderGroundAPIManager.USERINPUT_PREFERENCE_FRAGMENT_TAG);

        zipCodePreference = (EditTextPreference) settingsPreferenceFragment.findPreference(getString(R.string.zip_code_preference_key));
        zipCodePreference.setSummary((zipCodePreference.getText() != null && !zipCodePreference.getText().equals("")) ? zipCodePreference.getText() : getString(R.string.zip_code_preference_default_summary));
        zipCodePreference.setOnPreferenceChangeListener(new ZipCodePreferenceOnPreferenceChangeListener());

        unitsPreference = (ListPreference) settingsPreferenceFragment.findPreference(getString(R.string.units_preference_key));
        unitsPreference.setSummary(unitsPreference.getEntry());
        unitsPreference.setOnPreferenceChangeListener(new UnitsPreferenceOnPreferenceChangeListener());
    }

    /** Called when the Preferred ZIP code has been changed */
    private class ZipCodePreferenceOnPreferenceChangeListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            // Convert newValue to String
            String newZIPString = newValue.toString();

            // If the new Value is not empty, set it, else set the placeHolder
            zipCodePreference.setSummary(!newZIPString.equals("") ? newZIPString : getString(R.string.zip_code_preference_default_summary));

            return true;
        }
    }

    /** Called when the preferred temperature units have been changed */
    private class UnitsPreferenceOnPreferenceChangeListener implements Preference.OnPreferenceChangeListener {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            // Get the index of newValue in the array of values
            int index = unitsPreference.findIndexOfValue(newValue.toString());

            // Get the array of entries
            CharSequence[] entries = unitsPreference.getEntries();

            // Set the new summary with the selected entry
            String newSummaryString = entries[index].toString();
            unitsPreference.setSummary(newSummaryString);

            return true;
        }
    }
}

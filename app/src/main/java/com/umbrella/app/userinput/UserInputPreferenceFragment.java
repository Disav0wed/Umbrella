package com.umbrella.app.userinput;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.umbrella.R;

/**
 * Created by ibrahimserpici on 9/22/17.
 */

    /* Use PreferenceFragment which automatically save user preferences into SharedPreferences */
public class UserInputPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the Preferences XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}

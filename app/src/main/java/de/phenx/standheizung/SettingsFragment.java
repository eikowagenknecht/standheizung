package de.phenx.standheizung;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import de.phenx.standheizung.util.SMSUtils;
import de.phenx.standheizung.util.ThermoCall;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an xml resource
        addPreferencesFromResource(R.xml.preferences);

        // React to Setting changes
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String phoneNumber = sharedPref.getString("pref_phone_number", "");
        final String pin = sharedPref.getString("pref_pin", "");

        final ListPreference heatingTimePreference = (ListPreference) findPreference("pref_duration_minutes");
        heatingTimePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SMSUtils.sendSMS(getActivity(),
                        phoneNumber,
                        ThermoCall.DURATION.replace("%1", pin).replace("%2", newValue.toString()));
                return true;
            }
        });
    }
}

package de.phenx.standheizung;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Locale;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, true);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Set text in UI
        EditText timeEditText = (EditText) getActivity().findViewById(R.id.edittext_leaving_time);
        final String newText = String.format(Locale.GERMANY, "%02d", hourOfDay) + ":" + String.format(Locale.GERMANY, "%02d", minute);
        timeEditText.setText(newText);
        // And store it
        sharedPref.edit().putString("pref_starttime", newText).apply();
    }
}


package de.phenx.standheizung;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import de.phenx.standheizung.util.SMSUtils;
import de.phenx.standheizung.util.ThermoCall;

/**
 * TODO:
 * - Status Abfrage implementieren
 * - Benachrichtigungen
 * - Übersetztung DE/EN
 * - Source nach github
 * - Einstellung HTM an/aus ermöglichen
 * - Eigenes App Icon
 */
public class MainActivity extends AppCompatActivity {
    final int MODE_INSTANT = 0;
    final int MODE_TIME_BASED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleFirstStart();
        initializeLayout();
        initializeUiElements();
    }

    private void handleFirstStart() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("pref_first_app_start", true)) {
            // Include some more Settings
            sharedPref.edit().putInt("pref_mode", MODE_INSTANT).apply();

            // Store that we started one time at least
            sharedPref.edit().putBoolean("pref_first_app_start", false).apply();
        }
    }

    private void initializeLayout() {
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initializeUiElements() {
        initializeTimeEditText();
        initializeStatusText();
        initializeStartModeSpinner();
        initializeDurationSpinner();
        // initializeFloatingActionButton();
    }

    /*
    private void initializeFloatingActionButton() {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Phone Number: " + phoneNumber, Snackbar.LENGTH_LONG).show();
                //SMSUtils.sendSMS(MainActivity.this, phoneNumber, "This is a test message.");
                Snackbar.make(findViewById(R.id.fab), sharedPref.getString("pref_duration_minutes", ""), Snackbar.LENGTH_LONG).show();
            }
        });
    }
    */


    private void initializeTimeEditText() {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final String savedStartTime = sharedPref.getString("pref_starttime", "00:00");

        EditText timeEditText = (EditText) findViewById(R.id.edittext_leaving_time);
        timeEditText.setText(savedStartTime);
        timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new TimePickerFragment();
                dialogFragment.show(getFragmentManager(), "timePicker");
            }
        });
    }

    private void initializeStatusText() {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final String savedStatusText = sharedPref.getString("pref_status", "---");

        TextView statusTextView = (TextView) findViewById(R.id.textview_status_value);
        statusTextView.setText(savedStatusText);
    }

    private void initializeStartModeSpinner() {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Spinner startModeSpinner = (Spinner) findViewById(R.id.spinner_start_modes);
        int savedMode = sharedPref.getInt("pref_mode", MODE_INSTANT);
        startModeSpinner.setSelection(savedMode);
        startModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Snackbar.make(findViewById(R.id.fab), String.valueOf(position), Snackbar.LENGTH_LONG).show();

                // Hide unnecessary fields
                if (position == 0) {
                    setTimerItemsVisibility(false);
                    sharedPref.edit().putInt("pref_mode", MODE_INSTANT).apply();
                } else {
                    setTimerItemsVisibility(true);
                    sharedPref.edit().putInt("pref_mode", MODE_TIME_BASED).apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initializeDurationSpinner() {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Spinner durationSpinner = (Spinner) findViewById(R.id.spinner_duration);
        String savedDuration = sharedPref.getString("pref_duration_minutes", "30");
        ArrayAdapter<CharSequence> durationItemsAdapter = ArrayAdapter.createFromResource(this, R.array.durations, android.R.layout.simple_spinner_item);
        int savedSpinnerPosition = durationItemsAdapter.getPosition(savedDuration);
        durationSpinner.setSelection(savedSpinnerPosition);
        durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] durationsArray = getResources().getStringArray(R.array.durations);
                String newDuration = durationsArray[position];
                String oldDuration = sharedPref.getString("pref_duration_minutes", "");
                if (!newDuration.equals(oldDuration)) {
                    //Snackbar.make(findViewById(R.id.spinner_duration), "Position:" + String.valueOf(position) + ", Value: " + newDuration, Snackbar.LENGTH_LONG).show();
                    sharedPref.edit().putString("pref_duration_minutes", newDuration).apply();
                    final String phoneNumber = sharedPref.getString("pref_phone_number", "");
                    final String pin = sharedPref.getString("pref_pin", "");
                    SMSUtils.sendSMS(MainActivity.this, phoneNumber, ThermoCall.DURATION.replace("%1", pin).replace("%2", newDuration));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case SMSUtils.SMS_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, resend the message.
                } else {
                    // Permission was denied, tell the user that the app can't work without.
                }
            }
        }
    }
    */

    public void turnOn(View view) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String smsCommand;
        String statusText;

        if (sharedPref.getInt("pref_mode", MODE_INSTANT) == MODE_TIME_BASED) { // Time based
            EditText leavingTimeEditText = (EditText) findViewById(R.id.edittext_leaving_time);
            String time = leavingTimeEditText.getText().toString().replace(":", "");
            smsCommand = ThermoCall.TURN_ON_DELAYED.replace("%1", time);
            statusText = ("[" + getCurrentDay() + "] " + "Das Auto wird aufgeheizt sein um " + leavingTimeEditText.getText() + ".");
        } else { // Instant
            smsCommand = ThermoCall.TURN_ON;
            statusText = ("[" + getCurrentDay() + "] " + "Die Standheizung wurde angeschaltet um " + getCurrentTime() + ", Laufzeit " + sharedPref.getString("pref_duration_minutes", "") + " min.");
        }

        boolean smsSent = SMSUtils.sendSMS(this, PreferenceManager.getDefaultSharedPreferences(this).getString("pref_phone_number", ""), smsCommand);
        if (smsSent) {
            Snackbar.make(view, "Sent " + "\"" + smsCommand + "\"" + " to " + PreferenceManager.getDefaultSharedPreferences(this).getString("pref_phone_number", ""), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            setStatusText(statusText);
        }
    }

    public void setStatusText(String statusText) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //final String savedStatusText = sharedPref.getString("pref_status", "---");

        TextView statusTextView = (TextView) findViewById(R.id.textview_status_value);
        statusTextView.setText(statusText);
        sharedPref.edit().putString("pref_status", statusText).apply();
    }

    public void turnOff(View view) {
        String smsCommand = ThermoCall.TURN_OFF;
        boolean smsSent = SMSUtils.sendSMS(this, PreferenceManager.getDefaultSharedPreferences(this).getString("pref_phone_number", ""), ThermoCall.TURN_OFF);
        if (smsSent) {
            Snackbar.make(view, "Sent " + "\"" + smsCommand + "\"" + " to " + PreferenceManager.getDefaultSharedPreferences(this).getString("pref_phone_number", ""), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        setStatusText("Standheizung ausgeschaltet / deaktiviert.");
    }

    private void setTimerItemsVisibility(boolean showTimerItems) {
        View leavingTimeTextView = findViewById(R.id.textview_leaving_time);
        View leavingTimeEditText = findViewById(R.id.edittext_leaving_time);

        if (showTimerItems) {
            leavingTimeTextView.setVisibility(View.VISIBLE);
            leavingTimeEditText.setVisibility(View.VISIBLE);
        } else {
            leavingTimeTextView.setVisibility(View.GONE);
            leavingTimeEditText.setVisibility(View.GONE);
        }
    }

    public void refreshStatus(View view) {
        Snackbar.make(view, "Not implemented yet", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        /*
        String smsCommand = ThermoCall.STATUS;
        boolean smsSent = SMSUtils.sendSMS(this, PreferenceManager.getDefaultSharedPreferences(this).getString("pref_phone_number", ""), ThermoCall.STATUS);
        if (smsSent) {
            Snackbar.make(view, "Sent " + "\"" + smsCommand + "\"" + " to " + PreferenceManager.getDefaultSharedPreferences(this).getString("pref_phone_number", ""), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }*/

    }

    private String getCurrentDay() {
        final Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        String formattedDate = String.format(Locale.GERMANY, "%02d", day) + "." + String.format(Locale.GERMANY, "%02d", month) + "." + String.format(Locale.GERMANY, "%04d", year);
        return formattedDate;
    }

    private String getCurrentTime() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        String formattedTime = String.format(Locale.GERMANY, "%02d", hour) + ":" + String.format(Locale.GERMANY, "%02d", minute);
        return formattedTime;
    }
}

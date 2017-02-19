package de.phenx.standheizung.util;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.util.ArrayList;

import de.phenx.standheizung.R;

public class SMSUtils extends BroadcastReceiver {
    public static final String SENT_SMS_ACTION_NAME = "SMS_SENT";
    public static final String DELIVERED_SMS_ACTION_NAME = "SMS_DELIVERED";
    public static final String RECEIVED_SMS_ACTION_NAME = "SMS_RECEIVED";
    public static final int UNREGISTER_TIMEOUT_SECONDS = 10000;
    public static final int SMS_PERMISSION_REQUEST_CODE = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Message sent
        if (intent.getAction().equals(SENT_SMS_ACTION_NAME)) {
            switch (getResultCode()) {
                case Activity.RESULT_OK: // Sms sent
                    Toast.makeText(context,
                            context.getString(R.string.sms_sent),
                            Toast.LENGTH_LONG)
                            .show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context,
                            context.getString(R.string.sms_not_sent),
                            Toast.LENGTH_LONG)
                            .show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context,
                            context.getString(R.string.sms_not_sent_no_service),
                            Toast.LENGTH_LONG)
                            .show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context,
                            context.getString(R.string.sms_not_sent),
                            Toast.LENGTH_LONG)
                            .show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context,
                            context.getString(R.string.sms_not_sent_no_radio),
                            Toast.LENGTH_LONG)
                            .show();
                    break;
            }
        }
        // Message delivered
        else if (intent.getAction().equals(DELIVERED_SMS_ACTION_NAME)) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context,
                            context.getString(R.string.sms_delivered),
                            Toast.LENGTH_LONG)
                            .show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(context,
                            context.getString(R.string.sms_not_delivered),
                            Toast.LENGTH_LONG)
                            .show();
                    break;
            }
        }
        // Incoming message
        /*
        // http://stackoverflow.com/questions/1973071/broadcastreceiver-sms-received
        // https://mobiforge.com/design-development/sms-messaging-android
        else if (intent.getAction().equals(RECEIVED_SMS_ACTION_NAME)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] smsData = (Object[]) bundle.get("pdus");
                SmsMessage[] smsMessages = new SmsMessage[smsData.length];
                String stringMessage = "";
                for (int i = 0; i < smsMessages.length; i++) {
                    smsMessages[i] = SmsMessage.createFromPdu((byte[]) smsData[i]);
                    stringMessage += "SMS from " + smsMessages[i].getOriginatingAddress();
                    stringMessage += " :";
                    stringMessage += smsMessages[i].getMessageBody().toString();
                    stringMessage += "n";
                }
                //---display the new SMS message---
                Toast.makeText(context, stringMessage, Toast.LENGTH_SHORT).show();
            }
        }
        */
    }

    public static boolean canSendSMS(Activity activity) {
        return activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    public static boolean sendSMS(final Activity activity, String phoneNumber, String message) {
        if (!hasPermission(activity)) {
            requestPermission(activity);
            return false;
        }

        if (!canSendSMS(activity)) {
            Toast.makeText(activity,
                    activity.getString(R.string.sms_cannot_send),
                    Toast.LENGTH_LONG)
                    .show();
            return false;
        }

        if (!PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)) {
            Toast.makeText(activity,
                    activity.getString(R.string.phone_number_invalid),
                    Toast.LENGTH_LONG)
                    .show();
            return false;
        }

        PendingIntent sentPendingIntent = PendingIntent.getBroadcast(activity,
                0, new Intent(SENT_SMS_ACTION_NAME), 0);
        PendingIntent deliveredPendingIntent = PendingIntent.getBroadcast(activity,
                0, new Intent(DELIVERED_SMS_ACTION_NAME), 0);

        final SMSUtils smsUtils = new SMSUtils();
        activity.registerReceiver(smsUtils, new IntentFilter(SENT_SMS_ACTION_NAME));
        activity.registerReceiver(smsUtils, new IntentFilter(DELIVERED_SMS_ACTION_NAME));

        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> messageParts = smsManager.divideMessage(message);
        if (messageParts.size() > 1) {
            Toast.makeText(activity,
                    activity.getString(R.string.sms_too_long),
                    Toast.LENGTH_LONG)
                    .show();
            return false;
        }

        smsManager.sendTextMessage(phoneNumber, null, message, sentPendingIntent, deliveredPendingIntent);

        // We unsubscribe in 10 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                activity.unregisterReceiver(smsUtils);
            }
        }, SMSUtils.UNREGISTER_TIMEOUT_SECONDS * 1000);

        return true;
    }

    private static boolean hasPermission(final Activity activity) {
        int permissionCheck = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.SEND_SMS);
        return (permissionCheck == PackageManager.PERMISSION_GRANTED);
    }

    private static void requestPermission(final Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_REQUEST_CODE);
    }
}
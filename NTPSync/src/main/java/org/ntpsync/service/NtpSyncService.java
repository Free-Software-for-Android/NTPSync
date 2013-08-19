/*
 * Copyright (C) 2012 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * This file is part of NTPSync.
 * 
 * NTPSync is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NTPSync is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NTPSync.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.ntpsync.service;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.net.ntp.TimeInfo;
import org.ntpsync.R;
import org.ntpsync.util.Constants;
import org.ntpsync.util.Log;
import org.ntpsync.util.NtpSyncUtils;
import org.ntpsync.util.PreferenceHelper;
import org.ntpsync.util.Utils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.widget.Toast;

public class NtpSyncService extends IntentService {

    // extras that can be given by intent
    public static final String EXTRA_MESSENGER = "messenger";
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_DATA = "data";

    // possible actions in this service
    public static final int ACTION_QUERY = 1;
    public static final int ACTION_QUERY_DETAILED = 2;

    // keys for data bundle
    public static final String DATA_GET_NTP_SERVER_FROM_PREFS = "use_ntp_server_from_prefs";
    public static final String DATA_APPLY_DIRECTLY = "apply_directly";

    // messages that can be send to handler
    public static final int RETURN_GENERIC_ERROR = 0;
    public static final int RETURN_OKAY = 1;
    public static final int RETURN_SERVER_TIMEOUT = 2;
    public static final int RETURN_NO_ROOT = 3;

    // returned message data
    public static final String MESSAGE_DATA_TIME = "time";
    public static final String MESSAGE_DATA_DETAILED_OUTPUT = "detailed_output";

    Messenger mMessenger;
    Bundle mData;

    // private static WifiLock wifiLock;
    private static WakeLock wakeLock;

    public NtpSyncService() {
        super("NtpService");
    }

    private static void lock() {
        try {
            wakeLock.acquire();
        } catch (Exception e) {
            Log.e(Constants.TAG, "Error getting Lock!", e);
        }
    }

    private static void unlock() {
        if (wakeLock.isHeld())
            wakeLock.release();
    }

    private static void getLocks(Context context) {
        // initialise the lock
        wakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "NtpSyncWakeLock");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // lock cpu
        getLocks(this);
        lock();

        Bundle extras = intent.getExtras();
        if (extras == null) {
            Log.e(Constants.TAG, "Extra bundle is null!");
            return;
        }

        // fail if required keys are not present
        if (!extras.containsKey(EXTRA_ACTION)) {
            Log.e(Constants.TAG, "Extra bundle must contain a action!");
            return;
        }

        int action = extras.getInt(EXTRA_ACTION);

        // for these actions we get a result back which is send via the messenger and we require
        // a data bundle
        if (!(extras.containsKey(EXTRA_DATA))) {
            Log.e(Constants.TAG, "Extra bundle must contain a data bundle!");
            return;
        } else {
            mData = extras.getBundle(EXTRA_DATA);
        }

        boolean noMessenger = false;
        if (!(extras.containsKey(EXTRA_MESSENGER))) {
            Log.e(Constants.TAG, "No messenger present, using default result handling!");
            noMessenger = true;
        } else {
            mMessenger = (Messenger) extras.get(EXTRA_MESSENGER);
        }

        // get NTP server from preferences
        String ntpHostname = PreferenceHelper.getNtpServer(this);

        // default values
        int returnMessage = RETURN_GENERIC_ERROR;
        long offset = 0;

        // execute action from extra bundle
        switch (action) {
        case ACTION_QUERY:

            // return time to ui
            Bundle messageData = new Bundle();
            try {
                offset = NtpSyncUtils.query(ntpHostname);
                returnMessage = RETURN_OKAY;

                // calculate new time
                Date newTime = new Date(System.currentTimeMillis() + offset);

                messageData.putSerializable(MESSAGE_DATA_TIME, newTime);

                if (mData.containsKey(DATA_APPLY_DIRECTLY)) {
                    if (mData.getBoolean(DATA_APPLY_DIRECTLY)) {
                        returnMessage = Utils.setTime(offset);
                    }
                }
            } catch (IOException e) {
                returnMessage = RETURN_SERVER_TIMEOUT;
                Log.d(Constants.TAG, "Timeout on server!");
            }

            if (noMessenger) {
                Message msg = Message.obtain();
                msg.arg1 = returnMessage;
                msg.setData(messageData);
                handleResult(msg);
            } else {
                sendMessageToHandler(returnMessage, messageData);
            }

            break;

        case ACTION_QUERY_DETAILED:

            String output = null;
            Bundle messageDataDetailedQuery = null;
            try {
                TimeInfo info = NtpSyncUtils.detailedQuery(ntpHostname);

                output = NtpSyncUtils.processResponse(info, this);

                // return detailed output to ui
                messageDataDetailedQuery = new Bundle();
                messageDataDetailedQuery.putSerializable(MESSAGE_DATA_DETAILED_OUTPUT, output);

                returnMessage = RETURN_OKAY;
            } catch (IOException e) {
                returnMessage = RETURN_SERVER_TIMEOUT;
                Log.d(Constants.TAG, "Timeout on server!");
            }

            if (noMessenger) {
                Message msg = Message.obtain();
                msg.arg1 = returnMessage;
                msg.setData(messageDataDetailedQuery);
                handleResult(msg);
            } else {
                sendMessageToHandler(returnMessage, messageDataDetailedQuery);
            }

            break;

        default:
            break;

        }

        // unlock cpu
        unlock();
    }

    private void handleResult(final Message message) {
        Log.d(Constants.TAG, "Handle message directly in NtpSyncService...");

        // we need a looper to get toasts displayed from service!
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            public void run() {
                DateFormat df = DateFormat.getDateTimeInstance();

                switch (message.arg1) {
                case NtpSyncService.RETURN_GENERIC_ERROR:
                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.app_name) + ": "
                                    + getString(R.string.return_generic_error), Toast.LENGTH_LONG)
                            .show();

                    break;

                case NtpSyncService.RETURN_OKAY:

                    Bundle returnData = message.getData();
                    final Date newTime = (Date) returnData
                            .getSerializable(NtpSyncService.MESSAGE_DATA_TIME);

                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.app_name) + ": "
                                    + getString(R.string.return_set_time) + " " + df.format(newTime),
                            Toast.LENGTH_LONG).show();

                    break;

                case NtpSyncService.RETURN_SERVER_TIMEOUT:
                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.app_name) + ": "
                                    + getString(R.string.return_timeout), Toast.LENGTH_LONG).show();

                    break;

                case NtpSyncService.RETURN_NO_ROOT:
                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.app_name) + ": "
                                    + getString(R.string.return_no_root), Toast.LENGTH_LONG).show();

                    break;

                default:
                    break;
                }
            }
        });
    }

    private void sendMessageToHandler(Integer arg1, Integer arg2, Bundle messageData) {
        Message msg = Message.obtain();
        msg.arg1 = arg1;
        if (arg2 != null) {
            msg.arg2 = arg2;
        }
        if (messageData != null) {
            msg.setData(messageData);
        }

        try {
            mMessenger.send(msg);
        } catch (RemoteException e) {
            Log.w(Constants.TAG, "Exception sending message, Is handler present?", e);
        } catch (NullPointerException e) {
            Log.w(Constants.TAG, "Messenger is null!", e);
        }
    }

    private void sendMessageToHandler(Integer arg1, Bundle messageData) {
        sendMessageToHandler(arg1, null, messageData);
    }

}

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

import java.util.Date;

import org.ntpsync.util.Constants;
import org.ntpsync.util.Log;
import org.ntpsync.util.NtpSyncUtils;
import org.ntpsync.util.PreferencesHelper;
import org.ntpsync.util.Utils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;

public class NtpSyncService extends IntentService {

    // extras that can be given by intent
    public static final String EXTRA_MESSENGER = "messenger";
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_DATA = "data";

    // possible actions in this service
    public static final int ACTION_GET_TIME = 1;
    public static final int ACTION_GET_DETAILED = 2;
    public static final int ACTION_SET_TIME_SILENTLY = 3;

    // keys for data bundle
    public static final String DATA_GET_NTP_SERVER_FROM_PREFS = "use_ntp_server_from_prefs";
    public static final String DATA_NTP_SERVER = "ntp_server";
    public static final String DATA_APPLY_DIRECTLY = "apply_directly";

    // messages that can be send to handler
    public static final int MESSAGE_ERROR = 0;
    public static final int MESSAGE_OKAY = 1;
    public static final int MESSAGE_SERVER_TIMEOUT = 2;
    public static final int MESSAGE_NO_ROOT = 3;
    public static final int MESSAGE_UTIL_NOT_FOUND = 4;

    // returned message data
    public static final String MESSAGE_DATA_TIME = "time";
    public static final String MESSAGE_DATA_DETAILED_OUTPUT = "detailed_output";

    Messenger mMessenger;

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
        if (!(extras.containsKey(EXTRA_DATA) && extras.containsKey(EXTRA_ACTION))) {
            Log.e(Constants.TAG, "Extra bundle must contain a bundle and a action!");
            return;
        }

        Bundle data = extras.getBundle(EXTRA_DATA);
        int action = extras.getInt(EXTRA_ACTION);

        // for these actions we get a result back which is send via the messenger
        if (action == ACTION_GET_TIME || action == ACTION_GET_DETAILED) {
            if (!extras.containsKey(EXTRA_MESSENGER)) {
                Log.e(Constants.TAG,
                        "Extra bundle must contain a messenger to send result back to!");
                return;
            }
        }

        mMessenger = (Messenger) extras.get(EXTRA_MESSENGER);

        /* NTP server from prefs or from data bundle? */
        boolean getNtpServerFromPrefs = false;
        if (data.containsKey(DATA_GET_NTP_SERVER_FROM_PREFS)) {
            if (data.getBoolean(DATA_GET_NTP_SERVER_FROM_PREFS)) {
                getNtpServerFromPrefs = true;
            }
        }

        String ntpHostname = null;
        if (getNtpServerFromPrefs) {
            ntpHostname = PreferencesHelper.getNtpServer(this);
        } else {
            if (data.containsKey(DATA_NTP_SERVER)) {
                ntpHostname = data.getString(DATA_NTP_SERVER);
            } else {
                Log.e(Constants.TAG,
                        "Extra bundle must contain a ntp server or a boolean to indicate that the ntp server should be get from the prefs!");
                return;
            }

        }

        // default is error
        int returnMessage = MESSAGE_ERROR;
        long offset = 0;

        // execute action from extra bundle
        switch (action) {
        case ACTION_GET_TIME:

            try {
                offset = NtpSyncUtils.query(ntpHostname);
                returnMessage = MESSAGE_OKAY;
            } catch (Exception e) {
                // send timeout message to ui
                sendMessageToHandler(MESSAGE_SERVER_TIMEOUT);
                Log.d(Constants.TAG, "Timeout on server!");
                // abort directly
                return;
            }

            if (data.containsKey(DATA_APPLY_DIRECTLY)) {
                if (data.getBoolean(DATA_APPLY_DIRECTLY)) {
                    returnMessage = Utils.setTime(offset);
                }
            }

            // return time to ui
            Bundle messageData = new Bundle();

            // calculate new time
            Date newTime = new Date(System.currentTimeMillis() + offset);

            messageData.putSerializable(MESSAGE_DATA_TIME, newTime);

            sendMessageToHandler(returnMessage, messageData);

            break;

        case ACTION_SET_TIME_SILENTLY:
            try {
                // query time
                offset = NtpSyncUtils.query(ntpHostname);

                // set time
                returnMessage = Utils.setTime(offset);
            } catch (Exception e) {
                returnMessage = MESSAGE_SERVER_TIMEOUT;
            }

            Log.d(Constants.TAG, "Time was get and set silently with the following returnMessage: "
                    + returnMessage);

            break;

        case ACTION_GET_DETAILED:

            String output = null;
            try {
                output = NtpSyncUtils.detailedQuery(ntpHostname);
            } catch (Exception e) {
                // send timeout message to ui
                sendMessageToHandler(MESSAGE_SERVER_TIMEOUT);
                Log.d(Constants.TAG, "Timeout on server!");
                // abort directly
                return;
            }

            // return time to ui
            Bundle messageDataDetailedQuery = new Bundle();
            messageDataDetailedQuery.putSerializable(MESSAGE_DATA_DETAILED_OUTPUT, output);

            sendMessageToHandler(MESSAGE_OKAY, messageDataDetailedQuery);

            break;
        default:
            break;
        }

        // unlock cpu
        unlock();
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

    private void sendMessageToHandler(Integer arg1) {
        sendMessageToHandler(arg1, null, null);
    }

    private void sendMessageToHandler(Integer arg1, Bundle messageData) {
        sendMessageToHandler(arg1, null, messageData);
    }

}

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
import java.util.Timer;
import java.util.TimerTask;

import org.ntpsync.R;
import org.ntpsync.util.Constants;
import org.ntpsync.util.Log;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.widget.Toast;

import com.commonsware.cwac.wakeful.WakefulIntentService;

public class BackgroundService extends WakefulIntentService {

    public BackgroundService() {
        super("BackgroundService");
    }

    /**
     * Asynchronous background operations of service, with wakelock
     */
    @Override
    public void doWakefulWork(Intent intent) {
        Log.d(Constants.TAG, "Now we have internet! Start NTP query and set time...");

        /* Start NTP sync! */
        final Context appContext = getApplicationContext();
        // start service with ntp server from preferences
        Intent serviceIntent = new Intent(appContext, NtpSyncService.class);

        serviceIntent.putExtra(NtpSyncService.EXTRA_ACTION, NtpSyncService.ACTION_QUERY);

        // stop service by notifying it
        // Message is received after saving is done in service
        Handler resultHandler = new Handler() {
            public void handleMessage(Message message) {
                Log.d(Constants.TAG, "Handle message...");

                Toast toast = null;
                switch (message.arg1) {
                case NtpSyncService.RETURN_GENERIC_ERROR:
                    toast = Toast.makeText(appContext, getString(R.string.app_name) + ": "
                            + getString(R.string.return_generic_error), Toast.LENGTH_LONG);
                    toast.show();

                    break;

                case NtpSyncService.RETURN_OKAY:
                    Bundle returnData = message.getData();
                    Date newTime = (Date) returnData
                            .getSerializable(NtpSyncService.MESSAGE_DATA_TIME);

                    toast = Toast.makeText(appContext, getString(R.string.app_name) + ": "
                            + getString(R.string.return_set_time) + " " + newTime,
                            Toast.LENGTH_LONG);
                    toast.show();

                    break;

                case NtpSyncService.RETURN_SERVER_TIMEOUT:
                    toast = Toast.makeText(appContext, getString(R.string.app_name) + ": "
                            + getString(R.string.return_timeout), Toast.LENGTH_LONG);
                    toast.show();

                    break;

                case NtpSyncService.RETURN_NO_ROOT:
                    toast = Toast.makeText(appContext, getString(R.string.app_name) + ": "
                            + getString(R.string.return_no_root), Toast.LENGTH_LONG);
                    toast.show();

                    break;

                case NtpSyncService.RETURN_UTIL_NOT_FOUND:
                    toast = Toast.makeText(appContext, getString(R.string.app_name) + ": "
                            + getString(R.string.return_date_util), Toast.LENGTH_LONG);
                    toast.show();

                    break;

                default:
                    break;
                }

            };
        };

        // Create a new Messenger for the communication back
        Messenger messenger = new Messenger(resultHandler);
        serviceIntent.putExtra(NtpSyncService.EXTRA_MESSENGER, messenger);
        
        // TODO: handler is never called because service is dead when message comes back!

        Bundle data = new Bundle();
        data.putBoolean(NtpSyncService.DATA_GET_NTP_SERVER_FROM_PREFS, true);
        data.putBoolean(NtpSyncService.DATA_APPLY_DIRECTLY, true);
        serviceIntent.putExtra(NtpSyncService.EXTRA_DATA, data);

        appContext.startService(serviceIntent);
    }

}
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

import org.ntpsync.util.Constants;
import org.ntpsync.util.Log;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

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

        Bundle data = new Bundle();
        data.putBoolean(NtpSyncService.DATA_GET_NTP_SERVER_FROM_PREFS, true);
        data.putBoolean(NtpSyncService.DATA_APPLY_DIRECTLY, true);
        serviceIntent.putExtra(NtpSyncService.EXTRA_DATA, data);

        appContext.startService(serviceIntent);
    }

}
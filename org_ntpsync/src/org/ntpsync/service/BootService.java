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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

public class BootService extends Service {

    private ConnectionReceiver mConnectionReceiver;

    private class ConnectionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.d(Constants.TAG, "ConnectionMonitor invoked...");

                boolean noConnectivity = intent.getBooleanExtra(
                        ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                NetworkInfo aNetworkInfo = (NetworkInfo) intent
                        .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

                if (!noConnectivity) {
                    // if we have mobile or wifi connectivity...
                    if ((aNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
                            || (aNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI)) {

                        Log.d(Constants.TAG, "Now we have internet! Start NTP sync...");

                        /* Start NTP sync! */
                        Context appContext = context.getApplicationContext();
                        // start service and apply silently
                        Intent serviceIntent = new Intent(appContext, NtpSyncService.class);

                        serviceIntent.putExtra(NtpSyncService.EXTRA_ACTION,
                                NtpSyncService.ACTION_SET_TIME_SILENTLY);

                        appContext.startService(serviceIntent);

                        // stop BootService
                        stopSelf();
                    }
                }
            }
        }
    }

    @Override
    public void onCreate() {
        Log.d(Constants.TAG,
                "BootService invoked, registering connection receiver to wait for internet...");

        // register connection monitor to start NTP sync when network connection is
        // available
        mConnectionReceiver = new ConnectionReceiver();
        registerReceiver(mConnectionReceiver, new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Constants.TAG, "Received start id " + startId + ": " + intent);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(Constants.TAG, "Destroyed BootService!");

        unregisterReceiver(mConnectionReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

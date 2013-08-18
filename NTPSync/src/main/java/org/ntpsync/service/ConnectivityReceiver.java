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
import org.ntpsync.util.PreferenceHelper;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            Log.d(Constants.TAG, "ConnectivityReceiver invoked...");

            // only when background update is enabled in prefs
            if (PreferenceHelper.getSyncDaily(context)) {
                Log.d(Constants.TAG, "Daily sync is enabled!");

                boolean noConnectivity = intent.getBooleanExtra(
                        ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

                if (!noConnectivity) {

                    ConnectivityManager cm = (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo netInfo = cm.getActiveNetworkInfo();

                    // only when connected or while connecting...
                    if (netInfo != null && netInfo.isConnectedOrConnecting()) {

                        boolean updateOnlyOnWifi = PreferenceHelper.getSyncOnlyOnWifi(context);

                        // if we have mobile or wifi connectivity...
                        if (((netInfo.getType() == ConnectivityManager.TYPE_MOBILE) && updateOnlyOnWifi == false)
                                || (netInfo.getType() == ConnectivityManager.TYPE_WIFI)
                                || (netInfo.getType() == ConnectivityManager.TYPE_ETHERNET)) {
                            Log.d(Constants.TAG,
                                    "We have internet, start sync and disable receiver!");

                            // Start service with wakelock by using WakefulIntentService
                            Intent backgroundIntent = new Intent(context, BackgroundService.class);
                            WakefulIntentService.sendWakefulWork(context, backgroundIntent);

                            // disable receiver after we started the service
                            disableReceiver(context);
                        }
                    }
                }
            }
        }
    }

    /**
     * Enables ConnectivityReceiver
     * 
     * @param context
     */
    public static void enableReceiver(Context context) {
        ComponentName component = new ComponentName(context, ConnectivityReceiver.class);

        context.getPackageManager().setComponentEnabledSetting(component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    /**
     * Disables ConnectivityReceiver
     * 
     * @param context
     */
    public static void disableReceiver(Context context) {
        ComponentName component = new ComponentName(context, ConnectivityReceiver.class);

        context.getPackageManager().setComponentEnabledSetting(component,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}
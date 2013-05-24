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
import org.ntpsync.util.NtpSyncUtils;
import org.ntpsync.util.PreferenceHelper;
import org.ntpsync.util.Utils;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * This class exposes the remote service to the client
 */
public class NtpSyncRemoteService extends Service {

    public static final String PERMISSION_GET_TIME = "org.ntpsync.permission.GET_TIME";
    public static final String PERMISSION_SET_TIME = "org.ntpsync.permission.SET_TIME";

    public static final String OUTPUT_OFFSET = "offset";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Constants.TAG, "NtpSyncRemoteService, onCreate()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Constants.TAG, "NtpSyncRemoteService, onDestroy()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the interface
        return mBinder;
    }

    private final INtpSyncRemoteService.Stub mBinder = new INtpSyncRemoteService.Stub() {
        /**
         * Implementation of getOffset
         */
        @Override
        public int getOffset(String ntpHostname, Bundle output) throws RemoteException {
            Log.d(Constants.TAG, "getOffset called!");

            // check permission to do this
            if (checkCallingPermission(PERMISSION_GET_TIME) == PackageManager.PERMISSION_GRANTED) {
                Log.d(Constants.TAG, "Permission granted (GET_TIME)!");

                // get hostname from prefs if not defined
                if (ntpHostname == null) {
                    ntpHostname = PreferenceHelper.getNtpServer(NtpSyncRemoteService.this);
                }

                int returnMessage;
                try {
                    long offset = NtpSyncUtils.query(ntpHostname);

                    output.putLong(OUTPUT_OFFSET, offset);

                    returnMessage = NtpSyncService.RETURN_OKAY;
                } catch (Exception e) {
                    returnMessage = NtpSyncService.RETURN_SERVER_TIMEOUT;
                }

                return returnMessage;
            } else {
                Log.e(Constants.TAG, "Permission to get time is missing! You need "
                        + PERMISSION_GET_TIME);

                return NtpSyncService.RETURN_GENERIC_ERROR;
            }
        }

        /**
         * Implementation of setTime
         */
        @Override
        public int setTime(String ntpHostname, Bundle output) throws RemoteException {
            Log.d(Constants.TAG, "setTime called!");

            // check permission to do this
            if (checkCallingPermission(PERMISSION_SET_TIME) == PackageManager.PERMISSION_GRANTED) {
                Log.d(Constants.TAG, "Permission granted (SET_TIME)!");

                // get hostname from prefs if not defined
                if (ntpHostname == null) {
                    ntpHostname = PreferenceHelper.getNtpServer(NtpSyncRemoteService.this);
                }

                int returnMessage;
                try {
                    long offset = NtpSyncUtils.query(ntpHostname);

                    output.putLong(OUTPUT_OFFSET, offset);

                    returnMessage = Utils.setTime(offset);
                } catch (Exception e) {
                    returnMessage = NtpSyncService.RETURN_SERVER_TIMEOUT;
                }

                return returnMessage;
            } else {
                Log.e(Constants.TAG, "Permission to set time is missing! You need "
                        + PERMISSION_SET_TIME);

                return NtpSyncService.RETURN_GENERIC_ERROR;
            }
        }
    };

}
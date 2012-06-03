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

package org.ntpsync.apidemo;

import java.util.Date;

import org.ntpsync.service.INtpSyncRemoteService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class BaseActivity extends PreferenceActivity {

    public static final String TAG = "NTPSync API Demo";

    // messages that are returned from service
    public static final int RETURN_GENERIC_ERROR = 0;
    public static final int RETURN_OKAY = 1;
    public static final int RETURN_SERVER_TIMEOUT = 2;
    public static final int RETURN_NO_ROOT = 3;
    public static final int RETURN_UTIL_NOT_FOUND = 4;

    public static final String OUTPUT_OFFSET = "offset";

    private Activity mActivity;

    private Preference mGetTime;
    private Preference mSetTime;

    /** The primary interface we will be calling on the service. */
    INtpSyncRemoteService mService = null;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = INtpSyncRemoteService.Stub.asInterface(service);

            Log.d(TAG, "We are now connected to NtpSyncRemoteService!");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;

            Log.d(TAG, "We disconnected from NtpSyncRemoteService!");
        }
    };

    /**
     * Called when the activity is first created.
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;

        // load preferences from xml
        addPreferencesFromResource(R.xml.base_preference);

        // find preferences
        mGetTime = (Preference) findPreference("get_time_key");
        mSetTime = (Preference) findPreference("set_time_key");

        mGetTime.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                // IPC calls via AIDL are synchronous in Android!!!
                // Because of that we need to call the methods from AsyncTask or IntentService to
                // not block the UI
                AsyncTask<Void, Void, Integer> getTimeTask = new AsyncTask<Void, Void, Integer>() {
                    long offset;

                    @Override
                    protected Integer doInBackground(Void... unused) {
                        int result = RETURN_GENERIC_ERROR;

                        try {
                            Bundle output = new Bundle();
                            result = mService.getOffset(null, output);

                            offset = output.getLong(OUTPUT_OFFSET);

                            Log.d(TAG, "Result: " + result);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                        // return result to onPostExecute
                        return result;
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected void onPostExecute(Integer result) {
                        super.onPostExecute(result);

                        Toast toast = null;
                        switch (result) {
                        case RETURN_GENERIC_ERROR:
                            toast = Toast.makeText(mActivity, "Error", Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        case RETURN_OKAY:
                            // calculate new time
                            Date newTime = new Date(System.currentTimeMillis() + offset);

                            toast = Toast.makeText(mActivity, "NTP offset is " + offset + " ("
                                    + newTime + ")", Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        case RETURN_SERVER_TIMEOUT:
                            toast = Toast.makeText(mActivity, "Server timeout!", Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        default:
                            break;
                        }
                    }
                };

                getTimeTask.execute();

                return false;
            }
        });

        mSetTime.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // IPC calls via AIDL are synchronous in Android!!!
                // Because of that we need to call the methods from AsyncTask or IntentService to
                // not block the UI
                AsyncTask<Void, Void, Integer> setTimeTask = new AsyncTask<Void, Void, Integer>() {
                    long offset;

                    @Override
                    protected Integer doInBackground(Void... unused) {
                        int result = RETURN_GENERIC_ERROR;

                        try {
                            Bundle output = new Bundle();
                            result = mService.setTime(null, output);

                            Log.d(TAG, "Result: " + result);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                        // return result to onPostExecute
                        return result;
                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected void onPostExecute(Integer result) {
                        super.onPostExecute(result);

                        Toast toast = null;
                        switch (result) {
                        case RETURN_GENERIC_ERROR:
                            toast = Toast.makeText(mActivity, "Error!", Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        case RETURN_OKAY:
                            // calculate new time
                            Date newTime = new Date(System.currentTimeMillis() + offset);

                            toast = Toast.makeText(mActivity, "Time was set to " + newTime,
                                    Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        case RETURN_SERVER_TIMEOUT:
                            toast = Toast.makeText(mActivity, "Server timeout!", Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        case RETURN_NO_ROOT:
                            toast = Toast.makeText(mActivity, "No Root!", Toast.LENGTH_LONG);
                            toast.show();
                            break;

                        case RETURN_UTIL_NOT_FOUND:
                            toast = Toast.makeText(mActivity, "Date util not found!",
                                    Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        default:
                            break;
                        }
                    }
                };

                setTimeTask.execute();
                return false;
            }
        });

        // bind to NtpSync
        bindService(new Intent(INtpSyncRemoteService.class.getName()), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // unbind from NtpSync
        unbindService(mConnection);
    }
}

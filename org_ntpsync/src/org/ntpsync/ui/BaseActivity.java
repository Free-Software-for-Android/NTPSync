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

package org.ntpsync.ui;

import java.util.Date;

import org.donations.DonationsActivity;
import org.ntpsync.R;
import org.ntpsync.service.DailyListener;
import org.ntpsync.service.NtpSyncService;
import org.ntpsync.util.Constants;
import org.ntpsync.util.PreferenceHelper;
import org.ntpsync.util.Utils;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.Html;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class BaseActivity extends PreferenceActivity {

    Activity mActivity;

    private Preference mQuery;
    private Preference mDetailedQuery;
    private Preference mQueryAndSet;

    private Preference mSyncDailyPref;

    private Preference mHelp;
    private Preference mAbout;
    private Preference mDonations;

    private boolean progressEnabled;

    private void setIndeterminateProgress(Boolean enabled) {
        progressEnabled = enabled;
        setProgressBarIndeterminateVisibility(enabled);
    }

    /**
     * Retain activity on rotate and set back progress indicator
     * 
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setIndeterminateProgress(progressEnabled);
    }

    /** Called when the activity is first created. */
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // enable progress indicator for later use
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        setIndeterminateProgress(false);

        mActivity = this;

        // schedule daily sync
        WakefulIntentService.scheduleAlarms(new DailyListener(), this, false);

        getPreferenceManager().setSharedPreferencesName(Constants.PREFS_NAME);
        addPreferencesFromResource(R.xml.preferences);

        mQuery = (Preference) findPreference(getString(R.string.pref_query_key));
        mDetailedQuery = (Preference) findPreference(getString(R.string.pref_detailed_query_key));
        mQueryAndSet = (Preference) findPreference(getString(R.string.pref_query_and_set_key));
        mSyncDailyPref = findPreference(getString(R.string.pref_sync_daily_key));
        mHelp = (Preference) findPreference(getString(R.string.pref_help_key));
        mAbout = (Preference) findPreference(getString(R.string.pref_about_key));
        mDonations = (Preference) findPreference(getString(R.string.pref_donations_key));

        mQuery.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                // start progress indicator
                setIndeterminateProgress(true);

                // start service with ntp server from preferences
                Intent intent = new Intent(mActivity, NtpSyncService.class);

                intent.putExtra(NtpSyncService.EXTRA_ACTION, NtpSyncService.ACTION_QUERY);

                // Message is received after saving is done in service
                Handler resultHandler = new Handler() {
                    public void handleMessage(Message message) {
                        // stop progress indicator
                        setIndeterminateProgress(false);

                        Toast toast = null;
                        switch (message.arg1) {
                        case NtpSyncService.RETURN_GENERIC_ERROR:
                            toast = Toast.makeText(mActivity,
                                    getString(R.string.return_generic_error), Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        case NtpSyncService.RETURN_OKAY:
                            Bundle returnData = message.getData();
                            Date newTime = (Date) returnData
                                    .getSerializable(NtpSyncService.MESSAGE_DATA_TIME);

                            toast = Toast.makeText(mActivity, getString(R.string.return_get_time)
                                    + " " + newTime, Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        case NtpSyncService.RETURN_SERVER_TIMEOUT:
                            toast = Toast.makeText(mActivity, getString(R.string.return_timeout),
                                    Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        default:
                            break;
                        }

                    };
                };

                // Create a new Messenger for the communication back
                Messenger messenger = new Messenger(resultHandler);
                intent.putExtra(NtpSyncService.EXTRA_MESSENGER, messenger);

                Bundle data = new Bundle();
                data.putBoolean(NtpSyncService.DATA_GET_NTP_SERVER_FROM_PREFS, true);
                intent.putExtra(NtpSyncService.EXTRA_DATA, data);

                mActivity.startService(intent);

                return false;
            }

        });

        mQueryAndSet.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                // start progress indicator
                setIndeterminateProgress(true);

                // start service with ntp server from preferences
                Intent intent = new Intent(mActivity, NtpSyncService.class);

                intent.putExtra(NtpSyncService.EXTRA_ACTION, NtpSyncService.ACTION_QUERY);

                // Message is received after saving is done in service
                Handler resultHandler = new Handler() {
                    public void handleMessage(Message message) {
                        // stop progress indicator
                        setIndeterminateProgress(false);

                        Toast toast = null;
                        switch (message.arg1) {
                        case NtpSyncService.RETURN_GENERIC_ERROR:
                            toast = Toast.makeText(mActivity,
                                    getString(R.string.return_generic_error), Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        case NtpSyncService.RETURN_OKAY:
                            Bundle returnData = message.getData();
                            Date newTime = (Date) returnData
                                    .getSerializable(NtpSyncService.MESSAGE_DATA_TIME);

                            toast = Toast.makeText(mActivity, getString(R.string.return_set_time)
                                    + " " + newTime, Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        case NtpSyncService.RETURN_SERVER_TIMEOUT:
                            toast = Toast.makeText(mActivity, getString(R.string.return_timeout),
                                    Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        case NtpSyncService.RETURN_NO_ROOT:
                            Utils.showRootDialog(mActivity);

                            break;

                        case NtpSyncService.RETURN_UTIL_NOT_FOUND:
                            toast = Toast.makeText(mActivity, getString(R.string.return_date_util),
                                    Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        default:
                            break;
                        }

                    };
                };

                // Create a new Messenger for the communication back
                Messenger messenger = new Messenger(resultHandler);
                intent.putExtra(NtpSyncService.EXTRA_MESSENGER, messenger);

                Bundle data = new Bundle();
                data.putBoolean(NtpSyncService.DATA_GET_NTP_SERVER_FROM_PREFS, true);
                data.putBoolean(NtpSyncService.DATA_APPLY_DIRECTLY, true);
                intent.putExtra(NtpSyncService.EXTRA_DATA, data);

                mActivity.startService(intent);

                return false;
            }

        });

        mDetailedQuery.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                // start progress indicator
                setIndeterminateProgress(true);

                // start service with ntp server from preferences
                Intent intent = new Intent(mActivity, NtpSyncService.class);

                intent.putExtra(NtpSyncService.EXTRA_ACTION, NtpSyncService.ACTION_QUERY_DETAILED);

                // Message is received after saving is done in service
                Handler resultHandler = new Handler() {
                    public void handleMessage(Message message) {
                        // stop progress indicator
                        setIndeterminateProgress(false);

                        Toast toast = null;
                        switch (message.arg1) {
                        case NtpSyncService.RETURN_GENERIC_ERROR:
                            toast = Toast.makeText(mActivity,
                                    getString(R.string.return_generic_error), Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        case NtpSyncService.RETURN_OKAY:
                            Bundle returnData = message.getData();
                            String detailedOutput = (String) returnData
                                    .getString(NtpSyncService.MESSAGE_DATA_DETAILED_OUTPUT);

                            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                            builder.setTitle(R.string.detailed_query_title);
                            builder.setMessage(Html.fromHtml(detailedOutput));
                            AlertDialog alert = builder.create();
                            alert.show();

                            TextView txtAlertMsg = (TextView) alert
                                    .findViewById(android.R.id.message);
                            txtAlertMsg.setTextSize(13);

                            break;

                        case NtpSyncService.RETURN_SERVER_TIMEOUT:
                            toast = Toast.makeText(mActivity, getString(R.string.return_timeout),
                                    Toast.LENGTH_LONG);
                            toast.show();

                            break;

                        default:
                            break;
                        }

                    };
                };

                // Create a new Messenger for the communication back
                Messenger messenger = new Messenger(resultHandler);
                intent.putExtra(NtpSyncService.EXTRA_MESSENGER, messenger);

                Bundle data = new Bundle();
                data.putBoolean(NtpSyncService.DATA_GET_NTP_SERVER_FROM_PREFS, true);
                intent.putExtra(NtpSyncService.EXTRA_DATA, data);

                mActivity.startService(intent);

                return false;
            }

        });

        /*
         * Listen on click of update daily pref, register UpdateService if enabled,
         * setOnPreferenceChangeListener is not used because it is executed before setting the
         * preference value, this would lead to a false check in UpdateListener
         */
        mSyncDailyPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (PreferenceHelper.getSyncDaily(mActivity)) {
                    WakefulIntentService.scheduleAlarms(new DailyListener(), mActivity, false);
                } else {
                    WakefulIntentService.cancelAlarms(mActivity);
                }
                
                //TODO: REMOVE
                new DailyListener().sendWakefulWork(mActivity);

                return false;
            }

        });

        mHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(mActivity, HelpActivity.class));

                return false;
            }

        });

        mAbout.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(mActivity, AboutActivity.class));

                return false;
            }

        });

        mDonations.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(mActivity, DonationsActivity.class));

                return false;
            }

        });

    }
}
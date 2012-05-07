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

import org.donations.DonationsActivity;
import org.ntpsync.R;
import org.ntpsync.util.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class BaseActivity extends PreferenceActivity {

    Activity mActivity;

    private Preference mSync;

    private Preference mHelp;
    private Preference mAbout;
    private Preference mDonations;

    /** Called when the activity is first created. */
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;

        getPreferenceManager().setSharedPreferencesName(Constants.PREFS_NAME);
        addPreferencesFromResource(R.xml.preferences);

        mSync = (Preference) findPreference(getString(R.string.pref_sync_key));
        mHelp = (Preference) findPreference(getString(R.string.pref_help_key));
        mAbout = (Preference) findPreference(getString(R.string.pref_about_key));
        mDonations = (Preference) findPreference(getString(R.string.pref_donations_key));

        mSync.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                
                
                
                
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
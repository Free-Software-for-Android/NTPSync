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

import org.ntpsync.util.Constants;
import org.ntpsync.util.Log;
import org.ntpsync.util.NtpSyncUtils;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

public class NtpSyncService extends IntentService {

    public static final String EXTRA_NTP_SERVER = "ntp_server";
    public static final String EXTRA_APPLY_DIRECTLY = "apply_directly";

    public NtpSyncService() {
        super("NtpService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            Log.e(Constants.TAG, "Extra bundle is null!");
            return;
        }

        if (!extras.containsKey(EXTRA_NTP_SERVER)) {
            Log.e(Constants.TAG, "Extra bundle must contain a ntp server!");
            return;
        }

        try {
            NtpSyncUtils.query(extras.getString(EXTRA_NTP_SERVER));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (extras.containsKey(EXTRA_APPLY_DIRECTLY)) {
            if (extras.getBoolean(EXTRA_APPLY_DIRECTLY)) {

            }
        }

    }

}

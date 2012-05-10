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

package org.ntpsync.util;

import org.ntpsync.R;
import org.ntpsync.service.NtpSyncService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import com.stericson.RootTools.RootTools;

public class Utils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd.HHmmss");

    /**
     * Check if Android is rooted, check for su binary and display possible solutions if they are
     * not available
     * 
     * @param activity
     * @return true if phone is rooted
     */
    public static boolean isAndroidRooted() {
        boolean rootAvailable = false;

        // root check can be disabled for debugging in emulator
        if (Constants.DEBUG_DISABLE_ROOT_CHECK) {
            rootAvailable = true;
        } else {
            // check for root on device and call su binary
            try {
                if (RootTools.isAccessGiven()) {
                    rootAvailable = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                rootAvailable = false;
            }
        }

        return rootAvailable;
    }

    /**
     * Show dialog how to root Android
     * 
     * @param activity
     */
    public static void showRootDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(activity.getString(R.string.no_root_title));

        // build view from layout
        LayoutInflater factory = LayoutInflater.from(activity);
        final View dialogView = factory.inflate(R.layout.no_root_dialog, null);
        builder.setView(dialogView);

        builder.setNeutralButton(activity.getResources().getString(R.string.button_exit),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish(); // finish current activity, means exiting app
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Reads html files from /res/raw/example.html to output them as string. See
     * http://www.monocube.com/2011/02/08/android-tutorial-html-file-in-webview/
     * 
     * @param context
     *            current context
     * @param resourceID
     *            of html file to read
     * @return content of html file with formatting
     */
    public static String readContentFromResource(Context context, int resourceID) {
        InputStream raw = context.getResources().openRawResource(resourceID);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int i;
        try {
            i = raw.read();
            while (i != -1) {
                stream.write(i);
                i = raw.read();
            }
            raw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream.toString();
    }

    /**
     * Sets time in Android using "date -s $time" on command line as root
     * 
     * @param offset
     * @return true if it succeeded
     */
    public static int setTime(long offset) {
        if (isAndroidRooted()) {
            // check if binary is available and has right permissions
            if (RootTools.checkUtil(Constants.COMMAND_DATE)) {
                try {
                    // calculate new time based on system time and offset
                    long newTime = System.currentTimeMillis() + offset;

                    // set time using date command
                    RootTools.sendShell(
                            Constants.COMMAND_DATE + " -s " + DATE_FORMAT.format(newTime), -1);
                    Log.d(Constants.TAG, "Date was set successful using 'date -s $time' as root!");

                    // it works, thus return true
                    return NtpSyncService.MESSAGE_OKAY;
                } catch (Exception e) {
                    Log.e(Constants.TAG, "Error while using 'date -s $time' as root!", e);
                    return NtpSyncService.MESSAGE_ERROR;
                }
            } else {
                Log.e(Constants.TAG, "Util 'date' could not be found!");
                return NtpSyncService.MESSAGE_UTIL_NOT_FOUND;
            }
        } else {
            Log.e(Constants.TAG, "Android is not rooted!");
            return NtpSyncService.MESSAGE_NO_ROOT;
        }
    }

}

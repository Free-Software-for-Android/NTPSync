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

import com.stericson.RootTools.RootTools;

public class Constants {

    /*
     * DEBUG enables Log.d outputs, wrapped in org.adaway.util.Log and RootTools Debug Mode
     */
    public static final boolean DEBUG = false;
    public static final boolean DEBUG_DISABLE_ROOT_CHECK = false;

    // set RootTools to debug mode based on AdAway
    static {
        RootTools.debugMode = DEBUG;
    }

    public static final String TAG = "NTPSync";
    public static final String PREFS_NAME = "preferences";
    
    public static final String COMMAND_DATE = "date";

}

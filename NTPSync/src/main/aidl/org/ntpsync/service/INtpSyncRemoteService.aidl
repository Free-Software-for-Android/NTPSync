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

/**
 * Because Exceptions can't be thrown through an IPC call, we use return values.
 * The methods return values from the following list:
 *
 * RETURN_GENERIC_ERROR = 0
 * RETURN_OKAY = 1
 * RETURN_SERVER_TIMEOUT = 2
 * RETURN_NO_ROOT = 3
 */
interface INtpSyncRemoteService {
    /**
     * Gets current time offset from NTP server.
     * If ntpHostname is null the NTP server from NTPSync preferences is used
     *
     * Bundle output contains only one key-value pair:
     * type: Long, Key: offset
     */
    int getOffset(in String ntpHostname, out Bundle output);
    
    /**
     * Sets the time queried from a NTP server as the Android system time.
     * If no ntpHostname is null the NTP server from NTPSyncs config is used
     *
     * Bundle output contains only one key-value pair:
     * type: Long, Key: offset
     */
    int setTime(in String ntpHostname, out Bundle output);
}
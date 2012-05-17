/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ntpsync.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.NumberFormat;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpUtils;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;
import org.ntpsync.R;

import android.content.Context;

/***
 * This is based on the example program demonstrating how to use the NTPUDPClient class. This
 * program sends a Datagram client request packet to a Network time Protocol (NTP) service port on a
 * specified server, retrieves the time, and prints it to standard output along with the fields from
 * the NTP message header (e.g. stratum level, reference id, poll interval, root delay, mode, ...)
 * See <A HREF="ftp://ftp.rfc-editor.org/in-notes/rfc868.txt"> the spec </A> for details.
 * <p>
 * Usage: NTPClient <hostname-or-address-list> <br>
 * Example: NTPClient clock.psu.edu
 * 
 * @author Jason Mathews, MITRE Corp
 ***/
public class NtpSyncUtils {
    private static final NumberFormat numberFormat = new java.text.DecimalFormat("0.00");

    /**
     * Process <code>TimeInfo</code> object and print its details.
     * 
     * @param info
     *            <code>TimeInfo</code> object.
     */
    public static String processResponse(TimeInfo info, Context context) {
        String output = "";

        NtpV3Packet message = info.getMessage();
        int stratum = message.getStratum();
        String refType;
        if (stratum <= 0) {
            refType = context.getString(R.string.detailed_query_unspecified);
        } else if (stratum == 1) {
            refType = context.getString(R.string.detailed_query_primary_reference); // GPS, radio
                                                                                    // clock, etc.
        } else {
            refType = context.getString(R.string.detailed_query_secondary_reference);
        }
        // stratum should be 0..15...
        output += "<p><b>" + context.getString(R.string.detailed_query_server) + "</b><br/>"
                + context.getString(R.string.detailed_query_stratum) + " " + stratum + " "
                + refType;

        int version = message.getVersion();
        int li = message.getLeapIndicator();
        output += "<br/>" + context.getString(R.string.detailed_query_leap) + " " + li + "<br/>"
                + context.getString(R.string.detailed_query_version) + " " + version
                + "<br/>Precision: " + message.getPrecision();

        output += "<br/>" + context.getString(R.string.detailed_query_mode) + " "
                + message.getModeName() + " (" + message.getMode() + ")";
        int poll = message.getPoll();
        // poll value typically btwn MINPOLL (4) and MAXPOLL (14)
        output += "<br/>" + context.getString(R.string.detailed_query_poll) + " "
                + (poll <= 0 ? 1 : (int) Math.pow(2, poll)) + " seconds" + " (2 ** " + poll + ")";
        double disp = message.getRootDispersionInMillisDouble();
        output += "<br/>" + context.getString(R.string.detailed_query_rootdelay) + " "
                + numberFormat.format(message.getRootDelayInMillisDouble()) + "<br/>"
                + context.getString(R.string.detailed_query_rootdispersion) + " "
                + numberFormat.format(disp);

        int refId = message.getReferenceId();
        String refAddr = NtpUtils.getHostAddress(refId);
        String refName = null;
        if (refId != 0) {
            if (refAddr.equals("127.127.1.0")) {
                refName = "LOCAL"; // This is the ref address for the Local Clock
            } else if (stratum >= 2) {
                // If reference id has 127.127 prefix then it uses its own reference clock
                // defined in the form 127.127.clock-type.unit-num (e.g. 127.127.8.0 mode 5
                // for GENERIC DCF77 AM; see refclock.htm from the NTP software distribution.
                if (!refAddr.startsWith("127.127")) {
                    try {
                        InetAddress addr = InetAddress.getByName(refAddr);
                        String name = addr.getHostName();
                        if (name != null && !name.equals(refAddr)) {
                            refName = name;
                        }
                    } catch (UnknownHostException e) {
                        // some stratum-2 servers sync to ref clock device but fudge stratum level
                        // higher... (e.g. 2)
                        // ref not valid host maybe it's a reference clock name?
                        // otherwise just show the ref IP address.
                        refName = NtpUtils.getReferenceClock(message);
                    }
                }
            } else if (version >= 3 && (stratum == 0 || stratum == 1)) {
                refName = NtpUtils.getReferenceClock(message);
                // refname usually have at least 3 characters (e.g. GPS, WWV, LCL, etc.)
            }
            // otherwise give up on naming the beast...
        }
        if (refName != null && refName.length() > 1) {
            refAddr += " (" + refName + ")";
        }
        output += "<p><b>" + context.getString(R.string.detailed_query_reference_identifier)
                + "</b><br/>" + refAddr + "</p>";

        TimeStamp refNtpTime = message.getReferenceTimeStamp();
        output += "<p><b>" + context.getString(R.string.detailed_query_reference_timestamp)
                + "</b><br/>" + refNtpTime.toDateString() + "</p>";

        // Originate Time is time request sent by client (t1)
        TimeStamp origNtpTime = message.getOriginateTimeStamp();
        output += "<p><b>" + context.getString(R.string.detailed_query_originate_timestamp)
                + "</b><br/>" + origNtpTime.toDateString() + "</p>";

        long destTime = info.getReturnTime();
        // Receive Time is time request received by server (t2)
        TimeStamp rcvNtpTime = message.getReceiveTimeStamp();
        output += "<p><b>" + context.getString(R.string.detailed_query_receive_timestamp)
                + "</b><br/>" + rcvNtpTime.toDateString() + "</p>";

        // Transmit time is time reply sent by server (t3)
        TimeStamp xmitNtpTime = message.getTransmitTimeStamp();
        output += "<p><b>" + context.getString(R.string.detailed_query_transmit_timestamp)
                + "</b><br/>" + xmitNtpTime.toDateString() + "</p>";

        // Destination time is time reply received by client (t4)
        TimeStamp destNtpTime = TimeStamp.getNtpTime(destTime);
        output += "<p><b>" + context.getString(R.string.detailed_query_destination_timestamp)
                + "</b><br/>" + destNtpTime.toDateString() + "</p>";

        info.computeDetails(); // compute offset/delay if not already done
        Long offsetValue = info.getOffset();
        Long delayValue = info.getDelay();
        String delay = (delayValue == null) ? "N/A" : delayValue.toString();
        String offset = (offsetValue == null) ? "N/A" : offsetValue.toString();

        // offset in ms
        output += "<p><b>" + context.getString(R.string.detailed_query_computed_offset)
                + "</b><br/>" + context.getString(R.string.detailed_query_roundtrip_delay) + " "
                + delay + "<br/>" + context.getString(R.string.detailed_query_clock_offset) + " "
                + offset + "</p>";

        return output;
    }

    /**
     * Queries NTP server to get details
     * 
     * @param ntpServerHostname
     */
    public static TimeInfo detailedQuery(String ntpServerHostname) throws IOException,
            SocketException {
        NTPUDPClient client = new NTPUDPClient();
        // We want to timeout if a response takes longer than 10 seconds
        client.setDefaultTimeout(10000);

        TimeInfo info = null;
        try {
            client.open();

            InetAddress hostAddr = InetAddress.getByName(ntpServerHostname);
            Log.d(Constants.TAG, "> " + hostAddr.getHostName() + "/" + hostAddr.getHostAddress());
            info = client.getTime(hostAddr);
        } finally {
            client.close();
        }

        return info;
    }

    /**
     * Queries NTP server using UDP to get offset
     * 
     * @param ntpServerHostname
     * @return offset
     * @throws IOException
     *             , SocketException
     */
    public static long query(String ntpServerHostname) throws IOException, SocketException {
        NTPUDPClient client = new NTPUDPClient();
        // We want to timeout if a response takes longer than 10 seconds
        client.setDefaultTimeout(10000);

        TimeInfo info = null;
        try {
            client.open();

            InetAddress hostAddr = InetAddress.getByName(ntpServerHostname);
            Log.d(Constants.TAG, "Trying to get time from " + hostAddr.getHostName() + "/"
                    + hostAddr.getHostAddress());

            info = client.getTime(hostAddr);
        } finally {
            client.close();
        }

        // compute offset/delay if not already done
        info.computeDetails();

        return info.getOffset();
    }
}

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

import android.support.v4.app.FragmentTransaction;
import org.ntpsync.BuildConfig;
import org.ntpsync.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import org.sufficientlysecure.donations.DonationsFragment;

public class HelpActivity extends FragmentActivity {

    /**
     * Google
     */
    private static final String GOOGLE_PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAg8bTVFK5zIg4FGYkHKKQ/j/iGZQlXU0qkAv2BA6epOX1ihbMz78iD4SmViJlECHN8bKMHxouRNd9pkmQKxwEBHg5/xDC/PHmSCXFx/gcY/xa4etA1CSfXjcsS9i94n+j0gGYUg69rNkp+p/09nO9sgfRTAQppTxtgKaXwpfKe1A8oqmDUfOnPzsEAG6ogQL6Svo6ynYLVKIvRPPhXkq+fp6sJ5YVT5Hr356yCXlM++G56Pk8Z+tPzNjjvGSSs/MsYtgFaqhPCsnKhb55xHkc8GJ9haq8k3PSqwMSeJHnGiDq5lzdmsjdmGkWdQq2jIhKlhMZMm5VQWn0T59+xjjIIwIDAQAB";
    private static final String[] GOOGLE_CATALOG = new String[]{"ntpsync.donation.1",
            "ntpsync.donation.2", "ntpsync.donation.3", "ntpsync.donation.5", "ntpsync.donation.8",
            "ntpsync.donation.13"};

    /**
     * PayPal
     */
    private static final String PAYPAL_USER = "dominik@dominikschuermann.de";
    private static final String PAYPAL_CURRENCY_CODE = "EUR";


    /**
     * Flattr
     */
    private static final String FLATTR_PROJECT_URL = "http://code.google.com/p/ntp-sync/";
    // without http:// !
    private static final String FLATTR_URL = "flattr.com/thing/669294/NTPSync";

    /**
     * Instantiate View for this Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.help_activity);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        DonationsFragment donationsFragment;
        if (BuildConfig.DONATIONS_GOOGLE) {
            donationsFragment = DonationsFragment.newInstance(true, GOOGLE_PUBKEY, GOOGLE_CATALOG, false, null, null,
                    null, false, null, null);
        } else {
            donationsFragment = DonationsFragment.newInstance(false, null, null, true, PAYPAL_USER,
                    PAYPAL_CURRENCY_CODE, getString(R.string.pref_paypal_donation_item), true, FLATTR_PROJECT_URL, FLATTR_URL);
        }

        ft.replace(R.id.help_donations_container, donationsFragment);
        ft.commit();
    }

}

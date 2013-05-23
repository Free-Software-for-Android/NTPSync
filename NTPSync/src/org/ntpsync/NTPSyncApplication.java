package org.ntpsync;

import org.ntpsync.util.Constants;
import org.sufficientlysecure.rootcommands.RootCommands;

import android.app.Application;

public class NTPSyncApplication extends Application {

    // set RootCommands to debug mode based on NTPSync
    static {
        RootCommands.DEBUG = Constants.DEBUG;
    }

}

# NTPSync

NTPSync is a simple NTP time synchronization app for Android. 

For more information visit http://code.google.com/p/ntp-sync/


# Build using Gradle

1. Have Android SDK "tools", "platform-tools", and "build-tools" directories in your PATH (http://developer.android.com/sdk/index.html)
2. Export ANDROID_HOME pointing to your Android SDK
3. Install Gradle (Minimum version: 1.6)
4. Execute ``gradle assemble``

## More build information

Two productFlavors are build with gradle. One for Google Play (without Paypal and Flattr Donations) and one for F-Droid (without Google Play Donations).

# Contribute

Fork NTPSync and do a Pull Request. I will merge your changes back into the main project.

# Libraries

All JAR-Libraries are provided in this repository under "app/libs", all Android Library projects are under "libraries".

## Build Apache Commons Net

1. Download Source zip file from http://commons.apache.org/net/download_net.cgi
2. Strip it down to NTP only:
 * remove src/test
 * remove src/main/java/examples
 * in src/main/java/org/apache/commons/net remove every folder except io, util, ntp
3. execute ``mvn package`` to build jar in target directory

# Use NTPSync in your Android application
You want to query NTP servers from your Android app or set the system clock to NTP time?

This can be done very easy using interprocess communication (IPC) in Android with AIDL. NTPSync provides you with an Interface where your application can connect to.

See https://github.com/dschuermann/ntp-sync/tree/master/API-Demo for a complete example.

* You need the following permissions in your Android Manifest, adapted to your needs:
```xml
<uses-permission android:name="org.ntpsync.permission.GET_TIME" />
<uses-permission android:name="org.ntpsync.permission.SET_TIME" />
```

* copy the following file with the correct path to your project: https://github.com/dschuermann/ntp-sync/tree/master/API-Demo/src/main/aidl/org/ntpsync/service/INtpSyncRemoteService.aidl
* Connect to NTPSyncs service like shown in https://github.com/dschuermann/ntp-sync/tree/master/API-Demo/src/main/java/org/ntpsync/apidemo/BaseActivity.java

# Translations

Translations are hosted on Transifex, which is configured by ".tx/config"

1. To pull newest translations install transifex client (e.g. ``apt-get install transifex-client``)
2. Config Transifex client with "~/.transifexrc"
3. Go into root folder of git repo
4. execute ``tx pull`` (``tx pull -a`` to get all languages)

see http://help.transifex.net/features/client/index.html#user-client

# Coding Style

## Code
* Indentation: 4 spaces, no tabs
* Maximum line width for code and comments: 100
* Opening braces don't go on their own line
* Field names: Non-public, non-static fields start with m.
* Acronyms are words: Treat acronyms as words in names, yielding !XmlHttpRequest, getUrl(), etc.

See http://source.android.com/source/code-style.html

## XML
* XML Maximum line width 999
* XML: Split multiple attributes each on a new line (Eclipse: Properties -> XML -> XML Files -> Editor)
* XML: Indent using spaces with Indention size 4 (Eclipse: Properties -> XML -> XML Files -> Editor)

See http://www.androidpolice.com/2009/11/04/auto-formatting-android-xml-files-with-eclipse/

# Licenses
NTPSync is licensed under the GPLv3+.  
The file COPYING includes the full license text.

## Details
NTPSync is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

NTPSync is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with NTPSync.  If not, see <http://www.gnu.org/licenses/>.

## Libraries
* Android Donations Lib  
  https://github.com/dschuermann/android-donations-lib  
  Apache License v2

* RootCommands  
  https://github.com/dschuermann/root-commands  
  Apache License v2

* HTMLCleaner  
  http://htmlcleaner.sourceforge.net/  
  BSD License

* HtmlSpanner  
  Apache License v2

* Apache Commons Net  
  http://commons.apache.org/net/  
  Apache License v2

## Images

* icon.svg  
  Based on Tango Icon Library  
  http://tango.freedesktop.org/  
  Public Domain
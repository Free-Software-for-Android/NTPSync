# NTPSync

I needed NTP in one of my projects, and I found this old NTPSync app which was not maintained. I forked it, and updated it for Android Marshmallow.

Please note that NTP works only on rooted devices. If your device is not rooted, this code WILL NOT work on your device.


# Installing

All you need is to open this app as a new project in Android Studio and open the build.gradle file. If Android Studio is up-to-date and it has build-tool 24.x installed, it'll compile this project and it'll be ready for your use.

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
The file LICENSE includes the full license text.

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

* HtmlTextView  
  https://github.com/dschuermann/html-textview  
  Apache License v2

* Apache Commons Net  
  http://commons.apache.org/net/  
  Apache License v2

## Images

* icon.svg  
  Based on Tango Icon Library  
  http://tango.freedesktop.org/  
  Public Domain

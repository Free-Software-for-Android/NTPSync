

# Contribute

Fork NTPSync and do a merge request. I will merge your changes back into the main project.

# Build using Ant

## Command Line

1. Execute "ant -Dsdk.dir=/opt/android-sdk/ release" in the folder org_ntpsync with the appropriate paths. 

## Local.properties

1. Alternatively you could add a file local.properties in org_ntpsync folder with the following lines, altered to your locations of the SDK:

    sdk.dir=/opt/android-sdk

2. execute "ant release"

# Build using Eclipse

1. New -> Android Project -> Create project from existing source, choose org_ntpsync
2. Optional (As of Android Tools r17 the libraries are automatically added from the libs folder): Add Java libs (Properties of org_ntpsync -> Java Build Path -> Libraries -> add all libraries from libs folder in org_ntpsync)
3. Now NTPSync can be build

# Build Libraries

## Build Apache Commons Net

1. Download Source IP from http://commons.apache.org/net/download_net.cgi

2. Strip it down to NTP only:

  1. remove src/test
  2. remove src/main/java/examples
  3. in src/main/java/org/apache/commons/net remove every folder except io, util, ntp

3. execute "mvn package" to build jar in target directory
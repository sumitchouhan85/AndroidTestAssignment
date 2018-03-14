# AndroidTestAssignment
Assignment 

The goal of this assignment is to create an Android application that will
detect if the device is located inside of a geofence area.
Geofence area is defined as a combination of some geographic point,
radius, and specific Wifi network name. A device is considered to be inside
of the geofence area if the device is connected to the specified WiFi
network or remains geographically inside the defined circle.
Note that if device coordinates are reported outside of the zone, but the
device still connected to the specific Wifi network, then the device is
treated as being inside the geofence area.
Application activity should provide controls to configure the geofence area
and display current status: inside OR outside.
Once you have completed this task, host your source code on GitHub and
a README file in the root with instructions. Keep in mind we prefer some
commits history in the repo vs single commit. Tests are welcome and
encouraged.
Reply back to the email for this assignment with a LINK TO THE GITHUB
REPO.

Dependencies:-
    compile 'com.google.android.gms:play-services-location:11.0.4'
    compile 'com.google.android.gms:play-services-maps:11.0.4'
    compile 'org.greenrobot:eventbus:3.1.1'

1) On initial screen user can see instruction and config controls 
2) Google map with Geofence marker and current location Marker
3) As soon as user tap on Map it points out geofence marker with Hue color, after that click on CREATE GEOFENCE button will create geofence with 500 meter radius
4) CLEAR GEOFENCE button used to clear geofence created previously
5) GreenRobot event bus is added to subscribe geo fence and network change events
6) Android annotation used

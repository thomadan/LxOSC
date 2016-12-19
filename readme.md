## LxOSC
This project is an Android application sending light sensor values as OSC messages over network through the UDP protocol.

### Building and running the application
As LxOSC is not on the Google Play store, running this application is a matter of opening this project in [Android Studio](https://developer.android.com/studio/index.html), and loading the application onto a connected Android device with [developer mode enabled](https://developer.android.com/studio/run/device.html). This application is built to API level 22, meaning a device should be running version 5.1 (Lollipop) or later of the Android operating system.

### Operating the application

One must first set the correct IP address and port number of the device receiving the OSC messages. The method for gathering the device IP address is found varies with operating system.

Due to illuminance levels varying greatly depending on conditions, there is the option of limiting the maximum range of values to be used. In addition, a dual range slider allows for setting both a minimum and maximum value to be used. The value sent in an OSC message is scaled from between the set minimum and maximum value set to a floating-point value between 0 and 1, in order for the received value to be of good use.

The bar in the middle of the screen is a graphical representation of what is sent with an OSC message, and ranges between 0 and 1.

A Max patch is included as an example of how to receive and format OSC messages received from the application.

### About the light sensor
The accuracy and sensitivity of the light sensor varies between devices. Therefore, this application should not be considered an accurate luminance measuring device, but rather used for creative purposes.

### Credits
The application utilizes the [JavaOSC](https://github.com/hoijui/JavaOSC) library originally developed by [illposed](http://www.illposed.com/), allowing for creating and sending OSC messages over UDP.

The dual thumb range seekbar library used here is [anothem](https://github.com/anothem)'s fork of [this](https://code.google.com/archive/p/range-seek-bar/) project, and is found on Github [here](https://github.com/anothem/android-range-seek-bar).





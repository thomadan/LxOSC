## LxOSC
This project is an Android application sending the device light sensor measurements as OSC messages over a network through UDP. The accuracy and sensitivity of the light sensor varies between devices. For this reason, this application should not be considered an accurate luminance measuring device, but is rather meant to be used for creative purposes.
<img src="pic_mountain.jpg" alt="Mountain View" style="width:304px;height:228px;">
<p align="center">
  <img src="http://thomasdahlandersen.net/images/lxosc/mainactivitymedium.png" alt="LxOSC interface"/>
</p>

Running this application is a matter of opening this project in [Android Studio](https://developer.android.com/studio/index.html) and loading the application onto a connected Android device with [developer mode enabled](https://developer.android.com/studio/run/device.html). LxOSC is built to API level 22, meaning the device used should be running version 5.1 (Lollipop) or later of the Android operating system.

In order for the receiving device to receive the OSC messages, one must first set the IP address and port number of the receiving device.

A Max patch is included as an example of how to receive and format OSC messages from the LxOSC application. In the example patch, the *udpreceive* object must be given as argument the correct port number on which to listen for incoming OSC messages.

The OSC message format as sent over UDP is formatted as bytes according to certain rules and is rather unreadable, even in ASCII encoding. However when presented to the JavaOSC library or received from the udpreceive object it looks like this:
```
/LxOSC/0.23
```
The application utilizes the [JavaOSC](https://github.com/hoijui/JavaOSC) library originally developed by [illposed](http://www.illposed.com/), allowing for formatting and sending OSC messages over UDP.

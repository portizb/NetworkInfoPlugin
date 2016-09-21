# Cordova NetworkInformation Plugin
==========================================

This repo contains an example project which illustrates use of the [Network Information Cordova/Phonegap plugin](https://github.com/portizb/networkinformation).

<!-- START table-of-contents -->
**Table of Contents**

- [Overview](#overview)
- [Installation](#installation)
- [Building and running](#building-and-running)
- [More Info](#more-info)

<!-- END table-of-contents -->

## Overview

This Cordova plugin for Android is used to obtain network information such as ip address, network address and network mask.

## Building for Android

This plugin must not depend on libraries only present in API 23+, so you __don't need build using Android SDK Platform v23 or above__. This removes the dependency on API 23 and will allow you to build against legacy API versions (22 and below). You can check the currently installed platform versions with the following command:

    cordova platform ls


## Installation

### Using the Cordova CLI

#### Create the project

    $ cordova create netinfo com.telefonica.movistar.netinfo NetworkInformation
    
#### Install the plugin

    $ cd netinfo
    $ cordova plugin add https://github.com/portizb/networkinformation.git
    
Edit `www/js/index.js` and add the following code inside `onDeviceReady`

```js
    var success = function(message) {
        alert(message);
    }

    var failure = function() {
        alert("Error calling NetworkInformation Plugin");
    }

    netinfo.lookup("172.26.23.22", success, failure);
```

## Building and running

The plugin currently only supports the Android platform.

# Install Android platform

    cordova platform add android

# Build the code

    cordova build android
    
# Run the code

    cordova run android

## More Info

For more information on setting up Cordova see [the documentation](http://cordova.apache.org/docs/en/latest/guide/cli/index.html)

For more info on plugins see the [Plugin Development Guide](http://cordova.apache.org/docs/en/latest/guide/hybrid/plugins/index.html)

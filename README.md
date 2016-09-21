# Cordova NetworkInformation Plugin

Simple plugin that returns your network information.

## Using

Create a new Cordova Project

    $ cordova create netinfo com.telefonica.movistar.netinfo NetworkInformation
    
Install the plugin

    $ cd hello
    $ cordova plugin add https://github.com/portizb/networkinformation.git
    

Edit `www/js/index.js` and add the following code inside `onDeviceReady`

```js
    var success = function(message) {
        alert(message);
    }

    var failure = function() {
        alert("Error calling Hello Plugin");
    }

    netinfo.lookup("172.26.23.22", success, failure);
```

Install Android platform

    cordova platform add android
    
Run the code

    cordova run 

## More Info

For more information on setting up Cordova see [the documentation](http://cordova.apache.org/docs/en/latest/guide/cli/index.html)

For more info on plugins see the [Plugin Development Guide](http://cordova.apache.org/docs/en/latest/guide/hybrid/plugins/index.html)

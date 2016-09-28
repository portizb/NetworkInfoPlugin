package com.movistar.tvsindesco.cordova.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.movistar.tvsindesco.net.NetworkInfo;
import com.movistar.tvsindesco.net.NetworkInfoProvider;

public class NetworkInfoPlugin extends CordovaPlugin {

    private static final String LOG_TAG = "NetworkInfoPlugin";

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("lookup")) {

            try {

                NetworkInfo networkInfo = NetworkInfoProvider.getNetworkInfo();
                String message = (networkInfo == null) ? "Network Information not found" : networkInfo.toString();
                
                callbackContext.success(message);
                return true;

            } catch (Exception e) {
                callbackContext.error ("Fatal error: " + e.toString());
                return false;
            }

        } else {

            callbackContext.error ("Invalid command");
            return false;

        }
    }
}

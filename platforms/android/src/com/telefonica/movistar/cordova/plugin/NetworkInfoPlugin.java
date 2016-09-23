package com.telefonica.movistar.cordova.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.telefonica.movistar.net.NetworkInfo;
import com.telefonica.movistar.net.NetworkInfoProvider;

public class NetworkInfo extends CordovaPlugin {

    private static final String LOG_TAG = "NetworkInfo";

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("lookup")) {

            try {

                NetworkInfo networkInfo = NetworkInfoProvider.getNetworkInfo();

                if (networkInfo != null)
                    message = networkInfo.toString();
                else
                    message = "Network Information not found";

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

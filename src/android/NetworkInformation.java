package com.telefonica.movistar.cordova.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class NetworkInformation extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("lookup")) {

            String name = data.getString(0);
            String message = "Lookup " + name;
            callbackContext.success(message);

            return true;

        } else {

            return false;

        }
    }
}

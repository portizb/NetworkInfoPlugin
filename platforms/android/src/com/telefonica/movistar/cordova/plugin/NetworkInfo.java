package com.telefonica.movistar.cordova.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.telefonica.movistar.net.GatewayResolver;
import com.telefonica.movistar.net.GatewayAddressInfo;

public class NetworkInfo extends CordovaPlugin {

    private static final String LOG_TAG = "NetworkInfo";

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("lookup")) {

            try {

                String ip = data.getString(0);
                String message = "Lookup " + ip;

                GatewayAddressInfo gwAddress = GatewayResolver.lookup("172.26.23.22");

                if (gwAddress != null)
                    message = "Gateway " +  gwAddress.getIpAddress().getHostAddress() + " for ip " + ip;
                else
                    message = "Gateway not found for ip " + ip;

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

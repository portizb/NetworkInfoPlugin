package com.movistar.tvsindesco.cordova.plugin;

import org.apache.cordova.*;

import org.json.JSONArray;
import org.json.JSONObject;
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
                JSONObject json = new JSONObject();

                if (networkInfo) {
                    json.put("ipAddress", networkInfo.getIpAddress().getHostAddress());
                    json.put("gatewayAddress", networkInfo.getGatewayAddress().getHostAddress());
                    json.put("networkAddress", networkInfo.getNetworkAddress().getHostAddress());
                    json.put("subnetMask", networkInfo.getSubnetMask());
                }

                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
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

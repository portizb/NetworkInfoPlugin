package com.movistar.tvsindesco.net;

/**
* Created by pablo on 14/09/16.
*/

public class NetworkInfoProvider {
  private static final String ipAddressLookup = "172.26.23.22";
  
  public static NetworkInfo getNetworkInfo () throws Exception {
    return new NetworkInfo(IpConverter.toInetAddress("192.168.1.38"),
          IpConverter.toInetAddress("192.168.1.1"),
          IpConverter.toInetAddress(subnetAddr), -255);
    }
}

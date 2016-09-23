package com.telefonica.movistar.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by pablo on 15/09/16.
 */
public class IpConverter {

    public static String toIp (long ip) {
        return ((ip >> 24) & 0xFF) + "."
                + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 8) & 0xFF) + "."
                + (ip & 0xFF);
    }

    public static int toInteger(String ipAddress) {
        int result = 0;
        String[] ipAddressInArray = ipAddress.split("\\.");

        for (int i = 3; i >= 0; i--) {
            result |= (Integer.parseInt(ipAddressInArray[3 - i]) << (i * 8));
        }

        return result & 0xFFFFFFFF;
    }

    public static String toBinaryString (Integer number) {
        return Integer.toBinaryString (number.intValue());
    }

    public static InetAddress toInetAddress (String ipAddress) throws UnknownHostException {
        final byte[] byteIpAddr = new byte[4];
        final String[] ipParts = ipAddress.split("\\.");

        for (int i = 0; i < 4; i++) {
            byteIpAddr[i] = (byte) Integer.parseInt(ipParts[i]);
        }

        return InetAddress.getByAddress(byteIpAddr);
    }
}

package com.telefonica.movistar.net;

import com.telefonica.movistar.android.net.netlink.RtNetlinkAddressMessage;
import com.telefonica.movistar.android.net.netlink.RtNetlinkRouteMessage;

/**
 * Created by pablo on 14/09/16.
 */
public class NetworkInfoProvider {

    private static final String ipAddressLookup = "172.26.23.22";

    public static NetworkInfo getNetworkInfo () throws Exception {

        RtNetlinkRouteMessage msgRoute = NetlinkHelper.getRoute(ipAddressLookup);

        if (msgRoute != null) {

            RtNetlinkAddressMessage msgsAddress[] = NetlinkHelper.getAddress();

            for (RtNetlinkAddressMessage msgAddr : msgsAddress)
            {
                if (msgAddr.getIfAddrMsg().ifa_index == msgRoute.getOiface()) {

                    int subnetMask = (0xffffffff << (32 - msgAddr.getIfAddrMsg().ifa_prefix_len));
                    String subnetAddr = IpConverter.toIp(IpConverter.toInteger(msgAddr.getAddress().getHostAddress()) & subnetMask);

                    return new NetworkInfo(msgRoute.getSource(), msgRoute.getGateway(),
                          IpConverter.toInetAddress(subnetAddr), subnetMask);
                }
            }
        }

        return null;
    }
}

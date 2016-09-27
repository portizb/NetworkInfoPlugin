package com.telefonica.movistar.net;

import java.net.InetAddress;

/**
 * Created by pablo on 14/09/16.
 */
public class NetworkInfo {

    private InetAddress mIpAddress;
    private InetAddress mGatewayAddress;
    private InetAddress mNetworkAddress;
    private int mSubnetMask;

    public NetworkInfo(InetAddress ipAddress, InetAddress gatewayAddress, InetAddress networkAddress, int subnetMask) {
        this.mIpAddress = ipAddress;
        this.mGatewayAddress = gatewayAddress;
        this.mNetworkAddress = networkAddress;
        this.mSubnetMask = subnetMask;
    }

    public NetworkInfo(InetAddress ipAddress, InetAddress networkAddress, int subnetMask) {
        this.mIpAddress = ipAddress;
        this.mGatewayAddress = null;
        this.mNetworkAddress = networkAddress;
        this.mSubnetMask = subnetMask;
    }

    public InetAddress getIpAddress () {
        return mIpAddress; 
    }
    
    public InetAddress getGatewayAddress () { 
        return mGatewayAddress; 
    }

    public InetAddress getNetworkAddress () { 
        return mNetworkAddress; 
    }

    public int getSubnetMask () { 
        return mSubnetMask; 
    }

    @Override
    public String toString() {
        final String ipAddress = (mIpAddress == null) ? "" : mIpAddress.getHostAddress();
        final String gatewayAddress = (mGatewayAddress == null) ? "" : mGatewayAddress.getHostAddress();
        final String networkAddress = (mNetworkAddress == null) ? "" : mNetworkAddress.getHostAddress();

        return "NetworkInfo{ "
                + "ipAddress{" + ipAddress + "} "
                + "gatewayAddress{" + gatewayAddress + "} "
                + "networkAddress{" + networkAddress + "}, "
                + "subnetMask{" + mSubnetMask + "} "
                + "}";
    }
}

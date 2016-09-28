package com.movistar.tvsindesco.android.system;

import java.net.SocketAddress;

/**
 * Created by pablo on 5/09/16.
 */
public final class NetlinkSocketAddress extends SocketAddress {
    /** port ID */
    private final int nlPortId;
    /** multicast groups mask */
    private final int nlGroupsMask;

    public NetlinkSocketAddress() {
        this(0, 0);
    }

    public NetlinkSocketAddress(int nlPortId) {
        this(nlPortId, 0);
    }

    public NetlinkSocketAddress(int nlPortId, int nlGroupsMask) {
        this.nlPortId = nlPortId;
        this.nlGroupsMask = nlGroupsMask;
    }

    public int getPortId() {
        return nlPortId;
    }

    public int getGroupsMask() {
        return nlGroupsMask;
    }
}

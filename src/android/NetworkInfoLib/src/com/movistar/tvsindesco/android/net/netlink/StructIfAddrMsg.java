package com.movistar.tvsindesco.android.net.netlink;

import com.movistar.tvsindesco.android.system.OsConstants;

import java.nio.ByteBuffer;

/**
 * Created by pablo on 7/09/16.
 */
public class StructIfAddrMsg {
    // Already aligned.
    public static final int STRUCT_SIZE = 8;

    // IFA Flags
    public static final short IFA_F_SECONDARY = 0x01;
    public static final short IFA_F_NODAD = 0x02;
    public static final short IFA_F_OPTIMISTIC = 0x04;
    public static final short IFA_F_DADFAILED = 0x08;
    public static final short IFA_F_HOMEADDRESS = 0x10;
    public static final short IFA_F_TEMPORARY = IFA_F_SECONDARY;
    public static final short IFA_F_DEPRECATED = 0x20;
    public static final short IFA_F_TENTATIVE = 0x40;
    public static final short IFA_F_PERMANENT = 0x80;

    // Distance to the destination
    public static final short RT_SCOPE_UNIVERSE = 0;
    public static final short RT_SCOPE_SITE = 200;
    public static final short RT_SCOPE_LINK = 253;
    public static final short RT_SCOPE_HOST = 254;
    public static final short RT_SCOPE_NOWHERE = 255;

    public byte ifa_family;         /* Address family of route */
    public byte ifa_prefix_len;  /* Length of prefix */
    public byte ifa_flags;       /* Flags */
    public byte ifa_scope;      /* TOS filter */
    public int ifa_index;    /* Link index */

    public StructIfAddrMsg() {
        ifa_family = (byte) OsConstants.AF_UNSPEC;
        ifa_prefix_len = (byte) 0;
        ifa_flags = (byte) 0;
        ifa_scope = (byte) 0;
        ifa_index = 0;
    }

    public static String stringForRouteScope (short routeProtocol) {
        switch (routeProtocol) {
            case RT_SCOPE_UNIVERSE: return "RT_SCOPE_UNIVERSE";
            case RT_SCOPE_SITE: return "RT_SCOPE_SITE";
            case RT_SCOPE_LINK: return "RT_SCOPE_LINK";
            case RT_SCOPE_HOST: return "RT_SCOPE_HOST";
            case RT_SCOPE_NOWHERE: return "RT_SCOPE_NOWHERE";
            default:
                return "unknown route scope: " + String.valueOf(routeProtocol);
        }
    }

    public static String stringForRouteFlags(int flags) {
        final StringBuilder sb = new StringBuilder();
        if ((flags & IFA_F_SECONDARY) != 0) {
            sb.append("IFA_F_SECONDARY");
        }
        if ((flags & IFA_F_NODAD) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("IFA_F_NODAD");
        }
        if ((flags & IFA_F_OPTIMISTIC) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("IFA_F_OPTIMISTIC");
        }
        if ((flags & IFA_F_DADFAILED) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("IFA_F_DADFAILED");
        }
        if ((flags & IFA_F_HOMEADDRESS) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("IFA_F_HOMEADDRESS");
        }
        if ((flags & IFA_F_TEMPORARY) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("IFA_F_TEMPORARY");
        }
        if ((flags & IFA_F_DEPRECATED) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("IFA_F_DEPRECATED");
        }
        if ((flags & IFA_F_TENTATIVE) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("IFA_F_TENTATIVE");
        }
        if ((flags & IFA_F_PERMANENT) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("IFA_F_PERMANENT");
        }

        return sb.toString();
    }

    private static boolean hasAvailableSpace(ByteBuffer byteBuffer) {
        return byteBuffer != null && byteBuffer.remaining() >= STRUCT_SIZE;
    }

    public static StructIfAddrMsg parse(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) { return null; }

        final StructIfAddrMsg rtmsg = new StructIfAddrMsg();
        rtmsg.ifa_family = byteBuffer.get();
        rtmsg.ifa_prefix_len = byteBuffer.get();
        rtmsg.ifa_flags = byteBuffer.get();
        rtmsg.ifa_scope = byteBuffer.get();
        rtmsg.ifa_index = byteBuffer.getInt();

        return rtmsg;
    }

    public boolean pack(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) { return false; }
// The ByteOrder must have already been set by the caller. In most
// cases ByteOrder.nativeOrder() is correct, with the exception
// of usage within unittests.
        byteBuffer.put(ifa_family);
        byteBuffer.put(ifa_prefix_len);
        byteBuffer.put(ifa_flags);
        byteBuffer.put(ifa_scope);
        byteBuffer.putInt(ifa_index);

        return true;
    }

    @Override
    public String toString() {
        final String flagsStr = "" + ifa_flags
                + "(" + stringForRouteFlags(ifa_flags) + ")";
        final String scopeStr = "" + ifa_scope
                + "(" + stringForRouteScope(ifa_scope) + ")";

        return "StructIfAddrMsg{ "
                + "family{" + NetlinkConstants.stringForAddressFamily((int) ifa_family) + "}, "
                + "ifa_prefix_len{" + ifa_prefix_len + "}, "
                + "ifa_flags{" + flagsStr + "}, "
                + "ifa_scope{" + scopeStr + "}, "
                + "ifa_index{" + ifa_index + "} "
                + "}";
    }
}

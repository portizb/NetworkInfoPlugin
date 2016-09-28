package com.movistar.tvsindesco.android.net.netlink;

import com.movistar.tvsindesco.android.system.OsConstants;

import java.nio.ByteBuffer;

/**
 * Created by pablo on 7/09/16.
 */
public class StructRtMsg {
    // Already aligned.
    public static final int STRUCT_SIZE = 12;

    // Route Type
    public static final short RTN_UNSPEC = 0;
    public static final short RTN_UNICAST = 1;
    public static final short RTN_LOCAL = 2;
    public static final short RTN_BROADCAST = 3;
    public static final short RTN_ANYCAST = 4;
    public static final short RTN_MULTICAST = 5;
    public static final short RTN_BLACKHOLE = 6;
    public static final short RTN_UNREACHABLE = 7;
    public static final short RTN_PROHIBIT = 8;
    public static final short RTN_THROW = 9;
    public static final short RTN_NAT = 10;
    public static final short RTN_XRESOLVE = 11;

    // Routing Origin
    public static final short RTPROT_UNSPEC = 0;
    public static final short RTPROT_REDIRECT = 1;
    public static final short RTPROT_KERNEL = 2;
    public static final short RTPROT_BOOT = 3;
    public static final short RTPROT_STATIC = 4;

    // Routing Table
    public static final short RT_TABLE_UNSPEC = 0;
    public static final short RT_TABLE_COMPAT = 252;
    public static final short RT_TABLE_DEFAULT = 253;
    public static final short RT_TABLE_MAIN = 254;
    public static final short RT_TABLE_LOCAL = 255;

    // Route Flags
    public static final short RTM_F_NOTIFY = 0x100;
    public static final short RTM_F_CLONED = 0x200;
    public static final short RTM_F_EQUALIZE = 0x400;
    public static final short RTM_F_PREFIX = 0x800;

    // Distance to the destination
    public static final short RT_SCOPE_UNIVERSE = 0;
    public static final short RT_SCOPE_SITE = 200;
    public static final short RT_SCOPE_LINK = 253;
    public static final short RT_SCOPE_HOST = 254;
    public static final short RT_SCOPE_NOWHERE = 255;

    public byte rtm_family;   /* Address family of route */
    public byte rtm_dst_len;  /* Length of destination */
    public byte rtm_src_len;  /* Length of source */
    public byte rtm_tos;      /* TOS filter */

    public byte rtm_table;    /* Routing table ID */
    public byte rtm_protocol; /* Routing protocol; see below */
    public byte rtm_scope;    /* See below */
    public byte rtm_type;     /* See below */

    public int rtm_flags;

    public StructRtMsg() {
        rtm_family = (byte) OsConstants.AF_UNSPEC;
        rtm_dst_len = (byte) 0;
        rtm_src_len = (byte) 0;
        rtm_tos = (byte) 0;
        rtm_table = (byte) 0;
        rtm_protocol = (byte) 0;
        rtm_scope = (byte) 0;
        rtm_type = (byte) 0;
        rtm_flags = 0;
    }

    public static String stringForRouteType(short routeType) {
        switch (routeType) {
            case RTN_UNSPEC: return "RTN_UNSPEC";
            case RTN_UNICAST: return "RTN_UNICAST";
            case RTN_LOCAL: return "RTN_LOCAL";
            case RTN_BROADCAST: return "RTN_BROADCAST";
            case RTN_ANYCAST: return "RTN_ANYCAST";
            case RTN_MULTICAST: return "RTN_MULTICAST";
            case RTN_BLACKHOLE: return "RTN_BLACKHOLE";
            case RTN_UNREACHABLE: return "RTN_UNREACHABLE";
            case RTN_PROHIBIT: return "RTN_PROHIBIT";
            case RTN_THROW: return "RTN_THROW";
            case RTN_NAT: return "RTN_NAT";
            case RTN_XRESOLVE: return "RTN_XRESOLVE";
            default:
                return "unknown route type: " + String.valueOf(routeType);
        }
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

    public static String stringForRouteOrigin (short routeOrigin) {
        switch (routeOrigin) {
            case RTPROT_UNSPEC: return "RTPROT_UNSPEC";
            case RTPROT_REDIRECT: return "RTPROT_REDIRECT";
            case RTPROT_KERNEL: return "RTPROT_KERNEL";
            case RTPROT_BOOT: return "RTPROT_BOOT";
            case RTPROT_STATIC: return "RTPROT_STATIC";
            default:
                return "unknown route origin: " + String.valueOf(routeOrigin);
        }
    }

    public static String stringForRouteTable (short routeTable) {
        switch (routeTable) {
            case RT_TABLE_UNSPEC: return "RT_TABLE_UNSPEC";
            case RT_TABLE_COMPAT: return "RT_TABLE_COMPAT";
            case RT_TABLE_DEFAULT: return "RT_TABLE_DEFAULT";
            case RT_TABLE_MAIN: return "RT_TABLE_MAIN";
            case RT_TABLE_LOCAL: return "RT_TABLE_LOCAL";
            default:
                return "unknown route table: " + String.valueOf(routeTable);
        }
    }

    public static String stringForRouteFlags(int flags) {
        final StringBuilder sb = new StringBuilder();
        if ((flags & RTM_F_NOTIFY) != 0) {
            sb.append("RTM_F_NOTIFY");
        }
        if ((flags & RTM_F_CLONED) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("RTM_F_CLONED");
        }
        if ((flags & RTM_F_EQUALIZE) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("RTM_F_EQUALIZE");
        }
        if ((flags & RTM_F_PREFIX) != 0) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("RTM_F_PREFIX");
        }

        return sb.toString();
    }

    private static boolean hasAvailableSpace(ByteBuffer byteBuffer) {
        return byteBuffer != null && byteBuffer.remaining() >= STRUCT_SIZE;
    }

    public static StructRtMsg parse(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) { return null; }
// The ByteOrder must have already been set by the caller. In most
// cases ByteOrder.nativeOrder() is correct, with the possible
// exception of usage within unittests.

        final StructRtMsg rtmsg = new StructRtMsg();
        rtmsg.rtm_family = byteBuffer.get();
        rtmsg.rtm_dst_len = byteBuffer.get();
        rtmsg.rtm_src_len = byteBuffer.get();
        rtmsg.rtm_tos = byteBuffer.get();
        rtmsg.rtm_table = byteBuffer.get();
        rtmsg.rtm_protocol = byteBuffer.get();
        rtmsg.rtm_scope = byteBuffer.get();
        rtmsg.rtm_type = byteBuffer.get();
        rtmsg.rtm_flags = byteBuffer.getInt();

        return rtmsg;
    }

    public boolean pack(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) { return false; }
// The ByteOrder must have already been set by the caller. In most
// cases ByteOrder.nativeOrder() is correct, with the exception
// of usage within unittests.
        byteBuffer.put(rtm_family);
        byteBuffer.put(rtm_dst_len);
        byteBuffer.put(rtm_src_len);
        byteBuffer.put(rtm_tos);
        byteBuffer.put(rtm_table);
        byteBuffer.put(rtm_protocol);
        byteBuffer.put(rtm_scope);
        byteBuffer.put(rtm_type);
        byteBuffer.putInt(rtm_flags);

        return true;
    }

    @Override
    public String toString() {
        final String typeStr = "" + rtm_type
                + "(" + StructRtMsg.stringForRouteType((short)rtm_type) + ")";
        final String tableStr = "" + rtm_table
                + "(" + StructRtMsg.stringForRouteTable((short)rtm_table) + ")";
        final String protocolStr = "" + rtm_protocol
                + "(" + StructRtMsg.stringForRouteOrigin((short)rtm_protocol) + ")";
        final String flagsStr = "" + rtm_flags
                + "(" + stringForRouteFlags(rtm_flags) + ")";
        final String scopeStr = "" + rtm_scope
                + "(" + stringForRouteScope(rtm_scope) + ")";

        return "StructRtMsg{ "
                + "family{" + NetlinkConstants.stringForAddressFamily((int) rtm_family) + "}, "
                + "rtm_dst_len{" + rtm_dst_len + "}, "
                + "rtm_src_len{" + rtm_src_len + "}, "
                + "rtm_tos{" + rtm_tos + "}, "
                + "rtm_table{" + tableStr + "}, "
                + "rtm_protocol{" + protocolStr + "} "
                + "rtm_scope{" + scopeStr + "} "
                + "nlmsg_type{" + typeStr + "}, "
                + "nlmsg_flags{" + flagsStr + ")} "
                + "}";
    }
}

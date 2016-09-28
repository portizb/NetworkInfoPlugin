package com.movistar.tvsindesco.android.net.netlink;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.telefonica.movistar.android.system.OsConstants;

import android.util.Log;
/**
 * Created by pablo on 7/09/16.
 */
public class RtNetlinkRouteMessage extends NetlinkMessage {

    private static final String TAG = "RtNetlinkRouteMessage";

    public static final short RTA_UNSPEC = 0;
    public static final short RTA_DST = 1;
    public static final short RTA_SRC = 2;
    public static final short RTA_IIF = 3;
    public static final short RTA_OIF = 4;
    public static final short RTA_GATEWAY = 5;
    public static final short RTA_PRIORITY = 6;
    public static final short RTA_PREFSRC = 7;
    public static final short RTA_METRICS = 8;
    public static final short RTA_MULTIPATH = 9;
    public static final short RTA_PROTOINFO = 10;
    public static final short RTA_FLOW = 11;
    public static final short RTA_CACHEINFO = 12;
    public static final short RTA_SESSION = 13;
    public static final short RTA_MP_ALGO = 14;
    public static final short RTA_TABLE = 15;
    public static final short RTA_MARK = 16;
    public static final short RTA_MFC_STATS = 17;

    private StructRtMsg mRtmsg;
    private InetAddress mDestination;
    private InetAddress mSource;
    private InetAddress mGateway;
    private int mOiface;

    private RtNetlinkRouteMessage(StructNlMsgHdr header) {
        super(header);
        mRtmsg = null;
        mDestination = null;
        mSource = null;
        mGateway = null;
        mOiface = 0;
    }

    private static StructNlAttr findNextAttrOfType(short attrType, ByteBuffer byteBuffer) {
        while (byteBuffer != null && byteBuffer.remaining() > 0) {

            final StructNlAttr nlAttr = StructNlAttr.peek(byteBuffer);

            if (nlAttr == null) {
                break;
            }

            if (nlAttr.nla_type == attrType) {
                return StructNlAttr.parse(byteBuffer);
            }

            if (byteBuffer.remaining() < nlAttr.getAlignedLength()) {
                break;
            }

            byteBuffer.position(byteBuffer.position() + nlAttr.getAlignedLength());
        }

        return null;
    }

    public static RtNetlinkRouteMessage parse(StructNlMsgHdr header, ByteBuffer byteBuffer) {
        final RtNetlinkRouteMessage rtMsg = new RtNetlinkRouteMessage(header);
        rtMsg.mRtmsg = StructRtMsg.parse(byteBuffer);

        if (rtMsg.mRtmsg == null) {
            return null;
        }

        final int baseOffset = byteBuffer.position();
        StructNlAttr nlAttr = findNextAttrOfType(RTA_DST, byteBuffer);

        if (nlAttr != null) {
            rtMsg.mDestination = nlAttr.getValueAsInetAddress();
        }

        byteBuffer.position(baseOffset);
        nlAttr = findNextAttrOfType(RTA_PREFSRC, byteBuffer);

        if (nlAttr != null) {
            rtMsg.mSource = nlAttr.getValueAsInetAddress();
        }

        byteBuffer.position(baseOffset);
        nlAttr = findNextAttrOfType(RTA_GATEWAY, byteBuffer);

        if (nlAttr != null) {
            rtMsg.mGateway = nlAttr.getValueAsInetAddress();
        }

        byteBuffer.position(baseOffset);
        nlAttr = findNextAttrOfType(RTA_OIF, byteBuffer);

        if (nlAttr != null) {
            rtMsg.mOiface = nlAttr.getValueAsInt(0);
        }

        byteBuffer.position(baseOffset);

        final int kMinConsumed = StructNlMsgHdr.STRUCT_SIZE + StructRtMsg.STRUCT_SIZE;
        final int kAdditionalSpace = NetlinkConstants.alignedLengthOf(
                rtMsg.mHeader.nlmsg_len - kMinConsumed);

        if (byteBuffer.remaining() < kAdditionalSpace) {
            byteBuffer.position(byteBuffer.limit());
        } else {
            byteBuffer.position(baseOffset + kAdditionalSpace);
        }

        return rtMsg;
    }

    /**
     * A convenience method to create an RTM_GETROUTE request message.
     */
    public static byte[] newGetRouteRequest(int seqNo, String destination) {
        final int length = StructNlMsgHdr.STRUCT_SIZE + StructRtMsg.STRUCT_SIZE + StructNlAttr.NLA_HEADERLEN + 16 + 4;
        final byte[] bytes = new byte[length];

        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.nativeOrder());

        final StructNlMsgHdr nlmsghdr = new StructNlMsgHdr();
        nlmsghdr.nlmsg_len = length;
        nlmsghdr.nlmsg_type = NetlinkConstants.RTM_GETROUTE;
        nlmsghdr.nlmsg_flags = StructNlMsgHdr.NLM_F_REQUEST | StructNlMsgHdr.NLM_F_ACK;
        nlmsghdr.nlmsg_seq = seqNo;
        nlmsghdr.nlmsg_pid = android.os.Process.myPid();
        nlmsghdr.pack(byteBuffer);

        final StructRtMsg rtmsg = new StructRtMsg();
        rtmsg.rtm_family = OsConstants.AF_INET;
        rtmsg.rtm_dst_len = 32;
        rtmsg.rtm_table = (byte)StructRtMsg.RT_TABLE_MAIN;
        rtmsg.pack(byteBuffer);

        final StructNlAttr nlAttr = new StructNlAttr();
        nlAttr.nla_type = RTA_DST;
        nlAttr.nla_len = StructNlAttr.NLA_HEADERLEN + 16;
        nlAttr.nla_value = destination.getBytes();
        nlAttr.pack(byteBuffer);

        return bytes;
    }

    public InetAddress getSource() {
        return mSource;
    }

    public InetAddress getDestination() {
        return mDestination;
    }

    public InetAddress getGateway() {
        return mGateway;
    }

    public int getOiface() {
        return mOiface;
    }

    @Override
    public String toString() {
        final String ipSource = (mSource == null) ? "" : mSource.getHostAddress();
        final String ipDestination = (mDestination == null) ? "" : mDestination.getHostAddress();
        final String ipGateway = (mGateway == null) ? "" : mGateway.getHostAddress();
        return "RtNetlinkRouteMessage{ "
                + "nlmsghdr{" + (mHeader == null ? "" : mHeader.toString()) + "}, "
                + "mRtmsg{" + (mRtmsg == null ? "" : mRtmsg.toString()) + "}, "
                + "destination{" + ipDestination + "} "
                + "gateway{" + ipGateway + "}, "
                + "iif{" + mOiface + "}, "
                + "source{" + ipSource + "} "
                + "}";
    }
}

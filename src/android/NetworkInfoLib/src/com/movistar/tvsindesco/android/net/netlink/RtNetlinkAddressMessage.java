package com.movistar.tvsindesco.android.net.netlink;

import android.util.Log;

import com.telefonica.movistar.android.system.OsConstants;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by pablo on 7/09/16.
 */
public class RtNetlinkAddressMessage extends NetlinkMessage {

    private static final String TAG = "RtNetlinkAddressMessage";

    public static final short IFA_UNSPEC = 0;
    public static final short IFA_ADDRESS = 1;
    public static final short IFA_LOCAL = 2;
    public static final short IFA_LABEL = 3;
    public static final short IFA_BROADCAST = 4;
    public static final short IFA_ANYCAST = 5;
    public static final short IFA_CACHEINFO = 6;

    private StructIfAddrMsg mIfAddrmsg;
    private InetAddress mAddress;
    private InetAddress mLocal;
    private String mLabel;
    private InetAddress mBroadcast;
    private InetAddress mAnycast;
    private StructIfaCacheInfo mCacheInfo;

    private RtNetlinkAddressMessage(StructNlMsgHdr header) {
        super(header);
        mIfAddrmsg = null;
        mAddress = null;
        mLocal = null;
        mLabel = null;
        mBroadcast = null;
        mAnycast = null;
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

    public static RtNetlinkAddressMessage parse(StructNlMsgHdr header, ByteBuffer byteBuffer) {
        final RtNetlinkAddressMessage rtMsg = new RtNetlinkAddressMessage(header);
        rtMsg.mIfAddrmsg = StructIfAddrMsg.parse(byteBuffer);

        if (rtMsg.mIfAddrmsg == null) {
            return null;
        }

        final int baseOffset = byteBuffer.position();
        StructNlAttr nlAttr = findNextAttrOfType(IFA_ADDRESS, byteBuffer);

        if (nlAttr != null) {
            rtMsg.mAddress = nlAttr.getValueAsInetAddress();
        }

        byteBuffer.position(baseOffset);
        nlAttr = findNextAttrOfType(IFA_LOCAL, byteBuffer);

        if (nlAttr != null) {
            rtMsg.mLocal = nlAttr.getValueAsInetAddress();
        }

        byteBuffer.position(baseOffset);
        nlAttr = findNextAttrOfType(IFA_LABEL, byteBuffer);

        if (nlAttr != null) {
            rtMsg.mLabel = new String(nlAttr.getValueAsByteBuffer().array());
        }

        byteBuffer.position(baseOffset);
        nlAttr = findNextAttrOfType(IFA_BROADCAST, byteBuffer);

        if (nlAttr != null) {
            rtMsg.mBroadcast =  nlAttr.getValueAsInetAddress();
        }

        byteBuffer.position(baseOffset);
        nlAttr = findNextAttrOfType(IFA_ANYCAST, byteBuffer);

        if (nlAttr != null) {
            rtMsg.mAnycast =  nlAttr.getValueAsInetAddress();
        }

        byteBuffer.position(baseOffset);
        nlAttr = findNextAttrOfType(IFA_CACHEINFO, byteBuffer);

        if (nlAttr != null) {
            rtMsg.mCacheInfo =  StructIfaCacheInfo.parse(nlAttr.getValueAsByteBuffer());
        }

        byteBuffer.position(baseOffset);

        final int kMinConsumed = StructNlMsgHdr.STRUCT_SIZE + StructIfAddrMsg.STRUCT_SIZE;
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
    public static byte[] newGetAddressRequest(int seqNo) {
        final int length = StructNlMsgHdr.STRUCT_SIZE  + StructIfAddrMsg.STRUCT_SIZE;
        final byte[] bytes = new byte[length];

        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.nativeOrder());

        final StructNlMsgHdr nlmsghdr = new StructNlMsgHdr();
        nlmsghdr.nlmsg_len = length;
        nlmsghdr.nlmsg_type = NetlinkConstants.RTM_GETADDR;
        nlmsghdr.nlmsg_flags = StructNlMsgHdr.NLM_F_REQUEST | StructNlMsgHdr.NLM_F_ROOT;
        nlmsghdr.nlmsg_seq = seqNo;
        nlmsghdr.nlmsg_pid = android.os.Process.myPid();
        nlmsghdr.pack(byteBuffer);

        final StructIfAddrMsg ifAddrmsg = new StructIfAddrMsg();

        ifAddrmsg.ifa_family = OsConstants.AF_INET;
        ifAddrmsg.pack(byteBuffer);

        return bytes;
    }

    public StructIfAddrMsg getIfAddrMsg() { return mIfAddrmsg; }

    public InetAddress getAddress() {
        return mAddress;
    }

    public InetAddress getLocal() {
        return mLocal;
    }

    public String getLabel() {
        return mLabel;
    }

    public InetAddress getBroadcast() {
        return mBroadcast;
    }

    public InetAddress getAnycast() {
        return mAnycast;
    }

    public StructIfaCacheInfo getCacheInfo() {
        return mCacheInfo;
    }

    @Override
    public String toString() {
        final String ipAddress = (mAddress == null) ? "" : mAddress.getHostAddress();
        final String ipLocal = (mLocal == null) ? "" : mLocal.getHostAddress();
        final String ipBroadcast = (mBroadcast == null) ? "" : mBroadcast.getHostAddress();
        final String ipAnycast = (mAnycast == null) ? "" : mAnycast.getHostAddress();
        return "RtNetlinkAddressMessage{ "
                + "nlmsghdr{" + (mHeader == null ? "" : mHeader.toString()) + "}, "
                + "mIfAddrmsg{" + (mIfAddrmsg == null ? "" : mIfAddrmsg.toString()) + "}, "
                + "address{" + ipAddress + "}, "
                + "local{" + ipLocal + "}, "
                + "label{" + mLabel + "}, "
                + "broadcast{" + ipBroadcast + "}, "
                + "anycast{" + ipAnycast + "}, "
                + "cacheinfo{" + (mCacheInfo == null ? "" : mCacheInfo.toString()) + "} "
                + "}";
    }
}

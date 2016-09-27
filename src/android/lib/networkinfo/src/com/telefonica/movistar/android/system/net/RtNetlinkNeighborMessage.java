package com.telefonica.movistar.android.net.netlink;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by pablo on 2/09/16.
 */
public class RtNetlinkNeighborMessage extends NetlinkMessage {
    public static final short NDA_UNSPEC = 0;
    public static final short NDA_DST = 1;
    public static final short NDA_LLADDR = 2;
    public static final short NDA_CACHEINFO = 3;
    public static final short NDA_PROBES = 4;
    public static final short NDA_VLAN = 5;
    public static final short NDA_PORT = 6;
    public static final short NDA_VNI = 7;
    public static final short NDA_IFINDEX = 8;
    public static final short NDA_MASTER = 9;

    private StructNdMsg mNdmsg;
    private InetAddress mDestination;
    private byte[] mLinkLayerAddr;
    private int mNumProbes;
    private StructNdaCacheInfo mCacheInfo;

    private RtNetlinkNeighborMessage(StructNlMsgHdr header) {
        super(header);
        mNdmsg = null;
        mDestination = null;
        mLinkLayerAddr = null;
        mNumProbes = 0;
        mCacheInfo = null;
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

    public static RtNetlinkNeighborMessage parse(StructNlMsgHdr header, ByteBuffer byteBuffer) {
        final RtNetlinkNeighborMessage neighMsg = new RtNetlinkNeighborMessage(header);
        neighMsg.mNdmsg = StructNdMsg.parse(byteBuffer);

        if (neighMsg.mNdmsg == null) {
            return null;
        }
// Some of these are message-type dependent, and not always present.
        final int baseOffset = byteBuffer.position();
        StructNlAttr nlAttr = findNextAttrOfType(NDA_DST, byteBuffer);

        if (nlAttr != null) {
            neighMsg.mDestination = nlAttr.getValueAsInetAddress();
        }

        byteBuffer.position(baseOffset);
        nlAttr = findNextAttrOfType(NDA_LLADDR, byteBuffer);

        if (nlAttr != null) {
            neighMsg.mLinkLayerAddr = nlAttr.nla_value;
        }

        byteBuffer.position(baseOffset);
        nlAttr = findNextAttrOfType(NDA_PROBES, byteBuffer);

        if (nlAttr != null) {
            neighMsg.mNumProbes = nlAttr.getValueAsInt(0);
        }

        byteBuffer.position(baseOffset);
        nlAttr = findNextAttrOfType(NDA_CACHEINFO, byteBuffer);

        if (nlAttr != null) {
            neighMsg.mCacheInfo = StructNdaCacheInfo.parse(nlAttr.getValueAsByteBuffer());
        }

        byteBuffer.position(baseOffset);

        final int kMinConsumed = StructNlMsgHdr.STRUCT_SIZE + StructNdMsg.STRUCT_SIZE;
        final int kAdditionalSpace = NetlinkConstants.alignedLengthOf(
                neighMsg.mHeader.nlmsg_len - kMinConsumed);

        if (byteBuffer.remaining() < kAdditionalSpace) {
            byteBuffer.position(byteBuffer.limit());
        } else {
            byteBuffer.position(baseOffset + kAdditionalSpace);
        }

        return neighMsg;
    }

    /**
     * A convenience method to create an RTM_GETNEIGH request message.
     */
    public static byte[] newGetNeighborsRequest(int seqNo) {
        final int length = StructNlMsgHdr.STRUCT_SIZE + StructNdMsg.STRUCT_SIZE;
        final byte[] bytes = new byte[length];

        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.nativeOrder());

        final StructNlMsgHdr nlmsghdr = new StructNlMsgHdr();
        nlmsghdr.nlmsg_len = length;
        nlmsghdr.nlmsg_type = NetlinkConstants.RTM_GETNEIGH;
        nlmsghdr.nlmsg_flags = StructNlMsgHdr.NLM_F_REQUEST|StructNlMsgHdr.NLM_F_DUMP;
        nlmsghdr.nlmsg_seq = seqNo;
        nlmsghdr.nlmsg_pid = android.os.Process.myPid();
        nlmsghdr.pack(byteBuffer);

        final StructNdMsg ndmsg = new StructNdMsg();
        ndmsg.pack(byteBuffer);

        return bytes;
    }

    public StructNdMsg getNdHeader() {
        return mNdmsg;
    }

    public InetAddress getDestination() {
        return mDestination;
    }

    public byte[] getLinkLayerAddress() {
        return mLinkLayerAddr;
    }

    public int getProbes() {
        return mNumProbes;
    }

    public StructNdaCacheInfo getCacheInfo() {
        return mCacheInfo;
    }

    @Override
    public String toString() {
        final String ipLiteral = (mDestination == null) ? "" : mDestination.getHostAddress();
        return "RtNetlinkNeighborMessage{ "
                + "nlmsghdr{" + (mHeader == null ? "" : mHeader.toString()) + "}, "
                + "ndmsg{" + (mNdmsg == null ? "" : mNdmsg.toString()) + "}, "
                + "destination{" + ipLiteral + "} "
                + "linklayeraddr{" + NetlinkConstants.hexify(mLinkLayerAddr) + "} "
                + "probes{" + mNumProbes + "} "
                + "cacheinfo{" + (mCacheInfo == null ? "" : mCacheInfo.toString()) + "} "
                + "}";
    }
}

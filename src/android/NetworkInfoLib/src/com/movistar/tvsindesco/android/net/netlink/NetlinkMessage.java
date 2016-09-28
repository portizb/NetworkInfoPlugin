package com.telefonica.movistar.android.net.netlink;

/**
 * Created by pablo on 2/09/16.
 */

import java.nio.ByteBuffer;
/**
 * NetlinkMessage base class for other, more specific netlink message types.
 *
 * Classes that extend NetlinkMessage should:
 * - implement a public static parse(StructNlMsgHdr, ByteBuffer) method
 * - returning either null (parse errors) or a new object of the subclass
 * type (cast-able to NetlinkMessage)
 *
 * NetlinkMessage.parse() should be updated to know which nlmsg_type values
 * correspond with which message subclasses.
 *
 * @hide
 */
public class NetlinkMessage {
    private final static String TAG = "NetlinkMessage";

    protected StructNlMsgHdr mHeader;

    public NetlinkMessage(StructNlMsgHdr nlmsghdr) {
        mHeader = nlmsghdr;
    }

    public static NetlinkMessage parse(ByteBuffer byteBuffer) {
        final int startPosition = (byteBuffer != null) ? byteBuffer.position() : -1;
        final StructNlMsgHdr nlmsghdr = StructNlMsgHdr.parse(byteBuffer);

        if (nlmsghdr == null) {
            return null;
        }

        int payloadLength = NetlinkConstants.alignedLengthOf(nlmsghdr.nlmsg_len);
        payloadLength -= StructNlMsgHdr.STRUCT_SIZE;

        if (payloadLength < 0 || payloadLength > byteBuffer.remaining()) {
// Malformed message or runt buffer. Pretend the buffer was consumed.
            byteBuffer.position(byteBuffer.limit());
            return null;
        }

        switch (nlmsghdr.nlmsg_type) {
//case NetlinkConstants.NLMSG_NOOP:
            case NetlinkConstants.NLMSG_ERROR:
                return (NetlinkMessage) NetlinkErrorMessage.parse(nlmsghdr, byteBuffer);

            case NetlinkConstants.NLMSG_DONE:
                byteBuffer.position(byteBuffer.position() + payloadLength);
                return new NetlinkMessage(nlmsghdr);
//case NetlinkConstants.NLMSG_OVERRUN:

            case NetlinkConstants.RTM_NEWNEIGH:
            case NetlinkConstants.RTM_DELNEIGH:
            case NetlinkConstants.RTM_GETNEIGH:
                return (NetlinkMessage) RtNetlinkNeighborMessage.parse(nlmsghdr, byteBuffer);

            case NetlinkConstants.RTM_NEWROUTE:
            case NetlinkConstants.RTM_DELROUTE:
            case NetlinkConstants.RTM_GETROUTE:
                return (NetlinkMessage) RtNetlinkRouteMessage.parse(nlmsghdr, byteBuffer);

            case NetlinkConstants.RTM_NEWADDR:
            case NetlinkConstants.RTM_DELADDR:
            case NetlinkConstants.RTM_GETADDR:
                return (NetlinkMessage) RtNetlinkAddressMessage.parse(nlmsghdr, byteBuffer);

            default:
                if (nlmsghdr.nlmsg_type <= NetlinkConstants.NLMSG_MAX_RESERVED) {
// Netlink control message. Just parse the header for now,
// pretending the whole message was consumed.
                    byteBuffer.position(byteBuffer.position() + payloadLength);
                    return new NetlinkMessage(nlmsghdr);
                }
                return null;
        }
    }
    
    public StructNlMsgHdr getHeader() {
        return mHeader;
    }

    @Override
    public String toString() {
        return "NetlinkMessage{" + (mHeader == null ? "" : mHeader.toString()) + "}";
    }
}
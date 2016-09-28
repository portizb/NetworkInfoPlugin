package com.telefonica.movistar.android.net.netlink;

/**
 * Created by pablo on 2/09/16.
 */
import java.nio.ByteBuffer;
/**
 * A NetlinkMessage subclass for netlink error messages.
 *
 * @hide
 */
public class NetlinkErrorMessage extends NetlinkMessage {

    private StructNlMsgErr mNlMsgErr;

    NetlinkErrorMessage(StructNlMsgHdr header) {
        super(header);
        mNlMsgErr = null;
    }

    public static NetlinkErrorMessage parse(StructNlMsgHdr header, ByteBuffer byteBuffer) {
        final NetlinkErrorMessage errorMsg = new NetlinkErrorMessage(header);
        errorMsg.mNlMsgErr = StructNlMsgErr.parse(byteBuffer);

        if (errorMsg.mNlMsgErr == null) {
            return null;
        }

        return errorMsg;
    }

    public StructNlMsgErr getNlMsgError() {
        return mNlMsgErr;
    }

    @Override
    public String toString() {
        return "NetlinkErrorMessage{ "
                + "nlmsghdr{" + (mHeader == null ? "" : mHeader.toString()) + "}, "
                + "nlmsgerr{" + (mNlMsgErr == null ? "" : mNlMsgErr.toString()) + "} "
                + "}";
    }
}
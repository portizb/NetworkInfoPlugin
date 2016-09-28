package com.movistar.tvsindesco.android.net.netlink;

import com.movistar.tvsindesco.android.system.OsConstants;

import java.nio.ByteBuffer;

/**
 * Created by pablo on 8/09/16.
 *
 * General form of address family dependent message.
 */

public class StructRtGenMsg {
    // Already aligned.
    public static final int STRUCT_SIZE = 8;

    public byte rtgen_family;   /* Address family of route */

    public StructRtGenMsg() {
        rtgen_family = (byte) OsConstants.AF_UNSPEC;
    }

    private static boolean hasAvailableSpace(ByteBuffer byteBuffer) {
        return byteBuffer != null && byteBuffer.remaining() >= STRUCT_SIZE;
    }

    public static StructRtGenMsg parse(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) { return null; }
// The ByteOrder must have already been set by the caller. In most
// cases ByteOrder.nativeOrder() is correct, with the possible
// exception of usage within unittests.

        final StructRtGenMsg rtGenMsg = new StructRtGenMsg();
        rtGenMsg.rtgen_family = byteBuffer.get();

        return rtGenMsg;
    }

    public boolean pack(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) { return false; }
// The ByteOrder must have already been set by the caller. In most
// cases ByteOrder.nativeOrder() is correct, with the exception
// of usage within unittests.
        byteBuffer.put(rtgen_family);

        return true;
    }

    @Override
    public String toString() {

        return "StructRtGenMsg{ "
                + "family{" + NetlinkConstants.stringForAddressFamily((int) rtgen_family) + "} "
                + "}";
    }
}

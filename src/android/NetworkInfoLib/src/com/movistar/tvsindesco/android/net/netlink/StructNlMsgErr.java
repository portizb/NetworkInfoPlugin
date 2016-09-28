package com.movistar.tvsindesco.android.net.netlink;

/**
 * Created by pablo on 2/09/16.
 */

import com.movistar.tvsindesco.android.io.SizeOf;
import java.nio.ByteBuffer;

public class StructNlMsgErr {
    public static final int STRUCT_SIZE = SizeOf.INT + StructNlMsgHdr.STRUCT_SIZE;

    public int error;
    public StructNlMsgHdr msg;

    public StructNlMsgErr() {
        error = 0;
        msg = null;
    }

    public static boolean hasAvailableSpace(ByteBuffer byteBuffer) {
        return byteBuffer != null && byteBuffer.remaining() >= STRUCT_SIZE;
    }

    public static StructNlMsgErr parse(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) { return null; }
// The ByteOrder must have already been set by the caller. In most
// cases ByteOrder.nativeOrder() is correct, with the exception
// of usage within unittests.
        final StructNlMsgErr struct = new StructNlMsgErr();
        struct.error = byteBuffer.getInt();
        struct.msg = StructNlMsgHdr.parse(byteBuffer);

        return struct;
    }

    public boolean pack(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) { return false; }
// The ByteOrder must have already been set by the caller. In most
// cases ByteOrder.nativeOrder() is correct, with the possible
// exception of usage within unittests.
        byteBuffer.putInt(error);

        if (msg != null) {
            msg.pack(byteBuffer);
        }

        return true;
    }

    @Override
    public String toString() {
        return "StructNlMsgErr{ "
                + "error{" + error + "}, "
                + "msg{" + (msg == null ? "" : msg.toString()) + "} "
                + "}";
    }
}

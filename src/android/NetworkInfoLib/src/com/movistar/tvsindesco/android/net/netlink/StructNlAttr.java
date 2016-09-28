package com.movistar.tvsindesco.android.net.netlink;

/**
 * Created by pablo on 2/09/16.
 */

import com.telefonica.movistar.android.io.SizeOf;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.nio.ByteBuffer;

import android.util.Log;

/**
 * struct nlattr
 *
 * see: &lt;linux_src&gt;/include/uapi/linux/netlink.h
 *
 * @hide
 */
public class StructNlAttr {
    private static final String TAG = "StructNlAttr";
    // Already aligned.
    public static final int NLA_HEADERLEN = 4;

    public short nla_len;
    public short nla_type;
    public byte[] nla_value;
    public ByteOrder mByteOrder;

    public StructNlAttr() {
        mByteOrder = ByteOrder.nativeOrder();
    }

    private static boolean hasAvailableSpace(ByteBuffer byteBuffer) {
        return byteBuffer != null && byteBuffer.remaining() >= NLA_HEADERLEN;
    }

    // Return a (length, type) object only, without consuming any bytes in
// |byteBuffer| and without copying or interpreting any value bytes.
// This is used for scanning over a packed set of struct nlattr's,
// looking for instances of a particular type.
    public static StructNlAttr peek(ByteBuffer byteBuffer) {
        if (byteBuffer == null || byteBuffer.remaining() < NLA_HEADERLEN) {
            return null;
        }

        final int baseOffset = byteBuffer.position();
        final StructNlAttr struct = new StructNlAttr();
        struct.nla_len = byteBuffer.getShort();
        struct.nla_type = byteBuffer.getShort();
        struct.mByteOrder = byteBuffer.order();

        byteBuffer.position(baseOffset);

        if (struct.nla_len < NLA_HEADERLEN) {
// Malformed.
            Log.v(TAG, "StructNlAttr malformed: type=[" + struct.nla_type + "], len=[" + struct.nla_len + "]");
            return null;
        }

        return struct;
    }

    public static StructNlAttr parse(ByteBuffer byteBuffer) {
        final StructNlAttr struct = peek(byteBuffer);

        if (struct == null || byteBuffer.remaining() < struct.getAlignedLength()) {
            return null;
        }

        final int baseOffset = byteBuffer.position();
        byteBuffer.position(baseOffset + NLA_HEADERLEN);

        int valueLen = ((int) struct.nla_len) & 0xffff;
        valueLen -= NLA_HEADERLEN;

        if (valueLen > 0) {
            struct.nla_value = new byte[valueLen];
            byteBuffer.get(struct.nla_value, 0, valueLen);
            byteBuffer.position(baseOffset + struct.getAlignedLength());
        }

        return struct;
    }

    public boolean pack(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) { return false; }

        final int originalPosition = byteBuffer.position();
        byteBuffer.putShort(nla_len);
        byteBuffer.putShort(nla_type);
        byteBuffer.put(nla_value);

        byteBuffer.position(originalPosition + getAlignedLength());

        return true;
    }

    public int getAlignedLength() {
        return NetlinkConstants.alignedLengthOf(nla_len);
    }

    public ByteBuffer getValueAsByteBuffer() {
        if (nla_value == null) { return null; }

        final ByteBuffer byteBuffer = ByteBuffer.wrap(nla_value);
        byteBuffer.order(mByteOrder);

        return byteBuffer;
    }

    public int getValueAsInt(int defaultValue) {
        final ByteBuffer byteBuffer = getValueAsByteBuffer();

        if (byteBuffer == null || byteBuffer.remaining() != SizeOf.INT) {
            return defaultValue;
        }

        return getValueAsByteBuffer().getInt();
    }

    public InetAddress getValueAsInetAddress() {
        if (nla_value == null) {
            return null;
        }

        try {
            return InetAddress.getByAddress(nla_value);
        } catch (UnknownHostException ignored) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "StructNlAttr{ "
                + "nla_len{" + nla_len + "}, "
                + "nla_type{" + nla_type + "}, "
                + "nla_value{" + NetlinkConstants.hexify(nla_value) + "}, "
                + "}";
    }
}

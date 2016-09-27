package com.telefonica.movistar.android.net.netlink;

/**
 * Created by pablo on 2/09/16.
 */

import android.util.Log;

import java.nio.ByteBuffer;

public class StructNlMsgHdr {
    private static final String TAG = "StructNlMsgHdr";
    // Already aligned.
    public static final int STRUCT_SIZE = 16;
    public static final short NLM_F_REQUEST = 0x0001;
    public static final short NLM_F_MULTI = 0x0002;
    public static final short NLM_F_ACK = 0x0004;
    public static final short NLM_F_ECHO = 0x0008;

    // Flags for a GET request.
    public static final short NLM_F_ROOT = 0x0100;
    public static final short NLM_F_MATCH = 0x0200;
    public static final short NLM_F_DUMP = NLM_F_ROOT|NLM_F_MATCH;

    public int nlmsg_len;
    public short nlmsg_type;
    public short nlmsg_flags;
    public int nlmsg_seq;
    public int nlmsg_pid;

    public StructNlMsgHdr() {
        nlmsg_len = 0;
        nlmsg_type = 0;
        nlmsg_flags = 0;
        nlmsg_seq = 0;
        nlmsg_pid = 0;
    }

    public static String stringForNlMsgFlags(short flags) {
        final StringBuilder sb = new StringBuilder();
        if ((flags & NLM_F_REQUEST) != 0) {
            sb.append("NLM_F_REQUEST");
        }
        if ((flags & NLM_F_MULTI) != 0) {
            if (sb.length() > 0) { sb.append("|"); }
            sb.append("NLM_F_MULTI");
        }
        if ((flags & NLM_F_ACK) != 0) {
            if (sb.length() > 0) { sb.append("|"); }
            sb.append("NLM_F_ACK");
        }
        if ((flags & NLM_F_ECHO) != 0) {
            if (sb.length() > 0) { sb.append("|"); }
            sb.append("NLM_F_ECHO");
        }
        if ((flags & NLM_F_ROOT) != 0) {
            if (sb.length() > 0) { sb.append("|"); }
            sb.append("NLM_F_ROOT");
        }
        if ((flags & NLM_F_MATCH) != 0) {
            if (sb.length() > 0) { sb.append("|"); }
            sb.append("NLM_F_MATCH");
        }
        return sb.toString();
    }

    public static boolean hasAvailableSpace(ByteBuffer byteBuffer) {
        return byteBuffer != null && byteBuffer.remaining() >= STRUCT_SIZE;
    }

    public static StructNlMsgHdr parse(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) { return null; }
// The ByteOrder must have already been set by the caller. In most
// cases ByteOrder.nativeOrder() is correct, with the exception
// of usage within unittests.
        final StructNlMsgHdr struct = new StructNlMsgHdr();
        struct.nlmsg_len = byteBuffer.getInt();
        struct.nlmsg_type = byteBuffer.getShort();
        struct.nlmsg_flags = byteBuffer.getShort();
        struct.nlmsg_seq = byteBuffer.getInt();
        struct.nlmsg_pid = byteBuffer.getInt();
        if (struct.nlmsg_len < STRUCT_SIZE) {
// Malformed.
            Log.v(TAG, "Struct NlMsgHdr MalFormed");
            return null;
        }
        return struct;
    }

    public boolean pack(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) { return false; }
// The ByteOrder must have already been set by the caller. In most
// cases ByteOrder.nativeOrder() is correct, with the possible
// exception of usage within unittests.
        byteBuffer.putInt(nlmsg_len);
        byteBuffer.putShort(nlmsg_type);
        byteBuffer.putShort(nlmsg_flags);
        byteBuffer.putInt(nlmsg_seq);
        byteBuffer.putInt(nlmsg_pid);
        return true;
    }

    @Override
    public String toString() {
        final String typeStr = "" + nlmsg_type
                + "(" + NetlinkConstants.stringForNlMsgType(nlmsg_type) + ")";
        final String flagsStr = "" + nlmsg_flags
                + "(" + stringForNlMsgFlags(nlmsg_flags) + ")";
        return "StructNlMsgHdr{ "
                + "nlmsg_len{" + nlmsg_len + "}, "
                + "nlmsg_type{" + typeStr + "}, "
                + "nlmsg_flags{" + flagsStr + ")}, "
                + "nlmsg_seq{" + nlmsg_seq + "}, "
                + "nlmsg_pid{" + nlmsg_pid + "} "
                + "}";
    }
}

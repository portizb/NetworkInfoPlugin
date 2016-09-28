package com.movistar.tvsindesco.android.net.netlink;

/**
 * Created by pablo on 2/09/16.
 */
import com.telefonica.movistar.android.system.Os;
import com.telefonica.movistar.android.system.OsConstants;

import java.nio.ByteBuffer;

/**
 * struct ifa_cacheinfo
 *
 * see: &lt;linux_src&gt;/include/uapi/linux/if_addr.h
 *
 * @hide
 */
public class StructIfaCacheInfo {
    // Already aligned.
    public static final int STRUCT_SIZE = 16;
    public static final int INFINITY_LIFE_TIME = 0xFFFFFFFF;

    private static final long CLOCK_TICKS_PER_SECOND = Os.sysconf(OsConstants._SC_CLK_TCK);

    public int ifa_prefered;
    public int ifa_valid;
    public int cstamp;
    public int tstamp;

    public StructIfaCacheInfo() {}

    private static boolean hasAvailableSpace(ByteBuffer byteBuffer) {
        return byteBuffer != null && byteBuffer.remaining() >= STRUCT_SIZE;
    }

    public static StructIfaCacheInfo parse(ByteBuffer byteBuffer) {
        if (!hasAvailableSpace(byteBuffer)) { return null; }

        final StructIfaCacheInfo struct = new StructIfaCacheInfo();
        struct.ifa_prefered = byteBuffer.getInt();
        struct.ifa_valid = byteBuffer.getInt();
        struct.cstamp = byteBuffer.getInt();
        struct.tstamp = byteBuffer.getInt();

        return struct;
    }

    private static long ticksToMilliSeconds(int intClockTicks) {
        final long longClockTicks = (long) intClockTicks & 0xffffffff;
        return (longClockTicks * 1000) / CLOCK_TICKS_PER_SECOND;
    }

    public long createdTimeStamp() {
        return ticksToMilliSeconds(cstamp);
    }

    public long updatedTimeStamp() {
        return ticksToMilliSeconds(tstamp);
    }

    @Override
    public String toString() {
        return "StructIfaCacheInfo{ "
                + "ifa_prefered{" + ifa_prefered + "}, "
                + "ifa_valid{" + ifa_valid + "}, "
                + "cstamp{" + createdTimeStamp() + "}, "
                + "tstamp{" + updatedTimeStamp() + "} "
                + "}";
    }
}

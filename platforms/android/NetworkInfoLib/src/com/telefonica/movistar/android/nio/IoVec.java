package com.telefonica.movistar.android.nio;

/**
 * Created by pablo on 7/09/16.
 */
/**
 * Used to implement java.nio read(ByteBuffer[])/write(ByteBuffer[]) operations as POSIX readv(2)
 * and writev(2) calls.
 */
import android.system.ErrnoException;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.telefonica.movistar.android.system.Os;

final class IoVec {
    enum Direction { READV, WRITEV };
    private final ByteBuffer[] byteBuffers;
    private final int offset;
    private final int bufferCount;
    private final Object[] ioBuffers;
    private final int[] offsets;
    private final int[] byteCounts;
    private final Direction direction;

    public IoVec(ByteBuffer[] byteBuffers, int offset, int bufferCount, Direction direction) {
        this.byteBuffers = byteBuffers;
        this.offset = offset;
        this.bufferCount = bufferCount;
        this.direction = direction;
        this.ioBuffers = new Object[bufferCount];
        this.offsets = new int[bufferCount];
        this.byteCounts = new int[bufferCount];
    }

    private byte[] unsafeArray(ByteBuffer b) {
        return b.array();
    }

    private int unsafeArrayOffset(ByteBuffer b) {
        return b.arrayOffset();
    }

    private void checkWritable(ByteBuffer b) {
        if (b.isReadOnly()) {
            throw new IllegalArgumentException();
          }
    }

    public int init() {
        int totalRemaining = 0;
        for (int i = 0; i < bufferCount; ++i) {
            ByteBuffer b = byteBuffers[i + offset];

            if (direction == Direction.READV) {
                checkWritable(b);
            }

            int remaining = b.remaining();

            if (b.isDirect()) {
                ioBuffers[i] = b;
                offsets[i] = b.position();
            } else {
                ioBuffers[i] = unsafeArray(b);
                offsets[i] = unsafeArrayOffset(b) + b.position();
            }

            byteCounts[i] = remaining;
            totalRemaining += remaining;
        }

        return totalRemaining;
    }

    public int doTransfer(FileDescriptor fd) throws IOException {
        try {
            if (direction == Direction.READV) {
                int result = Os.readv(fd, ioBuffers, offsets, byteCounts);
                if (result == 0) {
                    result = -1;
                }
                return result;
            } else {
                return Os.writev(fd, ioBuffers, offsets, byteCounts);
            }
        } catch (Exception ex) {
            throw new IOException(ex.fillInStackTrace());
        }
    }

    void didTransfer(int byteCount) {
        for (int i = 0; byteCount > 0 && i < bufferCount; ++i) {
            ByteBuffer b = byteBuffers[i + offset];
            if (byteCounts[i] < byteCount) {
                b.position(b.limit());
                byteCount -= byteCounts[i];
            } else {
                b.position((direction == Direction.WRITEV ? b.position() : 0) + byteCount);
                byteCount = 0;
            }
        }
    }
}

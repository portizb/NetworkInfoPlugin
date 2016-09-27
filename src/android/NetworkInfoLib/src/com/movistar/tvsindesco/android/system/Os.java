package com.movistar.tvsindesco.android.system;

import android.system.ErrnoException;

import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * Created by pablo on 7/07/16.
 */
public class Os {

    private static final String LOG_TAG = "Os";

    static {
        System.loadLibrary("os-jni");
    }

    private static native int readBytes(FileDescriptor fd, Object buffer, int offset, int byteCount) throws ErrnoException, InterruptedIOException;
    private static native int writeBytes(FileDescriptor fd, Object buffer, int offset, int byteCount) throws ErrnoException, InterruptedIOException;

    public static native FileDescriptor socket(int domain, int type, int protocol) throws ErrnoException;
    public static native void bind(FileDescriptor fd, SocketAddress address) throws ErrnoException, SocketException;
    public static native void connect(FileDescriptor fd, SocketAddress address) throws ErrnoException, SocketException;
    public static native int close(FileDescriptor fd) throws ErrnoException;

    public static native int readv(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, SocketException;
    public static native int writev(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, SocketException;

    public static native SocketAddress getsockname(FileDescriptor fd) throws ErrnoException;
    public static native void setsockoptInt(FileDescriptor fd, int level, int option, int value) throws ErrnoException;
    public static native void setsockoptTimeval(FileDescriptor fd, int level, int option, StructTimeval value) throws ErrnoException;
    public static native long sysconf(int name);


    public static void bind(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException {
        bind(fd, new InetSocketAddress(address, port));
    }

    public static void connect(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException {
        connect(fd, new InetSocketAddress(address, port));
    }

    public static int read(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException {
        final int bytesRead;
        final int position = buffer.position();

        if (buffer.isDirect()) {
            bytesRead = readBytes(fd, buffer, position, buffer.remaining());
        } else {
            bytesRead = readBytes(fd, buffer.array(), buffer.arrayOffset() + position, buffer.remaining());
        }

        maybeUpdateBufferPosition(buffer, position, bytesRead);
        return bytesRead;
    }

    public static int write(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException {
        return writeBytes(fd, bytes, byteOffset, byteCount);
    }

    private static void maybeUpdateBufferPosition(ByteBuffer buffer, int originalPosition, int bytesReadOrWritten) {
        if (bytesReadOrWritten > 0) {
            buffer.position(bytesReadOrWritten + originalPosition);
        }
    }
}

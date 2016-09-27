//
// Created by pablo on 5/07/16.
//

#include <jni.h>
#include <string.h>
#include <stdio.h>

#include <sys/socket.h>
#include <sys/sysconf.h>
#include <sys/uio.h>

#include <linux/if_addr.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include <unistd.h>
#include <alloca.h>

#include <android/log.h>


#define MAX_LOG_MESSAGE_LENGTH 256

#define LOG_TAG    "SO-Native"
#define DEBUG(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#define inetAddressClassName "java/net/InetAddress"
#define inet6AddressClassName "java/net/InetAddress"
#define inetSocketAddressClassName "java/net/SocketAddress"
#define inetSocketAddressHolderClassName "java/net/InetSocketAddress$InetSocketAddressHolder"
#define netlinkSocketAddressClassName "com/telefonica/movistar/android/system/NetlinkSocketAddress"

static void throwException(JNIEnv* env, const char* className, const char* message)
{
    jclass clazz = (*env)->FindClass(env, className);

    if (NULL != clazz)
    {
        (*env)->ThrowNew(env, clazz, message);
        (*env)->DeleteLocalRef(env, clazz);
    }
}

static void throwErrnoException(JNIEnv * env, const char* className, int errnum)
{
    char buffer[MAX_LOG_MESSAGE_LENGTH];

    if (-1 == strerror_r(errnum, buffer, MAX_LOG_MESSAGE_LENGTH))
        strerror_r(errno, buffer, MAX_LOG_MESSAGE_LENGTH);

    throwException(env, className, buffer);
}

static void throwNullPointerException(JNIEnv * env, const char* className)
{
    char buffer[MAX_LOG_MESSAGE_LENGTH];

    strcpy(buffer, "Null Pointer Exception");

    throwException(env, className, buffer);
}

static jobject jniCreateFileDescriptor(JNIEnv *env, int fd)
{
    jclass class_fdesc = (*env)->FindClass(env, "java/io/FileDescriptor");
    jmethodID constructor_fdesc = (*env)->GetMethodID(env, class_fdesc, "<init>", "()V");
    if (constructor_fdesc == NULL) return NULL;

    jobject fdObject = (*env)->NewObject(env, class_fdesc, constructor_fdesc);
    jfieldID field_fd = (*env)->GetFieldID(env, class_fdesc, "descriptor", "I");
    if (field_fd == NULL) return NULL;

    (*env)->SetIntField(env, fdObject, field_fd, fd);

    return fdObject;
}

static jint jniGetFDFromFileDescriptor(JNIEnv *env, jobject fileDescriptor)
{
    jint fd = -1;
    jclass class_fdesc = (*env)->FindClass(env, "java/io/FileDescriptor");

    if (class_fdesc != NULL) {

        jfieldID field_fd = (*env)->GetFieldID(env, class_fdesc, "descriptor", "I");

        if (field_fd != NULL && fileDescriptor != NULL) {
            fd = (*env)->GetIntField(env, fileDescriptor, field_fd);
        }
    }

    return fd;
}

static int jniSetFileDescriptorOfFD(JNIEnv *env, jobject fileDescriptor, jint fd)
{
    jclass fdClass = (*env)->FindClass(env, "java/io/FileDescriptor");

    if (fdClass != NULL) {
        jfieldID fdClassDescriptorFieldID = (*env)->GetFieldID(env, fdClass, "descriptor", "I");

        if (fdClassDescriptorFieldID != NULL && fileDescriptor != NULL) {
            (*env)->SetIntField(env, fileDescriptor, fdClassDescriptorFieldID, fd);
        }
    }

    return fd;
}

static jobject sockaddrToInetAddress(JNIEnv* env, struct sockaddr_storage *ss, jint* port)
{
    const void* rawAddress;
    size_t addressLength;
    int sin_port = 0;
    int scope_id = 0;

    if (ss->ss_family == AF_INET) {
        const struct sockaddr_in *sin = (const struct sockaddr_in*)(ss);
        rawAddress = (void *) sin->sin_addr.s_addr;
        addressLength = 4;
        sin_port = ntohs(sin->sin_port);
    } else if (ss->ss_family == AF_INET6) {
        const struct sockaddr_in6 *sin6 = (const struct sockaddr_in6*)(ss);
        rawAddress = sin6->sin6_addr.s6_addr;
        addressLength = 16;
        sin_port = ntohs(sin6->sin6_port);
        scope_id = sin6->sin6_scope_id;
    } else {
        throwException(env, "java/lang/IllegalArgumentException",
                             "sockaddrToInetAddress unsupported ss_family");
        return NULL;
    }

    if (port != NULL) {
        *port = sin_port;
    }

    jbyteArray javaBytes = (*env)->NewByteArray(env, (jsize) addressLength);
    jbyte* ptrBytes = (*env)->GetByteArrayElements(env, javaBytes, NULL);

    if (ptrBytes == NULL) {
        throwNullPointerException(env, NULL);
        return NULL;
    }

    (*env)->SetByteArrayRegion(env, ptrBytes, 0, (jsize) addressLength,
                               (const jbyte*)(rawAddress));

    jclass inetAddressClassz = (*env)->FindClass(env, inetAddressClassName);

    jmethodID getByAddressMethod = (*env)->GetStaticMethodID(env, inetAddressClassz,
            "getByAddress", "(Ljava/lang/String;[BI)Ljava/net/InetAddress;");

    if (getByAddressMethod == NULL) {
        return NULL;
    }

    return (*env)->CallStaticObjectMethod(env, inetAddressClassz, getByAddressMethod,
            NULL, ptrBytes, scope_id);
}

static jobject makeSocketAddress(JNIEnv* env, struct sockaddr_storage *ss)
{
    if (ss->ss_family == AF_INET || ss->ss_family == AF_INET6) {
        jint port;
        jobject inetAddress = sockaddrToInetAddress(env, ss, &port);

        if (inetAddress == NULL) {
            return NULL; // Exception already thrown.
        }

        jclass inetSocketAddressClassz = (*env)->FindClass(env, inetSocketAddressClassName);

        jmethodID ctor = (*env)->GetMethodID(env, inetSocketAddressClassz, "<init>", "(Ljava/net/InetAddress;I)V");
        return (*env)->NewObject(env, inetSocketAddressClassz, ctor, inetAddress, port);

    } else if ( ss->ss_family == AF_NETLINK) {
        const struct sockaddr_nl *nl_addr = (const struct sockaddr_nl *) (ss);

        jclass netlinkSocketAddressClassz = (*env)->FindClass(env, netlinkSocketAddressClassName);

        jmethodID ctor = (*env)->GetMethodID(env, netlinkSocketAddressClassz, "<init>", "(II)V");

        return (*env)->NewObject(env, netlinkSocketAddressClassz, ctor,
                                 (jint) (nl_addr->nl_pid),
                                 (jint) (nl_addr->nl_groups));
    }

    return NULL;
}

static void javaInetSocketAddressToInetAddressAndPort(
        JNIEnv* env, jobject inetSocketAddress, jobject inetAddress, jint *port)
{
    jclass inetSocketAddressClassz = (*env)->FindClass(env, inetSocketAddressClassName);
    jclass inetSocketAddressHolderClassz = (*env)->FindClass(env, inetSocketAddressHolderClassName);

    jfieldID holderFid = (*env)->GetFieldID(env, inetSocketAddressClassz, "holder", "Ljava/net/InetSocketAddress$InetSocketAddressHolder;");
    jobject holder = (*env)->GetObjectField(env, inetSocketAddress, holderFid);

    jfieldID addressFid = (*env)->GetFieldID(env, inetSocketAddressHolderClassz, "addr", "Ljava/net/InetAddress;");
    jfieldID portFid = (*env)->GetFieldID(env, inetSocketAddressHolderClassz, "port", "I");

    inetAddress = (*env)->GetObjectField(env, holder, addressFid);
    *port = (*env)->GetIntField(env, holder, portFid);
}

static jboolean inetAddressToSockaddr(JNIEnv* env, jobject inetAddress, int port, struct sockaddr_storage *ss, socklen_t *sa_len)
{
    memset(ss, 0, sizeof(*ss));
    *sa_len = 0;

    if (inetAddress == NULL) {
        throwNullPointerException(env, NULL);
        return JNI_FALSE;
    }

    jclass inetAddressClassz = (*env)->FindClass(env, inetAddressClassName);

    // Get the address family.
    jfieldID familyFid = (*env)->GetFieldID(env, inetAddressClassz, "family", "I");
    ss->ss_family = (__kernel_sa_family_t) (*env)->GetIntField(env, inetAddress, familyFid);
    if (ss->ss_family == AF_UNSPEC) {
        *sa_len = sizeof(ss->ss_family);
        return JNI_TRUE; // Job done!
    }

    // Check this is an address family we support.
    if (ss->ss_family != AF_INET && ss->ss_family != AF_INET6) {
        throwException(env, "java/lang/IllegalArgumentException", "inetAddressToSockaddr bad family");
        return JNI_FALSE;
    }

    // Get the byte array that stores the IP address bytes in the InetAddress.
    jfieldID bytesFid = (*env)->GetFieldID(env, inetAddressClassz, "ipaddress", "[B");
    jbyteArray javaBytes = (*env)->GetObjectField(env, inetAddress, bytesFid);
    jbyte* ptrBytes = (*env)->GetByteArrayElements(env, javaBytes, NULL);

    if (ptrBytes == NULL) {
        throwNullPointerException(env, NULL);
        return JNI_FALSE;
    }

    struct sockaddr_in6 *sin6 = (struct sockaddr_in6 *)(ss);
    sin6->sin6_port = htons(port);

    if (ss->ss_family == AF_INET6) {
        // IPv6 address. Copy the bytes...
        jbyte *dst = (jbyte *) (sin6->sin6_addr.s6_addr);
        (*env)->GetByteArrayRegion(env, javaBytes, 0, 16, dst);
        // ...and set the scope id...

        jclass inet6AddressClassz = (*env)->FindClass(env, inet6AddressClassName);

        jfieldID scopeFid = (*env)->GetFieldID(env, inet6AddressClassz, "scope_id", "I");
        sin6->sin6_scope_id = (__u32) (*env)->GetIntField(env, inetAddress, scopeFid);
        *sa_len = sizeof(struct sockaddr_in6);
    } else {

        // We should represent this Inet4Address as an IPv4 sockaddr_in.
        struct sockaddr_in *sin = (struct sockaddr_in *)(ss);
        sin->sin_port = htons(port);
        jbyte* dst = (jbyte*)(sin->sin_addr.s_addr);
        (*env)->GetByteArrayRegion(env, javaBytes, 0, 4, dst);
        *sa_len = sizeof(struct sockaddr_in);
    }

    (*env)->ReleaseByteArrayElements(env, javaBytes, ptrBytes, JNI_ABORT);
    return JNI_TRUE;

}

static jboolean javaInetSocketAddressToSockaddr(
        JNIEnv* env, jobject socketAddress, struct sockaddr_storage *ss, socklen_t *sa_len)
{
    jobject inetAddress = NULL;
    jint port;

    javaInetSocketAddressToInetAddressAndPort(env, socketAddress, inetAddress, &port);
    return inetAddressToSockaddr(env, inetAddress, port, ss, sa_len);
}

static jboolean javaNetlinkSocketAddressToSockaddr(JNIEnv* env, jobject socketAddress, struct sockaddr_storage *ss, socklen_t *sa_len)
{
    jclass netlinkSocketAddressClassz = (*env)->FindClass(env, netlinkSocketAddressClassName);

    jfieldID nlPidFid = (*env)->GetFieldID(env, netlinkSocketAddressClassz, "nlPortId", "I");
    jfieldID nlGroupsFid = (*env)->GetFieldID(env, netlinkSocketAddressClassz, "nlGroupsMask", "I");

    if ((nlPidFid != NULL) && (nlGroupsFid != NULL)) {

        struct sockaddr_nl *nlAddr = (struct sockaddr_nl *) (ss);
        nlAddr->nl_family = AF_NETLINK;
        nlAddr->nl_pid = (__u32) (*env)->GetIntField(env, socketAddress, nlPidFid);
        nlAddr->nl_groups = (__u32) (*env)->GetIntField(env, socketAddress, nlGroupsFid);
        *sa_len = sizeof(struct sockaddr_nl);

        return JNI_TRUE;
    }

    return JNI_FALSE;
}

static jboolean javaSocketAddressToSockaddr(
        JNIEnv* env, jobject socketAddress, struct sockaddr_storage *ss, socklen_t *sa_len)
{
    if (socketAddress == NULL) {
        throwNullPointerException(env, NULL);
        return JNI_FALSE;
    }

    if ((*env)->IsInstanceOf(env, socketAddress, (*env)->FindClass(env, netlinkSocketAddressClassName))) {
        return javaNetlinkSocketAddressToSockaddr(env, socketAddress, ss, sa_len);
    } else if ((*env)->IsInstanceOf(env, socketAddress, (*env)->FindClass(env, inetSocketAddressClassName))) {
        return javaInetSocketAddressToSockaddr(env, socketAddress, ss, sa_len);
    }

   throwException(env, "java/lang/UnsupportedOperationException", "unsupported SocketAddress subclass");

    return JNI_FALSE;
}



jobject Java_com_telefonica_movistar_android_system_Os_socket(JNIEnv* env, jclass clazz, jint domain, jint type, jint protocol)
{
    int fd;

    if ((fd = TEMP_FAILURE_RETRY(socket(domain, type, protocol))) == -1)
        throwErrnoException (env, "socket", errno);

    return fd != -1 ? jniCreateFileDescriptor(env, fd) : NULL;
}

void Java_com_telefonica_movistar_android_system_Os_connect(JNIEnv* env, jclass clazz, jobject fileDescriptor, jobject socketAddress)
{
    struct sockaddr_storage ss;
    socklen_t sa_len;

    if (!javaSocketAddressToSockaddr(env, socketAddress, &ss, &sa_len)) {
        return;
    }

    const struct sockaddr* sa = (const struct sockaddr*)(&ss);

    int fd = jniGetFDFromFileDescriptor (env, fileDescriptor);

    if (TEMP_FAILURE_RETRY(connect(fd, sa, sa_len)) == -1)
        throwErrnoException (env, "connect", errno);
}

void Java_com_telefonica_movistar_android_system_Os_bind(JNIEnv* env, jclass clazz, jobject fileDescriptor, jobject socketAddress)
{
    struct sockaddr_storage ss;
    socklen_t sa_len;

    if (!javaSocketAddressToSockaddr(env, socketAddress, &ss, &sa_len)) {
        return;
    }

    const struct sockaddr* sa = (const struct sockaddr*)(&ss);

    int fd = jniGetFDFromFileDescriptor (env, fileDescriptor);

    if (TEMP_FAILURE_RETRY(bind(fd, sa, sa_len)) == -1)
        throwErrnoException (env, "bind", errno);
}

void Java_com_telefonica_movistar_android_system_Os_close(JNIEnv* env, jclass clazz, jobject fileDescriptor)
{
    int fd = jniGetFDFromFileDescriptor (env, fileDescriptor);
    jniSetFileDescriptorOfFD(env, fileDescriptor, -1);

    if (TEMP_FAILURE_RETRY(close(fd)) == -1)
        throwErrnoException (env, "close", errno);
}

jobject Java_com_telefonica_movistar_android_system_Os_getsockname(JNIEnv* env, jclass clazz, jobject fileDescriptor)
{
    struct sockaddr_storage ss;
    struct sockaddr* sa = (struct sockaddr*)(&ss);
    socklen_t byteCount = sizeof(ss);

    memset(&ss, 0, byteCount);

    int fd = jniGetFDFromFileDescriptor(env, fileDescriptor);
    int rc = TEMP_FAILURE_RETRY(getsockname(fd, sa, &byteCount));

    if (rc == -1) {
        throwErrnoException(env, "getsockname", errno);
        return NULL;
    }

    return makeSocketAddress(env, &ss);
}


void Java_com_telefonica_movistar_android_system_Os_setsockoptInt(JNIEnv* env, jclass clazz, jobject fileDescriptor, jint level, jint option, jint value)
{
    int fd = jniGetFDFromFileDescriptor(env, fileDescriptor);

    if (TEMP_FAILURE_RETRY(setsockopt(fd, level, option, &value, sizeof(value))) == -1)
        throwErrnoException (env, "setsockopt", errno);
}

void Java_com_telefonica_movistar_android_system_Os_setsockoptTimeval(JNIEnv* env, jclass clazz, jobject fileDescriptor, jint level, jint option, jobject javaTimeval)
{
    jclass fdClass = (*env)->GetObjectClass(env, javaTimeval);

    if (fdClass != NULL) {

        jfieldID tvSecFid = (*env)->GetFieldID(env, fdClass, "tv_sec", "J");
        jfieldID tvUsecFid = (*env)->GetFieldID(env, fdClass, "tv_usec", "J");

        int fd = jniGetFDFromFileDescriptor(env, fileDescriptor);

        struct timeval value;
        value.tv_sec = (*env)->GetLongField(env, javaTimeval, tvSecFid);
        value.tv_usec = (*env)->GetLongField(env, javaTimeval, tvUsecFid);

        if (TEMP_FAILURE_RETRY(setsockopt(fd, level, option, &value, sizeof(value))) == -1)
            throwErrnoException(env, "setsockopt", errno);
    }
}

jint Java_com_telefonica_movistar_android_system_Os_readBytes(JNIEnv* env, jclass clazz, jobject fileDescriptor, jbyteArray javaBytes, jint byteOffset, jint byteCount)
{
    ssize_t size;
    jsize bufLen = byteCount - byteOffset;
    jbyte *buf = alloca((size_t)bufLen);

    int fd = jniGetFDFromFileDescriptor (env, fileDescriptor);

    if (-1 == (size = TEMP_FAILURE_RETRY(read(fd, buf, (size_t)bufLen))))
        throwErrnoException(env, "read", errno);

    (*env)->SetByteArrayRegion(env, javaBytes, byteOffset, (jsize)size, buf);

    return (jint)size;
}

jint Java_com_telefonica_movistar_android_system_Os_readv(JNIEnv* env, jclass clazz, jobject fileDescriptor, jobjectArray buffers, jintArray offsets, jintArray byteCounts) {
    ssize_t size = 0;

    // TODO
    return size;
}

jint Java_com_telefonica_movistar_android_system_Os_writeBytes(JNIEnv* env, jclass clazz, jobject fileDescriptor, jbyteArray javaBytes, jint byteOffset, jint byteCount) {
    ssize_t size;
    jbyte* ptrBytes = (*env)->GetByteArrayElements(env, javaBytes, NULL);

    if (ptrBytes == NULL) {
        return -1;
    }

    int fd = jniGetFDFromFileDescriptor (env, fileDescriptor);

    if (-1 == (size = TEMP_FAILURE_RETRY(write(fd, ptrBytes + byteOffset, (size_t)byteCount))))
        throwErrnoException(env, "write", errno);

    (*env)->ReleaseByteArrayElements(env, javaBytes, ptrBytes, JNI_ABORT);

    return (jint)size;
}

jint Java_com_telefonica_movistar_android_system_Os_writev(JNIEnv* env, jclass clazz, jobject fileDescriptor, jobjectArray buffers, jintArray offsets, jintArray byteCounts)
{
    ssize_t size = -1, i;
    struct iovec *iov = NULL;
    jbyte* ptrBuffer;
/*    size_t bufferCount = (*env)->GetArrayLength(env, buffers);

    if ((*env)->PushLocalFrame(env, bufferCount + 16) < 0) {
        return -1;
    }

    jint* ptrOffsets = (*env)->GetIntArrayElements(env, offsets, NULL);

    if (ptrOffsets == NULL) {
        return -1;
    }

    jint* ptrByteCounts = (*env)->GetIntArrayElements(env, byteCounts, NULL);

    if (ptrByteCounts == NULL) {
        (*env)->ReleaseIntArrayElements(env, offsets, ptrOffsets, JNI_ABORT);
        return -1;
    }

    if ((iov = calloc (bufferCount, sizeof(iov[0]))) == NULL) {
        (*env)->ReleaseIntArrayElements(env, offsets, ptrOffsets, JNI_ABORT);
        (*env)->ReleaseIntArrayElements(env, byteCounts, ptrByteCounts, JNI_ABORT);
        return -1;
    }

    if ((ptrBuffer = calloc (bufferCount, sizeof(iov[0]))) == NULL) {
        (*env)->ReleaseIntArrayElements(env, offsets, ptrOffsets, JNI_ABORT);
        (*env)->ReleaseIntArrayElements(env, byteCounts, ptrByteCounts, JNI_ABORT);
        return -1;
    }

    for (size_t i = 0; i < bufferCount; ++i) {
        jobject buffer = (*env)->GetObjectArrayElement(env, buffers, i);
        ptrBuffer[i] = (*env)->GetByteArrayElements(env, (jbyteArray)buffer, NULL);

        if (ptrBuffer[i] == NULL) {
            continue;
        }

        iov[i].iov_base = ptrBuffer[i] + ptrOffsets[i];
        iov[i].iov_len = ptrByteCounts[i];
    }

    int fd = jniGetFDFromFileDescriptor (env, fileDescriptor);

    if (-1 == (size = TEMP_FAILURE_RETRY(writev(fd, iov, (size_t)bufferCount))))
        throwErrnoException(env, "write", errno);

    (*env)->ReleaseIntArrayElements(env, offsets, ptrOffsets, JNI_ABORT);
    (*env)->ReleaseIntArrayElements(env, byteCounts, ptrByteCounts, JNI_ABORT);

    for (i = 0; i < bufferCount; ++i) {
        jobject buffer = (*env)->GetObjectArrayElement(env, buffers, i);

        if (ptrBuffer[i]) {
            (*env)->ReleaseIntArrayElements(env, buffer, ptrBuffer[i], JNI_ABORT);
        }
    }*/

    return size;
}

jlong Java_com_telefonica_movistar_android_system_Os_sysconf(JNIEnv* env, jclass clazz, jint name) {
    errno = 0;

    long result = sysconf(name);
    if (result == -1L && errno == EINVAL) {
        throwErrnoException(env, "sysconf", errno);
    }
    return result;
}

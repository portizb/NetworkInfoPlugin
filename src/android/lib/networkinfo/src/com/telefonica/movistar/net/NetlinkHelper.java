package com.telefonica.movistar.net;

import android.util.Log;

import com.telefonica.movistar.android.net.netlink.NetlinkConstants;
import com.telefonica.movistar.android.net.netlink.NetlinkErrorMessage;
import com.telefonica.movistar.android.net.netlink.NetlinkMessage;
import com.telefonica.movistar.android.net.netlink.NetlinkSocket;
import com.telefonica.movistar.android.net.netlink.RtNetlinkAddressMessage;
import com.telefonica.movistar.android.net.netlink.RtNetlinkNeighborMessage;
import com.telefonica.movistar.android.net.netlink.RtNetlinkRouteMessage;
import com.telefonica.movistar.android.net.netlink.StructNlMsgHdr;
import com.telefonica.movistar.android.system.NetlinkSocketAddress;
import com.telefonica.movistar.android.system.OsConstants;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by pablo on 6/09/16.
 */
public class NetlinkHelper {

    private static final String TAG = "NetlinkHelper";

    private static final int SEQNO = 1;
    private static final long TIMEOUT = 500;

    private static int myPid = android.os.Process.myPid();

    public static RtNetlinkNeighborMessage[] getNeighbor () throws Exception {

        ArrayList<RtNetlinkNeighborMessage> messages = new ArrayList<RtNetlinkNeighborMessage>();

        int neighMessageCount = 0;
        int doneMessageCount = 0;

        NetlinkSocket s = new NetlinkSocket(OsConstants.NETLINK_ROUTE);
        s.connectToKernel();

        NetlinkSocketAddress localAddr = s.getLocalAddress();
        Log.v(TAG, "bound to sockaddr_nl{"
                + ((long) (localAddr.getPortId() & 0xffffffff)) + ", "
                + localAddr.getGroupsMask()
                + "}");

        final byte[] request = RtNetlinkNeighborMessage.newGetNeighborsRequest(SEQNO);

        s.sendMessage(request, 0, request.length, TIMEOUT);

        while (doneMessageCount == 0) {
            ByteBuffer response = s.recvMessage(TIMEOUT);

            while (response.remaining() > 0) {
                final NetlinkMessage msg = NetlinkMessage.parse(response);

                final StructNlMsgHdr hdr = msg.getHeader();

                if(hdr.nlmsg_pid != myPid || hdr.nlmsg_seq != SEQNO) {
                    Log.v(TAG, "wrong seq " + hdr.nlmsg_seq + " or pid " + hdr.nlmsg_pid);
                    continue;
                }

                if (hdr.nlmsg_type == NetlinkConstants.NLMSG_DONE) {
                    doneMessageCount++;
                    continue;
                }
                else if (hdr.nlmsg_type == NetlinkConstants.RTM_NEWNEIGH) {
                    Log.v(TAG, msg.toString());

                    messages.add((RtNetlinkNeighborMessage)msg);

                    neighMessageCount++;
                }
                else if (msg instanceof NetlinkErrorMessage) {
                    doneMessageCount++;
                    break;
                }
                else {
                    Log.v(TAG, "Error found " + ((NetlinkErrorMessage)msg).getNlMsgError());
                }
            }
        }

        s.close();

        return messages.toArray(new RtNetlinkNeighborMessage[messages.size()]);
    }

    public static RtNetlinkRouteMessage getRoute (String destination) throws Exception {

        RtNetlinkRouteMessage message = null;

        int routeMessageCount = 0;
        int doneMessageCount = 0;

        NetlinkSocket s = new NetlinkSocket(OsConstants.NETLINK_ROUTE);
        s.connectToKernel();

        NetlinkSocketAddress localAddr = s.getLocalAddress();
        Log.v(TAG, "bound to sockaddr_nl{"
                + ((long) (localAddr.getPortId() & 0xffffffff)) + ", "
                + localAddr.getGroupsMask()
                + "}");

        final int SEQNO = 1;
        final byte[] request = RtNetlinkRouteMessage.newGetRouteRequest(SEQNO, destination);

        if (s.sendMessage(request, 0, request.length, TIMEOUT)) {

            while (doneMessageCount == 0) {
                ByteBuffer response = s.recvMessage(TIMEOUT);

                while (response.remaining() > 0) {
                    final NetlinkMessage msg = NetlinkMessage.parse(response);

                    if (msg == null) {
                        doneMessageCount++;
                        break;
                    }

                    final StructNlMsgHdr hdr = msg.getHeader();

                    if (hdr.nlmsg_pid != myPid || hdr.nlmsg_seq != SEQNO) {
                        Log.v(TAG, "wrong seq " + hdr.nlmsg_seq + " or pid " + hdr.nlmsg_pid);
                        continue;
                    }

                    if (hdr.nlmsg_type == NetlinkConstants.NLMSG_DONE) {
                        doneMessageCount++;
                        continue;
                    } else if (hdr.nlmsg_type == NetlinkConstants.RTM_NEWROUTE) {
                        Log.v(TAG, msg.toString());
                        message = (RtNetlinkRouteMessage) msg;
                        routeMessageCount++;
                    } else if (msg instanceof NetlinkErrorMessage) {
                        if (((NetlinkErrorMessage) msg).getNlMsgError().error == 0) {
                            doneMessageCount++;
                            break;
                        } else {
                            Log.v(TAG, "Error found " + ((NetlinkErrorMessage) msg).getNlMsgError());
                        }
                    }
                }
            }
        }

        s.close();

        return message;
    }

    public static RtNetlinkAddressMessage[] getAddress () throws Exception {

        ArrayList<RtNetlinkAddressMessage> messages = new ArrayList<RtNetlinkAddressMessage>();

        int addressMessageCount = 0;
        int doneMessageCount = 0;

        NetlinkSocket s = new NetlinkSocket(OsConstants.NETLINK_ROUTE);
        s.connectToKernel();

        NetlinkSocketAddress localAddr = s.getLocalAddress();
        Log.v(TAG, "bound to sockaddr_nl{"
                + ((long) (localAddr.getPortId() & 0xffffffff)) + ", "
                + localAddr.getGroupsMask()
                + "}");

        final int SEQNO = 0;
        final byte[] request = RtNetlinkAddressMessage.newGetAddressRequest(SEQNO);

        s.sendMessage(request, 0, request.length, TIMEOUT);

        while (doneMessageCount == 0) {
            ByteBuffer response = s.recvMessage(TIMEOUT);

            while (response.remaining() > 0) {
                final NetlinkMessage msg = NetlinkMessage.parse(response);

                if (msg == null) {
                    doneMessageCount++;
                    break;
                }

                final StructNlMsgHdr hdr = msg.getHeader();

                if(hdr.nlmsg_pid != myPid || hdr.nlmsg_seq != SEQNO) {
                    Log.v(TAG, "wrong seq " + hdr.nlmsg_seq + " or pid " + hdr.nlmsg_pid);
                    continue;
                }

                if (hdr.nlmsg_type == NetlinkConstants.NLMSG_DONE) {
                    doneMessageCount++;
                    continue;
                }
                else if (hdr.nlmsg_type == NetlinkConstants.RTM_NEWADDR) {
                    Log.v(TAG, msg.toString());
                    messages.add((RtNetlinkAddressMessage)msg);
                    addressMessageCount++;
                }
                else if (msg instanceof NetlinkErrorMessage) {
                    if (((NetlinkErrorMessage)msg).getNlMsgError().error == 0)
                    {
                        doneMessageCount++;
                        break;
                    }
                    else {
                        Log.v(TAG, "Error found " + ((NetlinkErrorMessage)msg).getNlMsgError());
                    }
                }
            }
        }

        s.close();

        return messages.toArray(new RtNetlinkAddressMessage[messages.size()]);
    }
}

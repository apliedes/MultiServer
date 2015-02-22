package fi.kvy.stargate.multiserver;

import java.io.IOException;

import java.nio.ByteBuffer;

import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SelectableChannel;

import java.nio.ByteOrder;

import java.net.SocketAddress;

import java.util.Calendar;

/**
 * Implementation of a simple UDP time (RFC 868) service using MultiplexedServer
 * interface.
 */
public class DatagramTimeServer implements MultiplexedServer {
    private static final long SECONDS_1900_TO_1970 = 2208988800L;

    public SocketChannel accept(ServerSocketChannel fromChannel)
    throws IOException {
        // Nothing to accept for UDP, we shouldn't be here!
        return null;
    }

    public void read(SelectableChannel fromChannel)
    throws IOException {
        DatagramChannel channel = (DatagramChannel) fromChannel;
        // In IPv4, UDP size is limited to 64k (minus headers which we ignore).
        ByteBuffer buffer = ByteBuffer.allocate(64 * 1024);
        SocketAddress address = channel.receive(buffer);

        Calendar now = Calendar.getInstance();
        long secondsSince1970
            = now.getTimeInMillis() / 1000;
        long secondsSince1900 = secondsSince1970 + SECONDS_1900_TO_1970;

        ByteBuffer longBuffer = ByteBuffer.allocate(8);
        longBuffer.order(ByteOrder.BIG_ENDIAN);
        longBuffer.position(4);
        ByteBuffer intBuffer = longBuffer.slice();
        longBuffer.clear();
        longBuffer.putLong(secondsSince1900);
        
        channel.send(intBuffer, address);
    }
}

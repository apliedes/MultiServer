package fi.kvy.stargate.multiserver;

import java.io.IOException;

import java.nio.ByteBuffer;

import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SelectableChannel;

import java.net.SocketAddress;

import java.util.Date;

/**
 * Implementation of a simple UDP daytime (RFC 867) service using
 * MultiplexedServer interface.
 */
public class DatagramDaytimeServer implements MultiplexedServer {
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

        ByteBuffer responseBuffer = ByteBuffer.allocate(100);
        Date now = new Date();
        responseBuffer.asCharBuffer().put(now.toString() + "\n");
        channel.send(responseBuffer, address);
    }
}

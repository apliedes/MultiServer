package fi.kvy.stargate.multiserver;

import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectableChannel;

import java.util.Date;

/**
 * Implementation of a simple TCP daytime (RFC 867) service using
 * MultiplexedServer interface.
 */
public class StreamDaytimeServer implements MultiplexedServer {
    public SocketChannel accept(ServerSocketChannel fromChannel)
    throws IOException {
        SocketChannel newChannel = fromChannel.accept();
        newChannel.configureBlocking(false);

        // We don't read anything, only write the answer and close
        // the connection.
        Date now = new Date();
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.asCharBuffer().put(now.toString() + "\n");
        newChannel.write(buffer);
        newChannel.close();

        return null;
    }

    public void read(SelectableChannel fromChannel)
    throws IOException {
    }
}

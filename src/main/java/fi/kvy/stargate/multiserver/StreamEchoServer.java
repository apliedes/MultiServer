package fi.kvy.stargate.multiserver;

import java.io.IOException;

import java.nio.ByteBuffer;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectableChannel;

/**
 * Implementation of a simple TCP echo (RFC 862) service using MultiplexedServer
 * interface.
 */
public class StreamEchoServer implements MultiplexedServer {
    public SocketChannel accept(ServerSocketChannel fromChannel)
    throws IOException {
        SocketChannel newChannel = fromChannel.accept();
        newChannel.configureBlocking(false);

        return newChannel;
    }

    public void read(SelectableChannel fromChannel)
    throws IOException {
        SocketChannel channel = (SocketChannel) fromChannel;
        // Echo up to 1k bytes at a time, if we receive more, we'll simply
        // get selected again and echo another up to 1k bytes.
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(buffer);
        
        // It may be we can't write everything here, in which case we would
        // like to be selected for blocking. However, without a state we don't
        // have a channel specific buffer to write from. But we are marked as
        // non-blocking, so at least we won't just block the whole multiplexer.
        if (bytesRead > 0) {
            int bytesLeft = bytesRead;

            buffer.flip();
            
            while (bytesLeft > 0) {
                int bytesWritten = channel.write(buffer);
                bytesLeft -= bytesWritten;
            }
        } else if (bytesRead == -1) {
            fromChannel.close();
        }
    }
}

package fi.kvy.stargate.multiserver;

import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectableChannel;

import java.nio.ByteOrder;

import java.util.Calendar;

/**
 * Implementation of a simple TCP time (RFC 868) service using
 * MultiplexedServer interface.
 */
public class StreamTimeServer implements MultiplexedServer {
    private static final long SECONDS_1900_TO_1970 = 2208988800L;

    public SocketChannel accept(ServerSocketChannel fromChannel)
    throws IOException {
        SocketChannel newChannel = fromChannel.accept();
        newChannel.configureBlocking(false);

        // We don't read anything, only write the answer and close
        // the connection.
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
        
        newChannel.write(intBuffer);
        newChannel.close();

        return null;
    }

    public void read(SelectableChannel fromChannel)
    throws IOException {
    }
}

package fi.kvy.stargate.multiserver;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectableChannel;

/**
 * Interface for simple stateless stream and datagram services. The services
 * can only react to two things: a new connection is incoming, or an existing
 * connection receives data.
 */
public interface MultiplexedServer {
    /**
     * A new connection is ready to be established in the given channel. If a
     * non-null channel is returned, that channel will be selected for reading,
     * and all reads from it will be handled by the same object that accepted
     * the new connection.
     *
     * @param  fromChannel  channel that a new connection can be accepted from
     * @return              a new channel to be read from, or null if no new
     *                      channel to read from is esatblished.
     */
    public SocketChannel accept(ServerSocketChannel fromChannel) throws IOException;

    /**
     * A channel can be read from.
     *
     * @param  fromChannel  the channel we can read from.
     */
    public void read(SelectableChannel fromChannel) throws IOException;
}

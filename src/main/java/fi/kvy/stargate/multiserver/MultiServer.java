package fi.kvy.stargate.multiserver;

import java.io.IOException;
import java.io.InterruptedIOException;

import java.net.InetSocketAddress;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import java.lang.InterruptedException;

/**
 * A simple server infrastructure that can multiplex services defined by the
 * MultiplexedServer interface.
 */
public class MultiServer {
    private static final int DEFAULT_RFC_862_PORT =  7;
    private static final int DEFAULT_RFC_867_PORT = 13;
    private static final int DEFAULT_RFC_868_PORT = 37;

    /**
     * Helper to start both a stream and a datagram server to the given port
     * for the given selector.
     */
    private static void startServices(Selector selector,
                               int port,
                               MultiplexedServer streamServer,
                               MultiplexedServer datagramServer)
    throws IOException {
        ServerSocketChannel streamChannel = ServerSocketChannel.open();

        streamChannel.bind(new InetSocketAddress(port));
        streamChannel.configureBlocking(false);

        SelectionKey streamSelectionKey
            = streamChannel.register(selector, SelectionKey.OP_ACCEPT);
        streamSelectionKey.attach(streamServer);

        DatagramChannel datagramChannel = DatagramChannel.open();

        datagramChannel.bind(new InetSocketAddress(port));
        datagramChannel.configureBlocking(false);

        SelectionKey datagramSelectionKey
            = datagramChannel.register(selector, SelectionKey.OP_READ);
        datagramSelectionKey.attach(datagramServer);
    }

    /**
     * Print out usage and exit.
     */
    private static void usageAndExit() {
        System.err.println("Usage: MultiServer <options>");
        System.err.println("valid options: --rfc-862-port <port>");
        System.err.println("               --rfc-867-port <port>");
        System.err.println("               --rfc-868-port <port>");

        System.exit(1);
    }

    public static void main(String[] args) throws IOException {
        int rfc862Port = DEFAULT_RFC_862_PORT;
        int rfc867Port = DEFAULT_RFC_867_PORT;
        int rfc868Port = DEFAULT_RFC_868_PORT;
        Selector selector = Selector.open();

        for (int i = 0; i < args.length; i++) {
            if (i < args.length - 1) {
                if (args[i].equals("--rfc-862-port")) {
                    i++;
                    rfc862Port = Integer.parseInt(args[i]);
                } else if (args[i].equals("--rfc-867-port")) {
                    i++;
                    rfc867Port = Integer.parseInt(args[i]);
                } else if (args[i].equals("--rfc-868-port")) {
                    i++;
                    rfc868Port = Integer.parseInt(args[i]);
                } else {
                    usageAndExit();
                }
            } else {
                usageAndExit();
            }
        }

        startServices(selector,
                      rfc862Port,
                      new StreamEchoServer(),
                      new DatagramEchoServer());
        startServices(selector,
                      rfc867Port,
                      new StreamDaytimeServer(),
                      new DatagramDaytimeServer());
        startServices(selector,
                      rfc868Port,
                      new StreamTimeServer(),
                      new DatagramTimeServer());

        try {
            for (;;) {
                int numSelected = 0;
                numSelected = selector.select();
                
                if (numSelected == 0) {
                    continue;
                }
                
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                for (Iterator<SelectionKey> iterator = selectedKeys.iterator();
                     iterator.hasNext();
                     iterator.remove()) {
                    SelectionKey key = iterator.next();
                    MultiplexedServer multiplexedServer
                        = (MultiplexedServer) key.attachment();
                    
                    if (key.isAcceptable()) {
                        try {
                            SocketChannel newChannel
                                = multiplexedServer.accept
                                ((ServerSocketChannel) key.channel());
                            
                            if (newChannel != null) {
                                newChannel.configureBlocking(false);
                                SelectionKey newKey
                                    = newChannel.register
                                    (selector, SelectionKey.OP_READ);
                                newKey.attach(multiplexedServer);
                            }
                        } catch (InterruptedIOException e) {
                            throw e;
                        } catch (IOException e) {
                            key.channel().close();
                        }
                    } else if (key.isReadable()) {
                        try {
                            multiplexedServer.read
                                ((SelectableChannel) key.channel());
                        } catch (InterruptedIOException e) {
                            throw e;
                        } catch (IOException e) {
                            key.channel().close();
                        }
                    }
                }
            }
        } catch (InterruptedIOException e) {
            // This should catch interrupts for a graceful exit,
            // but it doesn't work on my machine.
            System.out.println("interrupted");
        }
        
        System.exit(0);
    }
}

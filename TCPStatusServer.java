/**
 * Mina implementation of udp listener for performance issues in native UDP datagram socket
 */
package ehouse4openremote;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class TCPStatusServer
{   
    public  TCPStatusServer() throws IOException
    {
        int sPort = Integer.parseInt(ehousecommunication.OpenRemotePort);
        
        IoAcceptor acceptor = new NioSocketAcceptor();

        //acceptor.getFilterChain().addLast( "logger", new LoggingFilter() );
        //acceptor.getFilterChain().addLast( "codec", new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName( "UTF-8" ))));

        System.out.println("[startup] Starting TCP eHouse server on port: " + String.valueOf(sPort));

        acceptor.setHandler( new TCPStatusServerHandler() );
        acceptor.getSessionConfig().setReadBufferSize( 2048 );
        acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, 2 );
        acceptor.bind( new InetSocketAddress(sPort) );
    } 
}

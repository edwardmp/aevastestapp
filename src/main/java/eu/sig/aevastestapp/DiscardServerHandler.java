package eu.sig.aevastestapp;


import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class DiscardServerHandler extends SimpleChannelHandler {


    public static void main(String[] args) {
        try {
            ChannelFactory factory =
                    new NioServerSocketChannelFactory(
                            Executors.newCachedThreadPool(),
                            Executors.newCachedThreadPool());

            ServerBootstrap bootstrap = new ServerBootstrap(factory);

            ChannelPipelineFactory pipeline = new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() {
                    return Channels.pipeline(new DiscardServerHandler());
                }
            };

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            //tmf.init(null);

            // Initialize the SSLContext to work with our key managers.
            SSLContext serverContext = SSLContext.getInstance("TLS");
            serverContext.init(null, null, null);

            SSLEngine engine = serverContext.createSSLEngine();
            engine.setUseClientMode(false);
            engine.setNeedClientAuth(true);
            SslHandler handl = new SslHandler(engine);
            handl.handleDownstream(null, null);
            pipeline.getPipeline().addLast("ssl", handl);

            bootstrap.setPipelineFactory(pipeline);

            bootstrap.setOption("child.tcpNoDelay", true);

            bootstrap.setOption("child.keepAlive", true);
            bootstrap.bind(new InetSocketAddress(8080));
        } catch (Exception e) {

        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        Channel ch = e.getChannel();
        ch.write(e.getMessage());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {

        e.getCause().printStackTrace();

        Channel ch = e.getChannel();
        ch.close();
    }
}
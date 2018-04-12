package com.secrething.tools.local.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalServerChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author liuzz
 * @create 2018/3/14
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private String localAddress;

    public Server(String localAddress) {
        this.localAddress = localAddress;
    }

    public void run() throws Exception {
        //暂时只用 nio方式吧
        /*ServerBootstrap b = ServerBootstrapFactory.newNioServerBootstrap();
        try {
            b.childHandler(new ServerInitializer()).option(ChannelOption.SO_BACKLOG, Integer.valueOf(128)).childOption(ChannelOption.SO_KEEPALIVE, Boolean.valueOf(true));
            ChannelFuture f = b.bind(this.port).sync();
            logger.info("server started !");
            f.channel().closeFuture().sync();
        } finally {
            b.config().childGroup().shutdownGracefully();
            b.config().group().shutdownGracefully();
        }*/
        EventLoopGroup eventLoopGroup = new DefaultEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(eventLoopGroup);
            b.channel(LocalServerChannel.class);
            b.childHandler(new ServerInitializer());
            LocalAddress address = new LocalAddress(this.localAddress);
            b.bind(address).addListener(new ChannelFutureListener() {
                @Override public void operationComplete(ChannelFuture future) throws Exception {
                    System.out.println("local server successively bind");
                }
            });
        } catch (Exception e) {
            System.out.println("error !" + e);
        }

    }
}

package com.secrething.tools.server;

import com.secrething.tools.server.factory.ServerBootstrapFactory;
import com.secrething.tools.server.handler.ServerInitializer;
import com.secrething.tools.server.service.ProcessService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author liuzz
 * @create 2018/3/14
 */
public abstract class NettyServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private int port;
    public NettyServer() {
        this.port = 9999;
    }

    public NettyServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port = 9999;
        if (args.length == 1) {
            port = Integer.valueOf(args[0]).intValue();
        }

    }

    public abstract NettyServer getInstance();
    public abstract ProcessService getProcessService();
    public void run() throws Exception {
        //暂时只用 nio方式吧
        ServerBootstrap b = ServerBootstrapFactory.newNioServerBootstrap();
        try {
            b.childHandler(new ServerInitializer(getProcessService())).option(ChannelOption.SO_BACKLOG, Integer.valueOf(128)).childOption(ChannelOption.SO_KEEPALIVE, Boolean.valueOf(true));
            ChannelFuture f = b.bind(this.port).sync();
            logger.info("server started !");
            f.channel().closeFuture().sync();
        } finally {
            b.config().childGroup().shutdownGracefully();
            b.config().group().shutdownGracefully();
        }

    }
}

package com.secrething.tools.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;

/**
 * Created by liuzz on 2018/4/10.
 */
public class BootFactory {
    public static <C extends Channel> Bootstrap createBoot(EventLoopGroup workerGroup, String proxy_ip, int proxy_prot, Class<C> channelClz, ChannelInitializer<C> channelInitializer) {
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(channelClz);
        b.remoteAddress(proxy_ip, proxy_prot);
        b.option(ChannelOption.SO_KEEPALIVE, Boolean.valueOf(true));
        b.handler(channelInitializer);
        return b;
    }
}

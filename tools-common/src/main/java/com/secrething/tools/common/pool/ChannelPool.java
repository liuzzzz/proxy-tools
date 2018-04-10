package com.secrething.tools.common.pool;

import com.google.common.collect.Lists;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import java.util.List;

/**
 * Created by liuzz on 2018/4/10.
 */
public class ChannelPool {
    private final List<Channel> deque = Lists.newCopyOnWriteArrayList();
    public static final int FREE = 0;
    public static final int RUNNING = 1;
    public static final int DEAD = 2;
    private int size = 30;
    private final Bootstrap bootstrap;

    public ChannelPool(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Channel chooseOne() {
        return deque.get((int) (Math.random() * size));
    }

    private synchronized void initChannels() {
        for (int i = 0; i < size; i++) {
            Channel channel = bootstrap.connect().channel();
            deque.add(channel);
        }
    }
}

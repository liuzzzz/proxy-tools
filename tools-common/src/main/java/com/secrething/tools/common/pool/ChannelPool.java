package com.secrething.tools.common.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by liuzz on 2018/4/10.
 */
public final class ChannelPool extends GenericObjectPool<Channel> {
    public ChannelPool(Bootstrap bootstrap) {
        super(new DefaultChannelFactory(bootstrap));
    }

    public ChannelPool(Bootstrap bootstrap, GenericObjectPoolConfig config) {
        super(new DefaultChannelFactory(bootstrap), config);
    }

    public ChannelPool(Bootstrap bootstrap, GenericObjectPoolConfig config, AbandonedConfig abandonedConfig) {
        super(new DefaultChannelFactory(bootstrap), config, abandonedConfig);
    }

    public Channel getResource() throws Exception{
        try {
            return borrowObject();
        } catch (Exception e) {
            throw e;
        }
    }

    private static class DefaultChannelFactory implements PooledObjectFactory<Channel> {
        private final Bootstrap bootstrap;

        private DefaultChannelFactory(Bootstrap bootstrap) {
            this.bootstrap = bootstrap;
        }

        @Override
        public PooledObject<Channel> makeObject() throws Exception {
            Channel channel = bootstrap.connect().sync().syncUninterruptibly().channel();
            final DefaultPooledObject<Channel> object = new DefaultPooledObject<>(channel);
            channel.closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    destroyObject(object);
                }
            });

            return object;
        }

        @Override
        public void destroyObject(PooledObject<Channel> p) throws Exception {
            p.getObject().disconnect();
        }

        @Override
        public boolean validateObject(PooledObject<Channel> p) {
            return p.getObject().isActive();
        }

        @Override
        public void activateObject(PooledObject<Channel> p) throws Exception {
            //throw new UnsupportedOperationException();
            //Channel channel = p.getObject();
            //channel.connect(channel.remoteAddress());
        }

        @Override
        public void passivateObject(PooledObject<Channel> p) throws Exception {
            //throw new UnsupportedOperationException();
        }
    }
}

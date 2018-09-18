package com.secrething.tools.client;

import com.google.common.cache.LoadingCache;
import com.secrething.tools.common.contant.ConfigProp;
import com.secrething.tools.common.pool.ChannelPool;
import com.secrething.tools.common.protocol.MessageProtocol;
import com.secrething.tools.common.protocol.RequestEntity;
import com.secrething.tools.common.protocol.ResponseEntity;
import com.secrething.tools.common.utils.CacheBuilder;
import com.secrething.tools.common.utils.MesgFormatter;
import com.secrething.tools.common.utils.SerializeUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author liuzz
 * @create 2018/3/14
 */
public class Client {
    public static final String proxy_ip;
    public static final int proxy_prot;
    public static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static final Client client = new Client();
    static LoadingCache<String, MessageFuture> loadingCache = CacheBuilder.build(16384, 1, TimeUnit.MINUTES);
    public static final ConcurrentMap<String, MessageFuture> futureConcurrentMap = loadingCache.asMap();

    static {
        String fromPro = ConfigProp.getConfig("proxy_ip");
        if (StringUtils.isBlank(fromPro)) {
            proxy_ip = "localhost";
        } else
            proxy_ip = fromPro;
        String port = ConfigProp.getConfig("proxy_prot");
        if (StringUtils.isBlank(port))
            proxy_prot = 9999;
        else {
            int p = 9999;
            try {
                p = Integer.valueOf(port);
            } catch (Exception e) {
            }
            proxy_prot = p;

        }
        client.init();

    }

    private String host = "";
    private int port = -1;
    private volatile EventLoopGroup workerGroup = null;
    private volatile Channel channel = null;
    private volatile Bootstrap bootstrap = null;
    private volatile boolean initMark = false;
    private ChannelPool channelPool = null;

    private Client() {
        //init();
    }

    private Client(String host, int port) {
        this();
        this.host = host;
        this.port = port;
    }

    public static String sendRequest(RequestEntity request) throws Exception {
        MessageFuture future = new MessageFuture(request);

        try {
            byte[] content = SerializeUtil.serialize(request);
            int contentLength = content.length;
            MessageProtocol protocol = new MessageProtocol(contentLength, content);
            protocol.setMessageUID(UUID.randomUUID().toString());
            protocol.setMesg_type(MessageProtocol.PROXY);
            futureConcurrentMap.put(protocol.getMessageUID(), future);
            client.sendRequest(protocol);
            ResponseEntity response = future.get(2, TimeUnit.MINUTES);
            if (null != response.getThrowable())
                response.getThrowable().printStackTrace();
            return response.getResult().toString();
        } catch (Exception e) {

        } finally {
            //client.close();
        }
        return "";
    }

    public static void main(String[] args) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        for (int i = 0; i < 1; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String url = "http://www.baidu.com";
                    String request = "{\"type\":\"0\",\"cid\":\"tuniu\",\"tripType\":\"1\",\"fromCity\":\"BKK\",\"toCity\":\"HKT\",\"fromDate\":\"20180421\",\"all\":\"\",\"adultNum\":\"1\",\"childNum\":\"0\",\"infantNumber\":\"0\",\"retDate\":\"\",\"maxWaitTime\":20000}";
                    int waitTime = 60000;
                    String res = ProxyHttpPoolManage.sendGetRequest(url, waitTime);
                    System.out.println(res);
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        client.channelPool.close();
    }

    private void init() {
        try {
            synchronized (this) {
                if (client.needInit()) {
                    initMark = true;
                    Bootstrap b = new Bootstrap();
                    workerGroup = new NioEventLoopGroup();
                    b.group(workerGroup);
                    b.channel(NioSocketChannel.class);
                    b.remoteAddress(proxy_ip, proxy_prot);
                    b.option(ChannelOption.SO_KEEPALIVE, Boolean.valueOf(true));
                    b.handler(new ClientInitializer());
                    bootstrap = b;
                    this.channelPool = new ChannelPool(b);
                }
            }

        } catch (Exception e) {
        }
    }

    private boolean needInit() {
        return !initMark;

    }

    private void connect(Bootstrap b) throws InterruptedException {
        if (null == this.channel) {
            ChannelFuture f = b.connect().sync().syncUninterruptibly();
            f.channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    logger.info("connection closed");
                    channel = null;
                }
            });
            this.channel = f.channel();
        }
    }

    private void sendRequest(MessageProtocol protocol) {
        try {
            long begin = System.currentTimeMillis();
            final Channel channel = channelPool.getResource();
            MesgFormatter.println("getResourceCost:{}",System.currentTimeMillis() - begin);
            synchronized (channelPool){
                MesgFormatter.println("create:{}",channelPool.getCreatedCount());
                MesgFormatter.println("borrow:{}",channelPool.getBorrowedCount());
                MesgFormatter.println("total:{}",channelPool.getMaxTotal());

            }
            channel.writeAndFlush(protocol).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    channelPool.returnObject(channel);
                }
            });
        } catch (Exception e) {
            logger.error("get channel resource fail", e);
        }
    }
}

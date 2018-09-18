package com.secrething.tools.server;

import com.secrething.tools.server.service.ProcessService;
import com.secrething.tools.server.service.impl.ProxyProcessServiceImpl;

/**
 * Created by liuzz on 2018/9/18 3:03 PM.
 */
public class Server extends NettyServer {
    public Server() {
    }

    public Server(int port) {
        super(port);
    }

    @Override
    public ProcessService getProcessService() {
        return new ProxyProcessServiceImpl();
    }
    public static void main(String[] args) throws Exception {
        int port = 9999;
        if (args.length == 1) {
            port = Integer.valueOf(args[0]).intValue();
        }
        Server server = new Server(port);
        server.run();

    }
}

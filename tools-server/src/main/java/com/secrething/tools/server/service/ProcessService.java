package com.secrething.tools.server.service;

import com.secrething.tools.common.protocol.MessageProtocol;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by liuzz on 2018/4/24.
 */
public interface ProcessService {
    MessageProtocol process(MessageProtocol inputMsg);
}

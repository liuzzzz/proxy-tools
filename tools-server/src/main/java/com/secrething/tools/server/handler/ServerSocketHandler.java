package com.secrething.tools.server.handler;

import com.secrething.tools.common.manage.HttpPoolManage;
import com.secrething.tools.common.protocol.MessageProtocol;
import com.secrething.tools.common.protocol.Param;
import com.secrething.tools.common.protocol.RequestEntity;
import com.secrething.tools.common.protocol.ResponseEntity;
import com.secrething.tools.common.utils.MesgFormatter;
import com.secrething.tools.common.utils.SerializeUtil;
import com.secrething.tools.server.service.ProcessService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liuzz
 * @create 2018/3/14
 */
public class ServerSocketHandler extends SimpleChannelInboundHandler<MessageProtocol> {


    private final ProcessService processService;

    public ServerSocketHandler(ProcessService processService) {
        this.processService = processService;
    }

    public void channelRead0(ChannelHandlerContext ctx, MessageProtocol inputMsg) throws Exception {
        MessageProtocol outMsg = processService.process(inputMsg);
        ctx.writeAndFlush(outMsg);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}

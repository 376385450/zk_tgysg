package com.sinohealth.web.ws;

import com.sinohealth.system.biz.ws.handler.BizWebSocketHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * @Author Rudolph
 * @Date 2022-06-01 16:09
 * @Desc
 */
@Component
public class ProjectInitializer extends ChannelInitializer<SocketChannel> {


    static final String WEBSOCKET_PROTOCOL = "WebSocket";

    @Value("${webSocket.netty.path:/ws}")
    String webSocketPath;

    @Autowired
    private BizWebSocketHandler webSocketHandler;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // 设置管道
        ChannelPipeline pipeline = ch.pipeline();
        // 流水线管道中的 handler, 处理实际业务
        // websocket 基于 http， 使用 http 编解码器
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ObjectEncoder());
        // 以块的方式来写处理器
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpObjectAggregator(8192));
        // 自定义 handler, 处理业务
        pipeline.addLast(webSocketHandler);
        pipeline.addLast(new WebSocketServerProtocolHandler(webSocketPath, WEBSOCKET_PROTOCOL, true, 65536, false, true));
    }
}

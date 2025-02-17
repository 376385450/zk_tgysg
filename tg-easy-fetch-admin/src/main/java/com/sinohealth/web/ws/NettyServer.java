package com.sinohealth.web.ws;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;


/**
 * @Author Rudolph
 * @Date 2022-05-31 10:27
 * @Desc
 */
@Slf4j
@Component
public class NettyServer {

    @Value("${websocket.netty.port:18999}")
    private int port;

    EventLoopGroup bossGroup;
    EventLoopGroup workGroup;

    @Autowired
    ProjectInitializer nettyInitializer;

    @PostConstruct
    public void start() {

        new Thread(() -> {
            bossGroup = new NioEventLoopGroup();
            workGroup = new NioEventLoopGroup();
            ServerBootstrap bootstrap = new ServerBootstrap();
            // bossGroup 辅助客户端 TCP 连接请求, workGroup 负责与客户端之前的读写操作
            bootstrap.group(bossGroup, workGroup);
            // 设置 NIO 类型的 channel
            bootstrap.channel(NioServerSocketChannel.class);
            // 设置监听端口
            bootstrap.localAddress(new InetSocketAddress(port));
            // 设置管道
            bootstrap.childHandler(nettyInitializer);

            // 配置完成, 开始绑定 server, 通过调用 sync 同步方法阻塞直到绑定成功
            try {
                ChannelFuture channelFuture = bootstrap.bind().sync();
                log.info("Start Websocket " + channelFuture.channel().localAddress());
                // 对关闭通道进行监听
                channelFuture.channel().closeFuture().sync();
            } catch (Throwable e) {
                log.error("", e);
            }
        }).start();

    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully().sync();
        }
        if (workGroup != null) {
            workGroup.shutdownGracefully().sync();
        }
    }
}

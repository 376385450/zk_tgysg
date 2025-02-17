package com.sinohealth.system.biz.ws.handler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.ip.IpUtils;
import com.sinohealth.system.biz.ws.msg.IMsg;
import com.sinohealth.system.biz.ws.msg.QueueMsg;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.mapper.SysUserMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-02-28 18:17
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class BizWebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Value("${webSocket.netty.path:/ws}")
    private String webSocketPath;

    private static final Map<Long, Channel> userMap = new ConcurrentHashMap<>();
    private static final Map<String, Long> channelUserMap = new ConcurrentHashMap<>();

    @Resource
    @Qualifier(ThreadPoolType.SCHEDULER_MSG)
    private ScheduledExecutorService scheduler;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SysUserMapper userMapper;


    public void pushMsgWithCluster(Long userId, IMsg msg) {
        try {
            Channel channel = userMap.get(userId);
            if (Objects.isNull(channel)) {
                Object host = redisTemplate.opsForHash().get(RedisKeys.Ws.Router, userId);
                if (Objects.nonNull(host)) {
                    String hostKey = RedisKeys.Ws.getMsgQueueKey(host.toString());
                    String txt = JsonUtils.format(msg);
                    this.add(hostKey, new QueueMsg(userId, txt));
                }
                return;
            }
            String txt = JsonUtils.format(msg);
            log.debug("PUSH: userId={} {}", userId, txt);
            channel.writeAndFlush(new TextWebSocketFrame(txt));
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private void add(String hostKey, QueueMsg msg) {
        redisTemplate.opsForList().leftPush(hostKey, JsonUtils.format(msg));
        redisTemplate.expire(hostKey, Duration.ofMinutes(2));
    }

    private Object poll(String queueKey) {
        return redisTemplate.opsForList().rightPop(queueKey);
    }

    private void pushTxtMsg(Long userId, String msg) {
        try {
            Channel channel = userMap.get(userId);
            if (Objects.isNull(channel)) {
                return;
            }
            log.debug("PUSH: userId={} {}", userId, msg);
            channel.writeAndFlush(new TextWebSocketFrame(msg));
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @PostConstruct
    private void handleQueueMsg() {
        // 定时消费 需要推送的消息
        scheduler.scheduleAtFixedRate(() -> {
            String queueKey = RedisKeys.Ws.getMsgQueueKey();
            for (int i = 0; i < 100; i++) {
                Object val = this.poll(queueKey);
                if (Objects.isNull(val)) {
                    return;
                }
                QueueMsg msg = JsonUtils.parse(val.toString(), QueueMsg.class);
                log.debug("read: userId={}", msg.getUserId());
                pushTxtMsg(msg.getUserId(), msg.getMsg());
            }
        }, 10, 1, TimeUnit.SECONDS);

        // 清理过时映射的连接
        scheduler.scheduleAtFixedRate(() -> {
            for (Map.Entry<Long, Channel> entry : userMap.entrySet()) {
                Long userId = entry.getKey();
                Object host = redisTemplate.opsForHash().get(RedisKeys.Ws.Router, userId);
                String hostIp = IpUtils.getHostIp();
                // 关闭时会触发 channelInactive
                if (Objects.isNull(host) || !Objects.equals(host, hostIp)) {
                    log.info("confuse: userId={}", userId);
                    String id = entry.getValue().id().asShortText();
                    channelUserMap.remove(id);
                    entry.getValue().close();
                    userMap.remove(userId);
                }
            }
        }, 2, 1, TimeUnit.MINUTES);
    }

    private void httpRequestHandler(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.uri();
        Map<String, String> params = WsSocketUtil.getParams(uri);
        log.info("http params:{}", params);

        String userIdStr = params.get("userId");
        Long userId = WsSocketUtil.parseUserId(userIdStr);
        if (Objects.isNull(userId)) {
            ctx.close();
            return;
        }

        // 校验用户合法性
        Integer existCnt = userMapper.selectCount(new QueryWrapper<SysUser>().lambda().eq(SysUser::getUserId, userId));
        if (Objects.isNull(existCnt) || existCnt == 0) {
            log.warn("invalid user: userId={}", userId);
            ctx.close();
            return;
        }

        // 关闭原有连接
        if (userMap.containsKey(userId)) {
            Channel last = userMap.get(userId);
            if (!Objects.equals(last, ctx.channel())) {
                last.writeAndFlush(new TextWebSocketFrame("reset"));
                log.warn("close last  user:{} host:{}", userId, WsSocketUtil.remote(ctx));
                last.close();
                userMap.remove(userId);
                channelUserMap.remove(last.id().asShortText());
            }
        }

        // 缓存连接映射
        String hostIp = IpUtils.getHostIp();
        redisTemplate.opsForHash().put(RedisKeys.Ws.Router, userId, hostIp);
        userMap.put(userId, ctx.channel());
        channelUserMap.put(WsSocketUtil.id(ctx), userId);

        // 判断请求路径是否跟配置中的一致
        if (webSocketPath.equals(WsSocketUtil.getBasePath(uri))) {
            // 因为有可能携带了参数，导致客户端一直无法返回握手包，因此在校验通过后，重置请求路径
            request.setUri(webSocketPath);
        } else {
            ctx.close();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Conn {} host:{} size: {} {}", WsSocketUtil.id(ctx), WsSocketUtil.remote(ctx), userMap.size(), channelUserMap.size());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String id = WsSocketUtil.id(ctx);
        Long userId = channelUserMap.get(id);
        if (Objects.nonNull(userId)) {
            log.debug("DisConn {} {} userId:{} size:{} {}", id, userId, WsSocketUtil.remote(ctx), userMap.size(), channelUserMap.size());
            userMap.remove(userId);
            redisTemplate.opsForHash().delete(RedisKeys.Ws.Router, userId);
            channelUserMap.remove(id);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.channel().flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof PingWebSocketFrame) {
            pingWebSocketFrameHandler(ctx, (PingWebSocketFrame) frame);
        } else if (frame instanceof TextWebSocketFrame) {
            textWebSocketFrameHandler(ctx, (TextWebSocketFrame) frame);
        } else if (frame instanceof CloseWebSocketFrame) {
            closeWebSocketFrameHandler(ctx, (CloseWebSocketFrame) frame);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        log.debug("MSG type: {}", msg.getClass());
        if (msg instanceof FullHttpRequest) {
            httpRequestHandler(ctx, (FullHttpRequest) msg);
        }
        if (ctx.channel().isOpen()) {
            super.channelRead(ctx, msg);
        }
    }

    /**
     * 客户端发送断开请求处理
     */
    private void closeWebSocketFrameHandler(ChannelHandlerContext ctx, CloseWebSocketFrame frame) {
        ctx.close();
    }

    /**
     * 创建连接之后，客户端发送的消息都会在这里处理
     */
    private void textWebSocketFrameHandler(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        String text = frame.text();
        if (Objects.equals(text, "ping")) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame("pong"));
        }
    }

    /**
     * 处理客户端心跳包
     */
    private void pingWebSocketFrameHandler(ChannelHandlerContext ctx, PingWebSocketFrame frame) {
        ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
    }

}

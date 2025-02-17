package com.sinohealth.web.ws;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Service;

/**
 * @Author Rudolph
 * @Date 2022-06-02 9:54
 * @Desc
 */

@Service
public class PushMsgServiceImpl implements PushMsgService {
    @Override
    public void pushMsgToUser(String userId, String msg) {
        Channel channel = NettyConfig.getChannel(userId);
        if (ObjectUtils.isNull(channel)) {
            throw new RuntimeException(">>>>>>>>>>>>>> 未连接 socket 服务");
        }

        channel.writeAndFlush(new TextWebSocketFrame(msg));
    }


    @Override
    public void pushMsgToAllUsers(String msg) {
        NettyConfig.getChannelGroup().writeAndFlush(new TextWebSocketFrame(msg));
    }
}

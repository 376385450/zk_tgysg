package com.sinohealth.web.ws;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.security.core.parameters.P;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Rudolph
 * @Date 2022-05-31 14:11
 * @Desc
 */

public class NettyConfig {

    /**
     * 全局单例 channel 组
     */

    private static volatile ChannelGroup channelGroup = null;

    /**
     * 存放请求ID与channel对应关系
     */
    private static volatile ConcurrentHashMap<String, Channel> channelMap = null;

    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();

    public static ChannelGroup getChannelGroup() {
        if (null == channelGroup) {
            synchronized (lock1) {
                if (null == channelGroup) {
                    channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
                }
            }
        }
        return channelGroup;
    }

    public static ConcurrentHashMap<String, Channel> getChannelMap() {
        if (null == channelMap) {
            synchronized (lock2) {
                if (null == channelMap) {
                    channelMap = new ConcurrentHashMap<>();
                }
            }
        }
        return channelMap;
    }

    public static Channel getChannel(String uid) {
        if (null == channelMap) {
            return getChannelMap().get(uid);
        }
        return channelMap.get(uid);
    }

}

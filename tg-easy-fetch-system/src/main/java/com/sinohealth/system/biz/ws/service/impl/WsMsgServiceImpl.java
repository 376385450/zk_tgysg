package com.sinohealth.system.biz.ws.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.system.biz.message.service.MessageService;
import com.sinohealth.system.biz.ws.handler.BizWebSocketHandler;
import com.sinohealth.system.biz.ws.msg.*;
import com.sinohealth.system.biz.ws.service.WsMsgService;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-02-29 10:55
 */
@Slf4j
@Service
public class WsMsgServiceImpl implements WsMsgService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    @Qualifier(ThreadPoolType.SCHEDULER)
    private ScheduledExecutorService scheduler;

    @Autowired
    private TgApplicationInfoMapper applicationInfoMapper;

    @Autowired
    private BizWebSocketHandler socket;

    @Autowired
    private MessageService messageService;


    @Override
    public boolean hasWsOnline(Long userId) {
        Object host = redisTemplate.opsForHash().get(RedisKeys.Ws.Router, userId);
        return Objects.nonNull(host);
    }

    @Override
    public void pushUnReadMsg() {
        Set userIds = redisTemplate.opsForHash().keys(RedisKeys.Ws.Router);
        pushUnReadMsg(userIds);
    }

    @Override
    public void pushUnReadMsg(Collection<Long> userIds) {
        for (Long userId : userIds) {
            if (!hasWsOnline(userId)) {
                continue;
            }
            UnReadMsg body = messageService.unReadMeg(userId);
            socket.pushMsgWithCluster(userId, body);
        }
    }

    @Override
    public void pushUnReadMsg(Long userId) {
        UnReadMsg body = messageService.unReadMeg(userId);
        socket.pushMsgWithCluster(userId, body);
    }

    @Override
    public void pushNoticeMsg(Long userId) {
        scheduler.schedule(() -> {
            socket.pushMsgWithCluster(userId, new SimpleMsg(userId, MsgType.NOTICE_REFRESH));
        }, 5, TimeUnit.SECONDS);
    }

    @Override
    public void pushNoticeMsg(Collection<Long> userIds, Long id) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        scheduler.schedule(() -> {
            for (Long userId : userIds) {
                if (!hasWsOnline(userId)) {
                    continue;
                }

                socket.pushMsgWithCluster(userId, new SingleIdMsg(userId, MsgType.NOTICE_REFRESH, id));
            }
            pushUnReadMsg(userIds);
        }, 5, TimeUnit.SECONDS);
    }

    @Override
    public void pushAnnouncementMsg(Long id) {
        Set userIds = redisTemplate.opsForHash().keys(RedisKeys.Ws.Router);
        if (CollectionUtils.isNotEmpty(userIds)) {
            scheduler.schedule(() -> {
                for (Object userId : userIds) {
                    if (!hasWsOnline((Long) userId)) {
                        continue;
                    }

                    socket.pushMsgWithCluster((Long) userId, new SingleIdMsg((Long) userId, MsgType.ANNOUNCEMENT, id));
                }
                pushUnReadMsg(userIds);
            }, 5, TimeUnit.SECONDS);
        }
    }

    @Override
    public void pushTodoMsg(Long userId) {
        scheduler.schedule(() -> socket.pushMsgWithCluster(userId, new SimpleMsg(userId, MsgType.TODO_REFRESH)), 5, TimeUnit.SECONDS);
    }

    @Override
    public void pushDownloadMsg(Long userId) {
        scheduler.schedule(() -> socket.pushMsgWithCluster(userId, new SimpleMsg(userId, MsgType.DOWNLOAD_REFRESH)), 3, TimeUnit.SECONDS);
    }

    @Override
    public void noticeAudit(Long applyId) {
        TgApplicationInfo info = applicationInfoMapper.selectOne(new QueryWrapper<TgApplicationInfo>().lambda()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getHandleIndexMappingJson, TgApplicationInfo::getCurrentIndex)
                .eq(TgApplicationInfo::getId, applyId));

        JsonBeanConverter.convert2Obj(info);

        Set<Long> pushUsers = new HashSet<>();
        Map<Long, Map<String, Integer>> mapping = info.getHandlerIndexMapping();
        for (Map.Entry<Long, Map<String, Integer>> entry : mapping.entrySet()) {
            Long userId = entry.getKey();
            Map<String, Integer> nodes = entry.getValue();
            if (nodes.containsKey(info.getCurrentIndex() - 1 + "") || nodes.containsKey(info.getCurrentIndex() + "")) {
                pushUsers.add(userId);
            }
        }

        this.pushTodoMsg(pushUsers, applyId);

    }

    @Override
    public void pushTodoMsg(Collection<Long> userIds, Long applyId) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        scheduler.schedule(() -> {
            for (Long id : userIds) {
                if (!hasWsOnline(id)) {
                    continue;
                }
                socket.pushMsgWithCluster(id, new SingleIdMsg(id, MsgType.TODO_REFRESH, applyId));
            }
            pushUnReadMsg(userIds);
        }, 5, TimeUnit.SECONDS);

    }

    @Override
    public void pushAssetsMsg(Long assetsId) {
        Set userIds = redisTemplate.opsForHash().keys(RedisKeys.Ws.Router);
        scheduler.schedule(() -> {
            for (Object userId : userIds) {
                Long id = (Long) userId;
                if (!hasWsOnline(id)) {
                    continue;
                }

                socket.pushMsgWithCluster(id, new SingleIdMsg(id, MsgType.ASSETS_UPDATE, assetsId));
            }
            pushUnReadMsg(userIds);
        }, 5, TimeUnit.SECONDS);
    }
}

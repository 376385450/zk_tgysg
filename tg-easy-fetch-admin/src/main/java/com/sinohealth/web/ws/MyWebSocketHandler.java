package com.sinohealth.web.ws;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgDocInfo;
import com.sinohealth.system.domain.TgMessageRecordDim;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.dto.auditprocess.MessagePageDto;
import com.sinohealth.system.mapper.TgDocInfoMapper;
import com.sinohealth.system.mapper.TgMessageRecordDimMapper;
import com.sinohealth.system.mapper.TgTemplateInfoMapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;

/**
 * @Author Rudolph
 * @Date 2022-05-31 11:03
 * @Desc
 */
@Slf4j
//@Component
//@ChannelHandler.Sharable
public class MyWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    /**
     * 未读部分 需要推送的消息
     */
    private static final int PUSHED_MSG_TYPE = 0;
    /**
     * 已读历史消息
     */
    private static final int VIEWED_MSG_TYPE = 1;
    /**
     * 标记全部已读
     */
    private static final int MARK_ALL_MSG_TYPE = 2;

    @Autowired
    private TgMessageRecordDimMapper messageMapper;
    @Autowired
    private TgDocInfoMapper docInfoMapper;
    @Autowired
    private TgTemplateInfoMapper templateInfoMapper;

    @Resource
    @Qualifier(ThreadPoolType.SCHEDULER)
    private ScheduledExecutorService scheduler;

    private final Map<Integer, BiConsumer<ChannelHandlerContext, String>> funcMap = new HashMap<>();

    {
        funcMap.put(PUSHED_MSG_TYPE, this::handlePushMsg);
        funcMap.put(VIEWED_MSG_TYPE, this::handleViewMsg);
        funcMap.put(MARK_ALL_MSG_TYPE, this::markMsgAllRead);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info(">>>>>>>>>>>>>>> 新客户连接: {{" + ctx.channel().id().asLongText() + "}}");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        log.info(">>>>>>>>>>>>>>> | 服务器收到消息: {{" + msg.text() + "}}");

        // 获取用户ID, 关联 channel
        JSONObject jsonObject = JSONUtil.parseObj(msg.text());
        String uid = jsonObject.getStr("uid");
        if (StringUtils.isBlank(uid)) {
            return;
        }

        Integer msgType = Optional.ofNullable(jsonObject.getStr("messageList"))
                .map(Integer::valueOf).orElse(PUSHED_MSG_TYPE);
        Optional.ofNullable(funcMap.get(msgType)).ifPresent(v -> {
            v.accept(ctx, uid);
        });
    }

    // FIXME 分页
    // 获取未查看数据列表
    private void handleViewMsg(ChannelHandlerContext ctx, String uid) {
        List<MessagePageDto> listResult = new ArrayList<>();
        messageMapper.queryMessageListByViewed(uid).forEach((r) -> {
            try {
                MessagePageDto messagePageDto = buildResult(r);
                messagePageDto.setDataType(CommonConstants.MESSAGELIST);
                listResult.add(messagePageDto);
            } catch (NullPointerException e) {
                log.error("", e);
            }
        });
        MessagePageDto result = new MessagePageDto();
        result.setMessageCount(messageMapper.queryMessageCountByViewed(uid));
        result.setDataType(CommonConstants.MESSAGELIST);
        result.setMessageList(listResult);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(result)));
    }

    private void markMsgAllRead(ChannelHandlerContext ctx, String uid) {
        log.info("markAllRead uid={}", uid);
        messageMapper.markMsgAllRead(uid);
    }

    // 获取数据弹窗
    private void handlePushMsg(ChannelHandlerContext ctx, String uid) {
        addUserContext(ctx, uid);
        // TODO 主动推送 降低时延，需要独立ws服务 便于应用集群化
        TgMessageRecordDim record = messageMapper.queryOneMessageByPushed(uid);
        if (ObjectUtils.isNotNull(record)) {
            MessagePageDto result = buildResult(record);
            result.setDataType(CommonConstants.POPUPWINDOW);
            result.setMessageCount(messageMapper.queryMessageCountByViewed(uid));
            ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(result)));
            record.setPushed(CommonConstants.PUSHED);
            messageMapper.updateById(record);
        } else {
            MessagePageDto result = new MessagePageDto();
            result.setDataType(CommonConstants.MESSAGECOUNT);
            result.setMessageCount(messageMapper.queryMessageCountByViewed(uid));
            ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(result)));
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        log.info(">>>>>>>>>>>>>>> | 用户下线: {{" + ctx.channel().id().asLongText() + "}}");
        removeUserContext(ctx);
    }

    private Channel getUserContext(String uid) {
        return NettyConfig.getChannelMap().get(uid);
    }

    private void addUserContext(ChannelHandlerContext ctx, String uid) {
        // 将用户ID作为自定义属性加入到 channel中, 方便随时 channel 中获取用户 ID
        AttributeKey<String> key = AttributeKey.valueOf("userId");
        ctx.channel().attr(key).setIfAbsent(uid);
        NettyConfig.getChannelMap().putIfAbsent(uid, ctx.channel());
    }

    private void removeUserContext(ChannelHandlerContext ctx) {
        NettyConfig.getChannelGroup().remove(ctx.channel());
        AttributeKey<String> key = AttributeKey.valueOf("userId");
        String userId = ctx.channel().attr(key).get();
        NettyConfig.getChannelMap().remove(userId);
    }

    private String getUid(String uri) {
        return Arrays.stream(uri.split("/")).reduce((f, s) -> s).orElse(InfoConstants.UID_REQUIREMENT);
    }

    /**
     * 1、 提数的websocket添加申请模板名称和项目名称
     * 2、 文档的websocket添加文档名称到项目名称里
     */
    private MessagePageDto buildResult(TgMessageRecordDim record) {
        MessagePageDto result = new MessagePageDto();
        result.setRid(record.getId());
        result.setApplicationId(record.getApplicationId());
        result.setApplicantName(record.getApplicantName());
        result.setApplicationType(record.getApplicationType());
        result.setCreateTime(record.getApplyTime());

        TgApplicationInfo tgApplicationInfo = new TgApplicationInfo().selectById(record.getApplicationId());
        if (ApplicationConst.ApplicationType.DATA_APPLICATION.equals(record.getApplicationType())) {
            if (Objects.isNull(tgApplicationInfo)) {
                result.setProjectName("");
                result.setTemplateName("");
            } else {
                result.setProjectName(tgApplicationInfo.getProjectName());
                TgTemplateInfo tgTemplateInfo = templateInfoMapper.selectById(tgApplicationInfo.getTemplateId());
                result.setTemplateName(Optional.ofNullable(tgTemplateInfo).map(TgTemplateInfo::getTemplateName).orElse(""));
            }
        } else if (ApplicationConst.ApplicationType.DOC_APPLICATION.equals(record.getApplicationType())) {
            Optional.ofNullable(tgApplicationInfo).map(TgApplicationInfo::getDocId)
                    .map(docInfoMapper::selectById).map(TgDocInfo::getName)
                    .ifPresent(result::setProjectName);
        } else if (ApplicationConst.ApplicationType.TABLE_APPLICATION.equals(record.getApplicationType())) {
            Optional.ofNullable(tgApplicationInfo).map(TgApplicationInfo::getBaseTableName).ifPresent(result::setProjectName);
        }

        boolean applyUser = Optional.ofNullable(tgApplicationInfo).map(TgApplicationInfo::getApplicantId)
                .map(v -> Objects.equals(v, record.getAdviceWho())).orElse(false);
        final String projectName = result.getProjectName();
        boolean confirm = false;
        if (ObjectUtils.isNull(record.getTitle(), record.getContent())) {
            if (record.getType().equals(ApplicationConst.AuditStatus.AUDITING)) {
                result.setTitle("您有一条申请待审核");
                result.setHtmlTitle("<p>\n您有一条名为\"<span style=\"color:red\">" + projectName + "\"</span>的申请待审核\n</p>");
            } else if (record.getType().equals(ApplicationConst.AuditStatus.AUDIT_PASS)) {
                result.setTitle("您有一条申请审核通过");
                result.setHtmlTitle("<p>\n您有一条名为\"<span style=\"color:red\">" + projectName + "\"</span>的申请审核通过\n</p>");
            } else if (record.getType().equals(ApplicationConst.AuditStatus.AUDIT_FAIL)) {
                result.setTitle("您有一条申请审核驳回");
                result.setHtmlTitle("<p>\n您有一条名为\"<span style=\"color:red\">" + projectName + "\"</span>的申请审核驳回\n</p>");
            }

            if (Objects.equals(record.getDataState(), ApplyDataStateEnum.wait_confirm.name())) {
                result.setTitle("您审批的模板申请待确认");
                confirm = true;
                result.setHtmlTitle("<p>\n您审批的一条名为\"<span style=\"color:red\">" + projectName + "\"</span>的申请待确认\n</p>");
            } else if (Objects.equals(record.getDataState(), ApplyDataStateEnum.success.name())) {
                if (applyUser) {
                    result.setTitle("您的模板申请执行成功");
                    result.setHtmlTitle("<p>\n您申请的一条名为\"<span style=\"color:red\">" + projectName + "\"</span>执行成功\n</p>");
                } else {
                    result.setTitle("您审批的模板申请执行成功");
                    result.setHtmlTitle("<p>\n您审批的一条名为\"<span style=\"color:red\">" + projectName + "\"</span>的申请执行成功\n</p>");
                }
            } else if (Objects.equals(record.getDataState(), ApplyDataStateEnum.fail.name())) {
                if (applyUser) {
                    result.setTitle("您的模板申请执行失败");
                    result.setHtmlTitle("<p>\n您申请的一条名为\"<span style=\"color:red\">" + projectName + "\"</span>执行失败\n</p>");
                } else {
                    result.setTitle("您审批的模板申请执行失败");
                    result.setHtmlTitle("<p>\n您审批的一条名为\"<span style=\"color:red\">" + projectName + "\"</span>的申请执行失败\n</p>");
                }
            }

        } else {
            result.setTitle(record.getTitle());
            result.setContent(record.getContent());
            String html = String.format("<p>%s</p><p>%s</p>", record.getTitle(), record.getContent());
            result.setHtmlTitle(html);
        }

        if (confirm) {
            result.setType(ApplicationConst.AuditStatus.AUDITING);
        } else {
            result.setType(record.getType());
        }
        log.info("MyWebSocketHandler 数据打印: {}", JSON.toJSONString(result));
        return result;
    }
}

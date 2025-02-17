package com.sinohealth.system.service.impl;

import cn.hutool.core.io.IoUtil;
import com.sinohealth.common.core.mail.EmailDefaultHandler;
import com.sinohealth.common.module.file.dto.HuaweiPath;
import com.sinohealth.saas.file.api.rest.FileApi;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.config.FileProperties;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.dao.DataDescriptionDAO;
import com.sinohealth.system.domain.TgDataDescription;
import com.sinohealth.system.domain.constant.AsyncTaskConst;
import com.sinohealth.system.domain.value.deliver.*;
import com.sinohealth.system.domain.value.deliver.datasource.ApplicationDataSource;
import com.sinohealth.system.domain.value.deliver.resource.ZipResource;
import com.sinohealth.system.domain.value.deliver.sink.HttpServletResponseResourceSink;
import com.sinohealth.system.domain.value.deliver.sink.ObsResourceSink;
import com.sinohealth.system.domain.value.deliver.strategy.CompositeDeliverStrategy;
import com.sinohealth.system.domain.value.deliver.strategy.PackCompositeDeliverStrategy;
import com.sinohealth.system.dto.application.deliver.request.DeliverEmailTemplateUpdateRequestDTO;
import com.sinohealth.system.dto.application.deliver.*;
import com.sinohealth.system.dto.application.deliver.request.*;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.service.DataDeliverRecordService;
import com.sinohealth.system.service.DataDeliveryService;
import com.sinohealth.system.service.DeliverEmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 21:25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataDeliveryServiceImpl implements DataDeliveryService {

    private final PackCompositeDeliverStrategy packCompositeDeliverStrategy;

    private final CompositeDeliverStrategy compositeDeliverStrategy;

    private final DataDeliverRecordService dataDeliverRecordService;

    private final DeliverEmailTemplateService deliverEmailTemplateService;

    private final DataDescriptionDAO dataDescriptionDAO;

    private final TgApplicationInfoMapper applicationInfoMapper;

    private final UserDataAssetsDAO userDataAssetsDAO;

    @Autowired
    private FileApi fileApi;
    @Value("${spring.mail.username:tech@sinohealth.cn}")
    private String emailFrom;

    @Autowired
    private FileProperties fileProperties;


    @Override
    public HuaweiPath deliverExcel(ApplicationDeliverExcelRequest request) throws Exception {
        DeliverRequestContextHolder requestContextHolder = DeliverRequestContextHolder.build(request, DeliverResourceType.EXCEL);
        requestContextHolder.setSinkType(AsyncTaskConst.SINK_TYPE.OBS_SINK);
        dataDeliverRecordService.saveDownloadRecords(requestContextHolder);
        return doDeliver(requestContextHolder);
    }

    @Override
    public HuaweiPath deliverCsv(ApplicationDeliverCsvRequest request) throws Exception {
        DeliverRequestContextHolder requestContextHolder = DeliverRequestContextHolder.build(request, DeliverResourceType.CSV);
        requestContextHolder.setSinkType(AsyncTaskConst.SINK_TYPE.OBS_SINK);
        dataDeliverRecordService.saveDownloadRecords(requestContextHolder);
        return doDeliver(requestContextHolder);
    }

    @Override
    public void deliverPdf(ApplicationDeliverPdfRequest request) throws Exception {
        DeliverRequestContextHolder requestContextHolder = DeliverRequestContextHolder.build(request, DeliverResourceType.PDF);
        requestContextHolder.setSinkType(AsyncTaskConst.SINK_TYPE.SERVLET_SINK);
        dataDeliverRecordService.saveDownloadRecords(requestContextHolder);
        doDeliver(requestContextHolder);
    }

    @Override
    public void deliverImage(ApplicationDeliverImageRequest request) throws Exception {
        DeliverRequestContextHolder requestContextHolder = DeliverRequestContextHolder.build(request, DeliverResourceType.IMAGE);
        requestContextHolder.setSinkType(AsyncTaskConst.SINK_TYPE.SERVLET_SINK);
        dataDeliverRecordService.saveDownloadRecords(requestContextHolder);
        doDeliver(requestContextHolder);
    }

    /**
     * 异步发送邮件
     *
     * @param request
     * @throws Exception
     */
    @Override
    @Async(ThreadPoolType.ENHANCED_TTL)
    public void deliverEmail(ApplicationDeliverEmailRequest request) throws Exception {
        DeliverRequestContextHolder requestContextHolder = DeliverRequestContextHolder.build(request, DeliverResourceType.EXCEL);
        // 保存交付邮件记录， 先保存记录
        dataDeliverRecordService.saveSendEmailRecords(request, requestContextHolder);
        // 生成邮件附件，邮件附件先用excel文件
        Resource resource = generateDeliverResource(requestContextHolder);
        DiskFile tmpFile = DiskFile.createTmpFile(resource.getName());
        IoUtil.copy(resource.getInputStream(), new FileOutputStream(tmpFile.getFile()));
        // 发送邮件，奇怪的用法
        EmailDefaultHandler.setEmailFrom(new InternetAddress(emailFrom, "天宫易数阁平台", "UTF-8").toString());
        EmailDefaultHandler emailDefaultHandler = new EmailDefaultHandler();
        emailDefaultHandler.send(request.getEmailReceivers(), request.getEmailTitle(), request.getEmailBody(), Arrays.asList(tmpFile.getFile()));
        // 更新邮件发送模板
        deliverEmailTemplateService.updateTemplate(new DeliverEmailTemplateUpdateRequestDTO()
                .setNodeIds(request.getNodeIds())
                .setAssetsId(request.getAssetsId())
                .setTitle(request.getEmailTitle())
                .setContent(request.getEmailBody())
                .setReceiveMails(request.getEmailReceivers())
        );
    }

    @Override
    public ApplicationDataDescDocVerifyDTO verifyDataDescription(ApplicationDataDescDocVerifyRequest request) {
        DeliverRequestContextHolder requestContextHolder = DeliverRequestContextHolder.build(new DeliverPackBaseReq(request), null);
        if (CollectionUtils.isEmpty(requestContextHolder.getApplicationDataSources())) {
            return new ApplicationDataDescDocVerifyDTO();
        }
        List<Long> assetsIds = requestContextHolder.getApplicationDataSources().stream().map(ApplicationDataSource::getAssetsId)
                .distinct().collect(Collectors.toList());
        Map<Long, UserDataAssets> applicationInfoMap = userDataAssetsDAO.getBaseMapper().selectBatchIds(assetsIds).stream()
                .collect(Collectors.toMap(UserDataAssets::getId, Function.identity()));
        Map<Long, TgDataDescription> descriptionMap = dataDescriptionDAO.listByAssetsIds(assetsIds).stream()
                .collect(Collectors.toMap(TgDataDescription::getAssetsId, Function.identity()));
        List<Long> dissatisfyApplyIds = new ArrayList<>();
        List<String> dissatisfyApplyNames = new ArrayList<>();
        assetsIds.stream()
                .filter(assetsId -> !descriptionMap.containsKey(assetsId))
                .forEach(assetsId -> {
                    String projectName = applicationInfoMap.get(assetsId).getProjectName();
                    dissatisfyApplyNames.add(projectName);
                    dissatisfyApplyIds.add(assetsId);
                });
        return new ApplicationDataDescDocVerifyDTO()
                .setApplyIds(dissatisfyApplyIds)
                .setApplyNames(dissatisfyApplyNames);
    }


    /**
     * 使用http响应流同步处理
     * 打包支持多个文件
     * 单份只支持一个文件
     */
    private HuaweiPath doDeliver(DeliverRequestContextHolder requestContextHolder) throws Exception {
        // 校验
        requestContextHolder.checkTypeThrows();
        HuaweiPath huaweiPath = null;
        // 交付
        List<DataSource> dataSources = requestContextHolder.getDataSources();
        if (requestContextHolder.getPack()) {
            ZipResource zipResource = packCompositeDeliverStrategy.deliver(requestContextHolder);
            if (requestContextHolder.getSinkType() != null && requestContextHolder.getSinkType() == AsyncTaskConst.SINK_TYPE.OBS_SINK) {
                ObsResourceSink sink = new ObsResourceSink(zipResource, requestContextHolder.getType(), fileApi, fileProperties);
                huaweiPath = sink.process();
            } else {
                HttpServletResponseResourceSink sink = new HttpServletResponseResourceSink();
                sink.setType(DeliverResourceType.ZIP);
                sink.setResource(zipResource);
                sink.process();
            }
        } else if (dataSources.size() == 1) {
            List<Resource> resources = compositeDeliverStrategy.deliver(dataSources, requestContextHolder.getType());
            // 可能报错了，导致没有文件生成
            if (resources.size() == 1) {
                Resource resource = resources.get(0);
                if (requestContextHolder.getSinkType() != null && requestContextHolder.getSinkType() == AsyncTaskConst.SINK_TYPE.OBS_SINK) {
                    ObsResourceSink sink = new ObsResourceSink(resource, requestContextHolder.getType(), fileApi, fileProperties);
                    huaweiPath = sink.process();
                } else {
                    HttpServletResponseResourceSink sink = new HttpServletResponseResourceSink();
                    sink.setType(requestContextHolder.getType());
                    sink.setResource(resource);
                    sink.process();
                }
            } else {
                HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
                response.setContentType("text/html; charset=UTF-8");
                response.sendError(230, "文件导出失败");
            }
        } else {
            // 单份超过一个文件
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            response.setContentType("text/html; charset=UTF-8");
            response.sendError(231, "单份导出超过一个文件，请检查");
        }
        return huaweiPath;
    }

    /**
     * 返回导出的资源文件
     *
     * @param requestContextHolder
     * @return
     */
    private Resource generateDeliverResource(DeliverRequestContextHolder requestContextHolder) throws Exception {
        // 校验
        requestContextHolder.checkTypeThrows();
        // 交付
        List<DataSource> dataSources = requestContextHolder.getDataSources();
        if (requestContextHolder.getPack()) {
            ZipResource zipResource = packCompositeDeliverStrategy.deliver(requestContextHolder);
            return zipResource;
        } else if (dataSources.size() == 1) {
            List<Resource> resources = compositeDeliverStrategy.deliver(dataSources, requestContextHolder.getType());
            // 可能报错了，导致没有文件生成
            if (resources.size() == 1) {
                Resource resource = resources.get(0);
                return resource;
            } else {
                throw new IllegalArgumentException("导出文件失败");
            }
        } else {
            throw new IllegalArgumentException("多个文件必须打包");
        }
    }

}

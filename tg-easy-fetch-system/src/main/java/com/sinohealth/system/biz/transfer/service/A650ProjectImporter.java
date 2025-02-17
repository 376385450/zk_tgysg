package com.sinohealth.system.biz.transfer.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.sinohealth.system.biz.project.mapper.ProjectMapper;
import com.sinohealth.system.biz.transfer.dto.CrApplyVO;
import com.sinohealth.system.biz.transfer.dto.CrInCompleteCustomApplyVO;
import com.sinohealth.system.biz.transfer.dto.CrProjectVO;
import com.sinohealth.system.biz.transfer.listener.CrApplyListener;
import com.sinohealth.system.biz.transfer.listener.CrCustomFlowApplyListener;
import com.sinohealth.system.biz.transfer.listener.CrFlowApplyListener;
import com.sinohealth.system.biz.transfer.listener.CrInCompleteCustomListener;
import com.sinohealth.system.biz.transfer.listener.CrProjectListener;
import com.sinohealth.system.biz.transfer.listener.CrRangeApplyListener;
import com.sinohealth.system.mapper.CustomerMapper;
import com.sinohealth.system.mapper.ProjectHelperMapper;
import com.sinohealth.system.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-03-08 11:35
 */
@Slf4j
@Service
public class A650ProjectImporter {


    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private ProjectHelperMapper projectHelperMapper;
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;
    @Autowired
    private Validator validator;


    /**
     * 项目配置
     */
    public void parseProject(MultipartFile file) {
        ExcelReader reader = null;
        try {
            reader = EasyExcel.read(file.getInputStream()).build();
            ReadSheet readSheet1 = EasyExcel.readSheet(1).head(CrProjectVO.class).registerReadListener(
                    new CrProjectListener(projectMapper, customerMapper, sysUserMapper, projectHelperMapper,
                            dataSourceTransactionManager, transactionDefinition, validator)
            ).build();

            // 同时读多个sheet
//            ReadSheet readSheet2 =
//                    EasyExcel.readSheet(1).head(CrProjectVO.class).registerReadListener(new CrProjectListener()).build();
            // 这里注意 一定要把sheet1 sheet2 一起传进去，不然有个问题就是03版的excel 会读取多次，浪费性能
//            reader.read(readSheet1, readSheet2);
            reader.read(readSheet1);
        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (Objects.nonNull(reader)) {
                // 释放 读取文件产生的临时文件
                reader.finish();
            }
        }
    }

    /**
     * 宽表
     */
    public void parseWideApply(MultipartFile file, HttpServletRequest request) {

        //TODO 打包配置
    }

    /**
     * 工作流常规
     */
    public void parseFlowApply(MultipartFile file, HttpServletRequest request) {
        ExcelReader reader = null;
        try {
            reader = EasyExcel.read(file.getInputStream()).build();
            CrApplyListener readListener = new CrFlowApplyListener(request,
                    dataSourceTransactionManager, transactionDefinition, validator);
            ReadSheet s = EasyExcel.readSheet(2).head(CrApplyVO.class).registerReadListener(readListener).build();
            reader.read(s);
        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (Objects.nonNull(reader)) {
                reader.finish();
            }
        }
    }


    /**
     * 宽表 自定义
     */
//    @Deprecated
//    public void parseCustomApply(MultipartFile file, HttpServletRequest request) {
//        ExcelReader reader = null;
//        try {
//            reader = EasyExcel.read(file.getInputStream()).build();
//            CrApplyListener readListener = new CrCustomWideApplyListener(request,
//                    dataSourceTransactionManager, transactionDefinition, validator);
//            ReadSheet s = EasyExcel.readSheet(5).head(CrApplyVO.class).registerReadListener(readListener).build();
//            reader.read(s);
//        } catch (Exception e) {
//            log.error("", e);
//        } finally {
//            if (Objects.nonNull(reader)) {
//                reader.finish();
//            }
//        }
//    }

    /**
     * 自定义常规 工作流
     */
    public void parseRangeApply(MultipartFile file, HttpServletRequest request) {
        ExcelReader reader = null;
        try {
            reader = EasyExcel.read(file.getInputStream()).build();
            CrApplyListener readListener = new CrRangeApplyListener(request,
                    dataSourceTransactionManager, transactionDefinition, validator);
            ReadSheet s = EasyExcel.readSheet(3).head(CrApplyVO.class).registerReadListener(readListener).build();
            reader.read(s);
        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (Objects.nonNull(reader)) {
                reader.finish();
            }
        }
    }

    /**
     * 通用 无明细 工作流
     */
    public void parseInCompleteCustomApply(MultipartFile file, HttpServletRequest request) {
        CrInCompleteCustomListener listener = new CrInCompleteCustomListener(dataSourceTransactionManager,
                transactionDefinition, validator, request);
        this.commonParse(6, file, listener, CrInCompleteCustomApplyVO.class);
    }

    /**
     * 通用 工作流
     */
    public void parseCustomFlowApply(MultipartFile file, HttpServletRequest request) {
//        String sheetNo = request.getParameter("sheetNo");
//        Integer no = Optional.ofNullable(sheetNo).filter(StringUtils::isNoneBlank).map(Integer::parseInt).orElse(7);
        CrCustomFlowApplyListener listener = new CrCustomFlowApplyListener(request, dataSourceTransactionManager,
                transactionDefinition, validator);
        this.commonParseByName("通用", file, listener, CrApplyVO.class);
    }

    /**
     * @param sheetNo 1开始
     */
    public <T> void commonParse(Integer sheetNo, MultipartFile file, ReadListener<T> listener, Class<T> clazz) {
        ExcelReader reader = null;
        try {
            reader = EasyExcel.read(file.getInputStream()).build();
            ReadSheet sheet = EasyExcel.readSheet(sheetNo).head(clazz).registerReadListener(listener).build();
            log.info("NAME={}", sheet.getSheetName());
            reader.read(sheet);
        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (Objects.nonNull(reader)) {
                reader.finish();
            }
        }
    }

    public <T> void commonParseByName(String sheetName, MultipartFile file, ReadListener<T> listener, Class<T> clazz) {
        ExcelReader reader = null;
        try {
            reader = EasyExcel.read(file.getInputStream()).build();
            ReadSheet sheet = EasyExcel.readSheet(sheetName).head(clazz).registerReadListener(listener).build();
            log.info("NAME={}", sheet.getSheetName());
            reader.read(sheet);
        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (Objects.nonNull(reader)) {
                reader.finish();
            }
        }
    }

}

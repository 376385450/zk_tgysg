package com.sinohealth.system.domain.constant;

import com.sinohealth.system.biz.dataassets.service.AssetsUpgradeTriggerService;
import com.sinohealth.system.domain.FastEntry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-02-13 20:35
 */
public abstract class DataDirConst {

    /**
     * V1.8.2: 产品要求新增的排序值默为0 即最前面
     */
    public static final int DEFAULT_SORT = 0;

    public interface Status {
        int DELETE = 0;
        int ENABLE = 1;
        int DISABLE = 2;
    }

    public static final long TOP_PARENT_LEVEL = 0L;

    public interface PreviewPermissionType {
        int NONE = 1;
        int PREVIEW = 2;
        int MANAGER = 3;
    }

    public interface DocPermission {
        /**
         * 预览PDF
         */
        int CAN_VIEW_PDF = 101;
        /**
         * 下载PDF
         */
        int CAN_DOWNLOAD_PDF = 102;
        /**
         * 下载源文件
         */
        int CAN_DOWNLOAD_SRC = 103;
        /**
         * 需要阅读申请
         */
        int NEED_AUDIT = 104;

        List<Integer> ALL_PERMISSION = Arrays.asList(CAN_VIEW_PDF, CAN_DOWNLOAD_PDF);

//        业务方:不提供下载源文件功能
//        List<Integer> ALL_PERMISSION = Arrays.asList(CAN_VIEW_PDF, CAN_DOWNLOAD_PDF, CAN_DOWNLOAD_SRC);
    }

    public interface TablePermission {
        /**
         * 提数申请
         */
        int APPLICATION = 201;
        /**
         * 数据预览
         */
        int DATA_ASSERTS = 202;

        /**
         * 提数申请对应  我的数据
         */
        int APPLICATION_DATA = 203;
    }

    public interface TemplatePermission {
        /**
         * 申请
         */
        int APPLICATION = 301;
        /**
         * 我的数据
         */
        int APPLICATION_DATA = 302;

        List<Integer> NOT = Collections.singletonList(APPLICATION);
        List<Integer> HAVE = Arrays.asList(APPLICATION, APPLICATION_DATA);
    }

    /**
     * 资产树 操作类别
     */
    public interface ActionType {
        /**
         * 预览
         */
        int PREVIEW = 1;
        /**
         * 交付客户
         */
        int DELIVER = 2;
        /**
         * 更新 交付给客户的数据
         */
        int UPDATE = 3;
        /**
         * 说明文档
         */
        int DOC = 4;
        /**
         * 交付记录
         */
        int DELIVER_RECORD = 5;
        /**
         * 重新申请
         */
        int RE_APPLY = 6;
        /**
         * 删除资产
         */
        int DELETE = 7;
        /**
         * 手动更新资产
         *
         * @see AssetsUpgradeTriggerService#manualUpgrade
         */
        int MANUAL_UPGRADE = 8;
        /**
         * 作废资产 可使用的资产
         */
        int DEPRECATED = 9;

        /**
         * 图表 预览
         */
        int BI_PREVIEW = 101;
        /**
         * 图表 编辑
         */
        int BI_EDIT = 102;
        /**
         * 图表 复制
         */
        int BI_COPY = 103;
        /**
         * 删除
         */
        int BI_DELETE = 104;
        /**
         * 交付客户
         */
        int BI_DELIVER = 105;

        /**
         * 项目管理
         */
        int PROJECT_MANAGER = 201;

        /**
         * 文件预览
         */
        int FILE_PREVIEW = 301;
        /**
         * 文件下载
         */
        int FILE_DOWNLOAD = 302;
        /**
         * 文件删除
         */
        int FILE_DELETE = 303;

        List<Integer> PROJECT_ACTIONS = Collections.singletonList(PROJECT_MANAGER);

        // 验收记录按钮由前端控制
        List<Integer> BI_DEFAULT_ACTIONS = Arrays.asList(BI_PREVIEW, BI_EDIT, BI_DELETE, BI_COPY, BI_DELIVER);
    }

    public static List<FastEntry> DEFAULT_LIST = Arrays.asList(
            new FastEntry("DataManagement/mapDirectory", "资产地图", "UserHomePage_34", 0),
            new FastEntry("MyData", "我的数据", "UserHomePage_35", 1),
            new FastEntry("MyApplication", "我的申请", "UserHomePage_36", 2)
    );

    public static List<FastEntry> DATA_ASSET_DEFAULT_LIST = Arrays.asList(
            new FastEntry("AssetDirectory", "资产目录", "UserHomePage_34", 0),
            new FastEntry("MyData", "我的数据", "UserHomePage_35", 1),
            new FastEntry("MyApplication", "我的申请", "UserHomePage_36", 2)
    );

}

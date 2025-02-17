package com.sinohealth.system.domain.constant;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2023-03-09 3:32 下午
 */
public class AsyncTaskConst {
    public interface Type {
        /**
         * 下载CSV
         */
        int CSV = 3;

        /**
         * 下载EXCEL
         */
        int EXCEL = 4;

        /**
         * zip
         */
        int ZIP = 5;
    }

    public interface BUSINESS_TYPE {
        /**
         * 客户下载表单
         */
        int FORM = 1;

        /**
         * 客户下载图标
         */
        int CHART = 2;

        /**
         * 交付CSV
         */
        int DELIVERY_CSV = 3;

        /**
         * 交付EXCEL
         */
        int DELIVERY_EXCEL = 4;

        /**
         * 分配资产表
         */
        int DELIVERY_TABLE = 5;
    }

    public interface Status {
        /**
         * 成功
         */
        int SUCCEED = 0;

        /**
         * 进行中
         */
        int HANGING = 1;

        /**
         * 失败
         */
        int FAILED = 2;


    }

    public interface FLOW_STATUS {
        /**
         * 创建
         */
        int CREATED = 0;

        /**
         * 已执行
         */
        int EXECUTED = 1;

    }


    public interface DEL_FLAG {
        /**
         * 已删除
         */
        int DELETED = 1;

        /**
         * 未删除
         */
        int NORMAL = 0;
    }

    public interface ReadFlag{
        int READ = 1;
        int NO_READ = 0;
    }

    public interface SINK_TYPE {
        int SERVLET_SINK = 0;

        int OBS_SINK = 1;
    }
}

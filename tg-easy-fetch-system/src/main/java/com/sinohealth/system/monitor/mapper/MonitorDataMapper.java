package com.sinohealth.system.monitor.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-13 4:40 下午
 */
public interface MonitorDataMapper {

    /**
     * 地图目录——查看人数PV
     * @param tableId
     * @return
     */
    @Select("select count(*) pv from `event_log` where `operate_type` = 'QUERY' and `subject_id` = #{tableId} and `subject_type` = 'map' and `second_subject_type` = 'map_table_view'")
    Integer getTableViewPv(Long tableId);

    /**
     * 地图目录——查看人数UV
     * @param tableId
     * @return
     */
    @Select("select count(distinct `user_id`) uv from `event_log` where `operate_type` = 'QUERY' and `subject_id` = #{tableId} and `subject_type` = 'map' and `second_subject_type` = 'map_table_view'")
    Integer getTableViewUv(Long tableId);

    /**
     * 地图目录——查看人数近30天趋势数据
     * @param tableId
     * @return
     */
    @Select("select count(*) as pv, count(distinct user_id) as uv,log_date from `event_log` where `operate_type` = 'QUERY' and `subject_id` = #{tableId} and `subject_type` = 'map' and `second_subject_type` = 'map_table_view' and log_date >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) group by log_date")
    List<Map<String, Object>> getTableViewTrend(@Param("tableId") Long tableId, @Param("days") Integer days);


    /**
     * 发放数据——提数申请成功次数
     * @param tableId
     * @return
     */
    @Select("select count(*) as total from tg_application_info where current_audit_process_status = 2 and base_table_id = #{tableId}")
    Integer getApplicationTotal(Long tableId);

    /**
     * 发放数据——提数申请近30天成功次数
     * @param tableId
     * @return
     */
    @Select("select count(*) as total from tg_application_info where current_audit_process_status = 2 and base_table_id = #{tableId} and apply_passed_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)")
    Integer getApplicationDays(@Param("tableId") Long tableId, @Param("days") Integer days);

    /**
     * 发放数据——提数申请成功且未过期
     * @param tableId
     * @return
     */
    @Select("select count(*) as total from tg_application_info where current_audit_process_status = 2 and base_table_id = #{tableId} and data_expir > now()")
    Integer getApplicationEffect(Long tableId);

    /**
     * 客户数据——交付客户
     * @param tableId
     * @return
     */
    @Select("select count(*) from tg_application_info a inner join tg_customer_apply_auth b on a.id = b.apply_id where a.base_table_id = #{tableId} ")
    Integer getCustomerTableApply(Long tableId);

    /**
     * 客户数据——表单查看次数
     * @param tableId
     * @return
     */
    @Select("select count(*) as total from event_log where subject_id = #{tableId} and event_type = 'OPERATE' and operate_type = 'QUERY' and `subject_type` = 'customer' and `second_subject_type` = 'customer_table_view'")
    Integer getCustomerTableView(Long tableId);

    /**
     * 客户数据——资产查看次数
     * @param authIds
     * @return
     */
    List<Map<String, Object>> groupByCustomerAuthView(@Param("authIds") List<Long> authIds, @Param("userId") Long userId);

    /**
     * 客户数据——表单下载次数
     * @param tableId
     * @return
     */
    @Select("select count(*) as total from event_log where subject_id = #{tableId} and event_type = 'OPERATE' and operate_type = 'QUERY' and `subject_type` = 'customer' and `second_subject_type` = 'customer_table_download'")
    Integer getCustomerTableDownload(Long tableId);

    @Select({
            "<script>",
            "select subject_id as applyId, count(*) as total" +
                    "        from event_log" +
                    "        where subject_id in <foreach collection='applyIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>" +
                    "        and event_type = 'OPERATE' and operate_type = 'QUERY' and `subject_type` = 'customer' and `second_subject_type` = 'customer_apply_download'" +
                    "        group by subject_id",
            "</script>"
    })
    List<Map<String, Object>> groupByApplyDownloadView(@Param("applyIds") List<Long> applyIds);

    /**
     * 客户数据——表单查看次数近30天趋势
     * @param tableId
     * @return
     */
    @Select("select count(*) as total, log_date from event_log where subject_id = #{tableId} and event_type = 'OPERATE' and operate_type = 'QUERY' and `subject_type` = 'customer' and `second_subject_type` = 'customer_table_view' and log_date >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) group by log_date")
    List<Map<String, Object>> getCustomerTableViewTrend(@Param("tableId") Long tableId, @Param("days") Integer days);

    /**
     * 客户数据——表单下载次数近30天趋势
     * @param tableId
     * @return
     */
    @Select("select count(*) as total, log_date from event_log where subject_id = #{tableId} and event_type = 'OPERATE' and operate_type = 'QUERY' and `subject_type` = 'customer' and `second_subject_type` = 'customer_table_download' and log_date >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) group by log_date")
    List<Map<String, Object>> getCustomerTableDownloadTrend(@Param("tableId") Long tableId, @Param("days") Integer days);
}

package com.sinohealth.system.biz.ck.constant;

import com.sinohealth.common.utils.StringUtils;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-07-01 19:05
 */
public interface CkErrorMsg {

    /**
     * 棘手的问题，目前评估为客户端问题，遇到此报错需要重试 注意SQL的幂等性
     * <pre>
     *     com.sinohealth.system.service.impl.ApplicationServiceImpl:org.springframework.jdbc.BadSqlGrammarException
     *     StatementCallback; bad SQL grammar [SELECT
     * FROM ( SELECT  FROM tg_assets_wd_qgym_1159_20240701184254_snap ) t LIMIT 30 OFFSET 0];
     * nested exception is java.sql.SQLException: The target server failed to respond,
     * server ClickHouseNode [uri=http://192.168.56.56:8123/tgysg, options={socket_timeout=140000}]@-1382682879
     * </pre>
     */
    String NO_RESPOND = "target server failed to respond";

    String BAD_GRAMMAR = " bad SQL grammar ";

    String SYNTAX_ERROR = "SYNTAX_ERROR";


    /**
     * 是否重试
     *
     * @param msg 错误信息
     */
    static boolean needRetry(String msg) {
        // SQL错误，没有重试必要
        boolean syntax = StringUtils.contains(msg, CkErrorMsg.SYNTAX_ERROR)
                || StringUtils.contains(msg, CkErrorMsg.BAD_GRAMMAR);
        boolean noResp = StringUtils.contains(msg, CkErrorMsg.NO_RESPOND);
        return noResp || !syntax;
    }

}

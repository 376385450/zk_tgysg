package com.sinohealth.system.acl;

import java.io.InputStream;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-30 10:06
 */
public interface ArkbiRepository {

    InputStream getPdf(String extAnalysisId);

    InputStream getImage(String extAnalysisId);

    InputStream getExcel(String extAnalysisId);

    InputStream getCsv(String extAnalysisId);
}

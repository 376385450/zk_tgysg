package com.sinohealth.api.common;

import com.sinohealth.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface CaptchaApi {

    @GetMapping("/captchaImage")
    AjaxResult getCode(HttpServletResponse response) throws IOException;
}

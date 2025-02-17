package com.sinohealth.api.common;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.dto.common.OpenApiRequestDTO;
import com.sinohealth.system.dto.common.OpenApiResponseDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(value = "OpenApi控制器", tags = {"OpenApi管理"})
@RequestMapping("/api/openapi")
public interface OpenApiApi {

    @ApiOperation("获取API接口域名")
    @GetMapping("/getDomainName")
    AjaxResult<String> getDomainName();


    @RequestMapping("/**")
    OpenApiResponseDTO openApi(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam(value = "page", required = false) Long page,
                                      @RequestParam(value = "size", required = false) Long size,
                                      @RequestParam(value = "token", required = false) String token,
                                      @RequestBody(required = false) OpenApiRequestDTO requestDTO);
}

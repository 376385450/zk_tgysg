package com.sinohealth.framework.interceptor;

import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.ServletUtils;
import com.sinohealth.system.async.AsyncManager;
import com.sinohealth.system.async.factory.AsyncFactory;
import com.sinohealth.system.domain.SysUserTable;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.ITableInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.sinohealth.common.core.page.TableSupport.PAGE_NUM;
import static com.sinohealth.common.core.page.TableSupport.PAGE_SIZE;

/**
 * @author Jingjun
 * @since 2021/5/11
 */
@Component
@Slf4j
public class DataInterceptor implements HandlerInterceptor {

    private static String TABLE_ID = "tableId";
    private static String DIR_ID = "dirId";

    @Autowired
    private ISysUserService userService;
    @Autowired
    private ITableInfoService tableInfoService;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        log.info("method {} , url {} , userId {}", request.getMethod(), request.getRequestURI(), SecurityUtils.getUserId());

        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!ObjectUtils.isEmpty(pathVariables)) {
            boolean access = false;
            if (pathVariables.containsKey(TABLE_ID)) {
                Long tableId = Long.valueOf(pathVariables.get(TABLE_ID).toString());

                // 不拦截以下路径
                if (request.getRequestURI().contains("/system/table/status/" + tableId + "/update")) {
                    return true;
                }

                List<SysUserTable> userTables = userService.getUserTableFromCache(SecurityUtils.getUserId(), false);
                boolean canRead = request.getMethod().equalsIgnoreCase("get")
                        && !request.getRequestURI().contains("/system/table/" + tableId + "/export");

                TableInfo tableInfo = tableInfoService.getById(tableId);
                if (ObjectUtils.isEmpty(userTables)) {
                    if (canRead) {
                        access = checkDirAccessIfNotTableAccess(tableInfo.getDirId());
                    } else {
                        access = false;
                    }
                } else {
                    Optional<SysUserTable> opt = userTables.stream().filter(t -> t.getTableId().equals(tableId)).findFirst();
                    if (opt.isPresent()) {
                        switch (opt.get().getAccessType()) {
                            case 1:
                                // 可读
                                access = canRead
                                        || request.getRequestURI().contains("/system/table/" + tableId + "/dataInfo");
                                break;

                            case 2:
                                // 可读可导出
                                access = request.getMethod().equalsIgnoreCase("get")
                                        || request.getRequestURI().contains("/system/table/" + tableId + "/dataInfo")
                                        || request.getRequestURI().contains("/system/table/" + tableId + "/export");
                                break;

                            case 3:
                                // 可编辑
                                access = request.getMethod().equalsIgnoreCase("get")
                                        || request.getRequestURI().contains("/system/table/" + tableId + "/dataInfo")
                                        || request.getRequestURI().contains("/system/table/" + tableId + "/export")
                                        || request.getRequestURI().contains("/system/table/" + tableId + "/data");
                                break;

                            case 4:
                                // 元数据可编辑
                                access = request.getMethod().equalsIgnoreCase("get")
                                        || request.getRequestURI().contains("/system/table/" + tableId + "/dataInfo")
                                        || request.getRequestURI().contains("/system/table/" + tableId + "/export")
                                        || request.getRequestURI().contains("/system/table/" + tableId + "/data")
                                        || request.getRequestURI().contains("/system/table/" + tableId + "/metadata");
                                break;

                            case 5:
                                // 管理，可建表
                                access = true;
                                break;

                            default:
                                access = false;
                                break;
                        }
                        // 添加查询记录
                        if (access
                                && ((request.getMethod().equalsIgnoreCase("get")
                                && request.getRequestURI().contains("/system/table/" + tableId + "/data"))
                                || (request.getRequestURI().contains("/system/table/" + tableId + "/dataInfo")))) {
                            AsyncManager.me().execute(AsyncFactory.queryTableTimes(tableInfo, SecurityUtils.getUserId(),
                                    SecurityUtils.getUsername()));
                        }

                    } else {
                        // 没有配置表权限，查看是否有目录权限
                        if (canRead) {
                            access = checkDirAccessIfNotTableAccess(tableInfo.getDirId());
                        } else {
                            access = false;
                        }
                    }

                }
            } else if (pathVariables.containsKey(DIR_ID)) {
                access = isDirAccess(Long.valueOf(pathVariables.get(DIR_ID).toString()));
            }

            if (!access) {
                throw new CustomException("无权限访问！");
            }
        }


        return true;
    }

    private boolean checkDirAccessIfNotTableAccess(Long dirId) {

        boolean access = isDirAccess(dirId);
        if (access) {
            //只允许查第一页
            ServletUtils.getRequestAttributes().setAttribute(PAGE_NUM, 1, 0);
            ServletUtils.getRequestAttributes().setAttribute(PAGE_SIZE, 10, 0);
        }
        return access;
    }

    private boolean isDirAccess(Long dirId) {
        List<Long> dirIds = userService.getUserDirIdsFromCache(SecurityUtils.getUserId(), false);

        if (ObjectUtils.isEmpty(dirIds) || !dirIds.contains(dirId)) {
            return false;
        }
        return true;
    }
}

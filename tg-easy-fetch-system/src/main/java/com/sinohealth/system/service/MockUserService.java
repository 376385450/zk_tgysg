package com.sinohealth.system.service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Kuangcp
 * 2024-08-09 16:03
 */
public interface MockUserService {

    void fillUserAuthById(HttpServletRequest request, Long userId);
}

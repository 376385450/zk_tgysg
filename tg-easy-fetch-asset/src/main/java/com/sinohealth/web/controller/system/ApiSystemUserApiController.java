package com.sinohealth.web.controller.system;


import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.dto.UserCreateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author shallwetalk
 * @Date 2023/8/4
 */
@RestController
@RequestMapping("/api/easyfetch/user")
public class ApiSystemUserApiController {


    @Autowired
    private SysUserApiController sysUserController;


    /**
     * 添加易数阁用户
     *
     * @param dto
     * @return
     */

    public AjaxResult add(UserCreateDto dto) {
        return sysUserController.add(dto);
    }

    public AjaxResult getList() {
        return null;
    }
}

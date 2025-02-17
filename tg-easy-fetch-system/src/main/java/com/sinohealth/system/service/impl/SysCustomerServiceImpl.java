package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.entity.SysCustomer;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.system.mapper.SysCustomerMapper;
import com.sinohealth.system.service.ISysCustomerAuthService;
import com.sinohealth.system.service.ISysCustomerService;
import com.sinohealth.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public class SysCustomerServiceImpl extends ServiceImpl<SysCustomerMapper, SysCustomer> implements ISysCustomerService {

    @Autowired
    private ISysCustomerAuthService sysCustomerAuthService;

    @Autowired
    private ISysUserService sysUserService;

    @Override
    public int insert(SysCustomer sysCustomer) {
        return baseMapper.insert(sysCustomer);
    }

    @Override
    public void update(SysCustomer sysCustomer) {
        sysCustomer.setUpdateBy(SecurityUtils.getUserId());
        baseMapper.updateByUserId(sysCustomer);
    }

    @Override
    public SysCustomer getByUserId(Long userId) {
        return baseMapper.getByUserId(userId);
    }

    @Override
    public List<SysUser> selectList(Integer manageUserId, Integer status, String searchVal) {
        List<SysUser> list = baseMapper.queryList(manageUserId, status, searchVal);
        for (SysUser sysUser : list) {
            sysUser.setViewTableCnt(sysCustomerAuthService.getCountByUserId(sysUser.getUserId(), CommonConstants.ICON_PACK));
            //处理创建人字段
            SysUser createUser = sysUserService.selectUserByUserName(sysUser.getCreateBy());
            if (createUser != null && StringUtils.isNotEmpty(createUser.getOrgUserId())) {
                SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(createUser.getOrgUserId());
                if (sinoPassUserDTO != null) {
                    sysUser.setCreateByOri(sinoPassUserDTO.getViewName());
                }
            }
            //处理管理人员字段
            if (sysUser.getSysCustomer() != null && sysUser.getSysCustomer().getManageUser() != null) {
                SysUser managerUser = sysUserService.selectUserById(Long.parseLong(sysUser.getSysCustomer().getManageUser().toString()));
                if (managerUser != null && StringUtils.isNotEmpty(managerUser.getOrgUserId())) {
                    SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(managerUser.getOrgUserId());
                    if (sinoPassUserDTO != null) {
                        sysUser.getSysCustomer().setManageUserOri(sinoPassUserDTO.getViewName());
                    }
                }
            }
        }
        return list;
    }

    @Override
    public List<SysUser> selectSubList(Integer status, String searchVal, Integer parentAccountId) {
        List<SysUser> list = baseMapper.selectSubList(status, searchVal, parentAccountId);
        for (SysUser sysUser : list) {
            sysUser.setViewTableCnt(sysCustomerAuthService.getCountByUserId(sysUser.getUserId(), CommonConstants.ICON_PACK));
            //处理创建人字段
            SysUser createUser = sysUserService.selectUserByUserName(sysUser.getCreateBy());
            if (createUser != null && StringUtils.isNotEmpty(createUser.getOrgUserId())) {
                SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(createUser.getOrgUserId());
                if (sinoPassUserDTO != null) {
                    sysUser.setCreateByOri(sinoPassUserDTO.getViewName());
                }
            }
        }
        return list;
    }

    @Override
    public void updateByBatchIds(List<Long> ids, Integer manageId) {
        baseMapper.updateByBatchIds(ids, manageId);
    }

    @Override
    public List<SysUser> selectManageUser() {
        List<SysUser> list = baseMapper.selectManageUser();
        list.forEach(sysUser -> {
            if (StringUtils.isNotEmpty(sysUser.getOrgUserId())) {
                sysUser.setSinoPassUserDTO(SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId()));
            }
        });
        return list;
    }

    @Override
    public Map<String, Object> getUserCount() {
        return baseMapper.getUserCount();
    }

    @Override
    public String getClientNames(String ids) {
        if (StringUtils.isBlank(ids)) {
            return "";
        }
        List<String> names = baseMapper.selectNames(ids);
        return StringUtils.join(names, ",");
    }

    @Override
    public List<SysCustomer> listAllCustomers() {
        return baseMapper.listAllCustomers();
    }
}

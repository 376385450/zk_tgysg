package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.system.domain.TgCustomerApplyAuth;
import com.sinohealth.system.domain.vo.TgTableApplicationMappingInfo;
import com.sinohealth.system.dto.TgCustomerApplyAuthDto;
import com.sinohealth.system.mapper.DataDirMapper;
import com.sinohealth.system.mapper.SysCustomerAuthMapper;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.mapper.TgTableApplicationMappingInfoMapper;
import com.sinohealth.system.service.ISysCustomerAuthService;
import com.sinohealth.system.service.ISysUserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class SysCustomerServiceAuthImpl extends ServiceImpl<SysCustomerAuthMapper, TgCustomerApplyAuth> implements ISysCustomerAuthService {

    @Autowired
    private TgApplicationInfoMapper tgApplicationInfoMapper;
    @Autowired
    private TgTableApplicationMappingInfoMapper tableApplicationMappingInfoMapper;
    @Autowired
    private SysCustomerAuthMapper sysCustomerAuthMapper;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private DataDirMapper dataDirMapper;

    @Override
    public List<TgCustomerApplyAuthDto> queryList(Integer assetsId, Long userId) {
        List<TgCustomerApplyAuthDto> list = baseMapper.getList(assetsId, userId);
        list.forEach(tgCustomerApplyAuthDto -> {

            //处理更新人
            if (tgCustomerApplyAuthDto.getUpdateId() != null) {
                SysUser user = sysUserService.selectUserById(tgCustomerApplyAuthDto.getUpdateId());
                if (user != null && StringUtils.isNotEmpty(user.getOrgUserId())) {
                    SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                    if (sinoPassUserDTO != null) {
                        tgCustomerApplyAuthDto.setUpdateByOri(sinoPassUserDTO.getViewName());
                    }
                }
            }
        });
        return list;
    }

    @Override
    public List<TgCustomerApplyAuthDto> queryListV2(Long userId, Long applyId, List<Long> dataDirIds) {
        List<TgCustomerApplyAuthDto> list = baseMapper.getListV2(dataDirIds, userId, applyId);
        list.forEach(tgCustomerApplyAuthDto -> {
            //处理更新人
            if (tgCustomerApplyAuthDto.getUpdateId() != null) {
                SysUser user = sysUserService.selectUserById(tgCustomerApplyAuthDto.getUpdateId());
                if (user != null && StringUtils.isNotEmpty(user.getOrgUserId())) {
                    SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                    if (sinoPassUserDTO != null) {
                        tgCustomerApplyAuthDto.setUpdateByOri(sinoPassUserDTO.getViewName());
                    }
                }
            }
        });
        return list;
    }

    @Override
    public int getCountByUserId(Long userId) {
        return baseMapper.getCountByUserId(userId, null);
    }

    @Override
    public int getCountByUserId(Long userId, String excludeIcon) {
        return baseMapper.getCountByUserId(userId, excludeIcon);
    }

    @Override
    public List<TgCustomerApplyAuthDto> getListForApply(TgCustomerApplyAuthDto tgCustomerApplyAuthDto) {
        // 获取当前申请人的申请文件数据
        List<TgCustomerApplyAuthDto> tgCustomerApplyAuthDtos = baseMapper.getListForApply(tgCustomerApplyAuthDto);
        Map<Long, Long> volumeMap = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(tgCustomerApplyAuthDtos)) {
            List<Long> assetsIds = tgCustomerApplyAuthDtos.stream().map(TgCustomerApplyAuthDto::getAssetsId).collect(Collectors.toList());
            List<TgTableApplicationMappingInfo> mappingInfos = tableApplicationMappingInfoMapper.selectList(
                    new QueryWrapper<TgTableApplicationMappingInfo>().lambda().in(TgTableApplicationMappingInfo::getAssetsId, assetsIds));
            volumeMap = mappingInfos.stream().collect(Collectors.toMap(TgTableApplicationMappingInfo::getAssetsId,
                    TgTableApplicationMappingInfo::getDataVolume, (front, current) -> current));
        }

        for (TgCustomerApplyAuthDto entity : tgCustomerApplyAuthDtos) {
            //处理分配人
            if (entity.getUpdateId() != null) {
                SysUser user = sysUserService.selectUserById(entity.getUpdateId());
                if (user != null && org.apache.commons.lang3.StringUtils.isNotEmpty(user.getOrgUserId())) {
                    SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                    if (sinoPassUserDTO != null) {
                        entity.setUpdateByOri(sinoPassUserDTO.getViewName());
                    }
                    entity.setDataTotal(volumeMap.get(entity.getAssetsId()));
                }
            }
        }
        return tgCustomerApplyAuthDtos;
    }

    @Override
    public AjaxResult<Object> checkParam(Long assetsId, String auth, HttpServletResponse response) {
        TgCustomerApplyAuth applyAuth = sysCustomerAuthMapper.selectOne(new QueryWrapper<TgCustomerApplyAuth>().lambda()
                .eq(TgCustomerApplyAuth::getAssetsId, assetsId)
                .isNull(TgCustomerApplyAuth::getParentCustomerAuthId));
        if (null == applyAuth) {
            if (null != response) {
                response.setStatus(CommonConstants.CAN_NOT_LOAD_AUTH);
            }
            return AjaxResult.error("无法找到对应授权记录");
        }
        if (StringUtils.isBlank(applyAuth.getAuthType()) || !applyAuth.getAuthType().contains(auth)) {
            if (null != response) {
                response.setStatus(CommonConstants.CAN_NOT_DOWNLOAD_FILE);
            }
            return AjaxResult.error("权限限制,无法操作");
        }
        return null;
    }

    @Override
    public List<TgCustomerApplyAuth> queryForTree(Long userId, Integer status, String searchKey) {
        return sysCustomerAuthMapper.queryForTree(userId, status, searchKey);
    }
}

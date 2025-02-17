package com.sinohealth.system.biz.dataassets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.pdf.BizType;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.acl.OfficeRepository;
import com.sinohealth.system.biz.dataassets.dao.UserFileAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserFileAssets;
import com.sinohealth.system.biz.dataassets.dto.request.FileAssetsCreateRequest;
import com.sinohealth.system.biz.dataassets.service.UserFileAssetsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-18 15:15
 */
@Slf4j
@Service
public class UserFileAssetsServiceImpl implements UserFileAssetsService {

    @Autowired
    private UserFileAssetsDAO userFileAssetsDAO;
    @Autowired
    private OfficeRepository officeRepository;

    @Override
    public AjaxResult<Void> createFileAssets(FileAssetsCreateRequest request) {
        Long userId = SecurityUtils.getUserId();
        UserFileAssets assets = new UserFileAssets().setName(request.getName()).setPath(request.getPath())
                .setProjectId(request.getProjectId()).setCreator(userId);
        userFileAssetsDAO.save(assets);

        officeRepository.transformPdfAsync(request.getPath(), BizType.FILE_ASSETS, assets.getId() + "");
        return AjaxResult.succeed();
    }

    @Override
    public boolean existsFile(String filename, Long projectId) {
        Integer count = userFileAssetsDAO.getBaseMapper().selectCount(new QueryWrapper<UserFileAssets>().lambda()
                .eq(UserFileAssets::getName, filename)
                .eq(UserFileAssets::getProjectId, projectId)
        );
        return Objects.nonNull(count) && count > 0;
    }

    @Override
    public AjaxResult<Void> deleteById(Long id) {
        UserFileAssets assets = userFileAssetsDAO.getById(id);
        if (Objects.isNull(assets)) {
            return AjaxResult.error("资产不存在");
        }
        if (!Objects.equals(assets.getCreator(), SecurityUtils.getUserId())) {
            return AjaxResult.error("仅创建者可删除");
        }
        userFileAssetsDAO.removeById(id);
        return AjaxResult.succeed();
    }

    @Override
    public List<UserFileAssets> queryAvailableAssets(Collection<Long> projectIds) {
        if (CollectionUtils.isEmpty(projectIds)) {
            return Collections.emptyList();
        }
        return userFileAssetsDAO.list(new QueryWrapper<UserFileAssets>().lambda().in(UserFileAssets::getProjectId, projectIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePdfPath(String bizId, String pdfPath) {
        Long assetsId = Long.valueOf(bizId);
        userFileAssetsDAO.update(null, new UpdateWrapper<UserFileAssets>().lambda()
                .eq(UserFileAssets::getId, assetsId).set(UserFileAssets::getPdfPath, pdfPath));

    }
}

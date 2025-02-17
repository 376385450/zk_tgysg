package com.sinohealth.system.biz.template.dao.impl;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-27 14:29
 */
//@Repository
//public class TemplateSnapshotDAOImpl extends ServiceImpl<TemplateSnapshotMapper, TemplateSnapshot>
//        implements TemplateSnapshotDAO {
//
//    @Override
//    public Optional<TemplateSnapshot> queryByVersion(Long templateId, Integer version) {
//        if (Objects.isNull(templateId) || Objects.isNull(version)) {
//            return Optional.empty();
//        }
//
//        TemplateSnapshot template = this.baseMapper.selectOne(new QueryWrapper<TemplateSnapshot>().lambda()
//                .eq(TemplateSnapshot::getTemplateId, templateId)
//                .eq(TemplateSnapshot::getVersion, version)
//        );
//        return Optional.ofNullable(template);
//    }
//
//    @Override
//    public void saveNewVersion(TgTemplateInfo template) {
//        TemplateSnapshot en = new TemplateSnapshot();
//        BeanUtils.copyProperties(template, en);
//        en.setId(null);
//        en.setTemplateId(template.getId());
//        en.setCreateTime(DateUtils.getTime());
//        en.setVersion(template.getVersion());
//        this.baseMapper.insert(en);
//    }
//}

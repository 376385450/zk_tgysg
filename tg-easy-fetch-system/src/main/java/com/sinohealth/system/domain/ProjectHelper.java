package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author shallwetalk
 * @Date 2024/1/11
 */
@Data
@TableName("project_helper")
public class ProjectHelper {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long userId;

    public ProjectHelper(Long projectId, Long userId) {
        this.projectId = projectId;
        this.userId = userId;
    }

    public ProjectHelper() {
    }
}

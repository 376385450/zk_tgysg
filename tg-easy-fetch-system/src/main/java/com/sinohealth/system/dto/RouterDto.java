package com.sinohealth.system.dto;

import com.sinohealth.system.domain.vo.RouterVo;
import lombok.Data;

import java.util.List;

/**
 * @author Jingjun
 * @since 2021/6/7
 */
@Data
public class RouterDto {

    private List<RouterVo> menusRouter;

    private List<RouterVo> vueRouter;

}

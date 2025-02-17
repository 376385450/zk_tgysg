package com.sinohealth.system.dto.common;

import cn.hutool.http.HttpStatus;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * openApi 响应参数
 *
 * @author linkaiwei
 * @date 2021/7/22 17:40
 * @since dev
 */
@ApiModel
@Data
public class OpenApiResponseDTO {

    private Integer code;

    private String desc;

    private Object data;

    private PageInfo pageInfo;


    /**
     * 返回成功消息
     *
     * @param desc 返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static OpenApiResponseDTO success(String desc, Object data) {
        return new OpenApiResponseDTO(HttpStatus.HTTP_OK, desc, data);
    }

    /**
     * 返回成功消息
     *
     * @param desc     返回内容
     * @param data     数据对象
     * @param pageInfo 分页信息
     * @return 成功消息
     */
    public static OpenApiResponseDTO success(String desc, Object data, PageInfo pageInfo) {
        return new OpenApiResponseDTO(HttpStatus.HTTP_OK, desc, data, pageInfo);
    }

    /**
     * 返回错误消息
     *
     * @param desc 返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static OpenApiResponseDTO error(String desc, Object data) {
        return new OpenApiResponseDTO(HttpStatus.HTTP_INTERNAL_ERROR, desc, data);
    }


    public OpenApiResponseDTO() {
    }

    public OpenApiResponseDTO(Integer code, String desc, Object data) {
        this.code = code;
        this.desc = desc;
        this.data = data;
    }

    public OpenApiResponseDTO(Integer code, String desc, Object data, PageInfo pageInfo) {
        this.code = code;
        this.desc = desc;
        this.data = data;
        this.pageInfo = pageInfo;
    }


    /**
     * 分页信息
     *
     * @author linkaiwei
     * @date 2021/07/22 23:12
     * @since dev
     */
    @Data
    public static class PageInfo implements Serializable {

        /**
         * 是否第一页
         */
        private Boolean first;

        /**
         * 是否最后一页
         */
        private Boolean last;

        /**
         * 页码
         */
        private Long number;

        /**
         * 每页数量
         */
        private Long size;

        /**
         * 总页数
         */
        private Long totalPages;

        /**
         * 总数
         */
        private Long totalSize;


        public PageInfo() {
        }

        public PageInfo(Long number, Long size, Long totalSize) {
            this.first = number != null && number == 1;
            this.number = number == null ? 1 : number;
            this.size = size;
            this.totalPages = size == null || size == 0 || totalSize == null || totalSize == 0 ? 0 :
                    (totalSize - 1) / size + 1;
            this.totalSize = totalSize;
            this.last = totalPages == 0 || totalPages.intValue() == this.number;
        }
    }
}

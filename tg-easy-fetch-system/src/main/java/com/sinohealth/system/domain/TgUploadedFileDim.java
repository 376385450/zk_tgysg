package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@ApiModel(description = "提数文档信息表(TgDocInfo)实体类")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "dim_upload_file")
public class TgUploadedFileDim extends Model<TgUploadedFileDim> {
    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("板块类型，可能有多个板块需要上传文件，如文档板块")
    private String mappingType;

    @ApiModelProperty("板块映射对象的ID， 如果板块是文档， 则该值为某个文档对象的映射ID")
    private Long mappingId;

    @ApiModelProperty("源文件类型, 即文件尾缀")
    private String fileType;

    @ApiModelProperty("源文件名")
    private String fileName;

    @ApiModelProperty("源文件路径")
    private String filePath;

    /**
     * office服务返回的是可访问的url
     */
    @ApiModelProperty("水印PDF路径")
    private String pdfPath;

    @ApiModelProperty("上传时间")
    private String uploadTime;

    private Date lockExpTime_;

    private String lockOwner_;

    private Integer retryTimes;

    private String pdfExcepts;

    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    public static TgUploadedFileDim newInstance() {
        return new TgUploadedFileDim();
    }
}

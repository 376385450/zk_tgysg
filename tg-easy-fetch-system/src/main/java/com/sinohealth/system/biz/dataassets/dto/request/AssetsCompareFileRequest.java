package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.system.biz.dataassets.dto.FileAssetsUploadDTO;
import lombok.Data;

import java.util.List;

/**
 * @author Kuangcp
 * 2024-07-17 17:48
 */
@Data
public class AssetsCompareFileRequest {

    private List<FileAssetsUploadDTO> newFiles;
    private List<FileAssetsUploadDTO> oldFiles;
}

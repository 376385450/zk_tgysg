package com.sinohealth.common.utils.dto;

import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.ipaas.model.ResMaindataemployeepageListItemDataItem;
import com.sinohealth.ipaas.model.ResMaindatamainEmployeeselectbyidsItemDataItem;
import lombok.Data;

@Data
public class SinoPassUserDTO {


    private  String id;

    private  String userName;

    private String mobilePhone;

    private  String email;

    private  String mainOrganizationId;

    private String employeeStatusText;

    private String orgAdminTreePathText;

    private  String viewName;

    public SinoPassUserDTO() {
    }

    public SinoPassUserDTO(ResMaindataemployeepageListItemDataItem resMaindataemployeepageListItemDataItem) {
        this.id = resMaindataemployeepageListItemDataItem.getId();
        this.userName = resMaindataemployeepageListItemDataItem.getUserName();
        this.mobilePhone = resMaindataemployeepageListItemDataItem.getMobilePhone();
        this.email = resMaindataemployeepageListItemDataItem.getEmail();
        this.mainOrganizationId = resMaindataemployeepageListItemDataItem.getMainOrganizationId();
        this.employeeStatusText = resMaindataemployeepageListItemDataItem.getEmployeeStatusText();
        String orgAdminTreePathText = resMaindataemployeepageListItemDataItem.getOrgAdminTreePathText();
        if(StringUtils.indexStrCounts(orgAdminTreePathText, "/") <  2 ){
            this.orgAdminTreePathText = orgAdminTreePathText;
        }else if(StringUtils.indexStrCounts(orgAdminTreePathText, "/") >= 2
                && StringUtils.indexStrCounts(orgAdminTreePathText, "/") <  4){
            this.orgAdminTreePathText = orgAdminTreePathText.substring(StringUtils.getIndexOf(orgAdminTreePathText,"/",2)+1);
        }else{
            this.orgAdminTreePathText =orgAdminTreePathText.substring(
                    StringUtils.getIndexOf(orgAdminTreePathText ,"/",2)+1,
                    StringUtils.getIndexOf(orgAdminTreePathText,"/",4));
        }

        this.viewName = this.orgAdminTreePathText +"-"+userName;
    }

    public SinoPassUserDTO(ResMaindatamainEmployeeselectbyidsItemDataItem item) {
        this.id = item.getId();
        this.userName = item.getUserName();
        this.mobilePhone = item.getMobilePhone();
        this.email = item.getEmail();
        this.mainOrganizationId = item.getMainOrganizationId();
        this.employeeStatusText = item.getEmployeeStatusText();
//        String orgAdminTreePathText = item.getOrgAdminTreePathText();
//        if(StringUtils.indexStrCounts(orgAdminTreePathText, "/") <  2 ){
//            this.orgAdminTreePathText = orgAdminTreePathText;
//        }else if(StringUtils.indexStrCounts(orgAdminTreePathText, "/") >= 2
//                && StringUtils.indexStrCounts(orgAdminTreePathText, "/") <  4){
//            this.orgAdminTreePathText = orgAdminTreePathText.substring(StringUtils.getIndexOf(orgAdminTreePathText,"/",2)+1);
//        }else{
//            this.orgAdminTreePathText =orgAdminTreePathText.substring(
//                    StringUtils.getIndexOf(orgAdminTreePathText ,"/",2)+1,
//                    StringUtils.getIndexOf(orgAdminTreePathText,"/",4));
//        }
//
//        this.viewName = this.orgAdminTreePathText +"-"+userName;
    }

}

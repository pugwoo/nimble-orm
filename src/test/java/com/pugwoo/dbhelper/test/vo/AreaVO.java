package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.Column;
import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.test.entity.AreaDO;

public class AreaVO extends AreaDO {

    @RelatedColumn(localColumn = "layer_code,area_code", remoteColumn = "layer_code,area_code")
    private AreaLocationVO locationVO;

    public AreaLocationVO getLocationVO() {
        return locationVO;
    }

    public void setLocationVO(AreaLocationVO locationVO) {
        this.locationVO = locationVO;
    }
}

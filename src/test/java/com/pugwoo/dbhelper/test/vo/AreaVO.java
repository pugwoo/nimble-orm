package com.pugwoo.dbhelper.test.vo;

import com.pugwoo.dbhelper.annotation.RelatedColumn;
import com.pugwoo.dbhelper.test.entity.AreaDO;
import com.pugwoo.dbhelper.test.entity.AreaLocationDO;

public class AreaVO extends AreaDO {

    @RelatedColumn(localColumn = "layer_code,area_code", remoteColumn = "layer_code,area_code")
    private AreaLocationDO locationVO;

    public AreaLocationDO getLocationVO() {
        return locationVO;
    }

    public void setLocationVO(AreaLocationDO locationVO) {
        this.locationVO = locationVO;
    }
}

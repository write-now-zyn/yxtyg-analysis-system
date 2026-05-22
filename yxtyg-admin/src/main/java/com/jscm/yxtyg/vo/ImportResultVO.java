package com.jscm.yxtyg.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResultVO {

    private boolean success;
    private int insertCount;
    private int updateCount;
    private int failCount;
    private List<String> failMessages = new ArrayList<>();

    public String getMessage() {
        return String.format("导入完成！新增%d条，更新%d条，失败%d条", insertCount, updateCount, failCount);
    }
}

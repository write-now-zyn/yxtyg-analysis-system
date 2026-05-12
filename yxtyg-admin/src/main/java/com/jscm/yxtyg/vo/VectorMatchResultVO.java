package com.jscm.yxtyg.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 向量匹配结果
 */
@Data
public class VectorMatchResultVO {

    private List<SimilarTicketVO> similarTickets = new ArrayList<>();
}

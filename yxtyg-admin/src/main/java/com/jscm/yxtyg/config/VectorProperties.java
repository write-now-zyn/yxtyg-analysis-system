package com.jscm.yxtyg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 向量存储配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "vector")
public class VectorProperties {

    private Store store = new Store();

    private List<String> vectorizableStatuses = new ArrayList<>();

    @Data
    public static class Store {
        private String path = "./data/vectors";
    }
}

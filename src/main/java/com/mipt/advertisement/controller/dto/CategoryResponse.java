package com.mipt.advertisement.controller.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private String groupName;
    private List<CategoryInfo> categories;

    @Data
    @Builder
    public static class CategoryInfo {
        private String name;
        private String displayName;
        private String type;
    }
}
package com.mipt.advertisement.controller.dto;

import lombok.Data;
import java.util.Set;

@Data
public class AdvertisementPatchRequest {
    private String name;
    private String description;
    private Long price;
    private String category;
    private Set<String> photoUrls;
}
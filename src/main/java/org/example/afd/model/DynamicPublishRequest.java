package org.example.afd.model;

import lombok.Data;

import java.util.List;

@Data
public class DynamicPublishRequest {
    private String content;
    private String location;
    private Double longitude;
    private Double latitude;
    private List<String> imageUrls;
}
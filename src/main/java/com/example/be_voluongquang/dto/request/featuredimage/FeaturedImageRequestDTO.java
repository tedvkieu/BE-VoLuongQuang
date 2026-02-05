package com.example.be_voluongquang.dto.request.featuredimage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedImageRequestDTO {
    private String featuredImageId;
    private String title;
    private String linkUrl;
    private Integer sortOrder;
    private Boolean isActive;
}


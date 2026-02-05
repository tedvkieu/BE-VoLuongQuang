package com.example.be_voluongquang.dto.request.banner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerRequestDTO {
    private String bannerId;
    private String title;
    private String linkUrl;
    private Integer sortOrder;
    private Boolean isActive;
}

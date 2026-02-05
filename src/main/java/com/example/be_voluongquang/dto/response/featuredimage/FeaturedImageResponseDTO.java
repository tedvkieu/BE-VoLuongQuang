package com.example.be_voluongquang.dto.response.featuredimage;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedImageResponseDTO {
    private String featuredImageId;
    private String title;
    private String linkUrl;
    private Integer sortOrder;
    private Boolean isActive;
    private String imageId;
    private String imageUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private Boolean isDeleted;
}


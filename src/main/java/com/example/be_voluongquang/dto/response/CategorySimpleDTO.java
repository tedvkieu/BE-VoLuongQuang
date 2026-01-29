package com.example.be_voluongquang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySimpleDTO {
    private String categoryId;
    private String categoryName;
    private String categoryCode;
} 

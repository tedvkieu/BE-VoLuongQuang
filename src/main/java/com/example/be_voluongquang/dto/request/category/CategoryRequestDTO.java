package com.example.be_voluongquang.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDTO {
    private String categoryId;

    @NotBlank(message = "Category name is required")
    private String categoryName;

    @NotBlank(message = "Category code is required")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Category code must be lowercase and contain only letters, numbers, or underscore")
    private String categoryCode;
}

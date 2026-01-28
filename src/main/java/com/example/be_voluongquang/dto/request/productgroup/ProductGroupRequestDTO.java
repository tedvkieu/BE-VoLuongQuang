package com.example.be_voluongquang.dto.request.productgroup;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductGroupRequestDTO {
    private String groupId;

    @NotBlank(message = "Product group name is required")
    private String groupName;
}

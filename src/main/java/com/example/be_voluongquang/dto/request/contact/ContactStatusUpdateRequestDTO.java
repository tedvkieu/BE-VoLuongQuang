package com.example.be_voluongquang.dto.request.contact;

import com.example.be_voluongquang.entity.ContactStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactStatusUpdateRequestDTO {
    @NotNull(message = "Status is required")
    private ContactStatus status;
}


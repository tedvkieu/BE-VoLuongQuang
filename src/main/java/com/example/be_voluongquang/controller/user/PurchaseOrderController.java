package com.example.be_voluongquang.controller.user;

import com.example.be_voluongquang.dto.request.purchaseorder.PurchaseOrderCreateRequestDTO;
import com.example.be_voluongquang.dto.response.purchaseorder.PurchaseOrderResponseDTO;
import com.example.be_voluongquang.security.JwtAuthenticationFilter;
import com.example.be_voluongquang.services.PurchaseOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(path = "/api/purchase-order", produces = MediaType.APPLICATION_JSON_VALUE)
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','CUSTOMER')")
    public ResponseEntity<PurchaseOrderResponseDTO> createOrder(
            @Valid @RequestBody PurchaseOrderCreateRequestDTO payload,
            HttpServletRequest request) {
        String userId = resolveUserId(request);
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PurchaseOrderResponseDTO response = purchaseOrderService.createOrder(userId, payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String resolveUserId(HttpServletRequest request) {
        Object attr = request.getAttribute(JwtAuthenticationFilter.ATTR_AUTHENTICATED_USER_ID);
        return attr instanceof String attrValue ? attrValue : null;
    }
}


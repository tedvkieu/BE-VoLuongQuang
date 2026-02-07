package com.example.be_voluongquang.controller.admin;

import com.example.be_voluongquang.dto.request.purchaseorder.PurchaseOrderUpdateRequestDTO;
import com.example.be_voluongquang.dto.request.purchaseorder.PurchaseOrderStatusUpdateRequestDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.purchaseorder.PurchaseOrderAdminListItemDTO;
import com.example.be_voluongquang.dto.response.purchaseorder.PurchaseOrderResponseDTO;
import com.example.be_voluongquang.entity.PurchaseOrderStatus;
import com.example.be_voluongquang.services.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/admin/purchase-order", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class AdminPurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public AdminPurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping
    public PagedResponse<PurchaseOrderAdminListItemDTO> getOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(required = false) Boolean isDeleted) {

        int safePage = page != null && page >= 0 ? page : 0;
        int safeSize = size != null && size > 0 ? size : 10;
        return purchaseOrderService.getOrdersPage(safePage, safeSize, search, status, isDeleted);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponseDTO> getOrderDetail(@PathVariable("id") String id) {
        return ResponseEntity.ok(purchaseOrderService.getOrderDetail(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PurchaseOrderResponseDTO> updateStatus(
            @PathVariable("id") String id,
            @Valid @RequestBody PurchaseOrderStatusUpdateRequestDTO request) {
        return ResponseEntity.ok(purchaseOrderService.updateOrderStatus(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponseDTO> updateOrder(
            @PathVariable("id") String id,
            @Valid @RequestBody PurchaseOrderUpdateRequestDTO request) {
        return ResponseEntity.ok(purchaseOrderService.updateOrder(id, request));
    }
}

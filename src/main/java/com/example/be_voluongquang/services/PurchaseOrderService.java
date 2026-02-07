package com.example.be_voluongquang.services;

import com.example.be_voluongquang.dto.request.purchaseorder.PurchaseOrderCreateRequestDTO;
import com.example.be_voluongquang.dto.request.purchaseorder.PurchaseOrderStatusUpdateRequestDTO;
import com.example.be_voluongquang.dto.request.purchaseorder.PurchaseOrderUpdateRequestDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.purchaseorder.PurchaseOrderAdminListItemDTO;
import com.example.be_voluongquang.dto.response.purchaseorder.PurchaseOrderResponseDTO;
import com.example.be_voluongquang.entity.PurchaseOrderStatus;

public interface PurchaseOrderService {
    PurchaseOrderResponseDTO createOrder(String userId, PurchaseOrderCreateRequestDTO payload);

    PagedResponse<PurchaseOrderAdminListItemDTO> getOrdersPage(
            int page,
            int size,
            String search,
            PurchaseOrderStatus status,
            Boolean isDeleted);

    PurchaseOrderResponseDTO getOrderDetail(String id);

    PurchaseOrderResponseDTO updateOrderStatus(String id, PurchaseOrderStatusUpdateRequestDTO request);

    PurchaseOrderResponseDTO updateOrder(String id, PurchaseOrderUpdateRequestDTO request);
}

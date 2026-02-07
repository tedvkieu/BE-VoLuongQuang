package com.example.be_voluongquang.services.impl;

import com.example.be_voluongquang.dto.request.purchaseorder.PurchaseOrderCreateRequestDTO;
import com.example.be_voluongquang.dto.request.purchaseorder.PurchaseOrderItemCreateRequestDTO;
import com.example.be_voluongquang.dto.request.purchaseorder.PurchaseOrderItemUpdateRequestDTO;
import com.example.be_voluongquang.dto.request.purchaseorder.PurchaseOrderStatusUpdateRequestDTO;
import com.example.be_voluongquang.dto.request.purchaseorder.PurchaseOrderUpdateRequestDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.purchaseorder.PurchaseOrderAdminListItemDTO;
import com.example.be_voluongquang.dto.response.purchaseorder.PurchaseOrderItemResponseDTO;
import com.example.be_voluongquang.dto.response.purchaseorder.PurchaseOrderResponseDTO;
import com.example.be_voluongquang.entity.ProductEntity;
import com.example.be_voluongquang.entity.PurchaseOrderEntity;
import com.example.be_voluongquang.entity.PurchaseOrderItemEntity;
import com.example.be_voluongquang.entity.PurchaseOrderStatus;
import com.example.be_voluongquang.exception.ResourceNotFoundException;
import com.example.be_voluongquang.repository.ProductRepository;
import com.example.be_voluongquang.repository.PurchaseOrderItemRepository;
import com.example.be_voluongquang.repository.PurchaseOrderRepository;
import com.example.be_voluongquang.services.PurchaseOrderService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public PurchaseOrderResponseDTO createOrder(String userId, PurchaseOrderCreateRequestDTO payload) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Không xác định được người dùng");
        }
        if (payload == null) {
            throw new IllegalArgumentException("Yêu cầu không hợp lệ");
        }
        if (payload.getItems() == null || payload.getItems().isEmpty()) {
            throw new IllegalArgumentException("Danh sách sản phẩm không được rỗng");
        }

        Map<String, Integer> quantityByProductId = new HashMap<>();
        for (PurchaseOrderItemCreateRequestDTO item : payload.getItems()) {
            if (item == null || item.getProductId() == null || item.getProductId().isBlank()) {
                throw new IllegalArgumentException("productId không hợp lệ");
            }
            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            if (quantity <= 0) {
                throw new IllegalArgumentException("Số lượng phải lớn hơn hoặc bằng 1");
            }
            quantityByProductId.merge(item.getProductId().trim(), quantity, Integer::sum);
        }

        List<String> productIds = new ArrayList<>(quantityByProductId.keySet());
        List<ProductEntity> products = productRepository.findAllById(productIds);
        Map<String, ProductEntity> productsById = new HashMap<>();
        for (ProductEntity product : products) {
            if (product != null && product.getProductId() != null) {
                productsById.put(product.getProductId(), product);
            }
        }

        for (String productId : productIds) {
            if (!productsById.containsKey(productId)) {
                throw new ResourceNotFoundException("Product", "productId", productId);
            }
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal finalAmount = BigDecimal.ZERO;

        PurchaseOrderEntity order = PurchaseOrderEntity.builder()
                .userId(userId)
                .customerName(payload.getCustomerName())
                .customerPhone(payload.getCustomerPhone())
                .customerEmail(payload.getCustomerEmail())
                .customerAddress(payload.getCustomerAddress())
                .status(PurchaseOrderStatus.NEW)
                .build();

        order = purchaseOrderRepository.save(order);

        List<PurchaseOrderItemEntity> itemEntities = new ArrayList<>();
        List<PurchaseOrderItemResponseDTO> itemResponses = new ArrayList<>();

        for (String productId : productIds) {
            ProductEntity product = productsById.get(productId);
            int quantity = quantityByProductId.getOrDefault(productId, 0);

            BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice() == null ? 0.0 : product.getPrice());
            int discountPercent = product.getDiscountPercent() == null ? 0 : product.getDiscountPercent();
            if (discountPercent < 0) discountPercent = 0;
            if (discountPercent > 100) discountPercent = 100;

            BigDecimal discountFactor = BigDecimal.valueOf(100 - discountPercent)
                    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
            BigDecimal finalUnitPrice = unitPrice.multiply(discountFactor);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            BigDecimal finalLineTotal = finalUnitPrice.multiply(BigDecimal.valueOf(quantity));

            totalAmount = totalAmount.add(lineTotal);
            finalAmount = finalAmount.add(finalLineTotal);

            PurchaseOrderItemEntity itemEntity = PurchaseOrderItemEntity.builder()
                    .purchaseOrderId(order.getPurchaseOrderId())
                    .purchaseOrder(order)
                    .productId(productId)
                    .product(product)
                    .productName(product.getName() == null ? "" : product.getName())
                    .productUnit(product.getUnit())
                    .productImageUrl(product.getImageUrl())
                    .quantity(quantity)
                    .unitPrice(unitPrice.doubleValue())
                    .discountPercent(discountPercent)
                    .finalUnitPrice(finalUnitPrice.doubleValue())
                    .lineTotal(lineTotal.doubleValue())
                    .finalLineTotal(finalLineTotal.doubleValue())
                    .build();

            itemEntities.add(itemEntity);
            itemResponses.add(PurchaseOrderItemResponseDTO.builder()
                    .productId(productId)
                    .productName(itemEntity.getProductName())
                    .productUnit(itemEntity.getProductUnit())
                    .productImageUrl(itemEntity.getProductImageUrl())
                    .quantity(quantity)
                    .unitPrice(itemEntity.getUnitPrice())
                    .discountPercent(discountPercent)
                    .finalUnitPrice(itemEntity.getFinalUnitPrice())
                    .lineTotal(itemEntity.getLineTotal())
                    .finalLineTotal(itemEntity.getFinalLineTotal())
                    .build());
        }

        purchaseOrderItemRepository.saveAll(itemEntities);

        BigDecimal discountAmount = totalAmount.subtract(finalAmount);

        order.setTotalAmount(totalAmount.doubleValue());
        order.setFinalAmount(finalAmount.doubleValue());
        order.setDiscountAmount(discountAmount.doubleValue());
        order.setItems(itemEntities);
        order = purchaseOrderRepository.save(order);

        return PurchaseOrderResponseDTO.builder()
                .purchaseOrderId(order.getPurchaseOrderId())
                .userId(order.getUserId())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .customerAddress(order.getCustomerAddress())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PurchaseOrderAdminListItemDTO> getOrdersPage(
            int page,
            int size,
            String search,
            PurchaseOrderStatus status,
            Boolean isDeleted) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 10 : size;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        String normalizedSearch = search != null && !search.trim().isEmpty() ? search.trim() : null;
        String searchPattern = normalizedSearch != null ? "%" + normalizedSearch + "%" : null;
        Page<PurchaseOrderEntity> entityPage = purchaseOrderRepository.searchOrders(
                searchPattern,
                status,
                isDeleted != null ? isDeleted : Boolean.FALSE,
                pageable);

        Page<PurchaseOrderAdminListItemDTO> dtoPage = entityPage.map(this::toAdminListItem);
        return PagedResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponseDTO getOrderDetail(String id) {
        PurchaseOrderEntity order = purchaseOrderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "purchaseOrderId", id));

        List<PurchaseOrderItemResponseDTO> items = new ArrayList<>();
        if (order.getItems() != null) {
            for (PurchaseOrderItemEntity item : order.getItems()) {
                if (item == null) continue;
                items.add(PurchaseOrderItemResponseDTO.builder()
                        .purchaseOrderItemId(item.getPurchaseOrderItemId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productUnit(item.getProductUnit())
                        .productImageUrl(item.getProductImageUrl())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .discountPercent(item.getDiscountPercent())
                        .finalUnitPrice(item.getFinalUnitPrice())
                        .lineTotal(item.getLineTotal())
                        .finalLineTotal(item.getFinalLineTotal())
                        .build());
            }
        }

        return PurchaseOrderResponseDTO.builder()
                .purchaseOrderId(order.getPurchaseOrderId())
                .userId(order.getUserId())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .customerAddress(order.getCustomerAddress())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    @Override
    @Transactional
    public PurchaseOrderResponseDTO updateOrderStatus(String id, PurchaseOrderStatusUpdateRequestDTO request) {
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("status is required");
        }
        PurchaseOrderEntity order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "purchaseOrderId", id));
        order.setStatus(request.getStatus());
        purchaseOrderRepository.save(order);
        return getOrderDetail(id);
    }

    @Override
    @Transactional
    public PurchaseOrderResponseDTO updateOrder(String id, PurchaseOrderUpdateRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu không hợp lệ");
        }

        PurchaseOrderEntity order = purchaseOrderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "purchaseOrderId", id));

        if (Boolean.TRUE.equals(order.getIsDeleted())) {
            throw new IllegalStateException("Đơn hàng đã bị tắt");
        }
        if (order.getStatus() != PurchaseOrderStatus.NEW) {
            throw new IllegalStateException("Chỉ được chỉnh sửa đơn hàng ở trạng thái NEW");
        }

        if (request.getCustomerName() != null) {
            String value = request.getCustomerName().trim();
            if (value.isBlank()) {
                throw new IllegalArgumentException("customerName không được rỗng");
            }
            order.setCustomerName(value);
        }
        if (request.getCustomerPhone() != null) {
            String value = request.getCustomerPhone().trim();
            if (value.isBlank()) {
                throw new IllegalArgumentException("customerPhone không được rỗng");
            }
            order.setCustomerPhone(value);
        }
        if (request.getCustomerEmail() != null) {
            String value = request.getCustomerEmail().trim();
            order.setCustomerEmail(value.isBlank() ? null : value);
        }
        if (request.getCustomerAddress() != null) {
            String value = request.getCustomerAddress().trim();
            if (value.isBlank()) {
                throw new IllegalArgumentException("customerAddress không được rỗng");
            }
            order.setCustomerAddress(value);
        }

        if (request.getItems() != null) {
            Map<String, PurchaseOrderItemEntity> itemsById = new HashMap<>();
            if (order.getItems() != null) {
                for (PurchaseOrderItemEntity item : order.getItems()) {
                    if (item == null || item.getPurchaseOrderItemId() == null) continue;
                    itemsById.put(item.getPurchaseOrderItemId(), item);
                }
            }

            for (PurchaseOrderItemUpdateRequestDTO itemUpdate : request.getItems()) {
                if (itemUpdate == null) continue;
                String itemId = itemUpdate.getPurchaseOrderItemId() == null ? null : itemUpdate.getPurchaseOrderItemId().trim();
                if (itemId == null || itemId.isBlank()) {
                    throw new IllegalArgumentException("purchaseOrderItemId không hợp lệ");
                }
                PurchaseOrderItemEntity item = itemsById.get(itemId);
                if (item == null) {
                    throw new ResourceNotFoundException("PurchaseOrderItem", "purchaseOrderItemId", itemId);
                }
                Integer quantity = itemUpdate.getQuantity();
                if (quantity == null || quantity < 1) {
                    throw new IllegalArgumentException("quantity phải >= 1");
                }
                item.setQuantity(quantity);

                double unitPrice = item.getUnitPrice() == null ? 0.0 : item.getUnitPrice();
                int discountPercent = item.getDiscountPercent() == null ? 0 : item.getDiscountPercent();
                if (discountPercent < 0) discountPercent = 0;
                if (discountPercent > 100) discountPercent = 100;
                double finalUnitPrice = item.getFinalUnitPrice() != null
                        ? item.getFinalUnitPrice()
                        : BigDecimal.valueOf(unitPrice)
                                .multiply(BigDecimal.valueOf(100 - discountPercent))
                                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                                .doubleValue();

                BigDecimal lineTotal = BigDecimal.valueOf(unitPrice).multiply(BigDecimal.valueOf(quantity));
                BigDecimal finalLineTotal = BigDecimal.valueOf(finalUnitPrice).multiply(BigDecimal.valueOf(quantity));
                item.setLineTotal(lineTotal.doubleValue());
                item.setFinalLineTotal(finalLineTotal.doubleValue());
                item.setFinalUnitPrice(finalUnitPrice);
                item.setDiscountPercent(discountPercent);
            }

            if (order.getItems() == null || order.getItems().isEmpty()) {
                throw new IllegalArgumentException("Danh sách sản phẩm không được rỗng");
            }
        }

        // Recalculate totals from items snapshot
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal finalAmount = BigDecimal.ZERO;
        if (order.getItems() != null) {
            for (PurchaseOrderItemEntity item : order.getItems()) {
                if (item == null) continue;
                BigDecimal lineTotal = BigDecimal.valueOf(item.getLineTotal() == null ? 0.0 : item.getLineTotal());
                BigDecimal finalLineTotal = BigDecimal.valueOf(item.getFinalLineTotal() == null ? 0.0 : item.getFinalLineTotal());
                totalAmount = totalAmount.add(lineTotal);
                finalAmount = finalAmount.add(finalLineTotal);
            }
        }
        BigDecimal discountAmount = totalAmount.subtract(finalAmount);

        order.setTotalAmount(totalAmount.doubleValue());
        order.setFinalAmount(finalAmount.doubleValue());
        order.setDiscountAmount(discountAmount.doubleValue());

        purchaseOrderRepository.save(order);
        return getOrderDetail(id);
    }

    private PurchaseOrderAdminListItemDTO toAdminListItem(PurchaseOrderEntity entity) {
        return PurchaseOrderAdminListItemDTO.builder()
                .purchaseOrderId(entity.getPurchaseOrderId())
                .userId(entity.getUserId())
                .customerName(entity.getCustomerName())
                .customerPhone(entity.getCustomerPhone())
                .customerEmail(entity.getCustomerEmail())
                .customerAddress(entity.getCustomerAddress())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .discountAmount(entity.getDiscountAmount())
                .finalAmount(entity.getFinalAmount())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

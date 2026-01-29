package com.example.be_voluongquang.services;

import com.example.be_voluongquang.dto.request.productgroup.ProductGroupRequestDTO;
import com.example.be_voluongquang.dto.response.PagedResponse;
import com.example.be_voluongquang.dto.response.ProductGroupSimpleDTO;
import com.example.be_voluongquang.dto.response.productgroup.ProductGroupResponseDTO;
import java.util.List;

public interface ProductGroupService {
    List<ProductGroupSimpleDTO> getAllProductGroups();

    ProductGroupResponseDTO getProductGroupById(String id);

    ProductGroupResponseDTO createProductGroup(ProductGroupRequestDTO request);

    ProductGroupResponseDTO updateProductGroup(String id, ProductGroupRequestDTO request);

    void deleteProductGroup(String id);

    PagedResponse<ProductGroupResponseDTO> getProductGroupsPage(int page, int size, String search, Boolean isDeleted);
}

package com.example.be_voluongquang.services;

import com.example.be_voluongquang.dto.response.ProductGroupSimpleDTO;
import java.util.List;

public interface ProductGroupService {
    List<ProductGroupSimpleDTO> getAllProductGroups();
} 
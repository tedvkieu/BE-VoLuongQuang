package com.example.be_voluongquang.services.impl;

import com.example.be_voluongquang.dto.response.ProductGroupSimpleDTO;
import com.example.be_voluongquang.entity.ProductGroupEntity;
import com.example.be_voluongquang.repository.ProductGroupRepository;
import com.example.be_voluongquang.services.ProductGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductGroupServiceImpl implements ProductGroupService {
    @Autowired
    private ProductGroupRepository productGroupRepository;

    @Override
    public List<ProductGroupSimpleDTO> getAllProductGroups() {
        List<ProductGroupEntity> groups = productGroupRepository.findAll();
        return groups.stream()
                .map(g -> ProductGroupSimpleDTO.builder()
                        .groupId(g.getGroupId())
                        .groupName(g.getGroupName())
                        .build())
                .collect(Collectors.toList());
    }
} 
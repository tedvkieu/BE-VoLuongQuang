package com.example.be_voluongquang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.be_voluongquang.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, String> {

}
package com.example.be_voluongquang.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "contact")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactEntity extends BaseEntity {

    @Id
    @Column(name = "contact_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String contactId;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "message", length = 4000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private ContactStatus status = ContactStatus.NEW;
}
